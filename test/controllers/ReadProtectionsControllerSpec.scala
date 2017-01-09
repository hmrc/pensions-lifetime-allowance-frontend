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

import auth.{MockAuthConnector, MockConfig}
import connectors.{PLAConnector, KeyStoreConnector}
import constructors.{ResponseConstructors, DisplayConstructors}
import models.{ExistingProtectionsDisplayModel, ExistingProtectionDisplayModel, TransformedReadResponseModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.http.HeaderNames.CACHE_CONTROL
import testHelpers.AuthorisedFakeRequestTo
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}

class ReadProtectionsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {


  val testSuccessResponse = HttpResponse(200, Some(Json.parse("""{"thisJson":"doesNotMatter"}""")))
  val testMCNeededResponse = HttpResponse(423)
  val testUpstreamErrorResponse = HttpResponse(503)

  val testTransformedReadResponseModel = TransformedReadResponseModel(None, Seq.empty)
  val testExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(None, Seq.empty)


  trait BaseTestReadProtectionsController extends ReadProtectionsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "urlUnimportant"

    override val keyStoreConnector = mock[KeyStoreConnector]
    override val plaConnector = mock[PLAConnector]
    override val displayConstructors = mock[DisplayConstructors]
    override val responseConstructors = mock[ResponseConstructors]
  }

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

  "Calling the currentProtections Action" when {

    "receiving an upstream error" should {
      object DataItem extends AuthorisedFakeRequestTo(TestReadProtectionsControllerUpstreamError.currentProtections)
      "return 500" in {status(DataItem.result) shouldBe 500}
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "receiving an MC needed response" should {
      object DataItem extends AuthorisedFakeRequestTo(TestReadProtectionsControllerMCNeeded.currentProtections)
      "return 423" in {
        status(DataItem.result) shouldBe 423
      }
      "show the MC Needed page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
      }
    }

    "receiving incorrect json in the PLA response" should {
      object DataItem extends AuthorisedFakeRequestTo(TestReadProtectionsControllerIncorrectResponseJson.currentProtections)
      "return 500" in {status(DataItem.result) shouldBe 500}
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "receiving a correct response from PLA" should {
      object DataItem extends AuthorisedFakeRequestTo(TestReadProtectionsControllerSuccess.currentProtections)
      "return 200" in {
        status(DataItem.result) shouldBe 200
      }
      "show the existing protections page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.existingProtections.pageHeading")
      }
    }


  }

}
