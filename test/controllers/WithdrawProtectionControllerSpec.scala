/*
 * Copyright 2017 HM Revenue & Customs
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

package controllers

import java.time.LocalDate
import java.util.UUID

import auth.{MockAuthConnector, MockConfig}
import com.kenshoo.play.metrics.PlayModule
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import enums.ApplicationType
import models._
import models.amendModels.{AmendProtectionModel, AmendsGAModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{AuthorisedFakeRequestTo, AuthorisedFakeRequestToPost}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
class WithdrawProtectionControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  override def bindModules = Seq(new PlayModule)

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]
  val mockPLAConnector = mock[PLAConnector]

  object TestWithdrawController extends WithdrawProtectionController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"

    override val displayConstructors: DisplayConstructors = mockDisplayConstructors
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    override val plaConnector: PLAConnector = mockPLAConnector
  }

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val tstPensionContributionNoPsoDisplaySections = Seq(

    AmendDisplaySectionModel("PensionsTakenBefore", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBefore("ip2014", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("PensionsTakenBetween", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendPensionsTakenBetween("ip2014", "active")), None, "No")
    )
    ),
    AmendDisplaySectionModel("OverseasPensions", Seq(
      AmendDisplayRowModel("YesNo", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "Yes"),
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendOverseasPensions("ip2014", "active")), None, "£100,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPensions", Seq(
      AmendDisplayRowModel("Amt", Some(controllers.routes.AmendsController.amendCurrentPensions("ip2014", "active")), None, "£1,000,000")
    )
    ),
    AmendDisplaySectionModel("CurrentPsos", Seq(
      AmendDisplayRowModel("YesNo", None, None, "No")
    )
    )
  )

  val tstAmendDisplayModel = AmendDisplayModel(
    protectionType = "IP2014",
    amended = true,
    pensionContributionSections = tstPensionContributionNoPsoDisplaySections,
    psoAdded = false,
    psoSections = Seq(),
    totalAmount = "£1,100,000"
  )

  "In AmendsController calling the withdrawSummary action" when {

    import testHelpers.AuthorisedFakeRequestTo

    "there is no stored protection model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestWithdrawController.withdrawSummary)

      "return 500" in {
        keystoreFetchCondition[ProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
      "have the correct cache control" in {
        DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
      }
    }

    "there is a stored protection model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestWithdrawController.withdrawSummary)

      "return 200" in {
        keystoreFetchCondition[ProtectionModel](Some(ip2016Protection))
        when(mockDisplayConstructors.createWithdrawSummaryTable(Matchers.any())(Matchers.any())).thenReturn(tstAmendDisplayModel)
        status(DataItem.result) shouldBe 200
        DataItem.jsoupDoc.body().getElementsByTag("li").text should include(Messages("pla.withdraw.pageBreadcrumb"))
      }
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }
}
