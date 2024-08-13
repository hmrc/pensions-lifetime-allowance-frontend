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

package controllers

import config.{FrontendAppConfig, PlaContext}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import views.html.pages.ip2014.withdrawn


class WithdrawnControllerSpec extends FakeApplication with MockitoSugar {

  val mockMCC: MessagesControllerComponents = fakeApplication().injector.instanceOf[MessagesControllerComponents]

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext = mock[PlaContext]
  implicit val mockWithdrawn: withdrawn = app.injector.instanceOf[withdrawn]

  val controller = new WithdrawnController(mockMCC, mockWithdrawn)
  val fakeRequest = FakeRequest("GET", "/")

  ("Withdrawn controller") should  {
     ("should show withdrawn page") in  {
      val result = controller.showWithdrawn()(fakeRequest)
      contentAsString(result) should include ("Sorry, applications for 2014 protection have ended")
    }
  }

  ("IP14 application") should  {
    Seq("/apply-for-ip14-pensions-taken", "/apply-for-ip14-pensions-taken-before", "/apply-for-ip14-pensions-taken-between",
        "/apply-for-ip14-overseas-pensions", "/apply-for-ip14-current-pensions", "/apply-for-ip14-pension-sharing-orders",
        "/apply-for-ip14-pension-sharing-order-details", "/apply-for-ip14-remove-pension-sharing-order-details",
        "/apply-for-ip14-submit-your-application").foreach { (path) =>
       (s"show withdrawn page for /protect-your-lifetime-allowance$path") in {

        val result = controller.showWithdrawn()(FakeRequest(GET, s"/protect-your-lifetime-allowance$path"))
        contentAsString(result) should include ("Sorry, applications for 2014 protection have ended")
      }
    }
  }

}
