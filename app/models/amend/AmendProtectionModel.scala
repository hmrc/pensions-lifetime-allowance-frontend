/*
 * Copyright 2023 HM Revenue & Customs
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

package models.amend

import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{DateModel, PensionDebitModel, ProtectionModel, TimeModel}
import play.api.libs.json.{Json, OFormat}

case class AmendProtectionModel(
    psaCheckReference: String,
    identifier: Long,
    sequenceNumber: Int,
    protectionType: AmendableProtectionType,
    status: AmendProtectionRequestStatus,
    pensionDebitTotalAmount: Option[Double],
    certificateDate: Option[DateModel],
    certificateTime: Option[TimeModel],
    protectionReference: Option[String],
    protectedAmount: Option[Double],
    original: AmendProtectionFields,
    updated: AmendProtectionFields
) {

  def hasChanges: Boolean = original != updated

  def updatedRelevantAmount: Double =
    List(
      updated.preADayPensionInPaymentAmount.map(_.toInt),
      updated.postADayBenefitCrystallisationEventAmount.map(_.toInt),
      updated.nonUKRightsAmount.map(_.toInt),
      Some(updated.uncrystallisedRightsAmount.toInt)
    ).flatten.sum.toDouble - pensionDebitTotalAmount.getOrElse[Double](0)

  def withPostADayBenefitCrystallisationEventAmount(value: Option[Double]): AmendProtectionModel =
    copy(
      updated = updated.copy(
        postADayBenefitCrystallisationEventAmount = value
      )
    )

  def withPreADayPensionInPaymentAmount(value: Option[Double]): AmendProtectionModel =
    copy(
      updated = updated.copy(
        preADayPensionInPaymentAmount = value
      )
    )

  def withUncrystallisedRightsAmount(value: Double): AmendProtectionModel =
    copy(
      updated = updated.copy(
        uncrystallisedRightsAmount = value
      )
    )

  def withNonUKRightsAmount(value: Option[Double]): AmendProtectionModel =
    copy(
      updated = updated.copy(
        nonUKRightsAmount = value
      )
    )

  def withPensionDebit(value: Option[PensionDebitModel]): AmendProtectionModel =
    copy(
      updated = updated.copy(
        pensionDebit = value
      )
    )

}

object AmendProtectionModel {

  def tryFromProtection(protectionModel: ProtectionModel): Option[AmendProtectionModel] =
    protectionModel.asAmendable.map { case (protectionType, status) =>
      val fields = AmendProtectionFields
        .fromProtection(protectionModel)
        .copy(
          pensionDebit = None
        )

      AmendProtectionModel(
        psaCheckReference = protectionModel.psaCheckReference,
        identifier = protectionModel.identifier,
        sequenceNumber = protectionModel.sequenceNumber,
        protectionType = protectionType,
        status = status,
        pensionDebitTotalAmount = protectionModel.pensionDebitTotalAmount,
        protectionReference = protectionModel.protectionReference,
        certificateDate = protectionModel.certificateDate,
        certificateTime = protectionModel.certificateTime,
        protectedAmount = protectionModel.protectedAmount,
        original = fields,
        updated = fields
      )
    }

  implicit val format: OFormat[AmendProtectionModel] = Json.format[AmendProtectionModel]
}

case class AmendProtectionFields(
    postADayBenefitCrystallisationEventAmount: Option[Double],
    preADayPensionInPaymentAmount: Option[Double],
    uncrystallisedRightsAmount: Double,
    nonUKRightsAmount: Option[Double],
    pensionDebit: Option[PensionDebitModel]
)

object AmendProtectionFields {

  def fromProtection(protectionModel: ProtectionModel): AmendProtectionFields = AmendProtectionFields(
    postADayBenefitCrystallisationEventAmount = protectionModel.postADayBenefitCrystallisationEventAmount,
    preADayPensionInPaymentAmount = protectionModel.preADayPensionInPaymentAmount,
    uncrystallisedRightsAmount = protectionModel.uncrystallisedRightsAmount.getOrElse(0),
    nonUKRightsAmount = protectionModel.nonUKRightsAmount,
    pensionDebit = None
  )

  implicit val format: OFormat[AmendProtectionFields] = Json.format[AmendProtectionFields]
}
