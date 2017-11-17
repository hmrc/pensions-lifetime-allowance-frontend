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

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import auth.{MockAuthConnector, MockConfig}
import com.kenshoo.play.metrics.PlayModule
import connectors.{CitizenDetailsConnector, KeyStoreConnector}
import constructors.DisplayConstructors
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.i18n.Messages
import testHelpers.AuthorisedFakeRequestTo
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.http.HeaderCarrier

class PrintControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {
  override def bindModules = Seq(new PlayModule)

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]
  val mockPlayAuthConnector = mock[PlayAuthConnector]

  val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val testProtectionModel = ProtectionModel(psaCheckReference = Some("tstPSACeckRef"), protectionID = Some(1111111))
  val testPrintDisplayModel = PrintDisplayModel("Testy", "Mctestface", "AA11TESTA", "IP2016", "open", "PSATestNum", "ProtRefTestNum", Some("£1,246,500"), Some("3 April 2016"))

  trait BaseTestPrintController extends PrintController {
    val keyStoreConnector = mockKeyStoreConnector
    val citizenDetailsConnector = mockCitizenDetailsConnector
    val displayConstructors = mockDisplayConstructors
    lazy val applicationConfig = MockConfig
    override lazy val authConnector = mockPlayAuthConnector
    lazy val postSignInRedirectUrl = "postSignInUrl"
    implicit val hc: HeaderCarrier = HeaderCarrier()

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]
  }

  object TestPrintControllerValidDetails extends BaseTestPrintController {
    override lazy val appConfig = MockConfig
    when(citizenDetailsConnector.getPersonDetails(Matchers.any())(Matchers.any())).thenReturn(Future(Some(testPersonalDetails)))
    when(keyStoreConnector.fetchAndGetFormData[ProtectionModel](Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(Future(Some(testProtectionModel)))
    when(displayConstructors.createPrintDisplayModel(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(testPrintDisplayModel)
  }

  def mockAuthRetrieval[A](retrieval: Retrieval[A], returnValue: A) = {
    when(mockPlayAuthConnector.authorise[A](Matchers.any(), Matchers.eq(retrieval))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(returnValue))
  }

  "Navigating to print protection" when {


    "Valid data is provided" should {
      object DataItem extends AuthorisedFakeRequestTo(TestPrintControllerValidDetails.printView)
      "return 200" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(DataItem.result) shouldBe 200
      }
      "show the print page" in {
        implicit val timeout: Timeout = Timeout.apply(5000, TimeUnit.MILLISECONDS)
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("Testy Mctestface")
      }
    }
  }

}
