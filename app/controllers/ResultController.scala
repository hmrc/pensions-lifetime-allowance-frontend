/*
 * Copyright 2016 HM Revenue & Customs
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

import auth.{AuthorisedForPLA, PLAUser}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{ExistingProtectionsConstructor, ResponseConstructors}
import enums.ApplicationType.ApplicationType
import enums.{ApplicationOutcome, ApplicationType}
import models.{ProtectionDisplayModel, ProtectionModel, RejectionResponseModel, SuccessResponseModel}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import utils.Constants
import views.html.pages.result._

import scala.concurrent.Future


object ResultController extends ResultController with ServicesConfig {
  override val keyStoreConnector = KeyStoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.confirmFPUrl

  override val plaConnector = PLAConnector
}

trait ResultController extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val plaConnector: PLAConnector

  val processFPApplication = AuthorisedByAny.async {
    implicit user => implicit request =>
      implicit val protectionType = ApplicationType.FP2016

      plaConnector.applyFP16(user.nino.get).map {
        response: HttpResponse => implicit val ap = applicationOutcome(response);
          ap match {
            case ApplicationOutcome.MCNeeded => Locked(manualCorrespondenceNeeded())
            case ApplicationOutcome.Successful => saveAndRedirectToDisplaySuccess(response)
            case ApplicationOutcome.Rejected => saveAndRedirectToDisplaySuccess(response)
          }
      }
  }

  val processIPApplication = AuthorisedByAny.async {
    implicit user => implicit request =>
      implicit val protectionType = ApplicationType.IP2016
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
        plaConnector.applyIP16(user.nino.get, userData.get)
          .map {
            response: HttpResponse => implicit val appOutcome: ApplicationOutcome.Value = applicationOutcome(response);
              appOutcome match {
                case ApplicationOutcome.MCNeeded => Locked(manualCorrespondenceNeeded())
                case ApplicationOutcome.Successful => saveAndRedirectToDisplaySuccess(response)
                case ApplicationOutcome.Rejected => saveAndRedirectToDisplaySuccess(response)
              }
          }
      )
  }

  val processIP14Application = AuthorisedByAny.async {
    implicit user => implicit request =>
      implicit val protectionType = ApplicationType.IP2014
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
        plaConnector.applyIP14(user.nino.get, userData.get)
          .map {
            response: HttpResponse =>
              implicit val appOutcome = applicationOutcome(response)
              appOutcome match {
                case ApplicationOutcome.MCNeeded => Locked(manualCorrespondenceNeeded())
                case ApplicationOutcome.Successful => saveAndRedirectToDisplaySuccess(response)
                case ApplicationOutcome.Rejected => saveAndRedirectToDisplaySuccess(response)
              }
          }
      )
  }

  def applicationOutcome(response: HttpResponse)(implicit user: PLAUser, protectionType: ApplicationType.Value): ApplicationOutcome.Value = {
    if (response.status == 423) ApplicationOutcome.MCNeeded
    else {
      val notificationId = (response.json \ "notificationId").asOpt[Int]
      assert(notificationId.isDefined, s"no notification ID returned in $protectionType application response for user nino ${user.nino}")
      val successCodes = protectionType match {
        case ApplicationType.FP2016 => Constants.successCodes
        case ApplicationType.IP2016 => Constants.ip16SuccessCodes
        case ApplicationType.IP2014 => Constants.ip14SuccessCodes
      }
      if (successCodes.contains(notificationId.get)) ApplicationOutcome.Successful else ApplicationOutcome.Rejected
    }
  }

  private def saveAndRedirectToDisplaySuccess(response: HttpResponse)(implicit request: Request[AnyContent], protectionType: ApplicationType.Value, appOutcome: ApplicationOutcome.Value) = {

    appOutcome match {
      case ApplicationOutcome.Successful =>

        val successResponse: SuccessResponseModel = ResponseConstructors.createSuccessResponseFromJson(response.json)
        val protModel = response.json.validate[ProtectionModel]

        def redirectHelper(responseModel: SuccessResponseModel) = {
          import ApplicationType._
          responseModel.protectionType match {
            case IP2016 => Redirect(routes.ResultController.displayIP16())
            case IP2014 => Redirect(routes.ResultController.displayIP14())
            case FP2016 => Redirect(routes.ResultController.displayFP16())
          }
        }

        protModel.fold(
          errors => {
            Logger.error(s"Unable to create printable model from success response for ${
              successResponse.protectionType.toString()
            }")
            val errorModel = successResponse.copy(printable = false)
            keyStoreConnector.saveData[SuccessResponseModel]("successModel", errorModel)
            redirectHelper(errorModel)
          },
          success => {
            val protDisp: ProtectionDisplayModel = ExistingProtectionsConstructor.createProtectionDisplayModel(success, (response.json \ "psaCheckReference").toString())
            keyStoreConnector.saveData[ProtectionDisplayModel]("openProtection", protDisp)

            keyStoreConnector.saveData[SuccessResponseModel]("successModel", successResponse)
            redirectHelper(successResponse)
          }
        )
      case ApplicationOutcome.Rejected =>

        val rejectResponse: RejectionResponseModel = ResponseConstructors.createRejectionResponseFromJson(response.json)
        def redirectHelper(responseModel: RejectionResponseModel) = {
          import ApplicationType._
          responseModel.protectionType match {
            case IP2016 => Redirect(routes.ResultController.displayIP16())
            case IP2014 => Redirect(routes.ResultController.displayIP14())
            case FP2016 => Redirect(routes.ResultController.displayFP16())
          }
        }
        keyStoreConnector.saveData[RejectionResponseModel]("rejectModel", rejectResponse)
        redirectHelper(rejectResponse)
    } //end match

  } //end

  val displayIP16, displayIP14, displayFP16 = displayResult()

  def displayResult(): Action[AnyContent] = AuthorisedByAny.async{

//      keyStoreConnector.fetchAndGetFormData[String]("result").map {
//        case Some("success") => AuthorisedByAny.async {
//          implicit user => implicit request =>
//            keyStoreConnector.fetchAndGetFormData[SuccessResponseModel]("successModel").map {
//              case Some(model) => Ok(resultSuccess(model))
//              case _ => InternalServerError
//            }
//        } //end case
//        case Some("rejection") => AuthorisedByAny.async {
//          implicit user => implicit request =>
//            keyStoreConnector.fetchAndGetFormData[RejectionResponseModel]("rejectModel").map {
//              case Some(model) => Ok(resultRejected(model))
//              case _ => InternalServerError
//            }
//        } //end case
//      }

    implicit user => implicit request=>
    keyStoreConnector.fetchAndGetFormData[SuccessResponseModel]("successModel").map {
        case Some(model) => Ok(resultSuccess(model))
        case _ => InternalServerError
    }

  }//end method

}
