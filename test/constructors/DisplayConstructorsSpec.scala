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

import models.{ExistingProtectionDisplayModel, ExistingProtectionsDisplayModel, ProtectionModel, TransformedReadResponseModel}
import play.api.i18n.Messages
import play.test.FakeApplication
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

/**
  * Created by mark on 22/09/16.
  */
class DisplayConstructorsSpec extends UnitSpec with WithFakeApplication{

  val tstPSACheckRef = "PSA33456789"
  //Todo - printdisplaymodel -successdisplay

  "Existing Protections Constructor" should {

    "Populate the protection status string" when {
      "the protection is open" in         {DisplayConstructors.statusString(Some("Open")) shouldBe "open"}
      "the protection is dormant" in      {DisplayConstructors.statusString(Some("Dormant")) shouldBe "dormant"}
      "the protection is withdrawn" in    {DisplayConstructors.statusString(Some("Withdrawn")) shouldBe "withdrawn"}
      "the protection is expired" in      {DisplayConstructors.statusString(Some("Expired")) shouldBe "expired"}
      "the protection is unsuccessful" in {DisplayConstructors.statusString(Some("Unsuccessful")) shouldBe "unsuccessful"}
      "the protection is rejected" in     {DisplayConstructors.statusString(Some("Rejected")) shouldBe "rejected"}
      "there is no status recorded" in    {DisplayConstructors.statusString(None) shouldBe "notRecorded"}
    }

    "Populate the protection type string" when {
      "the protection is FP2016" in            {DisplayConstructors.protectionTypeString(Some("FP2016")) shouldBe "FP2016"}
      "the protection is IP2014" in            {DisplayConstructors.protectionTypeString(Some("IP2014")) shouldBe "IP2014"}
      "the protection is IP2016" in            {DisplayConstructors.protectionTypeString(Some("IP2016")) shouldBe "IP2016"}
      "the protection is primary" in           {DisplayConstructors.protectionTypeString(Some("Primary")) shouldBe "primary"}
      "the protection is enhanced" in          {DisplayConstructors.protectionTypeString(Some("Enhanced")) shouldBe "enhanced"}
      "the protection is fixed" in             {DisplayConstructors.protectionTypeString(Some("Fixed")) shouldBe "fixed"}
      "the protection is FP2014" in            {DisplayConstructors.protectionTypeString(Some("FP2014")) shouldBe "FP2014"}
      "the protection type is not recorded" in {DisplayConstructors.protectionTypeString(None) shouldBe "notRecorded"}
    }

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Open"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstExistingProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"))

      val tstProtectionModelDormant = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstExistingProtectionDisplayModelDormant = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)
      val tstTransformedReadResponseModel = TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(Some(tstExistingProtectionDisplayModelOpen), List(tstExistingProtectionDisplayModelDormant))

      DisplayConstructors.createExistingProtectionsDisplayModel(tstTransformedReadResponseModel) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with the same status" in {
      val tstProtectionModelOpen = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Open"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"))

      val tstProtectionModelDormant7 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FP2014"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant7 = ExistingProtectionDisplayModel(
        protectionType = "FP2014",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant6 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Fixed"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant6 = ExistingProtectionDisplayModel(
        protectionType = "fixed",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant5 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Enhanced"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant5 = ExistingProtectionDisplayModel(
        protectionType = "enhanced",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant4 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Primary"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant4 = ExistingProtectionDisplayModel(
        protectionType = "primary",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant3 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant3 = ExistingProtectionDisplayModel(
        protectionType = "IP2016",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant2 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FP2016"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant2 = ExistingProtectionDisplayModel(
        protectionType = "FP2016",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant1 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstExistingProtectionModel = TransformedReadResponseModel(Some(tstProtectionModelOpen), List(
        tstProtectionModelDormant3,
        tstProtectionModelDormant7,
        tstProtectionModelDormant2,
        tstProtectionModelDormant6,
        tstProtectionModelDormant1,
        tstProtectionModelDormant5,

        tstProtectionModelDormant4))

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(Some(tstProtectionDisplayModelOpen), List(
        tstProtectionDisplayModelDormant1,
        tstProtectionDisplayModelDormant2,
        tstProtectionDisplayModelDormant3,
        tstProtectionDisplayModelDormant4,
        tstProtectionDisplayModelDormant5,
        tstProtectionDisplayModelDormant6,
        tstProtectionDisplayModelDormant7))

      DisplayConstructors.createExistingProtectionsDisplayModel(tstExistingProtectionModel) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with a variety of statuses" in {
      val tstProtectionModelOpen = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Open"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"))

      val tstProtectionModelDormant8 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FP2014"),
        status = Some("Expired"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant8 = ExistingProtectionDisplayModel(
        protectionType = "FP2014",
        status = "expired",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant7 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FP2016"),
        status = Some("Expired"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant7 = ExistingProtectionDisplayModel(
        protectionType = "FP2016",
        status = "expired",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant6 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Rejected"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant6 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "rejected",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant5 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Unsuccessful"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant5 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "unsuccessful",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant4 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Primary"),
        status = Some("Withdrawn"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant4 = ExistingProtectionDisplayModel(
        protectionType = "primary",
        status = "withdrawn",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant3 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("Withdrawn"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant3 = ExistingProtectionDisplayModel(
        protectionType = "IP2016",
        status = "withdrawn",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant2 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Fixed"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant2 = ExistingProtectionDisplayModel(
        protectionType = "fixed",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant1 = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("Enhanced"),
        status = Some("Dormant"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormant1 = ExistingProtectionDisplayModel(
        protectionType = "enhanced",
        status = "dormant",
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstExistingProtectionModel = TransformedReadResponseModel(None, List(
        tstProtectionModelDormant3,
        tstProtectionModelDormant7,
        tstProtectionModelDormant2,
        tstProtectionModelDormant6,
        tstProtectionModelDormant1,
        tstProtectionModelDormant8,
        tstProtectionModelDormant5,
        tstProtectionModelDormant4))

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(None, List(
        tstProtectionDisplayModelDormant1,
        tstProtectionDisplayModelDormant2,
        tstProtectionDisplayModelDormant3,
        tstProtectionDisplayModelDormant4,
        tstProtectionDisplayModelDormant5,
        tstProtectionDisplayModelDormant6,
        tstProtectionDisplayModelDormant7,
        tstProtectionDisplayModelDormant8))

      DisplayConstructors.createExistingProtectionsDisplayModel(tstExistingProtectionModel) shouldBe tstExistingProtectionsDisplayModel
    }
  }



}
