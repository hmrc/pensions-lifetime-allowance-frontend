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

package models.display

import models.pla.AmendableProtectionType
import models.{AmendedProtectionType, NotificationId}
import models.pla.response.AmendProtectionResponseStatus
import play.api.mvc.Call

case class AmendPrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: AmendedProtectionType,
    status: Option[AmendProtectionResponseStatus],
    psaCheckReference: Option[String],
    protectionReference: Option[String],
    fixedProtectionReference: Option[String],
    protectedAmount: Option[String],
    certificateDate: Option[String],
    certificateTime: Option[String]
)

case class AmendDisplayModel(
    protectionType: AmendableProtectionType,
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

case class AmendOutcomeDisplayModel(
    notificationId: NotificationId,
    protectedAmount: String,
    details: Option[AmendPrintDisplayModel]
)

case class AmendOutcomeDisplayModelNoNotificationId(
    protectedAmount: String,
    protectionType: AmendedProtectionType,
    details: Option[AmendPrintDisplayModel]
)
