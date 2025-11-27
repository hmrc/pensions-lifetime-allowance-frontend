/*
 * Copyright 2025 HM Revenue & Customs
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

package constructors.display

import common.{Display, Exceptions}
import models.display.AmendOutcomeDisplayModel
import models.{AmendResponseModel, PersonalDetailsModel}
import play.api.i18n.{Lang, Messages}
import utils.Constants

object AmendOutcomeDisplayModelConstructor {

  def createAmendOutcomeDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang, messages: Messages): AmendOutcomeDisplayModel = {
    val printDetails =
      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(personalDetailsModelOpt, model.protection, nino)

    val notificationId = model.protection.notificationId.getOrElse(
      throw Exceptions.OptionNotDefinedException(
        "createAmendResultDisplayModel",
        "notificationId",
        model.protection.protectionType.getOrElse("No protection type in response")
      )
    )

    val protectedAmount = extractProtectedAmount(notificationId, model)

    val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

    AmendOutcomeDisplayModel(
      notificationId,
      protectedAmountString,
      Some(
        printDetails.copy(
          protectedAmount = printDetails.protectedAmount.map(_ => protectedAmountString)
        )
      )
    )
  }

  private def extractProtectedAmount(notificationId: Int, model: AmendResponseModel): Double =
    if (Constants.withdrawnNotificationIds.contains(notificationId)) {
      model.protection.relevantAmount.getOrElse {
        throw Exceptions.OptionNotDefinedException(
          "createAmendResultDisplayModel",
          "relevantAmount",
          model.protection.protectionType.getOrElse("No protection type in response")
        )
      }
    } else {
      model.protection.protectedAmount.getOrElse {
        throw Exceptions.OptionNotDefinedException(
          "createAmendResultDisplayModel",
          "protectedAmount",
          model.protection.protectionType.getOrElse("No protection type in response")
        )
      }
    }

}
