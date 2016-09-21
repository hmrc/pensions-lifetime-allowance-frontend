/*
 * Copyright 2016 HM Revenue & Customs
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

case class SuccessDisplayModel(
                                protectionType: ApplicationType.Value,
                                notificationId: String, protectedAmount: String,
                                printable: Boolean,
                                details: Option[ProtectionDetailsModel],
                                additionalInfo: Seq[String]
                                )

case class RejectionDisplayModel(
                                  notificationId: String,
                                  additionalInfo: Seq[String],
                                  protectionType: ApplicationType.Value
                                  )



case class ExistingProtectionDisplayModel(
                                   protectionType: String,
                                   status: String,
                                   psaCheckReference: Option[String],
                                   protectionReference: String,
                                   protectedAmount: Option[String],
                                   certificateDate: Option[String]
                                   )


case class ExistingProtectionsDisplayModel(
                                            activeProtection: Option[ExistingProtectionDisplayModel],
                                            otherProtections: Seq[ExistingProtectionDisplayModel]
                                            )


case class PrintDisplayModel (
                                firstName: String, surname: String,
                                nino: String,
                                protectionType: String,
                                status: String,
                                psaCheckReference: String,
                                protectionReference: String,
                                protectedAmount: Option[String],
                                certificateDate: Option[String]
                               )