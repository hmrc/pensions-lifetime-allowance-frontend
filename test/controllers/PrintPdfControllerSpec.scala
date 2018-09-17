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
import connectors.{KeyStoreConnector, PdfGeneratorConnector}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.{Application, Logger}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import play.api.test.Helpers._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Result
import testHelpers.MockTemplateRenderer
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class PrintPdfControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {


  implicit val hc = HeaderCarrier()
  lazy val ws: WSClient = fakeApplication.injector.instanceOf[WSClient]

  val keyStoreConnector = mock[KeyStoreConnector]
  private val mockPdfGeneratorConnector = mock[PdfGeneratorConnector]
  private val sessionId = SessionKeys.sessionId -> "pdf-test"
  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]

  val TestController = new PrintPdfController(keyStoreConnector,mockPdfGeneratorConnector,partialRetriever,templateRenderer) {
    override def createPdfResult(response: WSResponse): Result = Ok("Misc.Text")

    override val lookupRequestID = "psa-lookup-request"
    override val lookupResultID = "psa-lookup-result"
  }

  "printResultsPDF" should{
      "return a 303 (redirect) when unable to print a Results PDF" in{
        val request = FakeRequest().withSession(sessionId)
        keystoreFetchCondition[PSALookupRequest](None)
        val result = TestController.printResultsPDF.apply(request)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
      }

      "return a 200 when able to print a Results PDF" in{
         val testPsaLookupResult = PSALookupResult(
          pensionSchemeAdministratorCheckReference = "PSA12345678A",
          ltaType = 5,
          psaCheckResult = 1,
          protectedAmount = Some(BigDecimal(25000)),
          protectionNotificationNumber = Some("IP14000000000A")
        )

        val request = FakeRequest().withSession(sessionId)
        val response : Future[WSResponse] = ws.url("http://testsite.com").post("""{"foo":"bar"}""")

        keystoreFetchCondition[PSALookupResult](Option(testPsaLookupResult))
        when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

        val result = TestController.printResultsPDF.apply(request)
        val returnedHeader = Map("Set-Cookie" -> "mdtp=082c4de4d57fc34fa1e28c56e2e36b34519bcbb7-sessionId=pdf-test; Path=/; HTTPOnly", "Content-Disposition" -> "attachment; filename=lookup-result-IP14000000000A.pdf")

        status(result) shouldBe OK
        headers(result) shouldBe returnedHeader
      }
  }

  "printNotFoundPDF" should{
    "return a 303 (redirect) when unable to print a NotFound PDF" in{
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)
      val result = TestController.printNotFoundPDF.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return a 303 when unable to print a NotFound PDF when the lifetimeAllowanceReference is NOT defined" in{
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = None
      )

      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      val result = TestController.printNotFoundPDF.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return a 200 when able to print a NotFound PDF when the lifetimeAllowanceReference is defined" in{
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = Some("IP14000000000A")
      )

      val request = FakeRequest().withSession(sessionId)
      val response : Future[WSResponse] = ws.url("http://testsite.com").post("""{"foo":"bar"}""")

      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

      val result = TestController.printNotFoundPDF.apply(request)
      val returnedHeader = Map("Set-Cookie" -> "mdtp=082c4de4d57fc34fa1e28c56e2e36b34519bcbb7-sessionId=pdf-test; Path=/; HTTPOnly", "Content-Disposition" -> "attachment; filename=lookup-not-found.pdf")


      status(result) shouldBe OK
      headers(result) shouldBe returnedHeader
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(keyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))
}
