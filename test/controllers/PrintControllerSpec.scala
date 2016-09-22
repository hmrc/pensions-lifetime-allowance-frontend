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

import akka.util.Timeout
import auth.{MockAuthConnector, MockConfig}
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import constructors.DisplayConstructors
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import testHelpers.AuthorisedFakeRequestTo
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

import scala.concurrent.Future

class PrintControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]

  val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val testProtectionModel = ProtectionModel(psaCheckReference = Some("tstPSACeckRef"), protectionID = Some(1111111))
  val testPrintDisplayModel = PrintDisplayModel("Testy", "Mctestface", "AA11TESTA", "IP2016", "active", "PSATestNum", "ProtRefTestNum", Some("£1,246,500"), Some("3 April 2016"))

  trait BaseTestPrintController extends PrintController {
    val keyStoreConnector = mockKeyStoreConnector
    val citizenDetailsConnector = mockCitizenDetailsConnector
    val displayConstructors = mockDisplayConstructors
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "postSignInUrl"
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  object TestPrintControllerNoPersonalDetails extends BaseTestPrintController {

    when(citizenDetailsConnector.getPersonDetails(Matchers.any())(Matchers.any())).thenReturn(Future(None))
  }

  object TestPrintControllerNoProtectionModel extends BaseTestPrintController {

    when(citizenDetailsConnector.getPersonDetails(Matchers.any())(Matchers.any())).thenReturn(Future(Some(testPersonalDetails)))
    when(keyStoreConnector.fetchAndGetFormData[ProtectionModel](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(None))
  }

  object TestPrintControllerValidDetails extends BaseTestPrintController {

    when(citizenDetailsConnector.getPersonDetails(Matchers.any())(Matchers.any())).thenReturn(Future(Some(testPersonalDetails)))
    when(keyStoreConnector.fetchAndGetFormData[ProtectionModel](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(Some(testProtectionModel)))
    when(displayConstructors.createPrintDisplayModel(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(testPrintDisplayModel)
  }

  "Navigating to print protection" when {

    "There is no name recovered from citizen details" should {
      object DataItem extends AuthorisedFakeRequestTo(TestPrintControllerNoPersonalDetails.printView)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      }
    }

    "There is no active protection display model stored in keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestPrintControllerNoProtectionModel.printView)
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show technical error" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      }
    }

    "Valid data is provided" should {
      object DataItem extends AuthorisedFakeRequestTo(TestPrintControllerValidDetails.printView)
      "return 200" in {
        status(DataItem.result) shouldBe 200
      }
      "show the print page" in {
        implicit val timeout: Timeout = 5000
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("Testy Mctestface")
      }
    }
  }

}
