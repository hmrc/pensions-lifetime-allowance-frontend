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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import auth.MockConfig
import com.kenshoo.play.metrics.PlayModule
import connectors.{KeyStoreConnector, PLAConnector}
import constructors.{DisplayConstructors, ResponseConstructors}
import models.{ExistingProtectionDisplayModel, ExistingProtectionsDisplayModel, ProtectionModel, TransformedReadResponseModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import mocks.AuthMock
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.http.HeaderNames.CACHE_CONTROL
import testHelpers.AuthorisedFakeRequestTo
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.http.HttpResponse
import testHelpers.MockTemplateRenderer
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future

class ReadProtectionsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with AuthMock {
  override def bindModules = Seq(new PlayModule)


  val testSuccessResponse = HttpResponse(200, Some(Json.parse("""{"thisJson":"doesNotMatter"}""")))
  val testMCNeededResponse = HttpResponse(423)
  val testUpstreamErrorResponse = HttpResponse(503)

  val testTransformedReadResponseModel = TransformedReadResponseModel(None, Seq.empty)
  val testExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(None, Seq.empty)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val mockKeystoreConnector = mock[KeyStoreConnector]

  trait BaseTestReadProtectionsController extends ReadProtectionsController {
    lazy val appConfig = MockConfig
    override lazy val authConnector = mockAuthConnector
    lazy val postSignInRedirectUrl = "urlUnimportant"

    override def config: Configuration = mock[Configuration]
    override def env: Environment = mock[Environment]

    override val keyStoreConnector = mockKeystoreConnector
    override val plaConnector = mock[PLAConnector]
    override val displayConstructors = mock[DisplayConstructors]
    override val responseConstructors = mock[ResponseConstructors]
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
  }

  val fakeRequest = FakeRequest()

  object TestReadProtectionsControllerUpstreamError extends BaseTestReadProtectionsController {
    when(plaConnector.readProtections(Matchers.any())(Matchers.any())).thenReturn(testUpstreamErrorResponse)
  }

  object TestReadProtectionsControllerMCNeeded extends BaseTestReadProtectionsController {
    when(plaConnector.readProtections(Matchers.any())(Matchers.any())).thenReturn(testMCNeededResponse)
  }

  object TestReadProtectionsControllerIncorrectResponseJson extends BaseTestReadProtectionsController {
    when(plaConnector.readProtections(Matchers.any())(Matchers.any())).thenReturn(testSuccessResponse)
    when(responseConstructors.createTransformedReadResponseModelFromJson(Matchers.any())).thenReturn(None)
  }

  object TestReadProtectionsControllerSuccess extends BaseTestReadProtectionsController {
    when(plaConnector.readProtections(Matchers.any())(Matchers.any())).thenReturn(testSuccessResponse)
    when(responseConstructors.createTransformedReadResponseModelFromJson(Matchers.any())).thenReturn(Some(testTransformedReadResponseModel))
    when(displayConstructors.createExistingProtectionsDisplayModel(Matchers.any())).thenReturn(testExistingProtectionsDisplayModel)
  }

  val ip2016Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-09-04T09:00:19.157"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val nonAmendableProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("rejected"),
    certificateDate = Some("2016-09-04T09:00:19.157"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val mockCacheMap = mock[CacheMap]
  def mockKeystoreSave: OngoingStubbing[Future[CacheMap]] = {
    when(mockKeystoreConnector.saveData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(mockCacheMap)
  }

  "Calling saveActiveProtection" should {

    "return a true" when {

      "provided with no protection model" in {
        await(TestReadProtectionsControllerSuccess.saveActiveProtection(None)(fakeRequest)) shouldBe true
      }

      "provided with a protection model" in {
        when(mockKeystoreConnector.saveData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(mock[CacheMap]))
        await(TestReadProtectionsControllerSuccess.saveActiveProtection(Some(ip2016Protection))(fakeRequest)) shouldBe true
      }
    }
  }

  "Calling getAmendableProtection" should {

    "return an empty sequence if no protections exist" in {
      val model = TransformedReadResponseModel(None, Seq())

      TestReadProtectionsControllerSuccess.getAmendableProtections(model) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))

      TestReadProtectionsControllerSuccess.getAmendableProtections(model) shouldBe Seq()
    }

    "return a single element if only the active protection is amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))

      TestReadProtectionsControllerSuccess.getAmendableProtections(model) shouldBe Seq(ip2016Protection)
    }

    "return all inactive elements if only they are amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))

      TestReadProtectionsControllerSuccess.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection)
    }

    "return all elements if they are all amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))

      TestReadProtectionsControllerSuccess.getAmendableProtections(model) shouldBe Seq(ip2016Protection, ip2016Protection, ip2016Protection)
    }
  }

  "Calling saveAmendableProtection" should {

    "return an empty sequence if no protections exist" in {
      val model = TransformedReadResponseModel(None, Seq())
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return a single cache map if only the active protection is amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
    }

    "return a cache map per inactive elements if only they are amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
    }

    "return a cache map per element if they are all amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap, mockCacheMap)
    }
  }

  "Calling getNonAmendableProtection" should {

    "return an empty sequence if no protections exist" in {
      val model = TransformedReadResponseModel(None, Seq())

      TestReadProtectionsControllerSuccess.getNonAmendableProtections(model) shouldBe Seq()
    }

    "return an empty sequence if all protections are amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))

      TestReadProtectionsControllerSuccess.getNonAmendableProtections(model) shouldBe Seq()
    }

    "return a single element if only the active protection is non-amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))

      TestReadProtectionsControllerSuccess.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection)
    }

    "return all inactive elements if only they are non-amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))

      TestReadProtectionsControllerSuccess.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection, nonAmendableProtection)
    }

    "return all elements if they are all non-amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))

      TestReadProtectionsControllerSuccess.getNonAmendableProtections(model) shouldBe Seq(nonAmendableProtection, nonAmendableProtection, nonAmendableProtection)
    }
  }

  "Calling saveNonAmendableProtection" should {

    "return an empty sequence if no protections exist" in {
      val model = TransformedReadResponseModel(None, Seq())
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return an empty sequence if no protections are amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(ip2016Protection, ip2016Protection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq()
    }

    "return a single cache map if only the active protection is amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(ip2016Protection, ip2016Protection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap)
    }

    "return a cache map per inactive elements if only they are amendable" in {
      val model = TransformedReadResponseModel(Some(ip2016Protection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap)
    }

    "return a cache map per element if they are all amendable" in {
      val model = TransformedReadResponseModel(Some(nonAmendableProtection), Seq(nonAmendableProtection, nonAmendableProtection))
      mockKeystoreSave
      await(TestReadProtectionsControllerSuccess.saveNonAmendableProtections(model)(fakeRequest)) shouldBe Seq(mockCacheMap, mockCacheMap, mockCacheMap)
    }
  }

  "Calling the currentProtections Action" when {

    "receiving an upstream error" should {

      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      lazy val result = await(TestReadProtectionsControllerUpstreamError.currentProtections(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      "return 500" in {
        status(result) shouldBe 500}
      "show the technical error page for existing protections" in {
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "receiving an MC needed response" should {

      lazy val result = await(TestReadProtectionsControllerMCNeeded.currentProtections(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))

      "return 423" in {
        status(result) shouldBe 423
      }
      "show the MC Needed page" in {
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
      }
    }

    "receiving incorrect json in the PLA response" should {
      lazy val result = await(TestReadProtectionsControllerIncorrectResponseJson.currentProtections(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 500" in {
        mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
        status(result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "receiving a correct response from PLA" should {
      mockAuthRetrieval[Option[String]](Retrievals.nino, Some("AB123456A"))
      lazy val result = await(TestReadProtectionsControllerSuccess.currentProtections(fakeRequest))
      lazy val jsoupDoc = Jsoup.parse(bodyOf(result))
      "return 200" in {
        status(result) shouldBe 200
      }
      "show the existing protections page" in {
        jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.existingProtections.pageHeading")
      }
    }


  }

}
