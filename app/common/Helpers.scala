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

  def createAmendCallIfRequired(protection: ProtectionModel): Option[Call] =
    protection.asAmendable.map { case (protectionType, status) =>
      controllers.routes.AmendsController.amendsSummary(protectionType, status)
    }

  def createPsoRemoveCall(protection: ProtectionModel): Option[Call] =
    protection.asAmendable.map { case (protectionType, status) =>
      controllers.routes.AmendsRemovePensionSharingOrderController.removePso(protectionType, status)
    }

  def createAmendCall(protection: ProtectionModel, applicationSection: ApplicationStage.Value): Option[Call] = {
    import ApplicationStage._
    protection.asAmendable.map { case (protectionType, status) =>
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
  }

  def totalValue(protection: ProtectionModel): Double =
    List(
      protection.preADayPensionInPayment.map(_.toInt),
      protection.postADayBenefitCrystallisationEvents.map(_.toInt),
      protection.nonUKRights.map(_.toInt),
      protection.uncrystallisedRights.map(_.toInt)
    ).flatten.sum.toDouble - protection.pensionDebitTotalAmount.getOrElse(0d)

}
