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
import play.api.i18n.Messages

import javax.inject.{Inject, Singleton}

@Singleton
class DisplayConstructors @Inject() {

  def createPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  )(using Messages): PrintDisplayModel =
    PrintDisplayModelConstructor.createPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)

  def createExistingProtectionsDisplayModel(
      model: TransformedReadResponseModel
  )(using Messages): ExistingProtectionsDisplayModel =
    ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(model)

  def createAmendDisplayModel(model: AmendProtectionModel)(using Messages): AmendDisplayModel =
    AmendDisplayModelConstructor.createAmendDisplayModel(model)

  def createAmendOutcomeDisplayModel(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String,
      notificationId: NotificationId
  )(using Messages): AmendOutcomeDisplayModel =
    AmendOutcomeDisplayModelConstructor.createAmendOutcomeDisplayModel(
      model,
      personalDetailsModelOpt,
      nino,
      notificationId
    )

  def createAmendOutcomeDisplayModelNoNotificationId(
      model: AmendResponseModel,
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      nino: String
  )(using Messages): AmendOutcomeDisplayModelNoNotificationId =
    AmendOutcomeDisplayModelNoNotificationIdConstructor.createAmendOutcomeDisplayModelNoNotificationId(
      model,
      personalDetailsModelOpt,
      nino
    )

}
