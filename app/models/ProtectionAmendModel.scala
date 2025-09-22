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

package models

import play.api.libs.json.{Json, OFormat}

case class ProtectionAmendModel(
    psaCheckReference: Option[String],
    protectionID: Option[Long],
    certificateDate: Option[String] = None,
    version: Option[Int] = None,
    protectionType: Option[String] = None,
    status: Option[String] = None,
    protectedAmount: Option[Double] = None,
    relevantAmount: Option[Double] = None,
    postADayBenefitCrystallisationEvents: Option[Double] = None,
    preADayPensionInPayment: Option[Double] = None,
    uncrystallisedRights: Option[Double] = None,
    nonUKRights: Option[Double] = None,
    pensionDebitAmount: Option[Double] = None,
    pensionDebitEnteredAmount: Option[Double] = None,
    pensionDebitStartDate: Option[String] = None,
    pensionDebitTotalAmount: Option[Double] = None,
    pensionDebits: Option[List[PensionDebitModel]] = None,
    notificationId: Option[Int] = None,
    protectionReference: Option[String] = None,
    withdrawnDate: Option[String] = None
) {

  def isEmpty: Boolean = this == ProtectionAmendModel(None, None)

  def toProtectionModel: ProtectionModel = ProtectionModel(
    psaCheckReference = psaCheckReference,
    protectionID = protectionID,
    certificateDate = certificateDate,
    version = version,
    protectionType = protectionType,
    status = status,
    protectedAmount = protectedAmount,
    relevantAmount = relevantAmount,
    postADayBenefitCrystallisationEvents = postADayBenefitCrystallisationEvents,
    preADayPensionInPayment = preADayPensionInPayment,
    uncrystallisedRights = uncrystallisedRights,
    nonUKRights = nonUKRights,
    pensionDebitAmount = pensionDebitAmount,
    pensionDebitEnteredAmount = pensionDebitEnteredAmount,
    pensionDebitStartDate = pensionDebitStartDate,
    pensionDebitTotalAmount = pensionDebitTotalAmount,
    pensionDebits = pensionDebits,
    notificationId = notificationId,
    protectionReference = protectionReference,
    withdrawnDate = withdrawnDate
  )

}

object ProtectionAmendModel {

  def apply(protectionModel: ProtectionModel): ProtectionAmendModel = ProtectionAmendModel(
    psaCheckReference = protectionModel.psaCheckReference,
    protectionID = protectionModel.protectionID,
    certificateDate = protectionModel.certificateDate,
    version = protectionModel.version,
    protectionType = protectionModel.protectionType,
    status = protectionModel.status,
    protectedAmount = protectionModel.protectedAmount,
    relevantAmount = protectionModel.relevantAmount,
    postADayBenefitCrystallisationEvents = protectionModel.postADayBenefitCrystallisationEvents,
    preADayPensionInPayment = protectionModel.preADayPensionInPayment,
    uncrystallisedRights = protectionModel.uncrystallisedRights,
    nonUKRights = protectionModel.nonUKRights,
    pensionDebitAmount = protectionModel.pensionDebitAmount,
    pensionDebitEnteredAmount = protectionModel.pensionDebitEnteredAmount,
    pensionDebitStartDate = protectionModel.pensionDebitStartDate,
    pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount,
    pensionDebits = protectionModel.pensionDebits,
    notificationId = protectionModel.notificationId,
    protectionReference = protectionModel.protectionReference,
    withdrawnDate = protectionModel.withdrawnDate
  )

  implicit val format: OFormat[ProtectionAmendModel] = Json.format[ProtectionAmendModel]
}
