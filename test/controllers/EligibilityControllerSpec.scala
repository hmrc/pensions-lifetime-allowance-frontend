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

import play.api.i18n.Messages
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.http._
import play.api.test.FakeRequest
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import play.api.mvc.{AnyContent, Action}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import org.jsoup._
import testHelpers._


class WillAddToPensionControllerSpec extends UnitSpec with WithFakeApplication{


  val fakeRequest = FakeRequest("GET", "/")


  "GET for adding to pension" should {
    "return 200" in {
      val result = EligibilityController.addingToPension(fakeRequest)
      status(result) shouldBe 200
    }

    "return HTML" in {
      val result = EligibilityController.addingToPension(fakeRequest)
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }
  }


  "Submitting 'yes' in addingToPensionForm" should {

      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        EligibilityController.submitAddingToPension,
        ("willAddToPension", "yes")
      )

    "return 303" in {status(DataItem.result) shouldBe 303}

    "redirect to pension savings" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.PensionSavingsController.pensionSavings()}") }
  }

  "Submitting 'no' in addingToPensionForm" should {
  
      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        EligibilityController.submitAddingToPension,
        ("willAddToPension", "no")
      )

    "return 303" in { status(DataItem.result) shouldBe 303 }

    "redirect to apply FP 16" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.ApplyFPController.applyFP()}") }
  }

  "submitting addingToPensionForm with no data" should {

      object DataItem extends FakeRequestToPost(
        "adding-to-pension",
        EligibilityController.submitAddingToPension,
        ("willAddToPension", "")
      )

    "return 400" in { status(DataItem.result) shouldBe 400 }

    "fail with the correct error message" in {
      DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("Please indicate whether you will be adding to your pension")
    }

  }


}