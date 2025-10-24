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

import common.Strings
import enums.ApplicationType
import models.pla.response.ProtectionType
import play.api.mvc.Call

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
    protectionReference: String,
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
    certificateTime: Option[String] = None,
    withdrawnDate: Option[String] = None,
    lumpSumAmount: Option[String] = None,
    lumpSumPercentage: Option[String] = None,
    factor: Option[String] = None,
    enhancementFactor: Option[String] = None
)

case class ExistingProtectionsDisplayModel(
    activeProtection: Option[ExistingProtectionDisplayModel],
    inactiveProtections: ExistingInactiveProtectionsDisplayModel
)

case class ExistingInactiveProtectionsDisplayModel(
    dormantProtections: ExistingInactiveProtectionsByType,
    withdrawnProtections: ExistingInactiveProtectionsByType,
    unsuccessfulProtections: ExistingInactiveProtectionsByType,
    rejectedProtections: ExistingInactiveProtectionsByType,
    expiredProtections: ExistingInactiveProtectionsByType
) {

  def isEmpty: Boolean =
    dormantProtections.isEmpty && withdrawnProtections.isEmpty && unsuccessfulProtections.isEmpty && rejectedProtections.isEmpty && expiredProtections.isEmpty

  def nonEmpty: Boolean = !isEmpty

}

object ExistingInactiveProtectionsDisplayModel {

  def empty =
    ExistingInactiveProtectionsDisplayModel(
      ExistingInactiveProtectionsByType.empty,
      ExistingInactiveProtectionsByType.empty,
      ExistingInactiveProtectionsByType.empty,
      ExistingInactiveProtectionsByType.empty,
      ExistingInactiveProtectionsByType.empty
    )

}

case class ExistingInactiveProtectionsByType(
    protections: Seq[(String, Seq[ExistingProtectionDisplayModel])]
) {
  def isEmpty: Boolean = protections.isEmpty

  def nonEmpty: Boolean = protections.nonEmpty

  def sorted: ExistingInactiveProtectionsByType = ExistingInactiveProtectionsByType(
    protections.sortWith((s1, s2) => ExistingInactiveProtectionsByType.sortByProtectionType(s1._1, s2._1))
  )

}

object ExistingInactiveProtectionsByType {

  def empty = ExistingInactiveProtectionsByType(Seq.empty)

  private val protectionTypeOrder: Map[String, Int] = Seq(
    ProtectionType.IndividualProtection2016,
    ProtectionType.IndividualProtection2016LTA,
    ProtectionType.IndividualProtection2014,
    ProtectionType.IndividualProtection2014LTA,
    ProtectionType.FixedProtection2016,
    ProtectionType.FixedProtection2016LTA,
    ProtectionType.FixedProtection2014,
    ProtectionType.FixedProtection2014LTA,
    ProtectionType.PrimaryProtection,
    ProtectionType.PrimaryProtectionLTA,
    ProtectionType.EnhancedProtection,
    ProtectionType.EnhancedProtectionLTA,
    ProtectionType.FixedProtection,
    ProtectionType.FixedProtectionLTA,
    ProtectionType.InternationalEnhancementS221,
    ProtectionType.InternationalEnhancementS224,
    ProtectionType.PensionCreditRights
  )
    .map(_.toString)
    .zipWithIndex
    .toMap

  private def sortByProtectionType(t1: String, t2: String): Boolean = protectionTypeOrder(t1) < protectionTypeOrder(t2)
}

case class PrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: String,
    status: String,
    psaCheckReference: String,
    protectionReference: String,
    protectedAmount: Option[String],
    certificateDate: Option[String],
    certificateTime: Option[String] = None,
    lumpSumPercentage: Option[String] = None,
    lumpSumAmount: Option[String] = None,
    enhancementFactor: Option[String] = None,
    factor: Option[String] = None
) {

  def isFixedProtection2016: Boolean = {
    val fp2016Str = "FP2016"
    val fixedProtection2016Types = Set(
      fp2016Str.toLowerCase,
      ProtectionType.FixedProtection2016.toString.toLowerCase,
      ProtectionType.FixedProtection2016LTA.toString.toLowerCase
    )

    fixedProtection2016Types.contains(protectionType.toLowerCase)
  }

}

case class AmendPrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: String,
    status: Option[String],
    psaCheckReference: Option[String],
    protectionReference: Option[String],
    fixedProtectionReference: Option[String],
    protectedAmount: Option[String],
    certificateDate: Option[String],
    certificateTime: Option[String]
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

case class AmendResultDisplayModelNoNotificationId(
    protectedAmount: String,
    protectionType: String,
    details: Option[AmendPrintDisplayModel]
)

case class InactiveAmendResultDisplayModel(
    notificationId: Int,
    additionalInfo: Seq[String]
)
