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

import auth.{PLAUser, AuthorisedForPLA}
import config.{FrontendAppConfig,FrontendAuthConnector}
import connectors.KeyStoreConnector
import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import constructors.ResponseConstructors
import views.html.pages.result._
import connectors.PLAConnector
import utils.Constants
import enums.{ApplicationOutcome, ApplicationType}

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
  val plaConnector : PLAConnector

  val processFPApplication = AuthorisedByAny.async {
    implicit user =>  implicit request =>
      implicit val protectionType = ApplicationType.FP2016
      plaConnector.applyFP16(user.nino.get).map {
        response: HttpResponse => applicationOutcome(response) match {
          case ApplicationOutcome.MCNeeded   => Locked(manualCorrespondenceNeeded())
          case ApplicationOutcome.Successful => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case ApplicationOutcome.Rejected   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
  }

  def applicationOutcome(response: HttpResponse)(implicit user: PLAUser, protectionType: ApplicationType.Value): ApplicationOutcome.Value = {
    if(response.status == 423) ApplicationOutcome.MCNeeded else {
      val notificationId = (response.json \ "notificationId").asOpt[Int]
      assert(notificationId.isDefined, s"no notification ID returned in $protectionType application response for user nino ${user.nino}")
      val successCodes = protectionType match {
        case ApplicationType.FP2016 => Constants.successCodes
        case ApplicationType.IP2016 => Constants.ip16SuccessCodes
        case ApplicationType.IP2014 => Constants.ip14SuccessCodes
      }
      if(successCodes.contains(notificationId.get)) ApplicationOutcome.Successful else ApplicationOutcome.Rejected
    }
  }


  val processIPApplication = AuthorisedByAny.async {
    implicit user =>  implicit request =>
      implicit val protectionType = ApplicationType.IP2016
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
      plaConnector.applyIP16(user.nino.get, userData.get)
      .map {
        response: HttpResponse => applicationOutcome(response) match {
          case ApplicationOutcome.MCNeeded   => Locked(manualCorrespondenceNeeded())
          case ApplicationOutcome.Successful => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case ApplicationOutcome.Rejected   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
    )
  }


  val processIP14Application = AuthorisedByAny.async {
    implicit user =>  implicit request =>
      implicit val protectionType = ApplicationType.IP2014
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
      plaConnector.applyIP14(user.nino.get, userData.get)
      .map {
        response: HttpResponse => applicationOutcome(response) match {
          case ApplicationOutcome.MCNeeded   => Locked(manualCorrespondenceNeeded())
          case ApplicationOutcome.Successful => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case ApplicationOutcome.Rejected   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
    )
  }
}
