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

  implicit val lang: Lang = Lang.defaultLang

  implicit val messages: Messages = messagesApi.preferred(Seq(lang))

  def createPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  ): PrintDisplayModel =
    PrintDisplayModelConstructor.createPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)

  def createExistingProtectionsDisplayModel(model: TransformedReadResponseModel)(
      implicit lang: Lang
  ): ExistingProtectionsDisplayModel =
    ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(model)

  def createAmendDisplayModel(model: AmendProtectionModel): AmendDisplayModel =
    AmendDisplayModelConstructor.createAmendDisplayModel(model)

  def createActiveAmendResponseDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  ): ActiveAmendResultDisplayModel =
    ActiveAmendResultDisplayModelConstructor.createActiveAmendResponseDisplayModel(model, personalDetailsModelOpt, nino)

  def createAmendPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  ): AmendPrintDisplayModel =
    AmendPrintDisplayModelConstructor.createAmendPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)

  def createAmendResultDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  ): AmendResultDisplayModel =
    AmendResultDisplayModelConstructor.createAmendResultDisplayModel(model, personalDetailsModelOpt, nino)

  def createAmendResultDisplayModelNoNotificationId(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  ): AmendResultDisplayModelNoNotificationId =
    AmendResultDisplayModelNoNotificationIdConstructor.createAmendResultDisplayModelNoNotificationId(
      model,
      personalDetailsModelOpt,
      nino
    )

  def createInactiveAmendResponseDisplayModel(model: AmendResponseModel): InactiveAmendResultDisplayModel =
    InactiveAmendResultDisplayModelConstructor.createInactiveAmendResponseDisplayModel(model)

}
