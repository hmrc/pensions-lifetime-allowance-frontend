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

import common.Exceptions
import models.AmendResponseModel
import models.display._
import play.api.Logging
import play.api.i18n.Messages

object InactiveAmendResultDisplayModelConstructor extends Logging {

  def createInactiveAmendResponseDisplayModel(
      model: AmendResponseModel
  )(implicit messages: Messages): InactiveAmendResultDisplayModel = {
    val notificationId = model.protection.notificationId.getOrElse(
      throw Exceptions.OptionNotDefinedException(
        "createInactiveAmendResponseDisplayModel",
        "notificationId",
        model.protection.protectionType.getOrElse("No protection type in response")
      )
    )
    val additionalInfo = getAdditionalInfo("amendResultCode", notificationId)
    InactiveAmendResultDisplayModel(notificationId, additionalInfo)
  }

  private def getAdditionalInfo(messagesPrefix: String, notificationId: Int)(
      implicit messages: Messages
  ): List[String] = {

    def loop(notificationId: Int, i: Int = 1, paragraphs: List[String] = List.empty): List[String] = {
      val x: String = s"$messagesPrefix.$notificationId.$i"
      if (Messages(x) == x) {
        paragraphs
      } else {
        loop(notificationId, i + 1, paragraphs :+ i.toString)
      }
    }

    loop(notificationId)
  }

}
