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

import common.Strings
import enums.ApplicationStage
import models.ProtectionModel
import models.amendModels.AmendProtectionModel
import models.display.{AmendDisplayRowModel, AmendDisplaySectionModel}
import models.pla.response.ProtectionStatus.Open
import models.pla.response.ProtectionType.IndividualProtection2016
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import testHelpers.FakeApplication

trait DisplayConstructorsTestData extends FakeApplication {

  implicit val mockLang: Lang                                   = mock[Lang]
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
            )
          ),
          None,
          "Yes"
        ),
        AmendDisplayRowModel(
          "Amt",
          Some(
            controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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
              Strings.ProtectionTypeUrl.IndividualProtection2016,
              Strings.StatusUrl.Open
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

}
