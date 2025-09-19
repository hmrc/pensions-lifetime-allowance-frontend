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

import auth.{AuthFunction, AuthFunctionImpl}
import common.Strings
import config._
import connectors.PLAConnector
import mocks.AuthMock
import models._
import models.amendModels._
import models.pla.response.ProtectionStatus.{Dormant, Open}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import scala.concurrent.{ExecutionContext, Future}

class AmendsRemovePensionSharingOrderControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit lazy val mockMessage: Messages =
    fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockPlaConnector: PLAConnector               = mock[PLAConnector]
  val mockMCC: MessagesControllerComponents        = fakeApplication().injector.instanceOf[MessagesControllerComponents]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val mockTechnicalError: technicalError           = app.injector.instanceOf[technicalError]
  val mockRemovePsoDebits: removePsoDebits         = app.injector.instanceOf[removePsoDebits]
  val mockEnv: Environment                         = mock[Environment]
  val messagesApi: MessagesApi                     = mockMCC.messagesApi

  implicit val mockAppConfig: FrontendAppConfig = fakeApplication().injector.instanceOf[FrontendAppConfig]
  implicit val mockPlaContext: PlaContext       = mock[PlaContext]
  implicit val system: ActorSystem              = ActorSystem()
  implicit val materializer: Materializer       = mock[Materializer]
  implicit val mockLang: Lang                   = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF       = app.injector.instanceOf[FormWithCSRF]
  implicit val ec: ExecutionContext             = app.injector.instanceOf[ExecutionContext]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockPlaConnector)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  class Setup {

    val authFunction = new AuthFunctionImpl(
      mockMCC,
      mockAuthConnector,
      mockTechnicalError
    )

    val controller = new AmendsRemovePensionSharingOrderController(
      mockSessionCacheService,
      mockPlaConnector,
      mockMCC,
      authFunction,
      mockTechnicalError,
      mockRemovePsoDebits
    )

  }

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "Removing a recently added PSO" when {

    val testProtectionSinglePsoList = ProtectionModel(
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    "there is no amend protection model fetched from cache" in new Setup {
      lazy val result = controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)
      status(result) shouldBe 500
    }

    "show the technical error page for existing protections" in new Setup {
      lazy val result   = controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
      jsoupDoc.body
        .getElementById("tryAgainLink")
        .attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections}"
    }

    "have the correct cache control" in new Setup {
      lazy val result =
        await(controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest))
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache"
    }

    "a valid amend protection model is fetched from cache" in new Setup {
      lazy val result = controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList))
      )
      status(result) shouldBe 200
    }

    "show the remove pso page with correct details" in new Setup {
      lazy val result   = controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")(fakeRequest)
      lazy val jsoupDoc = Jsoup.parse(contentAsString(result))

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](
        Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList))
      )

      jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
    }

    "return 500 if the an amend protection model could not be retrieved from cache" in new Setup {
      object DataItem
          extends AuthorisedFakeRequestToPost(
            controller.removePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")
          )
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      status(DataItem.result) shouldEqual 500
    }
  }

  "Choosing remove with a valid amend protection model" in new Setup {
    val ip2016Protection = ProtectionModel(
      psaCheckReference = Some("testPSARef"),
      uncrystallisedRights = Some(100000.00),
      nonUKRights = Some(2000.00),
      preADayPensionInPayment = Some(2000.00),
      postADayBenefitCrystallisationEvents = Some(2000.00),
      notificationId = Some(12),
      protectionID = Some(12345),
      protectionType = Some("IP2016"),
      status = Some(Open.toString),
      certificateDate = Some("2016-04-17"),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0))),
      protectedAmount = Some(1250000),
      protectionReference = Some("PSA123456")
    )

    val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)
    object DataItem
        extends AuthorisedFakeRequestToPost(
          controller.submitRemovePso(Strings.ProtectionTypeURL.IndividualProtection2016, "open")
        )

    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
    cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

    status(DataItem.result) shouldBe 303
    redirectLocation(DataItem.result) shouldBe Some(
      s"${routes.AmendsController.amendsSummary(Strings.ProtectionTypeURL.IndividualProtection2016, "open")}"
    )

  }

}
