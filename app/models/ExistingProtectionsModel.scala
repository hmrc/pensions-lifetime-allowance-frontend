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

import play.api.libs.json.Json

case class ProtectionModel (
                             protectionID: String,
                             certificateDate: Option[String] = None,
                             version: Option[String] = None,
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
                             notificationId: Option[Int] = None,
                             protectionReference: Option[String] = None
                           )

object ProtectionModel {
  implicit val format = Json.format[ProtectionModel]
}


case class ExistingProtectionsModel(psaCheckReference: String, lifetimeAllowanceProtections: Seq[ProtectionModel]) {

    def activeProtections(): Seq[ProtectionModel] = lifetimeAllowanceProtections.filter(_.status.contains("1"))
    def otherProtections(): Seq[ProtectionModel] = lifetimeAllowanceProtections.filterNot(_.status.contains("1"))
}

object ExistingProtectionsModel {
  implicit val format = Json.format[ExistingProtectionsModel]
}

case class ProtectionDisplayModel(
                                 protectionType: String,
                                 status: String,
                                 psaCheckReference: String,
                                 protectionReference: String,
                                 relevantAmount: Option[String],
                                 certificateDate: Option[String]
                                 )


case class ExistingProtectionsDisplayModel(activeProtections: Seq[ProtectionDisplayModel], otherProtections: Seq[ProtectionDisplayModel])
