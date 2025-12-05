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
import models.pla.response.AmendProtectionResponseStatus.Withdrawn
import models.{AmendResponseModel, NotificationId, PersonalDetailsModel}
import play.api.i18n.{Lang, Messages}

object AmendOutcomeDisplayModelConstructor {

  def createAmendOutcomeDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String,
      notificationId: NotificationId
  )(implicit lang: Lang, messages: Messages): AmendOutcomeDisplayModel = {
    val printDetails =
      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(personalDetailsModelOpt, model, nino)

    val protectedAmount = extractProtectedAmount(notificationId, model)

    val protectedAmountString = Display.currencyDisplayString(protectedAmount)

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

  private def extractProtectedAmount(notificationId: NotificationId, model: AmendResponseModel): Int =
    if (notificationId.status == Withdrawn) {
      model.relevantAmount
    } else {
      model.protectedAmount.getOrElse {
        throw Exceptions.OptionNotDefinedException(
          "createAmendResultDisplayModel",
          "protectedAmount",
          model.protectionType.toString
        )
      }
    }

}
