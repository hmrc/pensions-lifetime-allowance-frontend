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
import config.{FrontendAppConfig,FrontendAuthConnector}
import models._

class IP2016ControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

    object TestIP2016Controller extends IP2016Controller {
        override lazy val applicationConfig = FrontendAppConfig
        override lazy val authConnector = FrontendAuthConnector
        override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
        override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    }

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
    "IP2016Controller should be correctly initialised" in {
        IP2016Controller.keyStoreConnector shouldBe KeyStoreConnector
    }






    "In IP2016Controller calling the .pensionsTaken action" when {

        "visited directly with no session ID" should {
            object DataItem extends FakeRequestTo("pensions-taken", TestIP2016Controller.pensionsTaken, None)

            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect to introduction page" in {
                redirectLocation(DataItem.result) shouldBe Some(s"http://localhost:9949/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A9012%2Fprotect-your-lifetime-allowance%2Fapply-ip&accountType=individual")
            }
        }
    }

}
