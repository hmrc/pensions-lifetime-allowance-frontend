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
import mocks.AuthMock
import models._
import models.amend.AmendProtectionModel
import models.pla.response.ProtectionStatus.Dormant
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SessionCacheService
import testHelpers._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF
import views.html.pages.amends._
import views.html.pages.fallback.technicalError

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AmendsPensionTakenBetweenControllerSpec
    extends FakeApplication
    with MockitoSugar
    with SessionCacheTestHelper
    with BeforeAndAfterEach
    with AuthMock
    with I18nSupport {

  implicit val fakeRequest: FakeRequest[AnyContent] = FakeRequest()

  val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]
  val messagesApi: MessagesApi          = mcc.messagesApi

  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  implicit val appConfig: FrontendAppConfig   = inject[FrontendAppConfig]
  implicit val system: ActorSystem            = ActorSystem()
  implicit val mockMaterializer: Materializer = mock[Materializer]
  implicit val mockLang: Lang                 = mock[Lang]
  implicit val formWithCSRF: FormWithCSRF     = inject[FormWithCSRF]
  implicit val ec: ExecutionContext           = inject[ExecutionContext]

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockAuthFunction: AuthFunction               = mock[AuthFunction]
  val technicalErrorView: technicalError           = inject[technicalError]

  val amendIP16PensionsTakenBetweenView: amendIP16PensionsTakenBetween =
    inject[amendIP16PensionsTakenBetween]

  val amendIP14PensionsTakenBetweenView: amendIP14PensionsTakenBetween =
    inject[amendIP14PensionsTakenBetween]

  val mockEnv: Environment = mock[Environment]

  override def beforeEach(): Unit = {
    reset(mockSessionCacheService)
    reset(mockAuthConnector)
    reset(mockEnv)
    super.beforeEach()
  }

  val authFunction = new AuthFunctionImpl(
    mcc,
    mockAuthConnector,
    technicalErrorView
  )

  val controller = new AmendsPensionTakenBetweenController(
    mockSessionCacheService,
    mcc,
    authFunction,
    technicalErrorView,
    amendIP16PensionsTakenBetweenView,
    amendIP14PensionsTakenBetweenView
  )

  val sessionId: String  = UUID.randomUUID.toString
  val mockUsername       = "mockuser"
  val mockUserId: String = "/auth/oid/" + mockUsername

  val individualProtection2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016ProtectionModel =
    AmendProtectionModel(individualProtection2016Protection, individualProtection2016Protection)

  val individualProtection2016LTAProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2016LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016LTAProtectionModel =
    AmendProtectionModel(individualProtection2016LTAProtection, individualProtection2016LTAProtection)

  val individualProtection2014Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2014"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2014ProtectionModel =
    AmendProtectionModel(individualProtection2014Protection, individualProtection2014Protection)

  val individualProtection2014LTAProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2014LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2014LTAProtectionModel =
    AmendProtectionModel(individualProtection2014LTAProtection, individualProtection2014LTAProtection)

  val individualProtection2016NoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2016"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016ProtectionModelWithNoDebit =
    AmendProtectionModel(individualProtection2016NoDebitProtection, individualProtection2016NoDebitProtection)

  val individualProtection2016LTANoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    identifier = Some(12345),
    protectionType = Some("IndividualProtection2016LTA"),
    status = Some(Dormant.toString),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val testAmendIndividualProtection2016LTAProtectionModelWithNoDebit =
    AmendProtectionModel(individualProtection2016LTANoDebitProtection, individualProtection2016LTANoDebitProtection)

  def cacheFetchCondition[T](data: Option[T]): Unit =
    when(mockSessionCacheService.fetchAndGetFormData[T](anyString())(any(), any()))
      .thenReturn(Future.successful(data))

  "In AmendsPensionTakenBetweenController calling the .amendPensionsTakenBetween action" when {
    "not supplied with a stored model" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result: Future[Result] =
        controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "open")(fakeRequest)

      status(result) shouldBe 500

    }
    "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £0.0)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModelWithNoDebit))

      val result: Future[Result] =
        controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementById("amendedPensionsTakenBetween").attr("class") shouldBe "govuk-radios__input"
    }

    "supplied with the stored test model for (dormant, IndividualProtection2016LTA, preADay = £0.0)" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModelWithNoDebit))

      val result: Future[Result] =
        controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant")(
          fakeRequest
        )
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementById("amendedPensionsTakenBetween").attr("class") shouldBe "govuk-radios__input"
    }
  }

  "supplied with the stored test model for (dormant, IndividualProtection2016, preADay = £2000)" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

    val result: Future[Result] =
      controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")(fakeRequest)

    status(result) shouldBe 200
  }

  "supplied with the stored test model for (dormant, IndividualProtection2016LTA, preADay = £2000)" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModel))

    val result: Future[Result] =
      controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant")(
        fakeRequest
      )

    status(result) shouldBe 200
  }

  "should take the user to the pensions taken before page" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

    val result: Future[Result] =
      controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")(fakeRequest)
    val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

    jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.title")
  }

  "return some HTML that" should {

    "contain some text and use the character set utf-8" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

      val result: Future[Result] =
        controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")(fakeRequest)

      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "have the value of the check box set as 'Yes' by default" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))

      val result: Future[Result] =
        controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")(fakeRequest)
      val jsoupDoc: Document = Jsoup.parse(contentAsString(result))

      jsoupDoc.body.getElementById("amendedPensionsTakenBetween").attr("class") shouldBe "govuk-radios__input"
      jsoupDoc.body.getElementById("amendedPensionsTakenBetween").attr("value") shouldBe "yes"
    }
  }

  "supplied with the stored test model for (dormant, IndividualProtection2014, preADay = £2000))" in {
    mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
    cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))

    val result: Future[Result] =
      controller.amendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant")(fakeRequest)

    status(result) shouldBe 200
  }

  "Submitting Amend IndividualProtection2016 Pensions Taken Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeUrl.IndividualProtection2016,
          Strings.StatusUrl.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant"),
        ("amendedPensionsTakenBetween", "yes")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsPensionUsedBetweenController.amendPensionsUsedBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant")}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant"),
        ("protectionType", Strings.ProtectionTypeUrl.IndividualProtection2016),
        ("status", "dormant")
      )

      status(result) shouldBe 400
    }

    "the data is invalid on additional validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016, "dormant"),
        ("amendedPensionsTakenBetween", "1")
      )

      status(result) shouldBe 400
    }
  }

  "Submitting Amend IndividualProtection2016LTA Pensions Taken Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeUrl.IndividualProtection2016LTA,
          Strings.StatusUrl.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2016LTAProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant"),
        ("amendedPensionsTakenBetween", "yes")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsPensionUsedBetweenController.amendPensionsUsedBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant")}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant"),
        ("protectionType", Strings.ProtectionTypeUrl.IndividualProtection2016LTA),
        ("status", "dormant")
      )

      status(result) shouldBe 400
    }

    "the data is invalid on additional validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2016LTA, "dormant"),
        ("amendedPensionsTakenBetween", "1")
      )

      status(result) shouldBe 400
    }
  }

  "Submitting Amend IndividualProtection2014 Pensions Taken Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeUrl.IndividualProtection2014,
          Strings.StatusUrl.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014ProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant"),
        ("amendedPensionsTakenBetween", "yes")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsPensionUsedBetweenController.amendPensionsUsedBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant")}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant"),
        ("protectionType", Strings.ProtectionTypeUrl.IndividualProtection2016),
        ("status", "dormant")
      )

      status(result) shouldBe 400
    }

    "the data is invalid on additional validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller.submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014, "dormant"),
        ("amendedPensionsTakenBetween", "1")
      )

      status(result) shouldBe 400
    }
  }

  "Submitting Amend IndividualProtection2014LTA Pensions Taken Between data" when {

    "the model can't be fetched from cache" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](None)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 500
    }

    "the data is valid with a no response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014LTAProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant"),
        ("amendedPensionsTakenBetween", "no")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(s"${routes.AmendsController.amendsSummary(
          Strings.ProtectionTypeUrl.IndividualProtection2014LTA,
          Strings.StatusUrl.Dormant
        )}")
    }

    "the data is valid with a yes response" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      cacheFetchCondition[AmendProtectionModel](Some(testAmendIndividualProtection2014LTAProtectionModel))
      cacheSaveCondition[AmendProtectionModel](mockSessionCacheService)

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant"),
        ("amendedPensionsTakenBetween", "yes")
      )

      status(result) shouldBe 303

      redirectLocation(result) shouldBe Some(
        s"${routes.AmendsPensionUsedBetweenController.amendPensionsUsedBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant")}"
      )
    }

    "the data is invalid" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant"),
        ("protectionType", Strings.ProtectionTypeUrl.IndividualProtection2016),
        ("status", "dormant")
      )

      status(result) shouldBe 400
    }

    "the data is invalid on additional validation" in {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))

      val result = FakeRequests.authorisedPost(
        controller
          .submitAmendPensionsTakenBetween(Strings.ProtectionTypeUrl.IndividualProtection2014LTA, "dormant"),
        ("amendedPensionsTakenBetween", "1")
      )

      status(result) shouldBe 400
    }
  }

}
