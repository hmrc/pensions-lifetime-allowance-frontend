/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import auth.AuthFunction
import common.Exceptions
import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, FrontendAppConfig, LocalTemplateRenderer}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.{ApplicationOutcome, ApplicationType}
import javax.inject.Inject
import models._
import play.api.{Configuration, Environment, Logger, Play}
import play.api.mvc._
import utils.Constants
import views.html.pages.result._

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.util.Random


class ResultController @Inject()(keyStoreConnector: KeyStoreConnector,
                                 plaConnector: PLAConnector,
                                 implicit val partialRetriever: PlaFormPartialRetriever,
                                 implicit val templateRenderer:LocalTemplateRenderer) extends BaseController with AuthFunction {
  lazy val appConfig = FrontendAppConfig
  override lazy val authConnector: AuthConnector = AuthClientConnector
  lazy val postSignInRedirectUrl = FrontendAppConfig.existingProtectionsUrl

  val responseConstructors: ResponseConstructors = ResponseConstructors

  override def config: Configuration = Play.current.configuration
  override def env: Environment = Play.current.injector.instanceOf[Environment]


  val processFPApplication = Action.async { implicit request =>
    genericAuthWithNino("FP2016") { nino =>
      implicit val protectionType = ApplicationType.FP2016
      plaConnector.applyFP16(nino).flatMap(
        response => routeViaMCNeededCheck(response, nino)
      )
    }
  }

  val processIPApplication = Action.async {
    implicit request =>
      genericAuthWithNino("IP2016") { nino =>
        implicit val protectionType = ApplicationType.IP2016
        for {
          userData <- keyStoreConnector.fetchAllUserData
          applicationResult <- plaConnector.applyIP16(nino, userData.get)
          response <- routeViaMCNeededCheck(applicationResult, nino)
        } yield response
      }
  }


  private[controllers] def routeViaMCNeededCheck(response: HttpResponse, nino: String)(implicit request: Request[AnyContent], protectionType: ApplicationType.Value): Future[Result] = {
    response.status match {
      case 423 => Future.successful(Locked(manualCorrespondenceNeeded()))
      case _ => saveAndRedirectToDisplay(response, nino)
    }
  }


  private[controllers] def saveAndRedirectToDisplay(response: HttpResponse, nino: String)(implicit request: Request[AnyContent], protectionType: ApplicationType.Value): Future[Result] = {
    responseConstructors.createApplyResponseModelFromJson(response.json).map {
      model =>
        if (model.protection.notificationId.isEmpty) {
          Logger.warn(s"No notification ID found in the ApplyResponseModel for user with nino $nino")
          Future.successful(InternalServerError(views.html.pages.fallback.noNotificationId()).withHeaders(CACHE_CONTROL -> "no-cache"))
        } else {
          keyStoreConnector.saveData[ApplyResponseModel](common.Strings.nameString("applyResponseModel"), model).map {
            cacheMap =>
              protectionType match {
                case ApplicationType.IP2016 => Redirect(routes.ResultController.displayIP16())
                case ApplicationType.FP2016 => Redirect(routes.ResultController.displayFP16())
              }
          }
        }
    }.getOrElse {
      Logger.warn(s"Unable to create ApplyResponseModel from application response for ${protectionType.toString} for user nino: $nino")
      Future.successful(InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache"))
    }
  }


  val displayIP16 = displayResult(ApplicationType.IP2016)
  val displayFP16 = displayResult(ApplicationType.FP2016)


  def displayResult(implicit protectionType: ApplicationType.Value): Action[AnyContent] = Action.async {
    implicit request =>
      genericAuthWithNino("existingProtections") { nino =>
        val showUserResearchPanel = setURPanelFlag
        val errorResponse = InternalServerError(views.html.pages.fallback.technicalError(protectionType.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        keyStoreConnector.fetchAndGetFormData[ApplyResponseModel](common.Strings.nameString("applyResponseModel")).map {
          case Some(model) =>
            val notificationId = model.protection.notificationId.getOrElse {
              throw new Exceptions.OptionNotDefinedException("applicationOutcome", "notificationId", protectionType.toString)
            }

            applicationOutcome(notificationId) match {

              case ApplicationOutcome.Successful =>
                keyStoreConnector.saveData[ProtectionModel]("openProtection", model.protection)
                val displayModel = DisplayConstructors.createSuccessDisplayModel(model)
                Ok(resultSuccess(displayModel, showUserResearchPanel))

              case ApplicationOutcome.SuccessfulInactive =>
                val displayModel = DisplayConstructors.createSuccessDisplayModel(model)
                Ok(resultSuccessInactive(displayModel, showUserResearchPanel))

              case ApplicationOutcome.Rejected =>
                val displayModel = DisplayConstructors.createRejectionDisplayModel(model)
                Ok(resultRejected(displayModel, showUserResearchPanel))
            }
          case _ =>
            Logger.warn(s"Could not retrieve ApplyResponseModel from keystore for user with nino: $nino")
            errorResponse
        }
      }
  }

  private[controllers] def setURPanelFlag(implicit hc: HeaderCarrier): Boolean = {
    val random = new Random()
    val seed = getLongFromSessionID(hc)
    random.setSeed(seed)
    random.nextInt(3) == 0
  }

  private[controllers] def getLongFromSessionID(implicit hc: HeaderCarrier): Long = {
    val session = hc.sessionId.map(_.value).getOrElse("0")
    val numericSessionValues = session.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    numericSessionValues.takeRight(10).toLong
  }


  def applicationOutcome(notificationId: Int)(implicit protectionType: ApplicationType.Value): ApplicationOutcome.Value = {
    val successCodes = protectionType match {
      case ApplicationType.FP2016 => Constants.successCodes
      case ApplicationType.IP2016 => Constants.ip16SuccessCodes
      case ApplicationType.IP2014 => Constants.ip14SuccessCodes
    }
    if (successCodes.contains(notificationId)) successfulOutcomeType(notificationId) else ApplicationOutcome.Rejected
  }

  private def successfulOutcomeType(notificationId: Int): ApplicationOutcome.Value = {
    if(Constants.inactiveSuccessCodes.contains(notificationId)) ApplicationOutcome.SuccessfulInactive else ApplicationOutcome.Successful
  }

}
