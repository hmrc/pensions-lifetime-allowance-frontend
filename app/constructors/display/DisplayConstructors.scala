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

import models.amendModels.AmendProtectionModel
import models.display.{
  ActiveAmendResultDisplayModel,
  AmendDisplayModel,
  AmendPrintDisplayModel,
  AmendResultDisplayModel,
  AmendResultDisplayModelNoNotificationId,
  ExistingProtectionsDisplayModel,
  InactiveAmendResultDisplayModel,
  PrintDisplayModel
}
import models.{AmendResponseModel, PersonalDetailsModel, ProtectionModel, TransformedReadResponseModel}
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

  def createActiveAmendResponseDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang): ActiveAmendResultDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(model, personalDetailsModelOpt, nino)
  }

  def createAmendPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  )(implicit lang: Lang): AmendPrintDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)
  }

  def createAmendResultDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang): AmendResultDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendResultDisplayModelConstructor.createAmendResultDisplayModel(model, personalDetailsModelOpt, nino)
  }

  def createAmendResultDisplayModelNoNotificationId(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(implicit lang: Lang): AmendResultDisplayModelNoNotificationId = {
    implicit val messages: Messages = messagesForLang(lang)

    AmendResultDisplayModelNoNotificationIdConstructor.createAmendResultDisplayModelNoNotificationId(
      model,
      personalDetailsModelOpt,
      nino
    )
  }

  def createInactiveAmendResponseDisplayModel(
      model: AmendResponseModel
  )(implicit lang: Lang): InactiveAmendResultDisplayModel = {
    implicit val messages: Messages = messagesForLang(lang)

    InactiveAmendResultDisplayModelConstructor.createInactiveAmendResponseDisplayModel(model)
  }

  private def messagesForLang(lang: Lang): Messages =
    messagesApi.preferred(Seq(lang))

}
