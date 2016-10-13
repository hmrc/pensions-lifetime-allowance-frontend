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

package common

import enums.ApplicationStage
import models.ProtectionModel
import play.api.mvc.Call

object Helpers {

  def protectionIsAmendable(protection: ProtectionModel): Boolean = {
    if(protection.status.exists(_.toLowerCase == "open") || protection.status.exists(_.toLowerCase == "dormant")) {
      protection.protectionType.exists(_.toLowerCase == "ip2016") || protection.protectionType.exists(_.toLowerCase == "ip2014")
    } else false
  }

  def createAmendCallIfRequired(protection: ProtectionModel): Option[Call] = {
    if (protectionIsAmendable(protection)) {
      val status = protection.status.map(_.toLowerCase).getOrElse("none")
      val protectionType = protection.protectionType.map(_.toLowerCase).getOrElse("none")
      Some(controllers.routes.AmendsController.amendsSummary(protectionType, status))
    } else None
  }

  def createAmendCall(protection: ProtectionModel, applicationSection: ApplicationStage.Value): Call = {
    val protectionType = protection.protectionType.getOrElse(throw new Exceptions.RequiredValueNotDefinedException("createAmendCall", "protectionType")).toLowerCase
    val status = protection.status.getOrElse(throw new Exceptions.RequiredValueNotDefinedException("createAmendCall", "protectionStatus")).toLowerCase

    import ApplicationStage._
    applicationSection match {
      case PensionsTakenBefore  => controllers.routes.AmendsController.amendPensionsTakenBefore(protectionType, status)
      case PensionsTakenBetween => controllers.routes.AmendsController.amendPensionsTakenBetween(protectionType, status)
      case OverseasPensions     => controllers.routes.AmendsController.amendOverseasPensions(protectionType, status)
      case CurrentPensions      => controllers.routes.AmendsController.amendCurrentPensions(protectionType, status)
      case CurrentPsos          => controllers.routes.AmendsController.amendPsoDetails(protectionType, status)
    }
  }

  def totalValue(protection: ProtectionModel): Double = {
    List(
      protection.preADayPensionInPayment,
      protection.postADayBenefitCrystallisationEvents,
      protection.nonUKRights,
      protection.uncrystallisedRights
    ).flatten.sum
  }
}
