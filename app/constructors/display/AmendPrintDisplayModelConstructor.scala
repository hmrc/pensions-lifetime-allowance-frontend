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

import constructors.display.PrintDisplayModelConstructor
import models.display.AmendPrintDisplayModel
import models.{PersonalDetailsModel, ProtectionModel}
import play.api.i18n.{Lang, Messages}
import utils.Constants

object AmendPrintDisplayModelConstructor {

  def createAmendPrintDisplayModel(
      personalDetailsModelOpt: Option[PersonalDetailsModel],
      protectionModel: ProtectionModel,
      nino: String
  )(implicit lang: Lang, messages: Messages): AmendPrintDisplayModel = {
    val printDisplayModel =
      PrintDisplayModelConstructor.createPrintDisplayModel(personalDetailsModelOpt, protectionModel, nino)

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
      Some(printDisplayModel.status).filter(_ => shouldDisplayStatus(protectionModel.notificationId))

    AmendPrintDisplayModel(
      firstName = printDisplayModel.firstName,
      surname = printDisplayModel.surname,
      nino = printDisplayModel.nino,
      protectionType = printDisplayModel.protectionType,
      status = status,
      psaCheckReference = psaCheckReference,
      protectionReference = protectionReference,
      fixedProtectionReference = fixedProtectionReference,
      protectedAmount = printDisplayModel.protectedAmount,
      certificateDate = printDisplayModel.certificateDate,
      certificateTime = printDisplayModel.certificateTime
    )
  }

  def shouldDisplayProtectionReference(notificationId: Option[Int]): Boolean = notificationId match {
    case Some(1) | Some(5) | Some(8) => true
    case _                           => false
  }

  def shouldDisplayFixedProtectionReference(notificationId: Option[Int]): Boolean =
    notificationId match {
      case Some(7) | Some(14) => true
      case _                  => false
    }

  def shouldDisplayPsaCheckReference(notificationId: Option[Int]): Boolean = notificationId match {
    case Some(1) | Some(5) | Some(8) => true
    case _                           => false
  }

  def shouldDisplayStatus(notificationId: Option[Int]): Boolean = notificationId match {
    case None                                                                                             => true
    case Some(notificationId) if Constants.dormantNotificationIds.contains(notificationId)                => true
    case Some(notificationId) if Constants.withdrawnNotificationIdsShowingStatus.contains(notificationId) => true
    case _                                                                                                => false
  }

}
