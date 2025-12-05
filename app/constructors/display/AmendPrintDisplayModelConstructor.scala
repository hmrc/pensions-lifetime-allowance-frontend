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

import models.display.AmendPrintDisplayModel
import models.{AmendResponseModel, NotificationId, PersonalDetailsModel}
import play.api.i18n.{Lang, Messages}
import utils.NotificationIds

object AmendPrintDisplayModelConstructor {

  def createAmendPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: AmendResponseModel,
      nino: String
  )(implicit lang: Lang, messages: Messages): AmendPrintDisplayModel = {
    val printDisplayModel =
      PrintDisplayModelConstructor.createPrintDisplayModel(
        personalDetailsModelOpt,
        protectionModel.toProtectionModel,
        nino
      )

    val protectionReference = Some(printDisplayModel.protectionReference).filter(_ =>
      shouldDisplayProtectionReference(protectionModel.notificationId)
    )

    val fixedProtectionReference = Some(printDisplayModel.protectionReference).filter(_ =>
      shouldDisplayFixedProtectionReference(protectionModel.notificationId)
    )

    val psaCheckReference = Some(printDisplayModel.psaCheckReference).filter(_ =>
      shouldDisplayPsaCheckReference(protectionModel.notificationId)
    )

    val status =
      Some(printDisplayModel.status)
        .filter(_ => shouldDisplayStatus(protectionModel.notificationId))
        .map(_ => protectionModel.status)

    AmendPrintDisplayModel(
      firstName = printDisplayModel.firstName,
      surname = printDisplayModel.surname,
      nino = printDisplayModel.nino,
      protectionType = protectionModel.protectionType,
      status = status,
      psaCheckReference = psaCheckReference,
      protectionReference = protectionReference,
      fixedProtectionReference = fixedProtectionReference,
      protectedAmount = printDisplayModel.protectedAmount,
      certificateDate = printDisplayModel.certificateDate,
      certificateTime = printDisplayModel.certificateTime
    )
  }

  def shouldDisplayProtectionReference(notificationId: Option[NotificationId]): Boolean = notificationId match {
    case Some(notificationId) => NotificationIds.showingProtectionReference.contains(notificationId)
    case None                 => false
  }

  def shouldDisplayFixedProtectionReference(notificationId: Option[NotificationId]): Boolean =
    notificationId match {
      case Some(notificationId) => NotificationIds.showingFixedProtection2016Details.contains(notificationId)
      case None                 => false
    }

  def shouldDisplayPsaCheckReference(notificationId: Option[NotificationId]): Boolean = notificationId match {
    case Some(notificationId) => NotificationIds.showingPsaCheckReference.contains(notificationId)
    case None                 => false
  }

  def shouldDisplayStatus(notificationId: Option[NotificationId]): Boolean = notificationId match {
    case Some(notificationId) => NotificationIds.showingStatus.contains(notificationId)
    case None                 => true
  }

}
