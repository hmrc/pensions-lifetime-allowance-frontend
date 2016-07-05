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
import config.{FrontendAppConfig,FrontendAuthConnector}
import connectors.KeyStoreConnector
import play.api.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import constructors.ResponseConstructors
import views.html.pages.result._
import connectors.PLAConnector
import utils.Constants


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
      plaConnector.applyFP16(user.nino.get).map {
        response: HttpResponse => applicationOutcome(response) match {
          case "successful" => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case "rejected"   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
  }

  def applicationOutcome(response: HttpResponse): String = {
    val notificationId = (response.json \ "notificationId").asOpt[Int]
    assert(notificationId.isDefined, Logger.error(s"no notification ID returned in FP application response. Response: $response"))
    if(Constants.successCodes.contains(notificationId.get)) "successful" else "rejected"
  }




  val processIPApplication = AuthorisedByAny.async {
    implicit user =>  implicit request =>
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
      plaConnector.applyIP16(user.nino.get, userData.get)
      .map {
        response: HttpResponse => ip16ApplicationOutcome(response) match {
          case "successful" => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case "rejected"   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
    )

  }

  def ip16ApplicationOutcome(response: HttpResponse): String = {
    val notificationId = (response.json \ "notificationId").asOpt[Int]
    assert(notificationId.isDefined, Logger.error(s"no notification ID returned in IP2016 application response. Response: $response"))
    if(Constants.ip16SuccessCodes.contains(notificationId.get)) "successful" else "rejected"
  }




  val processIP14Application = AuthorisedByAny.async {
    implicit user =>  implicit request =>
      keyStoreConnector.fetchAllUserData.flatMap(userData =>
      plaConnector.applyIP14(user.nino.get, userData.get)
      .map {
        response: HttpResponse => ip14ApplicationOutcome(response) match {
          case "successful" => Ok(resultSuccess(ResponseConstructors.createSuccessResponseFromJson(response.json)))
          case "rejected"   => Ok(resultRejected(ResponseConstructors.createRejectionResponseFromJson(response.json)))
        }
      }
    )

  }

  def ip14ApplicationOutcome(response: HttpResponse): String = {
    val notificationId = (response.json \ "notificationId").asOpt[Int]
    assert(notificationId.isDefined, Logger.error(s"no notification ID returned in IP2014 application response. Response: $response"))
    if(Constants.ip14SuccessCodes.contains(notificationId.get)) "successful" else "rejected"
  }
}
