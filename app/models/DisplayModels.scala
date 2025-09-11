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

package models

import enums.ApplicationType
import play.api.mvc.Call

import scala.collection.SeqMap

case class SuccessDisplayModel(
    protectionType: ApplicationType.Value,
    notificationId: String,
    protectedAmount: String,
    printable: Boolean,
    details: Option[ProtectionDetailsDisplayModel],
    additionalInfo: Seq[String]
)

case class RejectionDisplayModel(
    notificationId: String,
    additionalInfo: Seq[String],
    protectionType: ApplicationType.Value
)

case class ProtectionDetailsDisplayModel(
    protectionReference: Option[String],
    psaReference: String,
    applicationDate: Option[String]
)

case class ExistingProtectionDisplayModel(
    protectionType: String,
    status: String,
    amendCall: Option[Call],
    psaCheckReference: Option[String],
    protectionReference: String,
    protectedAmount: Option[String],
    certificateDate: Option[String],
    withdrawnDate: Option[String] = None
)

case class ExistingProtectionsDisplayModel(
    activeProtection: Option[ExistingProtectionDisplayModel],
    inactiveProtections: Option[ExistingInactiveProtectionsDisplayModel]
)

case class ExistingInactiveProtectionsDisplayModel(
    dormantProtections: SeqMap[String, Seq[ExistingProtectionDisplayModel]],
    withdrawnProtections: SeqMap[String, Seq[ExistingProtectionDisplayModel]],
    unsuccessfulProtections: SeqMap[String, Seq[ExistingProtectionDisplayModel]],
    rejectedProtections: SeqMap[String, Seq[ExistingProtectionDisplayModel]],
    expiredProtections: SeqMap[String, Seq[ExistingProtectionDisplayModel]]
)

case class PrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: String,
    status: String,
    psaCheckReference: String,
    protectionReference: String,
    protectedAmount: Option[String],
    certificateDate: Option[String]
)

case class AmendPrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: String,
    status: String,
    psaCheckReference: String,
    protectionReference: String,
    protectedAmount: Option[String],
    certificateDate: Option[String],
    notificationId: Int
)

case class AmendDisplayModel(
    protectionType: String,
    amended: Boolean,
    pensionContributionSections: Seq[AmendDisplaySectionModel],
    psoAdded: Boolean,
    psoSections: Seq[AmendDisplaySectionModel],
    totalAmount: String
)

case class AmendDisplaySectionModel(
    sectionId: String,
    rows: Seq[AmendDisplayRowModel]
)

case class AmendDisplayRowModel(
    rowId: String,
    changeLinkCall: Option[Call],
    removeLinkCall: Option[Call],
    displayValue: String*
)

case class ActiveAmendResultDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: ApplicationType.Value,
    notificationId: String,
    protectedAmount: String,
    details: Option[ProtectionDetailsDisplayModel]
)

case class AmendResultDisplayModel(
    notificationId: Int,
    protectedAmount: String,
    details: Option[AmendPrintDisplayModel]
)

case class InactiveAmendResultDisplayModel(
    notificationId: Int,
    additionalInfo: Seq[String]
)
