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

package constructors.display

import enums.ApplicationStage
import models.amend.AmendProtectionModel
import models.display.{AmendDisplayRowModel, AmendDisplaySectionModel, AmendPrintDisplayModel}
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.{AmendProtectionResponseStatus, ProtectionStatus, ProtectionType}
import models.{
  AmendResponseModel,
  AmendedProtectionType,
  DateModel,
  PensionDebitModel,
  Person,
  PersonalDetailsModel,
  ProtectionModel
}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import testHelpers.FakeApplication

trait DisplayConstructorsTestData extends FakeApplication {

  implicit val mockLang: Lang                                   = mock[Lang]
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit val mockMessage: Messages =
    inject[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val tstPsaCheckRef = "PSA33456789"

  val tstProtectionModel: ProtectionModel = ProtectionModel(
    psaCheckReference = tstPsaCheckRef,
    identifier = 100001,
    sequence = 1,
    protectionType = ProtectionType.IndividualProtection2016,
    status = ProtectionStatus.Open,
    certificateDate = None,
    certificateTime = None
  )

  val tstNoPsoAmendProtectionModel: AmendProtectionModel =
    AmendProtectionModel.tryFromProtection(tstProtectionModel).get

  val tstWithPsoAmendProtectionModel: AmendProtectionModel =
    tstNoPsoAmendProtectionModel.withPensionDebit(Some(PensionDebitModel(DateModel.of(2017, 3, 2), 1000)))

  val tstWithExistingPsoAmendProtectionModel: AmendProtectionModel = tstNoPsoAmendProtectionModel.copy(
    pensionDebitTotalAmount = Some(1000)
  )

  val tstPensionContributionNoPsoDisplaySections: Seq[AmendDisplaySectionModel] = Seq(
    AmendDisplaySectionModel(
      "PensionsTakenBefore",
      Seq(
        AmendDisplayRowModel(
          "YesNo",
          Some(
            controllers.routes.AmendsPensionTakenBeforeController.amendPensionsTakenBefore(
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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
              AmendableProtectionType.IndividualProtection2016,
              AmendProtectionRequestStatus.Open
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

  val tstNino = "testNino"

  val amendResponseModel = AmendResponseModel(
    identifier = 1,
    sequence = 1,
    protectionType = AmendedProtectionType.IndividualProtection2014,
    status = AmendProtectionResponseStatus.Open,
    certificateDate = None,
    certificateTime = None,
    protectionReference = None,
    psaCheckReference = tstPsaCheckRef,
    relevantAmount = 1_250_000,
    preADayPensionInPaymentAmount = 375_000,
    postADayBenefitCrystallisationEventAmount = 375_000,
    uncrystallisedRightsAmount = 375_000,
    nonUKRightsAmount = 375_000,
    notificationId = None,
    protectedAmount = Some(1_250_000),
    pensionDebitTotalAmount = Some(0),
    pensionDebit = None
  )

  val expectedAmendPrintDisplayModel = AmendPrintDisplayModel(
    firstName = "Testy",
    surname = "Mctestface",
    nino = tstNino,
    protectionType = AmendedProtectionType.IndividualProtection2014,
    status = None,
    psaCheckReference = Some(tstPsaCheckRef),
    protectionReference = Some("PSA123456"),
    fixedProtectionReference = None,
    protectedAmount = Some("£1,250,000"),
    certificateDate = Some("17 April 2016"),
    certificateTime = Some("3:14pm")
  )

  val tstPerson               = Person(firstName = "Testy", lastName = "McTestface")
  val tstPersonalDetailsModel = PersonalDetailsModel(tstPerson)

}
