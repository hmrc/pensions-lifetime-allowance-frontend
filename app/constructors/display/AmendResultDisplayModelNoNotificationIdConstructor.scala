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
import models.display.AmendResultDisplayModelNoNotificationId
import models.{AmendResponseModel, PersonalDetailsModel}
import play.api.i18n.{Lang, Messages}

object AmendResultDisplayModelNoNotificationIdConstructor {

  def createAmendResultDisplayModelNoNotificationId(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang, messages: Messages): AmendResultDisplayModelNoNotificationId = {
    val printDetails =
      AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(personalDetailsModelOpt, model.protection, nino)

    val protectionType = model.protection.protectionType.getOrElse {
      throw Exceptions.OptionNotDefinedException(
        "createAmendResultDisplayModelNoNotificationId",
        "protectionType",
        "No protection type in response"
      )
    }

    val protectedAmount = model.protection.protectedAmount.getOrElse {
      throw Exceptions.OptionNotDefinedException(
        "createAmendResultDisplayModelNoNotificationId",
        "protectedAmount",
        protectionType
      )
    }

    val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

    AmendResultDisplayModelNoNotificationId(
      protectedAmount = protectedAmountString,
      protectionType = protectionType,
      details = Some(printDetails)
    )
  }

}
