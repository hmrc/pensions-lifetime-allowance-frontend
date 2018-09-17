/*
 * Copyright 2018 HM Revenue & Customs
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

import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{CitizenDetailsConnector, KeyStoreConnector, PLAConnector}
import constructors.DisplayConstructors
import javax.inject.Inject
import mocks.AuthMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrintControllerSpec extends UnitSpec with MockitoSugar with AuthMock with WithFakeApplication {

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockCitizenDetailsConnector = mock[CitizenDetailsConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]
  val fakeRequest = FakeRequest()

  val keyStoreConnector = mock[KeyStoreConnector]
  val plaConnector = mock[PLAConnector]
  implicit val partialRetriever = mock[PlaFormPartialRetriever]
  implicit val templateRenderer = mock[LocalTemplateRenderer]
  val env = mock[Environment]


  val testPersonalDetails = PersonalDetailsModel(Person("McTestFace", "Testy"))
  val testProtectionModel = ProtectionModel(psaCheckReference = Some("tstPSACeckRef"), protectionID = Some(1111111))
  val testPrintDisplayModel = PrintDisplayModel("Testy", "Mctestface", "AA11TESTA", "IP2016", "open", "PSATestNum", "ProtRefTestNum", Some("£1,246,500"), Some("3 April 2016"))

  val TestPrintController = new PrintController(keyStoreConnector, mockCitizenDetailsConnector, env, partialRetriever, templateRenderer) {
    override lazy val authConnector: AuthConnector = mockAuthConnector
    override val displayConstructors = mockDisplayConstructors
  }



  "Navigating to print protection" should {
    "return 200" when {
      "Valid data is provided" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

        when(mockCitizenDetailsConnector.getPersonDetails(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testPersonalDetails)))
        when(keyStoreConnector.fetchAndGetFormData[ProtectionModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testProtectionModel)))
        when(mockDisplayConstructors.createPrintDisplayModel(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(testPrintDisplayModel)

        val result = await(TestPrintController.printView(fakeRequest))
        status(result) shouldBe 200
      }
    }

    "return a 303 redirect" when {
      "InValid data is provided" in {
        when(mockCitizenDetailsConnector.getPersonDetails(ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future(Some(testPersonalDetails)))
        when(keyStoreConnector.fetchAndGetFormData[ProtectionModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(None)
        when(mockDisplayConstructors.createPrintDisplayModel(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(testPrintDisplayModel)

        val result = await(TestPrintController.printView(fakeRequest))
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(routes.ReadProtectionsController.currentProtections().url)
      }
    }
  }
}


