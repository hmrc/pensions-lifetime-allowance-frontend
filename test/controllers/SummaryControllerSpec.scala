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


import java.util.UUID

import akka.util.Timeout
import auth._
import connectors.KeyStoreConnector
import constructors.SummaryConstructor
import enums.ApplicationType
import models.SummaryModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import testHelpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SummaryControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockSummaryConstructor = mock[SummaryConstructor]

  val tstSummaryModel = SummaryModel(ApplicationType.FP2016, false, List.empty, List.empty)

  object TestSummaryControllerNoData extends SummaryController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    val summaryConstructor = mockSummaryConstructor
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(None))
  }

  object TestSummaryControllerInvalidData extends SummaryController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    val summaryConstructor = mockSummaryConstructor
    when(summaryConstructor.createSummaryData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(None)
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  object TestSummaryControllerValidData extends SummaryController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    val summaryConstructor = mockSummaryConstructor
    when(summaryConstructor.createSummaryData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Some(tstSummaryModel))
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  val sessionId = UUID.randomUUID.toString
  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  "Navigating to summary when there is no user data" when {

    "user is applying for IP16" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerNoData.summaryIP16)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error for IP16" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2016Controller.pensionsTaken()}"
      }
    }

    "user is applying for IP14" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerNoData.summaryIP14)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error for IP14" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2014Controller.ip14PensionsTaken()}"
      }
    }
  }

  "Navigating to summary when there is invalid user data" when {

    "user is applying for IP16" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerInvalidData.summaryIP16)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error for IP16" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2016Controller.pensionsTaken()}"
      }
    }

    "user is applying for IP14" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerInvalidData.summaryIP14)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error for IP14" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2014Controller.ip14PensionsTaken()}"
      }
    }
  }

  "Navigating to summary when user has valid data" when {

    "user is applying for IP16" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerValidData.summaryIP16)
      "return 200" in {
        status(DataItem.result) shouldBe 200
      }
    }

    "user is applying for IP14" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestSummaryControllerValidData.summaryIP14)
      "return 200" in {
        status(DataItem.result) shouldBe 200
      }
    }
  }

  "Checking for data metrics flags" should {
    "return true for 'pensionsTakenBetween'" in {
      TestSummaryControllerValidData.recordDataMetrics("pensionsTakenBetween") shouldBe true
    }

    "return false for 'pensionsTakenBetweenAmt'" in {
      TestSummaryControllerValidData.recordDataMetrics("pensionsTakenBetweenAmt") shouldBe false
    }
  }
}
