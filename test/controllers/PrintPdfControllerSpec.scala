/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.Materializer
import config.wiring.PlaFormPartialRetriever
import config.{FrontendAppConfig, LocalTemplateRenderer}
import connectors.{KeyStoreConnector, PdfGeneratorConnector}
import models.{PSALookupRequest, PSALookupResult}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.ahc.cache.CacheableHttpResponseStatus
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.shaded.ahc.org.asynchttpclient.Response
import play.shaded.ahc.org.asynchttpclient.uri.Uri
import testHelpers.{FakeApplication, MockTemplateRenderer}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import utils.ActionWithSessionId

import scala.concurrent.{ExecutionContext, Future}

class PrintPdfControllerSpec extends FakeApplication with MockitoSugar with BeforeAndAfterEach {


  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  lazy val ws: WSClient = fakeApplication.injector.instanceOf[WSClient]

  val mockPdfGeneratorConnector: PdfGeneratorConnector = mock[PdfGeneratorConnector]
  private val sessionId = SessionKeys.sessionId -> "pdf-test"
  val mockKeyStoreConnector: KeyStoreConnector = mock[KeyStoreConnector]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val mockActionWithSessionId: ActionWithSessionId = fakeApplication.injector.instanceOf[ActionWithSessionId]
  val mockEnv: Environment = mock[Environment]
  val response: Future[WSResponse] = {
    val responseBuilder = new Response.ResponseBuilder()
    responseBuilder.accumulate(new CacheableHttpResponseStatus(Uri.create("http://testsite.com"), OK, "OK", ""))
    Future(new AhcWSResponse(responseBuilder.build()))
  }

  implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
  implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]
  implicit val mockAppConfig = fakeApplication.injector.instanceOf[FrontendAppConfig]
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = mock[Materializer]

  class Setup {

    val controller = new PrintPdfController(
      mockKeyStoreConnector,
      mockActionWithSessionId,
      mockPdfGeneratorConnector,
      mockMCC) {
      override val lookupRequestID = "psa-lookup-request"
      override val lookupResultID = "psa-lookup-result"
    }
  }

  "printResultsPDF" should {
    "return a 303 (redirect) when unable to print a Results PDF" in new Setup {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)
      val result = controller.printResultsPDF.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "return a 200 when able to print a Results PDF" in new Setup {
      val testPsaLookupResult = PSALookupResult(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        ltaType = 5,
        psaCheckResult = 1,
        protectedAmount = Some(BigDecimal(25000)),
        protectionNotificationNumber = Some("IP14000000000A")
      )



      val request = FakeRequest().withSession(sessionId)

      keystoreFetchCondition[PSALookupResult](Option(testPsaLookupResult))
      when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

      val result = controller.printResultsPDF.apply(request)
      val returnedHeader = Map("Content-Disposition" -> "attachment; filename=lookup-result-IP14000000000A.pdf")

      status(result) shouldBe OK
      headers(result) shouldBe returnedHeader
    }
  }


  "printNotFoundPDF" should {
    "return a 303 (redirect) when unable to print a NotFound PDF" in new Setup {
      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](None)
      val result = controller.printNotFoundPDF.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "return a 303 when unable to print a NotFound PDF when the lifetimeAllowanceReference is NOT defined" in new Setup {
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = None
      )

      val request = FakeRequest().withSession(sessionId)
      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      val result = controller.printNotFoundPDF.apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get shouldBe routes.LookupController.displaySchemeAdministratorReferenceForm.url
    }

    "return a 200 when able to print a NotFound PDF when the lifetimeAllowanceReference is defined" in new Setup {
      val testPsaLookupRequest = PSALookupRequest(
        pensionSchemeAdministratorCheckReference = "PSA12345678A",
        lifetimeAllowanceReference = Some("IP14000000000A")
      )

      val request = FakeRequest().withSession(sessionId)

      keystoreFetchCondition[PSALookupRequest](Option(testPsaLookupRequest))
      when(mockPdfGeneratorConnector.generatePdf(any())).thenReturn(response)

      val result = controller.printNotFoundPDF.apply(request)
      val returnedHeader = Map("Content-Disposition" -> "attachment; filename=lookup-not-found.pdf")


      status(result) shouldBe OK
      headers(result) shouldBe returnedHeader
    }
  }


    def keystoreFetchCondition[T](data: Option[T]): Unit = when(mockKeyStoreConnector.fetchAndGetFormData[T](any())(any(), any()))
      .thenReturn(Future.successful(data))

  }

