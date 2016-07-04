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

package constructors

import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import play.api.libs.json.{JsValue, Json}
import play.api.i18n.Messages
import models._

class ExistingProtectionsConstructorSpec extends UnitSpec with WithFakeApplication {

  val tstPSACheckRef = "PSA33456789"

  "Existing Protections Constructor" should {

    "Populate the protection status string" when {
      "the protection is open" in         {ExistingProtectionsConstructor.statusString(Some("Open")) shouldBe Messages("pla.protection.statuses.open")}
      "the protection is dormant" in      {ExistingProtectionsConstructor.statusString(Some("Dormant")) shouldBe Messages("pla.protection.statuses.dormant")}
      "the protection is withdrawn" in    {ExistingProtectionsConstructor.statusString(Some("Withdrawn")) shouldBe Messages("pla.protection.statuses.withdrawn")}
      "the protection is expired" in      {ExistingProtectionsConstructor.statusString(Some("Expired")) shouldBe Messages("pla.protection.statuses.expired")}
      "the protection is unsuccessful" in {ExistingProtectionsConstructor.statusString(Some("Unsuccessful")) shouldBe Messages("pla.protection.statuses.unsuccessful")}
      "the protection is rejected" in     {ExistingProtectionsConstructor.statusString(Some("Rejected")) shouldBe Messages("pla.protection.statuses.rejected")}
      "there is no status recorded" in    {ExistingProtectionsConstructor.statusString(None) shouldBe Messages("pla.protection.statuses.notRecorded")}
    }

    "Populate the protection type string" when {
      "the protection is FP2016" in            {ExistingProtectionsConstructor.protectionTypeString(Some("FP2016")) shouldBe Messages("pla.protection.types.FP2016")}
      "the protection is IP2014" in            {ExistingProtectionsConstructor.protectionTypeString(Some("IP2014")) shouldBe Messages("pla.protection.types.IP2014")}
      "the protection is IP2016" in            {ExistingProtectionsConstructor.protectionTypeString(Some("IP2016")) shouldBe Messages("pla.protection.types.IP2016")}
      "the protection is primary" in           {ExistingProtectionsConstructor.protectionTypeString(Some("Primary")) shouldBe Messages("pla.protection.types.primary")}
      "the protection is enhanced" in          {ExistingProtectionsConstructor.protectionTypeString(Some("Enhanced")) shouldBe Messages("pla.protection.types.enhanced")}
      "the protection is fixed" in             {ExistingProtectionsConstructor.protectionTypeString(Some("Fixed")) shouldBe Messages("pla.protection.types.fixed")}
      "the protection is FP2014" in            {ExistingProtectionsConstructor.protectionTypeString(Some("FP2014")) shouldBe Messages("pla.protection.types.FP2014")}
      "the protection type is not recorded" in {ExistingProtectionsConstructor.protectionTypeString(None) shouldBe Messages("pla.protection.types.notRecorded")}
    }

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel (
                           protectionID = Some(12345),
                           protectionType = Some("IP2014"),
                           status = Some("Open"),
                           certificateDate = Some("2016-04-17"),
                           relevantAmount = Some(1250000),
                           protectionReference = Some("PSA123456")
                         )
      val tstProtectionDisplayModelOpen = ProtectionDisplayModel(
                            protectionType = Messages("pla.protection.types.IP2014"),
                            status = Messages("pla.protection.statuses.open"),
                            psaCheckReference = tstPSACheckRef,
                            protectionReference = "PSA123456",
                            relevantAmount = Some("£1,250,000.00"),
                            certificateDate = Some("17 April 2016"))
    
      val tstProtectionModelDormant = ProtectionModel (
                           protectionID = Some(12345),
                           protectionType = Some("IP2014"),
                           status = Some("Dormant"),
                           certificateDate = None,
                           relevantAmount = None,
                           protectionReference = None
                         )
      val tstProtectionDisplayModelDormant = ProtectionDisplayModel(
                            protectionType = Messages("pla.protection.types.IP2014"),
                            status = Messages("pla.protection.statuses.dormant"),
                            psaCheckReference = tstPSACheckRef,
                            protectionReference = Messages("pla.protection.protectionReference"),
                            relevantAmount = None,
                            certificateDate = None)
      val tstExistingProtectionModel = ExistingProtectionsModel(tstPSACheckRef, List(tstProtectionModelOpen, tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(List(tstProtectionDisplayModelOpen), List(tstProtectionDisplayModelDormant))

      ExistingProtectionsConstructor.createExistingProtectionsDisplayModel(tstExistingProtectionModel) shouldBe tstExistingProtectionsDisplayModel
    }
  }

}
