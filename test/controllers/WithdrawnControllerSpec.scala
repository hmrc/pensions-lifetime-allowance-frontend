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

import org.scalatestplus.play.OneAppPerSuite
import play.api.http._
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import connectors.IdentityVerificationConnector
import testHelpers._
import org.scalatest._
import org.scalatest.Matchers._
import uk.gov.hmrc.time.DateTimeUtils._

class WithdrawnControllerSpec extends FunSpec with OneAppPerSuite {

  val fakeRequest = FakeRequest("GET", "/")

  def testWithdrawnController(): WithdrawnController = new WithdrawnController {}

  describe ("Withdrawn controller") {
    it ("should show withdrawn page") {
      val result = testWithdrawnController().showWithdrawn()(fakeRequest)
      contentAsString(result) should include ("Sorry, Applications for 2014 Protection Have Now Ended")
    }
  }

  describe ("IP14 application") {
    Seq("/apply-for-ip14-pensions-taken", "/apply-for-ip14-pensions-taken-before", "/apply-for-ip14-pensions-taken-between",
        "/apply-for-ip14-overseas-pensions", "/apply-for-ip14-current-pensions", "/apply-for-ip14-pension-sharing-orders",
        "/apply-for-ip14-pension-sharing-order-details", "/apply-for-ip14-remove-pension-sharing-order-details",
        "/apply-for-ip14-submit-your-application").foreach { (path) =>
      it (s"show withdrawn page for /protect-your-lifetime-allowance$path") {
        val result = route(FakeRequest(GET, s"/protect-your-lifetime-allowance$path")).get
        contentAsString(result) should include ("Sorry, Applications for 2014 Protection Have Now Ended")
      }
    }
  }
}
