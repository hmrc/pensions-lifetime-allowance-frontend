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
import connectors.KeyStoreConnector
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.jsoup._
import testHelpers._
import org.mockito.Matchers
import org.mockito.Mockito._
import scala.concurrent.Future
import models._


class EligibilityControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {


  val sessionId = UUID.randomUUID.toString
  val fakeRequest = FakeRequest("GET", "/protect-your-lifetime-allowance/")
  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val TestEligibilityController = new EligibilityController {
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
  }


  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }


///////////////////////////////////////////////
// Initial Setup
///////////////////////////////////////////////
  "EligibilityController should be correctly initialised" in {
    EligibilityController.keyStoreConnector shouldBe KeyStoreConnector
  }

///////////////////////////////////////////////
// Adding to pension
///////////////////////////////////////////////


  "In EligibilityController calling the .addingToPension action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("adding-to-pension", TestEligibilityController.addingToPension, None)

      "return 303" in {
        status(DataItem.result) shouldBe 303
      }

      "redirect to introduction page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}")
      }
    }

    "not supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("adding-to-pension", TestEligibilityController.addingToPension, Some(sessionId))
      "return a 200" in {
        keystoreFetchCondition[AddingToPensionModel](None)
        status(DataItem.result) shouldBe 200
      }

      "take user to the adding to pension page" in {
        keystoreFetchCondition[AddingToPensionModel](None)
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.addingToPension.pageHeading")
      }
    }

    "supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("adding-to-pension", TestEligibilityController.addingToPension, Some(sessionId))
      val testModel = new AddingToPensionModel(Some("yes"))
      "return a 200" in {
        keystoreFetchCondition[AddingToPensionModel](Some(testModel))
        status(DataItem.result) shouldBe 200
      }

      "take user to the adding to pension page" in {
        keystoreFetchCondition[AddingToPensionModel](Some(testModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.addingToPension.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AddingToPensionModel](Some(testModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the radio option `yes` selected by default" in {
          keystoreFetchCondition[AddingToPensionModel](Some(testModel))
          DataItem.jsoupDoc.body.getElementById("willAddToPension-yes").parent.classNames().contains("selected") shouldBe true
        }
      }
    }
  }

  "Submitting Adding To Pension data" when {

    "Submitting 'yes' in addingToPensionForm" should {

      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        TestEligibilityController.submitAddingToPension,
        sessionId,
        ("willAddToPension", "yes")
      )
      "return 303" in {status(DataItem.result) shouldBe 303}
      "redirect to pension savings" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.pensionSavings()}") }
    }

    "Submitting 'no' in addingToPensionForm" should {
    
      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        TestEligibilityController.submitAddingToPension,
        sessionId,
        ("willAddToPension", "no")
      )
      "return 303" in { status(DataItem.result) shouldBe 303 }
      "redirect to apply FP 16" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.applyFP()}") }
    }

    "submitting addingToPensionForm with no data" should {

      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        TestEligibilityController.submitAddingToPension,
        sessionId,
        ("willAddToPension", "")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("Please tell us whether you'll be adding to your pension in the future")
      }
    }

  }

///////////////////////////////////////////////
// Added to pension
///////////////////////////////////////////////

  "Calling the .addedToPension action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("added-to-pension", TestEligibilityController.addedToPension, None)

      "return 303" in {
        status(DataItem.result) shouldBe 303
      }

      "redirect to introduction page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}")
      }
    }

    "not supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("added-to-pension", TestEligibilityController.addedToPension, Some(sessionId))
      "return a 200" in {
        keystoreFetchCondition[AddedToPensionModel](None)
        status(DataItem.result) shouldBe 200
      }

      "take user to the added to pension page" in {
        keystoreFetchCondition[AddedToPensionModel](None)
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.addedToPension.pageHeading")
      }
    }

    "supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("added-to-pension", TestEligibilityController.addedToPension, Some(sessionId))
      val testModel = new AddedToPensionModel(Some("no"))
      "return a 200" in {
        keystoreFetchCondition[AddedToPensionModel](Some(testModel))
        status(DataItem.result) shouldBe 200
      }

      "take user to the added to pension page" in {
        keystoreFetchCondition[AddedToPensionModel](Some(testModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.addedToPension.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AddedToPensionModel](Some(testModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the radio option `no` selected by default" in {
          keystoreFetchCondition[AddedToPensionModel](Some(testModel))
          DataItem.jsoupDoc.body.getElementById("haveAddedToPension-no").parent.classNames().contains("selected") shouldBe true
        }
      }
    }
  }

  "Submitting Added To Pension data" when {

    "Submitting 'yes' in addedToPensionForm" should {

      object DataItem extends FakeRequestToPost(
        "added-to-pension",
        TestEligibilityController.submitAddedToPension,
        sessionId,
        ("haveAddedToPension", "yes")
      )
      "return 303" in {status(DataItem.result) shouldBe 303}
      "redirect to pension savings" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.pensionSavings()}") }
    }

    "Submitting 'no' in addedToPensionForm" should {
    
      object DataItem extends FakeRequestToPost(
        "added-to-pension",
        TestEligibilityController.submitAddedToPension,
        sessionId,
        ("haveAddedToPension", "no")
      )
      "return 303" in { status(DataItem.result) shouldBe 303 }
      "redirect to adding to pension" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.addingToPension()}") }
    }

    "submitting addedToPensionForm with no data" should {

      object DataItem extends FakeRequestToPost(
        "added-to-pension",
        TestEligibilityController.submitAddedToPension,
        sessionId,
        ("haveAddedToPension", "")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("Please tell us whether you've added to your pension since 6 April 2016")
      }
    }
  }

///////////////////////////////////////////////
// Pension savings
///////////////////////////////////////////////

  "Calling the .pensionSavings action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("pension-pot-size", TestEligibilityController.pensionSavings, None)

      "return 303" in {
        status(DataItem.result) shouldBe 303
      }

      "redirect to introduction page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}")
      }
    }

    "not supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("pension-pot-size", TestEligibilityController.pensionSavings, Some(sessionId))
      "return a 200" in {
        keystoreFetchCondition[PensionSavingsModel](None)
        status(DataItem.result) shouldBe 200
      }

      "take user to the pension savings page" in {
        keystoreFetchCondition[PensionSavingsModel](None)
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionSavings.pageHeading")
      }
    }

    "supplied with a pre-existing stored model" should {
      object DataItem extends FakeRequestTo("pension-pot-size", TestEligibilityController.pensionSavings, Some(sessionId))
      val testModel = new PensionSavingsModel(Some("no"))
      "return a 200" in {
        keystoreFetchCondition[PensionSavingsModel](Some(testModel))
        status(DataItem.result) shouldBe 200
      }

      "take user to the pension savings page" in {
        keystoreFetchCondition[PensionSavingsModel](Some(testModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionSavings.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[PensionSavingsModel](Some(testModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the radio option `no` selected by default" in {
          keystoreFetchCondition[PensionSavingsModel](Some(testModel))
          DataItem.jsoupDoc.body.getElementById("eligiblePensionSavings-no").parent.classNames().contains("selected") shouldBe true
        }
      }
    }
  }

  "Submitting Pension Savings data" when {

    "Submitting 'yes' in pensionSavingsForm" should {

      object DataItem extends FakeRequestToPost(
        "pension-savings",
        TestEligibilityController.submitPensionSavings,
        sessionId,
        ("eligiblePensionSavings", "yes")
      )
      "return 303" in {status(DataItem.result) shouldBe 303}
      "redirect to pension savings" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.applyIP()}") }
    }

    "Submitting 'no' in pensionSavingsForm" should {
    
      object DataItem extends FakeRequestToPost(
        "pension-savings",
        TestEligibilityController.submitPensionSavings,
        sessionId,
        ("eligiblePensionSavings", "no")
      )
      "return 303" in { status(DataItem.result) shouldBe 303 }
      "redirect to will add to pension" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.EligibilityController.cannotApply()}") }
    }

    "submitting pensionSavingsForm with no data" should {

      object DataItem extends FakeRequestToPost(
        "pension-savings",
        TestEligibilityController.submitPensionSavings,
        sessionId,
        ("eligiblePensionSavings", "")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("Please tell us if your pension savings were £1 million or more on 5 April 2016")
      }
    }
  }

///////////////////////////////////////////////
// Apply FP
///////////////////////////////////////////////

  "Calling the .applyFP action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("apply-fp", TestEligibilityController.applyFP, None)

      "return 303" in {
        status(DataItem.result) shouldBe 303
      }

      "redirect to introduction page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}")
      }
    }

    "navigated to with a valid session ID" should {
      object DataItem extends FakeRequestTo("apply-fp", TestEligibilityController.applyFP, Some(sessionId))
      "return a 200" in {
        status(DataItem.result) shouldBe 200
      }

      "take user to the Apply FP page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.applyFP.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }
      }
    }
  }

///////////////////////////////////////////////
// Apply IP
///////////////////////////////////////////////

  "Calling the .applyIP action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("apply-ip", TestEligibilityController.applyIP, None)

      "return 200" in {
        status(DataItem.result) shouldBe 200
      }

      "take user to the Apply FP page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.applyIP.pageHeading")
      }
    }

    "navigated to with a valid session ID" should {
      object DataItem extends FakeRequestTo("apply-ip", TestEligibilityController.applyIP, Some(sessionId))
      "return a 200" in {
        status(DataItem.result) shouldBe 200
      }

      "take user to the Apply FP page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.applyIP.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }
      }
    }
  }

///////////////////////////////////////////////
// Cannot apply
///////////////////////////////////////////////

  "Calling the .cannotApply action" when {

    "visited directly with no session ID" should {
      object DataItem extends FakeRequestTo("pension-below-threshold", TestEligibilityController.cannotApply, None)

      "return 303" in {
        status(DataItem.result) shouldBe 303
      }

      "redirect to introduction page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}")
      }
    }

    "navigated to with a valid session ID" should {
      object DataItem extends FakeRequestTo("pension-below-threshold", TestEligibilityController.cannotApply, Some(sessionId))
      "return a 200" in {
        status(DataItem.result) shouldBe 200
      }

      "take user to the Apply FP page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.cannotApply.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }
      }
    }
  }

}
