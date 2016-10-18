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

import common.Display
import enums.{ApplicationStage, ApplicationType}
import models._
import models.amendModels.AmendProtectionModel
import org.mockito.Matchers
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DisplayConstructorsSpec extends UnitSpec with WithFakeApplication{

  val tstPSACheckRef = "PSA33456789"

  val tstProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(100001),
    protectionType = Some("IP2016"),
    status = Some("active"),
    protectedAmount = Some(1100000.34),
    relevantAmount = Some(1100000.34),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = None,
    nonUKRights = Some(100000.0),
    uncrystallisedRights = Some(1000000.34)
  )
  val tstWithPsoProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(100001),
    protectionType = Some("IP2016"),
    status = Some("active"),
    protectedAmount = Some(1100000.34),
    relevantAmount = Some(1100000.34),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = None,
    pensionDebitTotalAmount = Some(1000.00),
    nonUKRights = Some(100000.0),
    uncrystallisedRights = Some(1000000.34)
  )

  val tstNoPsoAmendProtectionModel = AmendProtectionModel(tstProtection, tstProtection)
  val tstWithPsoAmendProtectionModel = AmendProtectionModel(tstWithPsoProtection, tstWithPsoProtection)

  val tstPensionContributionDisplaySections = Seq(
    AmendDisplaySectionModel("CurrentPensions",Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2016", "active")), None, "£1,000,000.34")
    )
    ),
    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2016", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2016", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2016", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2016", "active")), None, "£100,000")
    )
    )
  )

  val tstNoPsoDisplaySections = Seq(
    AmendDisplaySectionModel(ApplicationStage.CurrentPsos.toString,
      Seq(
      AmendDisplayRowModel("YesNo", None, None, "No")
      )
    ),
    AmendDisplaySectionModel("total-amount",
      Seq(
      AmendDisplayRowModel(s"${ApplicationStage.CurrentPsos.toString}-currentTotal", None, None, "£0")
      )
    )
  )
  val tstPsoDisplaySections = Seq(
    AmendDisplaySectionModel(ApplicationStage.CurrentPsos.toString, Seq(
      AmendDisplayRowModel("YesNo", None, None, "Yes"),
      AmendDisplayRowModel("Amt", None, None, "£1,000")
    )),
    AmendDisplaySectionModel("total-amount",
      Seq(
        AmendDisplayRowModel(s"${ApplicationStage.CurrentPsos.toString}-currentTotal", None, None, "£1,000")
      )
    )
  )

  "Existing Protections Constructor" should {

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel(
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
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "open")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"))

      val tstProtectionModelDormant = ProtectionModel(
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
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "dormant")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)
      val tstTransformedReadResponseModel = TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(Some(tstExistingProtectionDisplayModelOpen), List(tstExistingProtectionDisplayModelDormant))

      DisplayConstructors.createExistingProtectionsDisplayModel(tstTransformedReadResponseModel) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with the same status" in {
      val tstProtectionModelOpen = ProtectionModel(
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
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "open")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"))

      val tstProtectionModelDormant7 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant6 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant5 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant4 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant3 = ProtectionModel(
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
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2016", "dormant")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant2 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant1 = ProtectionModel(
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
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "dormant")),
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

      val tstProtectionModelDormant8 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant7 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant6 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant5 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant4 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant3 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant2 = ProtectionModel(
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
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None)

      val tstProtectionModelDormant1 = ProtectionModel(
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
        amendCall = None,
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

  "createPrintDisplayModel" should {

    "create a Print Display model" in {

      //Fake Input Values
      val tstPerson = Person("McTestface", "Testy")
      val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
      val tstProtectionModel = ProtectionModel (
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Open"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstNino = "testNino"

      //Expected Result
      val tstResultPrintDisplayModel = PrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = "testNino",
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = tstPSACheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016")
      )
      DisplayConstructors.createPrintDisplayModel(Some(tstPersonalDetailsModel),Some(tstProtectionModel),tstNino) shouldBe tstResultPrintDisplayModel

    }
  }

  "modelsDiffer" should {

    "return false for the same model" in {
      val tstModel = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000))
      DisplayConstructors.modelsDiffer(tstModel, tstModel) shouldBe false
    }

    "return false for two models with the same properties" in {
      val tstModel1 = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000), preADayPensionInPayment = Some(23412.87))
      val tstModel2 = ProtectionModel(psaCheckReference = Some("psaRef"), preADayPensionInPayment = Some(23412.87), protectionID = Some(10000))
      DisplayConstructors.modelsDiffer(tstModel1, tstModel2) shouldBe false
    }

    "return true for two models with different properties" in {
      val tstModel1 = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(100001), preADayPensionInPayment = Some(23412.87))
      val tstModel2 = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000),  preADayPensionInPayment = Some(23412.87))
      DisplayConstructors.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

    "return true for two models with different number of properties" in {
      val tstModel1 = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(100001), preADayPensionInPayment = Some(23412.87), version = Some(4))
      val tstModel2 = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000),  preADayPensionInPayment = Some(23412.87))
      DisplayConstructors.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

  }

  "createAmendDisplayModel" should {
    "correctly transform an AmendProtectionModel into an AmendDisplayModel without Psos" in {

      DisplayConstructors.createAmendDisplayModel(tstNoPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly transform an AmendProtectionModel into an AmendDisplayModel with PSOs" in {
      DisplayConstructors.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = false,
        psoSections = tstPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when not supplied with an amountOption" in {
      DisplayConstructors.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = false,
        psoSections = tstPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when amountOption is 0" in {
      val tstNoPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some("IP2016"),
        status = Some("active"),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = Some(0.0),
        postADayBenefitCrystallisationEvents = None,
        pensionDebitTotalAmount = Some(0.00),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)      )

      val amendModel = AmendProtectionModel(tstNoPsoAmountProtection, tstNoPsoAmountProtection)

      DisplayConstructors.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly produce a display section for any current PSO's" in {

      val tstNewPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some("IP2016"),
        status = Some("active"),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = Some(0.0),
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02", 1000.0))),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      val tstPsoAddedSection = Seq(
        AmendDisplaySectionModel(ApplicationStage.CurrentPsos.toString,
          Seq(AmendDisplayRowModel("YesNo", None, None, "No"))),
        AmendDisplaySectionModel("pensionDebits",
          Seq(
            AmendDisplayRowModel("CurrentPsos-psoDetails",
              changeLinkCall = Some(controllers.routes.AmendsController.amendPsoDetails("ip2016", "active")),
              removeLinkCall = None,
              "£1,000", "2 March 2017")
          )),
        AmendDisplaySectionModel("total-amount",
          Seq(AmendDisplayRowModel(s"${ApplicationStage.CurrentPsos.toString}-currentTotal", None, None, "£1,000"))
        )
      )

      DisplayConstructors.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = true,
        psoSections = tstPsoAddedSection,
        totalAmount = "£1,100,000.34"
      )

    }

    "should not create a display section for current PSO's if there is more than one pension debit in the model" in {

      val tstNewPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some("IP2016"),
        status = Some("active"),
        protectedAmount = Some(1000000.34),
        relevantAmount = Some(1000000.34),
        preADayPensionInPayment = Some(0.0),
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02", 1000.0), PensionDebitModel("2017-03-02", 2000.0))),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      val tstPsoSection = Seq(
        AmendDisplaySectionModel(ApplicationStage.CurrentPsos.toString,
          Seq(AmendDisplayRowModel("YesNo", None, None, "No"))
        ),
        AmendDisplaySectionModel("total-amount",
          Seq(AmendDisplayRowModel(s"${ApplicationStage.CurrentPsos.toString}-currentTotal", None, None, "£1,000"))
      ))

      DisplayConstructors.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionDisplaySections,
        psoAdded = true,
        psoSections = tstPsoSection,
        totalAmount = "£1,000,000.34"
      )

    }
  }

  "createActiveAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an ActiveAmendResultDisplayModel" in {
      val tstAmendResponseModel = AmendResponseModel(ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100003),
        protectionType = Some("IP2014"),
        protectionReference = Some("protectionRef"),
        certificateDate = Some("2016-06-14"),
        protectedAmount = Some(1350000.45),
        notificationId = Some(33)
      ))

      val tstActiveAmendResultDisplayModel = ActiveAmendResultDisplayModel(
        protectionType = ApplicationType.IP2014,
        notificationId = "33",
        protectedAmount = "£1,350,000.45",
        details = Some(ProtectionDetailsDisplayModel(
          protectionReference = Some("protectionRef"),
          psaReference = "psaRef",
          applicationDate = Some("14 June 2016")
        ))
      )

      DisplayConstructors.createActiveAmendResponseDisplayModel(tstAmendResponseModel) shouldBe tstActiveAmendResultDisplayModel
    }
  }

  "createInactiveAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an InActiveAmendResultDisplayModel" in {
      val tstAmendsResponseModel = AmendResponseModel(ProtectionModel(
        psaCheckReference = None,
        protectionID = None,
        notificationId = Some(30)
      ))
      val tstInactiveAmendsResultDisplayModel = InactiveAmendResultDisplayModel(
        notificationId = "30",
        additionalInfo = Seq("1","2")
      )

      DisplayConstructors.createInactiveAmendResponseDisplayModel(tstAmendsResponseModel) shouldBe tstInactiveAmendsResultDisplayModel

    }
  }

}
