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

package controllers

import java.util.UUID

import auth.{MockAuthConnector, MockConfig}
import connectors.KeyStoreConnector
import constructors.{ResponseConstructors, DisplayConstructors}
import models._
import models.amendModels.{AmendCurrentPensionModel, AmendProtectionModel}
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{AuthorisedFakeRequestToPost, AuthorisedFakeRequestTo}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

import scala.concurrent.Future

class AmendsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockKeyStoreConnector = mock[KeyStoreConnector]

  val testIP16DormantModel = AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None,None, protectionType = Some("IP2016"),status = Some("dormant"), relevantAmount = Some(100000), uncrystallisedRights = Some(100000)))

  object TestAmendsController extends AmendsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val responseConstructors = mock[ResponseConstructors]
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
    override val displayConstructors: DisplayConstructors = displayConstructors

    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
  }

  val sessionId = UUID.randomUUID.toString
  val fakeRequest = FakeRequest()

  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  "In AmendsController calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendCurrentPensions("ip2016","open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "supplied with a stored test model (£100000, IP2016, dormant)" should {
      val testModel = new AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None,None,uncrystallisedRights = Some(100000)))
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendCurrentPensions("ip2016","dormant"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testModel))
        status(DataItem.result) shouldBe 200
      }

      "take the user to the amend ip16 current pensions page" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the value 100000 completed in the amount input by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))
          DataItem.jsoupDoc.body.getElementById("amendedUKPensionAmt").attr("value") shouldBe "100000"
        }
      }
    }
  }

  "Submitting Amend IP16 Current Pensions data" when {

    "amount is set as '100,000'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "100000"), ("protectionType", "ip2016"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testIP16DormantModel))
        status(DataItem.result) shouldBe 303 }
      "redirect to Amends Summary page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}") }
    }

    "no amount is set" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", ""))
      "return 400" in {status(DataItem.result) shouldBe 400}
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorQuestion"))
      }
    }

    "amount is set as '5.001'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "5.001"))
      "return 400" in {status(DataItem.result) shouldBe 400}
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorDecimalPlaces"))
      }
    }

    "amount is set as '-25'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "-25"))
      "return 400" in {status(DataItem.result) shouldBe 400}
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorNegative"))
      }
    }

    "amount is set as '99999999999999.99'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "99999999999999.99"))
      "return 400" in {status(DataItem.result) shouldBe 400}
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorMaximum"))
      }
    }

  }
  
}
