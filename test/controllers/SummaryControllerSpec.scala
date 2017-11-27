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
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import auth._
import com.kenshoo.play.metrics.PlayModule
import connectors.KeyStoreConnector
import constructors.SummaryConstructor
import enums.ApplicationType
import models.SummaryModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import _root_.mock.AuthMock
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import testHelpers._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class SummaryControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with AuthMock {
  override def bindModules = Seq(new PlayModule)

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockSummaryConstructor = mock[SummaryConstructor]
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val fakeRequest = FakeRequest()

  val tstSummaryModel = SummaryModel(ApplicationType.FP2016, false, List.empty, List.empty)

  object TestSummaryControllerNoData extends SummaryController {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]

    val summaryConstructor = mockSummaryConstructor
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(None))
  }

  object TestSummaryControllerInvalidData extends SummaryController {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]

    val summaryConstructor = mockSummaryConstructor
    when(summaryConstructor.createSummaryData(Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(None)
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    when(keyStoreConnector.fetchAllUserData(Matchers.any())).thenReturn(Future(Some(CacheMap("tstID", Map.empty[String, JsValue]))))
  }

  object TestSummaryControllerValidData extends SummaryController {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/summary"

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]

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
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      lazy val result = await(TestSummaryControllerNoData.summaryIP16(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 500" in {
        status(result) shouldBe 500
      }
      "show technical error for IP16" in {
        implicit val timeout: Timeout = Timeout.apply(5000, TimeUnit.SECONDS)
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2016Controller.pensionsTaken()}"
      }
    }
  }

  "Navigating to summary when there is invalid user data" when {

    "user is applying for IP16" should {
      lazy val result = await(TestSummaryControllerInvalidData.summaryIP16(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 500" in {
        status(result) shouldBe 500
      }
      "show technical error for IP16" in {
        implicit val timeout: Timeout = Timeout.apply(5000, TimeUnit.SECONDS)
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.IP2016Controller.pensionsTaken()}"
      }
    }
  }

  "Navigating to summary when user has valid data" when {

    "user is applying for IP16" should {
      lazy val result = await(TestSummaryControllerValidData.summaryIP16(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 200" in {
        status(result) shouldBe 200
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
