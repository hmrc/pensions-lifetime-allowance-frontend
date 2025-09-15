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

package constructors

import common.Exceptions.{OptionNotDefinedException, RequiredValueNotDefinedException}
import common.Helpers
import enums.{ApplicationStage, ApplicationType}
import models._
import models.amendModels.AmendProtectionModel
import models.pla.response.ProtectionStatus.Open
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesProvider}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, MessagesControllerComponents}
import play.api.test.FakeRequest
import testHelpers.FakeApplication

class DisplayConstructorsSpec extends FakeApplication with MockitoSugar {

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val mockLang: Lang                                   = mock[Lang]
  implicit val mockMessagesProvider: MessagesProvider           = mock[MessagesProvider]
  implicit val controllerComponents: ControllerComponents       = mock[ControllerComponents]
  val displayConstructor: DisplayConstructors = fakeApplication().injector.instanceOf[DisplayConstructors]

  implicit val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val tstPSACheckRef = "PSA33456789"

  val tstProtection: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(100001),
    protectionType = Some("IP2016"),
    status = Some(Open.toString),
    protectedAmount = Some(1100000.34),
    relevantAmount = Some(1100000.34),
    preADayPensionInPayment = None,
    postADayBenefitCrystallisationEvents = None,
    nonUKRights = Some(100000.0),
    uncrystallisedRights = Some(1000000.34)
  )

  val tstWithPsoProtection: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(100001),
    protectionType = Some("IP2016"),
    status = Some(Open.toString),
    protectedAmount = Some(1100000.34),
    relevantAmount = Some(1100000.34),
    preADayPensionInPayment = None,
    postADayBenefitCrystallisationEvents = None,
    pensionDebitTotalAmount = Some(1000.00),
    nonUKRights = Some(100000.0),
    uncrystallisedRights = Some(1000000.34)
  )

  val tstNoPsoAmendProtectionModel: AmendProtectionModel = AmendProtectionModel(tstProtection, tstProtection)

  val tstWithPsoAmendProtectionModel: AmendProtectionModel =
    AmendProtectionModel(tstWithPsoProtection, tstWithPsoProtection)

  val tstPensionContributionNoPsoDisplaySections: Seq[AmendDisplaySectionModel] = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore("ip2016", "open")),
          None,
          "No"
        )
      )
    ),
    AmendDisplaySectionModel(
      "PensionsTakenBetween",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween("ip2016", "open")),
          None,
          "No"
        )
      )
    ),
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "open")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "open")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPensions",
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2016", "open")),
          None,
          "£1,000,000.34"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPsos",
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "No")
      )
    )
  )

  val tstPensionContributionPsoDisplaySections: Seq[AmendDisplaySectionModel] = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore("ip2016", "open")),
          None,
          "No"
        )
      )
    ),
    AmendDisplaySectionModel(
      "PensionsTakenBetween",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween("ip2016", "open")),
          None,
          "No"
        )
      )
    ),
    AmendDisplaySectionModel(
      "OverseasPensions",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "open")),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsOverseasPensionController.amendOverseasPensions("ip2016", "open")),
          None,
          "£100,000"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPensions",
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(controllers.routes.AmendsCurrentPensionController.amendCurrentPensions("ip2016", "open")),
          None,
          "£1,000,000.34"
        )
      )
    ),
    AmendDisplaySectionModel(
      "CurrentPsos",
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "Yes"),
        AmendDisplayRowModel("Amt", None, None, "£1,000")
      )
    )
  )

  val tstNoPsoDisplaySections: Seq[Nothing] = Seq()

  val tstPsoDisplaySections: Seq[AmendDisplaySectionModel] = Seq(
    AmendDisplaySectionModel(
      ApplicationStage.CurrentPsos.toString,
      Seq(
        AmendDisplayRowModel("YesNo", None, None, "Yes"),
        AmendDisplayRowModel("Amt", None, None, "£1,000")
      )
    )
  )

  "Existing Protections Constructor" should {

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("OPEN"),
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
        certificateDate = Some("17 April 2016")
      )

      val tstProtectionModelDormant = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("DORMANT"),
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
        certificateDate = None
      )
      val tstTransformedReadResponseModel =
        TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = Some(tstExistingProtectionDisplayModelOpen),
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections =
            ExistingInactiveProtectionsByType(Seq("IP2014" -> List(tstExistingProtectionDisplayModelDormant))),
          withdrawnProtections = ExistingInactiveProtectionsByType.empty,
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      displayConstructor.createExistingProtectionsDisplayModel(
        tstTransformedReadResponseModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with the same status" in {
      val tstProtectionModelOpen = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2014"),
        status = Some("OPEN"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"),
        withdrawnDate = None
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "open",
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "open")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION 2014"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2014 = ExistingProtectionDisplayModel(
        protectionType = "FP2014",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = "fixed",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("ENHANCED PROTECTION"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = "enhanced",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantPrimary = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("PRIMARY PROTECTION"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantPrimary = ExistingProtectionDisplayModel(
        protectionType = "primary",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantIP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2016"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2016 = ExistingProtectionDisplayModel(
        protectionType = "IP2016",
        status = "dormant",
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2016", "dormant")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION 2016"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2016 = ExistingProtectionDisplayModel(
        protectionType = "FP2016",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2014"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2014 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "dormant",
        amendCall = Some(controllers.routes.AmendsController.amendsSummary("ip2014", "dormant")),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstExistingProtectionModel = TransformedReadResponseModel(
        Some(tstProtectionModelOpen),
        List(
          tstProtectionModelDormantIP2016,
          tstProtectionModelDormantFP2014,
          tstProtectionModelDormantFP2016,
          tstProtectionModelDormantFixed,
          tstProtectionModelDormantIP2014,
          tstProtectionModelDormantEnhanced,
          tstProtectionModelDormantPrimary
        )
      )

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = Some(tstProtectionDisplayModelOpen),
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(
              "IP2016" -> List(
                tstProtectionDisplayModelDormantIP2016
              ),
              "IP2014" -> List(
                tstProtectionDisplayModelDormantIP2014
              ),
              "FP2016" -> List(
                tstProtectionDisplayModelDormantFP2016
              ),
              "FP2014" -> List(
                tstProtectionDisplayModelDormantFP2014
              ),
              "primary" -> List(
                tstProtectionDisplayModelDormantPrimary
              ),
              "enhanced" -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              "fixed" -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType.empty,
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      displayConstructor.createExistingProtectionsDisplayModel(
        tstExistingProtectionModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with a variety of statuses" in {

      val tstProtectionModelExpiredFP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION 2014"),
        status = Some("EXPIRED"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2014 = ExistingProtectionDisplayModel(
        protectionType = "FP2014",
        status = "expired",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelExpiredFP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION 2016"),
        status = Some("EXPIRED"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2016 = ExistingProtectionDisplayModel(
        protectionType = "FP2016",
        status = "expired",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelRejectedIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2014"),
        status = Some("REJECTED"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelRejectedIP2014 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "rejected",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelUnsuccessfulIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2014"),
        status = Some("UNSUCCESSFUL"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelUnsuccessfulIP2014 = ExistingProtectionDisplayModel(
        protectionType = "IP2014",
        status = "unsuccessful",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelWithdrawnPrimary = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("PRIMARY PROTECTION"),
        status = Some("WITHDRAWN"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnPrimary = ExistingProtectionDisplayModel(
        protectionType = "primary",
        status = "withdrawn",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelWithdrawnIP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("INDIVIDUAL PROTECTION 2016"),
        status = Some("WITHDRAWN"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnIP2016 = ExistingProtectionDisplayModel(
        protectionType = "IP2016",
        status = "withdrawn",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("FIXED PROTECTION"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = "fixed",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("ENHANCED PROTECTION"),
        status = Some("DORMANT"),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = "enhanced",
        status = "dormant",
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstExistingProtectionModel = TransformedReadResponseModel(
        None,
        List(
          tstProtectionModelWithdrawnIP2016,
          tstProtectionModelExpiredFP2016,
          tstProtectionModelDormantFixed,
          tstProtectionModelRejectedIP2014,
          tstProtectionModelDormantEnhanced,
          tstProtectionModelExpiredFP2014,
          tstProtectionModelUnsuccessfulIP2014,
          tstProtectionModelWithdrawnPrimary
        )
      )

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = None,
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(
              "enhanced" -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              "fixed" -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              "IP2016" -> List(
                tstProtectionDisplayModelWithdrawnIP2016
              ),
              "primary" -> List(
                tstProtectionDisplayModelWithdrawnPrimary
              )
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType(
            Seq(
              "IP2014" -> List(
                tstProtectionDisplayModelUnsuccessfulIP2014
              )
            )
          ),
          rejectedProtections = ExistingInactiveProtectionsByType(
            Seq(
              "IP2014" -> List(
                tstProtectionDisplayModelRejectedIP2014
              )
            )
          ),
          expiredProtections = ExistingInactiveProtectionsByType(
            Seq(
              "FP2016" -> List(
                tstProtectionDisplayModelExpiredFP2016
              ),
              "FP2014" -> List(
                tstProtectionDisplayModelExpiredFP2014
              )
            )
          )
        )
      )

      displayConstructor.createExistingProtectionsDisplayModel(
        tstExistingProtectionModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }
  }

  "createPrintDisplayModel" should {

    "create a Print Display model" in {
      val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
      val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
      val tstProtectionModel = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("IP2014"),
        status = Some("Open"),
        certificateDate = Some("2016-04-17"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"),
        notificationId = Some(1)
      )
      val nino = "testNino"

      val tstResultPrintDisplayModel = PrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = nino,
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = tstPSACheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016")
      )

      displayConstructor.createPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        tstProtectionModel,
        nino
      ) shouldBe tstResultPrintDisplayModel
    }
  }

  "createAmendPrintDisplayModel" should {

    val person               = Person(firstName = "Testy", lastName = "McTestface")
    val personalDetailsModel = PersonalDetailsModel(person)
    val protectionModel = ProtectionModel(
      psaCheckReference = Some(tstPSACheckRef),
      protectionID = Some(12345),
      protectionType = Some("IP2014"),
      status = Some("Open"),
      certificateDate = Some("2016-04-17"),
      protectedAmount = Some(1250000),
      protectionReference = Some("PSA123456"),
      notificationId = Some(1)
    )
    val nino = "testNino"

    "create AmendPrintDisplayModel" in {
      val expectedAmendPrintDisplayModel = AmendPrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = nino,
        protectionType = "IP2014",
        status = "open",
        psaCheckReference = tstPSACheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        notificationId = 1
      )

      displayConstructor.createAmendPrintDisplayModel(
        Some(personalDetailsModel),
        protectionModel,
        nino
      ) shouldBe expectedAmendPrintDisplayModel
    }

    "throw exception" when {

      "provided with empty PersonalDetailsModel" in {
        val exc = the[RequiredValueNotDefinedException] thrownBy
          displayConstructor.createAmendPrintDisplayModel(None, protectionModel, nino)

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "personalDetailsModel"
      }

      "provided with empty psaCheckReference" in {
        val exc = the[RequiredValueNotDefinedException] thrownBy displayConstructor.createAmendPrintDisplayModel(
          Some(personalDetailsModel),
          protectionModel.copy(psaCheckReference = None),
          nino
        )

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "psaCheckReference"
      }

      "provided with empty protectionReference" in {
        val exc = the[RequiredValueNotDefinedException] thrownBy displayConstructor.createAmendPrintDisplayModel(
          Some(personalDetailsModel),
          protectionModel.copy(protectionReference = None),
          nino
        )

        exc.functionName shouldBe "createPrintDisplayModel"
        exc.optionName shouldBe "protectionReference"
      }

      "provided with empty notificationId" in {
        val exc = the[OptionNotDefinedException] thrownBy displayConstructor.createAmendPrintDisplayModel(
          Some(personalDetailsModel),
          protectionModel.copy(notificationId = None),
          nino
        )

        exc.functionName shouldBe "createAmendPrintDisplayModel"
        exc.optionName shouldBe "notificationId"
        exc.applicationType shouldBe "IP2014"
      }
    }
  }

  "modelsDiffer" should {

    "return false for the same model" in {
      val tstModel = ProtectionModel(psaCheckReference = Some("psaRef"), protectionID = Some(10000))
      displayConstructor.modelsDiffer(tstModel, tstModel) shouldBe false
    }

    "return false for two models with the same properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        preADayPensionInPayment = Some(23412.87),
        protectionID = Some(10000)
      )
      displayConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe false
    }

    "return true for two models with different properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        preADayPensionInPayment = Some(23412.87)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      displayConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

    "return true for two models with different number of properties" in {
      val tstModel1 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        preADayPensionInPayment = Some(23412.87),
        version = Some(4)
      )
      val tstModel2 = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(10000),
        preADayPensionInPayment = Some(23412.87)
      )
      displayConstructor.modelsDiffer(tstModel1, tstModel2) shouldBe true
    }

  }

  "createAmendDisplayModel" should {
    "correctly transform an AmendProtectionModel into an AmendDisplayModel without Psos" in {

      displayConstructor.createAmendDisplayModel(tstNoPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly transform an AmendProtectionModel into an AmendDisplayModel with PSOs" in {
      displayConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when not supplied with an amountOption" in {
      displayConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when amountOption is 0" in {
      val tstNoPsoAmountProtection = ProtectionModel(
        psaCheckReference = Some("psaRef"),
        protectionID = Some(100001),
        protectionType = Some("IP2016"),
        status = Some(Open.toString),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebitTotalAmount = Some(0.00),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNoPsoAmountProtection, tstNoPsoAmountProtection)

      displayConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
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
        status = Some(Open.toString),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02", 1000.0))),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      val tstPsoAddedSection = Seq(
        AmendDisplaySectionModel(
          "pensionDebits",
          Seq(
            AmendDisplayRowModel(
              "CurrentPsos-psoDetails",
              changeLinkCall =
                Some(controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails("ip2016", "open")),
              removeLinkCall =
                Some(controllers.routes.AmendsRemovePensionSharingOrderController.removePso("ip2016", "open")),
              "£1,000",
              "2 March 2017"
            )
          )
        )
      )

      displayConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
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
        status = Some(Open.toString),
        protectedAmount = Some(1000000.34),
        relevantAmount = Some(1000000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02", 1000.0), PensionDebitModel("2017-03-02", 2000.0))),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      displayConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = "IP2016",
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = true,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,000,000.34"
      )

    }
  }

  "createActiveAmendResponseModel" should {

    "correctly transform an AmendResponseModel into an ActiveAmendResultDisplayModel" in {
      val amendResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = Some("psaRef"),
          protectionID = Some(100003),
          protectionType = Some("IP2014"),
          protectionReference = Some("protectionRef"),
          certificateDate = Some("2016-06-14"),
          protectedAmount = Some(1350000.45),
          notificationId = Some(33)
        )
      )

      val person               = Person(firstName = "Jim", lastName = "Davis")
      val personalDetailsModel = PersonalDetailsModel(person)
      val nino                 = "testNino"

      val activeAmendResultDisplayModel = ActiveAmendResultDisplayModel(
        firstName = "Jim",
        surname = "Davis",
        nino = nino,
        protectionType = ApplicationType.IP2014,
        notificationId = "33",
        protectedAmount = "£1,350,000.45",
        details = Some(
          ProtectionDetailsDisplayModel(
            protectionReference = Some("protectionRef"),
            psaReference = "psaRef",
            applicationDate = Some("14 June 2016")
          )
        )
      )

      displayConstructor.createActiveAmendResponseDisplayModel(
        amendResponseModel,
        Some(personalDetailsModel),
        nino
      ) shouldBe activeAmendResultDisplayModel
    }

    "throw exception" when {

      "protected amount is NOT present" in {
        val amendResponseModel = AmendResponseModel(
          ProtectionModel(
            psaCheckReference = Some("psaRef"),
            protectionID = Some(100003),
            protectionType = Some("IP2014"),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14"),
            protectedAmount = None,
            notificationId = Some(33)
          )
        )

        val person               = Person(firstName = "Jim", lastName = "Davis")
        val personalDetailsModel = PersonalDetailsModel(person)
        val nino                 = "testNino"

        val exc = the[OptionNotDefinedException] thrownBy
          displayConstructor.createActiveAmendResponseDisplayModel(amendResponseModel, Some(personalDetailsModel), nino)

        exc.functionName shouldBe "createActiveAmendResponseDisplayModel"
        exc.optionName shouldBe "protectedAmount"
        exc.applicationType shouldBe "IP2014"
      }

      "notification ID is NOT present" in {
        val amendResponseModel = AmendResponseModel(
          ProtectionModel(
            psaCheckReference = Some("psaRef"),
            protectionID = Some(100003),
            protectionType = Some("IP2014"),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14"),
            protectedAmount = Some(1350000.45),
            notificationId = None
          )
        )

        val person               = Person(firstName = "Jim", lastName = "Davis")
        val personalDetailsModel = PersonalDetailsModel(person)
        val nino                 = "testNino"

        val exc = the[OptionNotDefinedException] thrownBy
          displayConstructor.createActiveAmendResponseDisplayModel(amendResponseModel, Some(personalDetailsModel), nino)

        exc.functionName shouldBe "createActiveAmendResponseDisplayModel"
        exc.optionName shouldBe "notificationId"
        exc.applicationType shouldBe "IP2014"
      }
    }
  }

  "createAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an AmendResultDisplayModel" in {
      val tstAmendResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = Some("psaRef"),
          protectionID = Some(100003),
          protectionType = Some("IP2014"),
          status = Some("Open"),
          protectionReference = Some("protectionRef"),
          certificateDate = Some("2016-06-14"),
          protectedAmount = Some(1350000.45),
          notificationId = Some(33)
        )
      )

      val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
      val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
      val nino                    = "testNino"

      val tstAmendResultDisplayModel = AmendResultDisplayModel(
        notificationId = 33,
        protectedAmount = "£1,350,000.45",
        details = Some(
          AmendPrintDisplayModel(
            firstName = "Testy",
            surname = "Mctestface",
            nino = "testNino",
            protectionType = "IP2014",
            status = "open",
            psaCheckReference = "psaRef",
            protectionReference = "protectionRef",
            protectedAmount = Some("£1,350,000.45"),
            certificateDate = Some("14 June 2016"),
            notificationId = 33
          )
        )
      )

      displayConstructor.createAmendResultDisplayModel(
        tstAmendResponseModel,
        Some(tstPersonalDetailsModel),
        nino
      ) shouldBe tstAmendResultDisplayModel
    }
  }

  "createInactiveAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an InActiveAmendResultDisplayModel" in {
      val tstAmendsResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          notificationId = Some(30)
        )
      )
      val tstInactiveAmendsResultDisplayModel = InactiveAmendResultDisplayModel(
        notificationId = 30,
        additionalInfo = Seq("1")
      )

      displayConstructor.createInactiveAmendResponseDisplayModel(
        tstAmendsResponseModel
      ) shouldBe tstInactiveAmendsResultDisplayModel

    }
  }

  "createWithdrawSummaryTable" should {

    "create a valid amendDisplayModel" in {
      val model = ProtectionModel(
        Some("checkRef"),
        Some(33),
        protectionType = Some("type"),
        status = Some("status"),
        uncrystallisedRights = Some(1000)
      )
      val result = displayConstructor.createWithdrawSummaryTable(model)
      val pensionContributionSectionResult = List(
        AmendDisplaySectionModel("PensionsTakenBefore", List(AmendDisplayRowModel("YesNo", None, None, "No"))),
        AmendDisplaySectionModel("PensionsTakenBetween", List(AmendDisplayRowModel("YesNo", None, None, "No"))),
        AmendDisplaySectionModel("OverseasPensions", List(AmendDisplayRowModel("YesNo", None, None, "No"))),
        AmendDisplaySectionModel("CurrentPensions", List(AmendDisplayRowModel("Amt", None, None, "£1,000"))),
        AmendDisplaySectionModel("CurrentPsos", List(AmendDisplayRowModel("YesNo", None, None, "No")))
      )
      val psoSectionsResult = List(
        AmendDisplaySectionModel(
          "PensionsTakenBefore",
          List(
            AmendDisplayRowModel(
              "YesNo",
              Some(Helpers.createAmendCall(model, ApplicationStage.PensionsTakenBefore)),
              None,
              "No"
            )
          )
        ),
        AmendDisplaySectionModel(
          "PensionsTakenBetween",
          List(
            AmendDisplayRowModel(
              "YesNo",
              Some(Helpers.createAmendCall(model, ApplicationStage.PensionsTakenBetween)),
              None,
              "No"
            )
          )
        ),
        AmendDisplaySectionModel(
          "OverseasPensions",
          List(
            AmendDisplayRowModel(
              "YesNo",
              Some(Helpers.createAmendCall(model, ApplicationStage.OverseasPensions)),
              None,
              "No"
            )
          )
        ),
        AmendDisplaySectionModel(
          "CurrentPensions",
          List(
            AmendDisplayRowModel(
              "Amt",
              Some(Helpers.createAmendCall(model, ApplicationStage.CurrentPensions)),
              None,
              "£1,000"
            )
          )
        ),
        AmendDisplaySectionModel("CurrentPsos", List(AmendDisplayRowModel("YesNo", None, None, "No")))
      )

      result shouldBe AmendDisplayModel(
        "notRecorded",
        amended = false,
        pensionContributionSectionResult,
        psoAdded = false,
        psoSectionsResult,
        "£0"
      )
    }
  }

}
