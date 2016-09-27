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

import auth.AuthorisedForPLA
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.KeyStoreConnector
import enums.ApplicationType
import forms.AmendUKPensionForm._
import models.{AmendProtectionModel, AmendedUKPensionModel, ProtectionModel}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views._
import views.html.pages

import scala.concurrent.Future

object AmendsController extends AmendsController{
  val keyStoreConnector = KeyStoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait AmendsController  extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector

  val amendCurrentUKPension = AuthorisedByAny.async { implicit user => implicit request =>

    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel]("amendedProtection").map {
      case Some(data) =>
        val currentUKPensionAmt = AmendedUKPensionModel(Some(data.updatedProtection.uncrystallisedRights.get))
        Ok(pages.amends.amendIP16CurrentUKPension(amendUKPensionForm.fill(currentUKPensionAmt)))
      case _ =>
        Logger.error(s"Could not retrieve amend proteciton model for user with nino ${user.nino} when loading the amend current UK pension page")

        //TODO replace get request with intenral server error when summary page completed
        /////////

        val dummyProtModel = ProtectionModel(
          Some("testPSARef"),
          notificationId = Some(24),
          protectionID = Some(12345),
          protectionType = Some("FP2016"),
          certificateDate = Some("2016-04-17"),
          protectedAmount = Some(1250000),
          protectionReference = Some("PSA123456"))
        val dummyModel = AmendProtectionModel(dummyProtModel, dummyProtModel.copy())
        keyStoreConnector.saveFormData[AmendProtectionModel]("amendedProtection", dummyModel)
        /////////
        Ok(pages.amends.amendIP16CurrentUKPension(amendUKPensionForm.fill(AmendedUKPensionModel(Some(BigDecimal(9999999.99))))))
    }
  }

  def showDummy: Action[AnyContent] = Action {
    implicit request =>
      Ok("DUMMY SUMMARY PAGE:\n\t" + request)
  }

  val submitAmendCurrentUKPension = AuthorisedByAny.async { implicit user => implicit request =>

      amendUKPensionForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(pages.amends.amendIP16CurrentUKPension(errors))),
      success => {

        keyStoreConnector.fetchAndGetFormData[AmendProtectionModel]("amendedProtection").map{
          case Some(model) =>
            val updated = model.updatedProtection.copy(uncrystallisedRights = Some(success.amendedUKPensionAmt.get.toDouble))
            keyStoreConnector.saveFormData[AmendProtectionModel]("amendedProtection", AmendProtectionModel(model.originalProtection, updated))
            //TODO redirect back to summary page
            Redirect(routes.AmendsController.showDummy)
          case _ =>
            Logger.error(s"Could not retrieve amend protection model for user with nino ${user.nino} when amending current UK pension")
            InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.IP2016.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
        }
      }
    )
  }

}
