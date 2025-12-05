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

import models.amend.AmendProtectionModel
import models.display.{
  AmendDisplayModel,
  AmendOutcomeDisplayModel,
  AmendOutcomeDisplayModelNoNotificationId,
  ExistingProtectionsDisplayModel,
  PrintDisplayModel
}
import models.{AmendResponseModel, NotificationId, PersonalDetailsModel, ProtectionModel, TransformedReadResponseModel}
import play.api.i18n.{Lang, Messages, MessagesApi}

import javax.inject.Inject

class DisplayConstructors @Inject() (implicit messagesApi: MessagesApi) {

  def createPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  )(implicit lang: Lang): PrintDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    PrintDisplayModelConstructor.createPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)
  }

  def createExistingProtectionsDisplayModel(
      model: TransformedReadResponseModel
  )(implicit lang: Lang): ExistingProtectionsDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(model)
  }

  def createAmendDisplayModel(model: AmendProtectionModel)(implicit lang: Lang): AmendDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendDisplayModelConstructor.createAmendDisplayModel(model)
  }

  def createAmendOutcomeDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String,
      notificationId: NotificationId
  )(implicit lang: Lang): AmendOutcomeDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
      model,
      personalDetailsModelOpt,
      nino,
      notificationId
    )
  }

  def createAmendOutcomeDisplayModelNoNotificationId(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang): AmendOutcomeDisplayModelNoNotificationId = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendOutcomeDisplayModelNoNotificationIdConstructor.createAmendOutcomeDisplayModelNoNotificationId(
      model,
      personalDetailsModelOpt,
      nino
    )
  }

  private def messagesForLang(lang: Lang): Messages =
    messagesApi.preferred(Seq(lang))

}
