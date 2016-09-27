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
import common.Strings
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.KeyStoreConnector
import constructors.DisplayConstructors
import enums.ApplicationType
import forms.AmendUKPensionForm._
import models.{AmendProtectionModel, AmendedUKPensionModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object AmendsController extends AmendsController{
  val keyStoreConnector = KeyStoreConnector
  val displayConstructors = DisplayConstructors
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val postSignInRedirectUrl = FrontendAppConfig.ipStartUrl
}

trait AmendsController  extends FrontendController with AuthorisedForPLA {

  val keyStoreConnector: KeyStoreConnector
  val displayConstructors: DisplayConstructors

  def amendsSummary(protectionType: String, status: String) = AuthorisedByAny.async { implicit user => implicit request =>

    val protectionKey = Strings.keyStoreAmendFetchString(protectionType, status)
    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel](protectionKey).map {
      case Some(amendModel) => Ok(views.html.pages.amends.amendSummary(displayConstructors.createAmendDisplayModel(amendModel)))
      case _ => InternalServerError(views.html.pages.fallback.technicalError(ApplicationType.existingProtections.toString)).withHeaders(CACHE_CONTROL -> "no-cache")
    }
  }

  val amendCurrentUKPension = AuthorisedByAny.async { implicit user => implicit request =>

//    keyStoreConnector.fetchAndGetFormData[AmendProtectionModel]("amendCurrentUKPension").map {
//      case Some(data) =>
//        //Ok(pages.amends.amendIP16CurrentUKPension(amendUKPensionForm.fill(data.updatedProtection)))
//      //case _ => Ok(pages.ip2016.currentPensions(currentPensionsForm))
//    }

    Future.successful(Ok)
  }

  val submitAmendCurrentUKPension = AuthorisedByAny.async { implicit user => implicit request =>


//      amendUKPensionForm.bindFromRequest.fold(
//      errors => Future.successful(BadRequest(pages.amends.amendIP16CurrentUKPension(errors))),
//      success => {
//        keyStoreConnector.saveFormData("amendCurrentUKPension", success)
//        Future.successful(Redirect(/* TODO amends summary page**/ ))
//      }
//    )


    Future.successful(Ok)
  }

}
