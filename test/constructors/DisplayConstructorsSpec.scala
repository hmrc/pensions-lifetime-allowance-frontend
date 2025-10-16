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
import common.{Helpers, Strings}
import config.FrontendAppConfig
import enums.{ApplicationStage, ApplicationType}
import models._
import models.amendModels.AmendProtectionModel
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType
import models.pla.response.ProtectionType._
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesProvider}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.{Application, inject}
import testHelpers.FakeApplication

class DisplayConstructorsSpec extends FakeApplication with MockitoSugar with BeforeAndAfterEach {

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  implicit val mockLang: Lang                                   = mock[Lang]
  implicit val mockMessagesProvider: MessagesProvider           = mock[MessagesProvider]
  implicit val controllerComponents: ControllerComponents       = mock[ControllerComponents]
  implicit val appConfig: FrontendAppConfig                     = mock[FrontendAppConfig]
  implicit val appConfigHipDisabled: FrontendAppConfig          = mock[FrontendAppConfig]

  val application: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> false)
    .overrides(inject.bind[FrontendAppConfig].toInstance(appConfig))
    .build()

  val applicationHipDisabled: Application = new GuiceApplicationBuilder()
    .configure("metrics.enabled" -> false)
    .overrides(inject.bind[FrontendAppConfig].toInstance(appConfigHipDisabled))
    .build()

  val displayConstructor: DisplayConstructors = application.injector.instanceOf[DisplayConstructors]

  val displayConstructorHipDisabled: DisplayConstructors =
    applicationHipDisabled.injector.instanceOf[DisplayConstructors]

  implicit val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  override def beforeEach(): Unit = {
    reset(appConfig)
    reset(appConfigHipDisabled)

    when(appConfig.hipMigrationEnabled).thenReturn(true)
    when(appConfigHipDisabled.hipMigrationEnabled).thenReturn(false)
  }

  val tstPSACheckRef = "PSA33456789"

  val tstProtection: ProtectionModel = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(100001),
    protectionType = Some(IndividualProtection2016.toString),
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
    protectionType = Some(IndividualProtection2016.toString),
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
          Some(
            controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsCurrentPensionController.amendCurrentPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsPensionTakenBetweenController.amendPensionsTakenBetween(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
          Some(
            controllers.routes.AmendsCurrentPensionController.amendCurrentPensions(
              Strings.ProtectionTypeURL.IndividualProtection2016,
              Strings.StatusURL.Open
            )
          ),
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
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstExistingProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeURL.IndividualProtection2014,
            Strings.StatusURL.Open
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val tstProtectionModelDormant = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstExistingProtectionDisplayModelDormant = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeURL.IndividualProtection2014,
            Strings.StatusURL.Dormant
          )
        ),
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
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(IndividualProtection2014.toString -> List(tstExistingProtectionDisplayModelDormant))
          ),
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
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"),
        withdrawnDate = None
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeURL.IndividualProtection2014,
            Strings.StatusURL.Open
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm"),
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014.toString,
        status = Dormant.toString,
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
        protectionType = Some(FixedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection.toString,
        status = Dormant.toString,
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
        protectionType = Some(EnhancedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection.toString,
        status = Dormant.toString,
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
        protectionType = Some(PrimaryProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection.toString,
        status = Dormant.toString,
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
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeURL.IndividualProtection2016,
            Strings.StatusURL.Dormant
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2016.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016.toString,
        status = Dormant.toString,
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
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeURL.IndividualProtection2014,
            Strings.StatusURL.Dormant
          )
        ),
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
              IndividualProtection2016.toString -> List(
                tstProtectionDisplayModelDormantIP2016
              ),
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelDormantIP2014
              ),
              FixedProtection2016.toString -> List(
                tstProtectionDisplayModelDormantFP2016
              ),
              FixedProtection2014.toString -> List(
                tstProtectionDisplayModelDormantFP2014
              ),
              PrimaryProtection.toString -> List(
                tstProtectionDisplayModelDormantPrimary
              ),
              EnhancedProtection.toString -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection.toString -> List(
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
        protectionType = Some(FixedProtection2014.toString),
        status = Some(Expired.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014.toString,
        status = Expired.toString,
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
        protectionType = Some(FixedProtection2016.toString),
        status = Some(Expired.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016.toString,
        status = Expired.toString,
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
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Rejected.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelRejectedIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Rejected.toString,
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
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Unsuccessful.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelUnsuccessfulIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Unsuccessful.toString,
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
        protectionType = Some(PrimaryProtection.toString),
        status = Some(Withdrawn.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection.toString,
        status = Withdrawn.toString,
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
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Withdrawn.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016.toString,
        status = Withdrawn.toString,
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
        protectionType = Some(FixedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection.toString,
        status = Dormant.toString,
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
        protectionType = Some(EnhancedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection.toString,
        status = Dormant.toString,
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
              EnhancedProtection.toString -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection.toString -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2016.toString -> List(
                tstProtectionDisplayModelWithdrawnIP2016
              ),
              PrimaryProtection.toString -> List(
                tstProtectionDisplayModelWithdrawnPrimary
              )
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelUnsuccessfulIP2014
              )
            )
          ),
          rejectedProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelRejectedIP2014
              )
            )
          ),
          expiredProtections = ExistingInactiveProtectionsByType(
            Seq(
              FixedProtection2016.toString -> List(
                tstProtectionDisplayModelExpiredFP2016
              ),
              FixedProtection2014.toString -> List(
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

    "Handle all protection types" when
      ProtectionType.values.foreach(protectionType =>
        s"protection type is $protectionType" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = Some(tstPSACheckRef),
            protectionID = Some(12345),
            protectionType = Some(protectionType.toString),
            status = Some(Withdrawn.toString),
            certificateDate = Some("2016-04-17T15:14:00"),
            protectedAmount = Some(1250000),
            protectionReference = Some("PSA654321"),
            notificationId = Some(1)
          )

          val transformedReadResponseModel = TransformedReadResponseModel(
            activeProtection = None,
            inactiveProtections = Seq(protectionModel)
          )

          val existingProtectionDisplayModel = ExistingProtectionDisplayModel(
            protectionType = protectionType.toString,
            status = Withdrawn.toString,
            amendCall = None,
            psaCheckReference = Some(tstPSACheckRef),
            protectionReference = "PSA654321",
            protectedAmount = Some("£1,250,000"),
            certificateDate = Some("17 April 2016"),
            certificateTime = Some("3:14pm")
          )

          val existingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
            activeProtection = None,
            inactiveProtections = ExistingInactiveProtectionsDisplayModel(
              dormantProtections = ExistingInactiveProtectionsByType.empty,
              withdrawnProtections = ExistingInactiveProtectionsByType(
                Seq(
                  protectionType.toString -> Seq(existingProtectionDisplayModel)
                )
              ),
              unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
              rejectedProtections = ExistingInactiveProtectionsByType.empty,
              expiredProtections = ExistingInactiveProtectionsByType.empty
            )
          )

          displayConstructor.createExistingProtectionsDisplayModel(
            transformedReadResponseModel
          ) shouldBe existingProtectionsDisplayModel
        }
      )

    "Handle an unknown protection type as notRecorded" in {
      val protectionModel = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("unknown protection type"),
        status = Some(Withdrawn.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA654321"),
        notificationId = Some(1)
      )

      val transformedReadResponseModel = TransformedReadResponseModel(
        activeProtection = None,
        inactiveProtections = Seq(protectionModel)
      )

      val existingProtectionDisplayModel = ExistingProtectionDisplayModel(
        protectionType = "notRecorded",
        status = Withdrawn.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA654321",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val existingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = None,
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType.empty,
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              "notRecorded" -> Seq(existingProtectionDisplayModel)
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      displayConstructor.createExistingProtectionsDisplayModel(
        transformedReadResponseModel
      ) shouldBe existingProtectionsDisplayModel
    }

  }

  "createPrintDisplayModel" should {

    "create a Print Display model" in {
      val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
      val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
      val tstProtectionModel = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"),
        notificationId = Some(1)
      )
      val nino = "testNino"

      val tstResultPrintDisplayModel = PrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = nino,
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        psaCheckReference = tstPSACheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      displayConstructor.createPrintDisplayModel(
        Some(tstPersonalDetailsModel),
        tstProtectionModel,
        nino
      ) shouldBe tstResultPrintDisplayModel
    }
  }

  "createPrintDisplayModel" should {

    "create a Print Display model with None protectionReference" in {
      val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
      val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)
      val tstProtectionModel = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = None,
        notificationId = Some(1)
      )
      val nino = "testNino"

      val tstResultPrintDisplayModel = PrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = nino,
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        psaCheckReference = tstPSACheckRef,
        protectionReference = "None",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
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
      protectionType = Some(IndividualProtection2014.toString),
      status = Some(Open.toString),
      certificateDate = Some("2016-04-17T15:14:00"),
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
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        psaCheckReference = tstPSACheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm"),
        notificationId = 1
      )

      displayConstructor.createAmendPrintDisplayModel(
        Some(personalDetailsModel),
        protectionModel,
        nino
      ) shouldBe expectedAmendPrintDisplayModel
    }

    "create AmendPrintDisplayModel with empty protectionReference" in {
      val expectedAmendPrintDisplayModel = AmendPrintDisplayModel(
        firstName = "Testy",
        surname = "Mctestface",
        nino = nino,
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        psaCheckReference = tstPSACheckRef,
        protectionReference = "None",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm"),
        notificationId = 1
      )

      displayConstructor.createAmendPrintDisplayModel(
        Some(personalDetailsModel),
        protectionModel.copy(protectionReference = None),
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

      "provided with empty notificationId" in {
        val exc = the[OptionNotDefinedException] thrownBy displayConstructor.createAmendPrintDisplayModel(
          Some(personalDetailsModel),
          protectionModel.copy(notificationId = None),
          nino
        )

        exc.functionName shouldBe "createAmendPrintDisplayModel"
        exc.optionName shouldBe "notificationId"
        exc.applicationType shouldBe IndividualProtection2014.toString
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
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "correctly transform an AmendProtectionModel into an AmendDisplayModel with PSOs" in {
      displayConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
        amended = false,
        pensionContributionSections = tstPensionContributionPsoDisplaySections,
        psoAdded = false,
        psoSections = tstNoPsoDisplaySections,
        totalAmount = "£1,100,000.34"
      )
    }

    "return no current PSO's when not supplied with an amountOption" in {
      displayConstructor.createAmendDisplayModel(tstWithPsoAmendProtectionModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
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
        protectionType = Some(IndividualProtection2016.toString),
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
        protectionType = IndividualProtection2016.toString,
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
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Open.toString),
        protectedAmount = Some(1100000.34),
        relevantAmount = Some(1100000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(List(PensionDebitModel("2017-03-02T15:14:00", 1000.0))),
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
              changeLinkCall = Some(
                controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails(
                  Strings.ProtectionTypeURL.IndividualProtection2016,
                  Strings.StatusURL.Open
                )
              ),
              removeLinkCall = Some(
                controllers.routes.AmendsRemovePensionSharingOrderController.removePso(
                  Strings.ProtectionTypeURL.IndividualProtection2016,
                  Strings.StatusURL.Open
                )
              ),
              "£1,000",
              "2 March 2017"
            )
          )
        )
      )

      displayConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
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
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Open.toString),
        protectedAmount = Some(1000000.34),
        relevantAmount = Some(1000000.34),
        preADayPensionInPayment = None,
        postADayBenefitCrystallisationEvents = None,
        pensionDebits = Some(
          List(PensionDebitModel("2017-03-02T15:14:00", 1000.0), PensionDebitModel("2017-03-02T15:12:00", 2000.0))
        ),
        pensionDebitTotalAmount = Some(0.0),
        nonUKRights = Some(100000.0),
        uncrystallisedRights = Some(1000000.34)
      )

      val amendModel = AmendProtectionModel(tstNewPsoAmountProtection, tstNewPsoAmountProtection)

      displayConstructor.createAmendDisplayModel(amendModel) shouldBe AmendDisplayModel(
        protectionType = IndividualProtection2016.toString,
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
          protectionType = Some(IndividualProtection2014.toString),
          protectionReference = Some("protectionRef"),
          certificateDate = Some("2016-06-14T15:14:00"),
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
            protectionType = Some(IndividualProtection2014.toString),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14T15:14:00"),
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
        exc.applicationType shouldBe IndividualProtection2014.toString
      }

      "notification ID is NOT present" in {
        val amendResponseModel = AmendResponseModel(
          ProtectionModel(
            psaCheckReference = Some("psaRef"),
            protectionID = Some(100003),
            protectionType = Some(IndividualProtection2014.toString),
            protectionReference = Some("protectionRef"),
            certificateDate = Some("2016-06-14T15:14:00"),
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
        exc.applicationType shouldBe IndividualProtection2014.toString
      }
    }
  }

  "createAmendResponseModel" should {
    "correctly transform an AmendResponseModel into an AmendResultDisplayModel" in {
      val tstAmendResponseModel = AmendResponseModel(
        ProtectionModel(
          psaCheckReference = Some("psaRef"),
          protectionID = Some(100003),
          protectionType = Some(IndividualProtection2014.toString),
          status = Some(Open.toString),
          protectionReference = Some("protectionRef"),
          certificateDate = Some("2016-06-14T15:14:00"),
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
            protectionType = IndividualProtection2014.toString,
            status = Open.toString,
            psaCheckReference = "psaRef",
            protectionReference = "protectionRef",
            protectedAmount = Some("£1,350,000.45"),
            certificateDate = Some("14 June 2016"),
            certificateTime = Some("3:14pm"),
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

  "protectionTypeDisplaysLumpSumAmount" should {
    "operate correctly when the hip migration flag is enabled" should {
      "return true" when {
        val types = Seq(
          PrimaryProtection,
          PrimaryProtectionLTA
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysLumpSumAmount(protectionType) shouldBe true
          }
        )
      }

      "return false" when {
        val types = Seq(
          EnhancedProtection,
          EnhancedProtectionLTA,
          FixedProtection,
          FixedProtection2014,
          FixedProtection2014LTA,
          FixedProtection2016,
          FixedProtection2016LTA,
          FixedProtectionLTA,
          IndividualProtection2014,
          IndividualProtection2014LTA,
          IndividualProtection2016,
          IndividualProtection2016LTA,
          InternationalEnhancementS221,
          InternationalEnhancementS224,
          PensionCreditRights
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysLumpSumAmount(protectionType) shouldBe false
          }
        )
      }
    }

    "return false when the hip migration flag is disabled" when {
      val types = ProtectionType.values.map(_.toString)

      types.foreach(protectionType =>
        s"protection type is $protectionType" in {
          displayConstructorHipDisabled.protectionTypeDisplaysLumpSumAmount(protectionType) shouldBe false
        }
      )
    }
  }

  "protectionTypeDisplaysLumpSumPercentage" should {
    "operate correctly when the hip migration flag is enabled" should {
      "return true" when {
        val types = Seq(
          EnhancedProtection,
          EnhancedProtectionLTA
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysLumpSumPercentage(protectionType) shouldBe true
          }
        )
      }

      "return false" when {
        val types = Seq(
          PrimaryProtection,
          PrimaryProtectionLTA,
          FixedProtection,
          FixedProtection2014,
          FixedProtection2014LTA,
          FixedProtection2016,
          FixedProtection2016LTA,
          FixedProtectionLTA,
          IndividualProtection2014,
          IndividualProtection2014LTA,
          IndividualProtection2016,
          IndividualProtection2016LTA,
          InternationalEnhancementS221,
          InternationalEnhancementS224,
          PensionCreditRights
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysLumpSumPercentage(protectionType) shouldBe false
          }
        )
      }
    }

    "return false when the hip migration flag is disabled" when {
      val types = ProtectionType.values.map(_.toString)

      types.foreach(protectionType =>
        s"protection type is $protectionType" in {
          displayConstructorHipDisabled.protectionTypeDisplaysLumpSumPercentage(protectionType) shouldBe false
        }
      )
    }

  }

  "protectionTypeDisplaysEnhancementFactor" should {
    "operate correctly when the hip migration flag is enabled" should {
      "return true" when {
        val types = Seq(
          PensionCreditRights,
          InternationalEnhancementS221,
          InternationalEnhancementS224
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysEnhancementFactor(protectionType) shouldBe true
          }
        )
      }

      "return false" when {
        val types = Seq(
          EnhancedProtection,
          EnhancedProtectionLTA,
          FixedProtection,
          FixedProtection2014,
          FixedProtection2014LTA,
          FixedProtection2016,
          FixedProtection2016LTA,
          FixedProtectionLTA,
          IndividualProtection2014,
          IndividualProtection2014LTA,
          IndividualProtection2016,
          IndividualProtection2016LTA,
          PrimaryProtection,
          PrimaryProtectionLTA
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysEnhancementFactor(protectionType) shouldBe false
          }
        )
      }
    }

    "return false when the hip migration flag is disabled" when {
      val types = ProtectionType.values.map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          displayConstructorHipDisabled.protectionTypeDisplaysEnhancementFactor(protectionType) shouldBe false
        }
      )
    }
  }

  "protectionTypeDisplaysFactor" should {
    "operate correctly when the hip migration flag is enabled" should {
      "return true" when {
        val types = Seq(
          PrimaryProtection,
          PrimaryProtectionLTA
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysFactor(protectionType) shouldBe true
          }
        )
      }

      "return false" when {
        val types = Seq(
          EnhancedProtection,
          EnhancedProtectionLTA,
          FixedProtection,
          FixedProtection2014,
          FixedProtection2014LTA,
          FixedProtection2016,
          FixedProtection2016LTA,
          FixedProtectionLTA,
          IndividualProtection2014,
          IndividualProtection2014LTA,
          IndividualProtection2016,
          IndividualProtection2016LTA,
          InternationalEnhancementS221,
          InternationalEnhancementS224,
          PensionCreditRights
        ).map(_.toString)

        types.foreach(protectionType =>
          s"the protection type is $protectionType" in {
            displayConstructor.protectionTypeDisplaysFactor(protectionType) shouldBe false
          }
        )
      }
    }

    "return false when the hip migration flag is disabled" when {
      val types = ProtectionType.values.map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          displayConstructorHipDisabled.protectionTypeDisplaysFactor(protectionType) shouldBe false
        }
      )
    }
  }

}
