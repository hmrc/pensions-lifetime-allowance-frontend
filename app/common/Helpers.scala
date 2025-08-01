/*
 * Copyright 2023 HM Revenue & Customs
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

  def createAmendCallIfRequired(protection: ProtectionModel): Option[Call] = {
    val status         = protection.status.map(_.toLowerCase).getOrElse("none")
    val protectionType = protection.protectionType.map(_.toLowerCase).getOrElse("none")
    if (protection.isAmendable)
      Some(controllers.routes.AmendsController.amendsSummary(protectionType, status))
    else None
  }

  def createPsoRemoveCall(protection: ProtectionModel): Option[Call] =
    if (protection.isAmendable) {
      val status         = protection.status.map(_.toLowerCase).getOrElse("none")
      val protectionType = protection.protectionType.map(_.toLowerCase).getOrElse("none")
      Some(controllers.routes.AmendsRemovePensionSharingOrderController.removePso(protectionType, status))
    } else None

  def createAmendCall(protection: ProtectionModel, applicationSection: ApplicationStage.Value): Call = {
    val protectionType = protection.protectionType
      .getOrElse(throw new Exceptions.RequiredValueNotDefinedException("createAmendCall", "protectionType"))
      .toLowerCase
    val status = protection.status
      .getOrElse(throw new Exceptions.RequiredValueNotDefinedException("createAmendCall", "protectionStatus"))
      .toLowerCase

    import ApplicationStage._
    applicationSection match {
      case PensionsTakenBefore =>
        controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore(protectionType, status)
      case PensionsWorthBefore =>
        controllers.routes.AmendsPensionWorthBeforeController.amendPensionsWorthBefore(protectionType, status)
      case PensionsTakenBetween =>
        controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween(protectionType, status)
      case PensionsUsedBetween =>
        controllers.routes.AmendsPensionUsedBetweenController.amendPensionsUsedBetween(protectionType, status)
      case OverseasPensions =>
        controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(protectionType, status)
      case CurrentPensions =>
        controllers.routes.AmendsCurrentPensionController.amendCurrentPensions(protectionType, status)
      case CurrentPsos =>
        controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails(protectionType, status)
    }
  }

  def totalValue(protection: ProtectionModel): Double =
    List(
      protection.preADayPensionInPayment,
      protection.postADayBenefitCrystallisationEvents,
      protection.nonUKRights,
      protection.uncrystallisedRights
    ).flatten.sum - protection.pensionDebitTotalAmount.getOrElse(0.0)

}
