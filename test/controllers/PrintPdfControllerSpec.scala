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

import connectors.{KeyStoreConnector, PdfGeneratorConnector}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import play.api.test.Helpers._
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.Result

import scala.concurrent.Future


class PrintPdfControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite{

  implicit override lazy val app: Application = new GuiceApplicationBuilder().
    disable[com.kenshoo.play.metrics.PlayModule].build()
  implicit val hc = HeaderCarrier()

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]


  private val mockKeyStoreConnector = mock[KeyStoreConnector]
  private val mockPdfGeneratorConnector = mock[PdfGeneratorConnector]
  private val sessionId = SessionKeys.sessionId -> "pdf-test"

  object TestController extends PrintPdfController {
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    override val pdfGeneratorConnector: PdfGeneratorConnector = mockPdfGeneratorConnector
    override def createPdfResult(response: WSResponse): Result = Ok("its a me, peedeeef")

    val lookupRequestID = "psa-lookup-request"
    val lookupResultID = "psa-lookup-result"
  }

  "printResultsPDF" should{
      "return a 303 (redirect) when unable to print a Results PDF" in{
        val request = FakeRequest().withSession(sessionId)
        keystoreFetchCondition[PSALookupRequest](None)
        val result = TestController.printResultsPDF.apply(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
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
        val response : Future[WSResponse] = ws.url("http://somewhereovertherainbow.com").get()

        keystoreFetchCondition[PSALookupResult](Option(testPsaLookupResult))
        when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

        val result = TestController.printResultsPDF.apply(request)
        val returnedHeader = Map("Set-Cookie" -> "mdtp=082c4de4d57fc34fa1e28c56e2e36b34519bcbb7-sessionId=pdf-test; Path=/; HTTPOnly", "Content-Disposition" -> "attachment; filename=lookup-result-IP14000000000A.pdf")

        status(result) mustBe OK
        headers(result) mustBe returnedHeader
      }
  }

  "printNotFoundPDF" should{
    "return a 303 (redirect) when unable to print a NotFound PDF" in{
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)
      val result = TestController.printNotFoundPDF.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return a 303 when unable to print a NotFound PDF when the lifetimeAllowanceReference is NOT defined" in{
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = None
      )

      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      val result = TestController.printNotFoundPDF.apply(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).get mustBe routes.LookupController.displaySchemeAdministratorReferenceForm().url
    }

    "return a 200 when able to print a NotFound PDF when the lifetimeAllowanceReference is defined" in{
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = Some("IP14000000000A")
      )

      val request = FakeRequest().withSession(sessionId)
      val response : Future[WSResponse] = ws.url("http://somewhereovertherainbow.com").get()

      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

      val result = TestController.printNotFoundPDF.apply(request)
      val returnedHeader = Map("Set-Cookie" -> "mdtp=082c4de4d57fc34fa1e28c56e2e36b34519bcbb7-sessionId=pdf-test; Path=/; HTTPOnly", "Content-Disposition" -> "attachment; filename=lookup-not-found.pdf")

      status(result) mustBe OK
      headers(result) mustBe returnedHeader
    }
  }

  def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
    .thenReturn(Future.successful(data))
}
