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

import java.time.LocalDate
import java.util.UUID

import auth.{MockAuthConnector, MockConfig}
import connectors.{PLAConnector, KeyStoreConnector}
import constructors.{ResponseConstructors, DisplayConstructors}
import enums.ApplicationType
import models._
import models.amendModels.AmendProtectionModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers.{AuthorisedFakeRequestTo, AuthorisedFakeRequestToPost}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AmendsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockKeyStoreConnector = mock[KeyStoreConnector]
  val mockDisplayConstructors = mock[DisplayConstructors]
  val mockPLAConnector = mock[PLAConnector]
  val mockResponseConstructors = mock[ResponseConstructors]

  val testIP16DormantModel = AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, protectionType = Some("IP2016"), status = Some("dormant"), relevantAmount = Some(100000), uncrystallisedRights = Some(100000)))

  object TestAmendsController extends AmendsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"

    override val displayConstructors: DisplayConstructors = mockDisplayConstructors
    override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    override val plaConnector: PLAConnector = mockPLAConnector
    override val responseConstructors: ResponseConstructors = mockResponseConstructors
  }

  val sessionId = UUID.randomUUID.toString
  val fakeRequest = FakeRequest()

  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

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
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)


  val ip2014Protection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(2000.00),
    preADayPensionInPayment = Some(2000.00),
    postADayBenefitCrystallisationEvents = Some(2000.00),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2014"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))

  val testAmendIP2014ProtectionModel = AmendProtectionModel(ip2014Protection, ip2014Protection)


  val ip2016NoDebitProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    notificationId = Some(12),
    protectionID = Some(12345),
    protectionType = Some("IP2016"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456"))
  val testAmendIP2016ProtectionModelWithNoDebit = AmendProtectionModel(ip2016NoDebitProtection, ip2016NoDebitProtection)

  val noNotificationIdProtection = ProtectionModel(
    psaCheckReference = Some("testPSARef"),
    protectionID = Some(12345),
    uncrystallisedRights = Some(100000.00),
    nonUKRights = Some(0.0),
    preADayPensionInPayment = Some(0.0),
    postADayBenefitCrystallisationEvents = Some(0.0),
    protectionType = Some("IP2014"),
    status = Some("dormant"),
    certificateDate = Some("2016-04-17"),
    protectedAmount = Some(1250000),
    protectionReference = Some("PSA123456")
  )

  val tstAmendDisplayModel = AmendDisplayModel(
    protectionType = "IP2014",
    amended = true,
    pensionContributionSections = Seq.empty,
    psoAdded = false,
    psoSections = Seq.empty,
    totalAmount = "£1,100,000"
  )

  val ip2014ActiveAmendmentProtection = ProtectionModel(
  psaCheckReference = Some("psaRef"),
  protectionID = Some(12345),
  notificationId = Some(33)
  )
  val tstActiveAmendResponseModel = AmendResponseModel(ip2014ActiveAmendmentProtection)
  val tstActiveAmendResponseDisplayModel = ActiveAmendResultDisplayModel(
    protectionType = ApplicationType.IP2014,
    notificationId = "33",
    protectedAmount = "£1,100,000",
    details = None
  )

  val ip2016InactiveAmendmentProtection = ProtectionModel(
    psaCheckReference = Some("psaRef"),
    protectionID = Some(12345),
    notificationId = Some(43)
  )
  val tstInactiveAmendResponseModel = AmendResponseModel(ip2016InactiveAmendmentProtection)
  val tstInactiveAmendResponseDisplayModel = InactiveAmendResultDisplayModel(
    notificationId = "43",
    additionalInfo = Seq.empty
  )


  def keystoreFetchCondition[T](data: Option[T]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))
  }


  "In AmendsController calling the amendsSummary action" when {
    "there is no stored amends model" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendsSummary("ip2016", "open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "there is a stored, updated amends model" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendsSummary("ip2014", "dormant"))
      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockDisplayConstructors.createAmendDisplayModel(Matchers.any())).thenReturn(tstAmendDisplayModel)
        status(DataItem.result) shouldBe 200
      }
      "show the amends page for an updated protection for IP2014" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amends.heading.IP2014.changed")
      }
    }
  }

  "Calling the amendProtection action" when {
    "the hidden fields in the amendment summary page have not been populated correctly" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionTypez", "stuff"))
      "return 500" in {
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "the microservice returns a conflict response" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPLAConnector.amendProtection(Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(409)))
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "the microservice returns a manual correspondence needed response" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))
      "return a Locked response" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPLAConnector.amendProtection(Matchers.any(), Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(423)))
        status(DataItem.result) shouldBe 423
      }

      "show the MC Needed page" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.mcNeeded.pageHeading")
      }
    }

    "the microservice returns an invalid json response" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPLAConnector.amendProtection(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
        when(mockResponseConstructors.createAmendResponseModelFromJson(Matchers.any())).thenReturn(None)
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "the microservice returns a response with no notificationId" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPLAConnector.amendProtection(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
        when(mockResponseConstructors.createAmendResponseModelFromJson(Matchers.any())).thenReturn(Some(AmendResponseModel(noNotificationIdProtection)))
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for no notification ID" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.noNotificationId.pageHeading")
        DataItem.jsoupDoc.body.getElementById("existingProtectionsLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "the microservice returns a valid response" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.amendProtection, ("protectionType", "IP2014"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        when(mockPLAConnector.amendProtection(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(HttpResponse(200, responseJson = Some(Json.parse("""{"result":"doesNotMatter"}""")))))
        when(mockResponseConstructors.createAmendResponseModelFromJson(Matchers.any())).thenReturn(Some(tstActiveAmendResponseModel))
        when(mockKeyStoreConnector.saveData(Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(CacheMap("keyStoreId", Map.empty)))
        status(DataItem.result) shouldBe 303
      }
      "redirect to amendment outcome" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendmentOutcome()}")
      }
    }

  }

  "Calling the amendmentOutcome action" when {
    "there is no outcome object stored in keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendmentOutcome())
      "return 500" in {
        keystoreFetchCondition[AmendResponseModel](None)
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "there is an active protection outcome in keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendmentOutcome())
      "return 200" in {
        keystoreFetchCondition[AmendResponseModel](Some(tstActiveAmendResponseModel))
        when(mockDisplayConstructors.createActiveAmendResponseDisplayModel(Matchers.any())).thenReturn(tstActiveAmendResponseDisplayModel)
        status(DataItem.result) shouldBe 200
      }

      "show the active amendment result page" in {
        DataItem.jsoupDoc.body.getElementById("amendmentOutcome").text shouldEqual Messages("amendResultCode.33.heading")
      }
    }

    "there is an inactive protection outcome in keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendmentOutcome())
      "return 200" in {
        keystoreFetchCondition[AmendResponseModel](Some(tstInactiveAmendResponseModel))
        when(mockDisplayConstructors.createInactiveAmendResponseDisplayModel(Matchers.any())).thenReturn(tstInactiveAmendResponseDisplayModel)
        status(DataItem.result) shouldBe 200
      }

      "show the inactive amendment result page" in {
        DataItem.jsoupDoc.body.getElementById("resultPageHeading").text shouldEqual Messages("amendResultCode.43.heading")
      }
    }

  }

  "Calling the .amendCurrentPensions action" when {

    "not supplied with a stored model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendCurrentPensions("ip2016", "open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "supplied with a stored test model (£100000, IP2016, dormant)" should {
      val testModel = new AmendProtectionModel(ProtectionModel(None, None), ProtectionModel(None, None, uncrystallisedRights = Some(100000)))
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendCurrentPensions("ip2016", "dormant"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testModel))
        status(DataItem.result) shouldBe 200
      }

      "take the user to the amend ip16 current pensions page" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the value 100000 completed in the amount input by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testModel))
          DataItem.jsoupDoc.body.getElementById("amendedUKPensionAmt").attr("value") shouldBe "100000"
        }
      }
    }

    "supplied with a stored test model (£100000, IP2014, dormant)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendCurrentPensions("ip2014", "dormant"))
      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 200
      }
    }
  }

  "Submitting Amend IP16 Current Pensions data" when {

    "amount is set as '100,000'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "100000"), ("protectionType", "ip2016"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testIP16DormantModel))
        status(DataItem.result) shouldBe 303
      }
      "redirect to Amends Summary page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }
    }

    "no amount is set" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", ""))
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.currentPensions.errorQuestion"))
      }
    }

    "amount is set as '5.001'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "5.001"))
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.currentPensions.errorDecimalPlaces"))
      }
    }

    "amount is set as '-25'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "-25"))
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.currentPensions.errorNegative"))
      }
    }

    "amount is set as '99999999999999.99'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "99999999999999.99"))
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.currentPensions.errorMaximum"))
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendCurrentPension, ("amendedUKPensionAmt", "1000000"), ("protectionType", "IP2016"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

  }

  "In AmendsController calling the .amendPensionsTakenBefore action" when {

    "not supplied with a stored model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBefore("ip2016", "open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }
    "supplied with the stored test model for (dormant, IP2016, preADay = £0.0)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBefore("ip2016", "dormant"))

      "have the value of the check box set as 'No' by default" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))
        DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBefore-no").attr("checked") shouldBe "checked"
      }
    }

    "supplied with the stored test model for (dormant, IP2016, preADay = £2000)" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBefore("ip2016", "dormant"))
      "return 200" in {

        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 200
      }

      "should take the user to the pensions taken before page" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBefore-yes").attr("checked") shouldBe "checked"
        }

        "have the value of the input field set to 2000 by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBeforeAmt").attr("value") shouldBe "2000"
        }
      }
    }

    "supplied with the stored test model for (dormant, IP2014, preADay = £2000)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBefore("ip2014", "dormant"))
      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 200
      }
    }
  }

  "Submitting Amend IP16 Pensions Taken Before data" when {

    "there is an error reading the form" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore)
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "'Have you taken pensions before 2006?' is checked to 'No'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }
      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }
    }

    "'Have you taken pensions before 2006?' is set to 'yes', and value set to 2000" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "2000"), ("protectionType", "ip2016"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    }

    "no amount is set" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBefore.errorQuestion"))
      }
    }

    "amount is set as '5.001'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "5.001"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
      }
    }

    "amount is set as '-25'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "-25"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBefore.errorNegative"))
      }
    }

    "amount is set as '99999999999999.99'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "99999999999999.99"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBefore.errorMaximum"))
      }
    }
  }

  "Submitting Amend IP14 Pensions Taken Before data" when {

    "there is an error reading the form" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore)
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2014"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "'Have you taken pensions before 2006?' is checked to 'No'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "no"), ("amendedPensionsTakenBeforeAmt", "0"), ("protectionType", "ip2014"), ("status", "dormant"))

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }
    }

    "'Have you taken pensions before 2006?' is set to 'yes', and value set to 2000" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBefore,
        ("amendedPensionsTakenBefore", "yes"), ("amendedPensionsTakenBeforeAmt", "2000"), ("protectionType", "ip2014"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }
    }
  }

  "In AmendsController calling the .amendPensionsTakenBetween action" when {
    "not supplied with a stored model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBetween("ip2016", "open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }
    "supplied with the stored test model for (dormant, IP2016, preADay = £0.0)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBetween("ip2016", "dormant"))

      "have the value of the check box set as 'No' by default" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))
        DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBetween-no").attr("checked") shouldBe "checked"
      }
    }

    "supplied with the stored test model for (dormant, IP2016, preADay = £2000)" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBetween("ip2016", "dormant"))
      "return 200" in {

        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 200
      }

      "should take the user to the pensions taken before page" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBetween-yes").attr("checked") shouldBe "checked"
        }

        "have the value of the input field set to 2000 by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedPensionsTakenBetweenAmt").attr("value") shouldBe "2000"
        }
      }
    }

    "supplied with the stored test model for (dormant, IP2014, preADay = £2000)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPensionsTakenBetween("ip2014", "dormant"))
      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 200
      }
    }

  }

  "Submitting Amend IP16 Pensions Taken Between data" when {

    "there is an error reading the form" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween)
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "'Have you taken pensions before 2006?' is checked to 'No'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }
      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }
    }

    "'Before 5 April 1016, did you ...?' is set to 'yes', and value set to 2000" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "2000"), ("protectionType", "ip2016"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    }

    "no amount is set" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBetween.errorQuestion"))
      }
    }

    "amount is set as '5.001'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "5.001"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBetween.errorDecimalPlaces"))
      }
    }

    "amount is set as '-25'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "-25"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBetween.errorNegative"))
      }
    }

    "amount is set as '99999999999999.99'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "99999999999999.99"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.pensionsTakenBetween.errorMaximum"))
      }
    }
  }

  "Submitting Amend IP14 Pensions Taken Between data" when {

    "there is an error reading the form" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween)
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2014"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "'Have you taken pensions before 2006?' is checked to 'No'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "no"), ("amendedPensionsTakenBetweenAmt", "0"), ("protectionType", "ip2014"), ("status", "dormant"))

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }
    }

    "'Have you taken pensions before 2006?' is set to 'yes', and value set to 2000" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPensionsTakenBetween,
        ("amendedPensionsTakenBetween", "yes"), ("amendedPensionsTakenBetweenAmt", "2000"), ("protectionType", "ip2014"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "dormant")}")
      }
    }
  }

  "In AmendsController calling the .amendOverseasPensions action" when {

    "not supplied with a stored model" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendOverseasPensions("ip2016", "open"))
      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }
    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £0.0)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendOverseasPensions("ip2016", "dormant"))

      "have the value of the check box set as 'No' by default" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModelWithNoDebit))
        DataItem.jsoupDoc.body.getElementById("amendedOverseasPensions-no").attr("checked") shouldBe "checked"
      }
    }

    "supplied with the stored test model for (dormant, IP2016, nonUKRights = £2000)" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendOverseasPensions("ip2016", "dormant"))
      "return 200" in {

        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 200
      }

      "should take the user to the overseas pensions page" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.pageHeading")
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          contentType(DataItem.result) shouldBe Some("text/html")
          charset(DataItem.result) shouldBe Some("utf-8")
        }

        "have the value of the check box set as 'Yes' by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedOverseasPensions-yes").attr("checked") shouldBe "checked"
        }

        "have the value of the input field set to 2000 by default" in {
          keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
          DataItem.jsoupDoc.body.getElementById("amendedOverseasPensionsAmt").attr("value") shouldBe "2000"
        }
      }
    }

    "supplied with the stored test model for (dormant, IP2014, nonUKRights = £2000)" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendOverseasPensions("ip2014", "dormant"))
      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 200
      }
    }
  }


  "Submitting Amend IP16 Overseas Pensions data" when {

    "there is an error reading the form" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions)
      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
    }

    "the model can't be fetched from keyStore" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
    }

    "'Have you put money in an overseas pension?' is checked to 'No'" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "no"), ("amendedOverseasPensionsAmt", "0"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }
      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }
    }

    "'Have you put money in an overseas pension?' is checked to 'Yes', and value set to 2000" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "2000"), ("protectionType", "ip2016"), ("status", "dormant"))
      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to Amends Summary Page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "dormant")}")
      }

    }

    "no amount is set" should {
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", ""), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.overseasPensions.errorQuestion"))
      }
    }

    "amount is set as '5.001'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "5.001"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.overseasPensions.errorDecimalPlaces"))
      }
    }

    "amount is set as '-25'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "-25"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.overseasPensions.errorNegative"))
      }
    }

    "amount is set as '99999999999999.99'" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendOverseasPensions,
        ("amendedOverseasPensions", "yes"), ("amendedOverseasPensionsAmt", "99999999999999.99"), ("protectionType", "ip2016"), ("status", "dormant"))

      "return 400" in {
        status(DataItem.result) shouldBe 400
      }
      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include(Messages("pla.overseasPensions.errorMaximum"))
      }
    }
  }

  "Calling the amendPsoDetails action" when {

    val testProtectionNoPsoList = ProtectionModel (
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = None
    )

    val testProtectionEmptyPsoList = ProtectionModel (
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List.empty)
    )

    val testProtectionSinglePsoList = ProtectionModel (
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    val testProtectionMultiplePsoList = ProtectionModel (
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0), PensionDebitModel("2016-12-27", 11322.75)))
    )

    "there is no amendment model fetched from keystore" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPsoDetails("ip2014", "open"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "there is no PSO list stored in the AmendProtectionModel" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPsoDetails("ip2014", "open"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionNoPsoList, testProtectionNoPsoList)))
        status(DataItem.result) shouldBe 200
      }

      "show the amend PSO details page with no data completed" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amendPsoDetails.pageHeading")
        DataItem.jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual ""
        DataItem.jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual ""
        DataItem.jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual ""
      }
    }

    "there is an empty PSO list stored in the AmendProtectionModel" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPsoDetails("ip2016", "open"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionEmptyPsoList, testProtectionEmptyPsoList)))
        status(DataItem.result) shouldBe 200
      }

      "show the amend PSO details page with no data completed" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amendPsoDetails.pageHeading")
        DataItem.jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual ""
        DataItem.jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual ""
        DataItem.jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual ""
      }
    }

    "there is a PSO list of one PSO stored in the AmendProtectionModel" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPsoDetails("ip2016", "open"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList)))
        status(DataItem.result) shouldBe 200
      }

      "show the amend PSO details page with the correct data completed" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.amendPsoDetails.pageHeading")
        DataItem.jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual "23"
        DataItem.jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual "12"
        DataItem.jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual "2016"
        DataItem.jsoupDoc.body.getElementById("psoAmt").attr("value") shouldEqual "1000"
      }
    }

    "there is a PSO list of more then one PSO stored in the AmendProtectionModel" should {

      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.amendPsoDetails("ip2016", "open"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionMultiplePsoList, testProtectionMultiplePsoList)))
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }
  }

  "Submitting Amend PSOs data" when {

    "submitting a valid IP14 PSO's details on first valid day" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "6"),
        ("psoMonth", "4"),
        ("psoYear", "2014"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2014ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to the amends summary action for open IP 2014" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2014", "open")}")
      }
    }

    "submitting a valid IP16 PSO's details on first valid day" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "6"),
        ("psoMonth", "4"),
        ("psoYear", "2016"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2016"),
        ("status", "open"),
        ("existingPSO", "true")
      )

      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect to the amends summary action for open IP 2016" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")
      }
    }

    "submitting an invalid set of PSO details - missing day" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", ""),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.dayEmpty"))
      }
    }

    "submitting an invalid set of PSO details - missing month" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", ""),
        ("psoYear", "2015"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.monthEmpty"))
      }
    }

    "submitting an invalid set of PSO details - missing year" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", "1"),
        ("psoYear", ""),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.yearEmpty"))
      }
    }

    "submitting an invalid set of PSO details - invalid date" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "29"),
        ("psoMonth", "2"),
        ("psoYear", "2015"),
        ("psoAmt", "100000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.invalidDate"))
      }
    }

    "submitting an invalid set of PSO details - date before 6 April 2014 for ip14" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "5"),
        ("psoMonth", "4"),
        ("psoYear", "2014"),
        ("psoAmt", "1000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP14PsoDetails.errorDateOutOfRange"))
      }
    }

    "submitting an invalid set of PSO details - date before 6 April 2016 for ip16" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "5"),
        ("psoMonth", "4"),
        ("psoYear", "2016"),
        ("psoAmt", "1000"),
        ("protectionType", "ip2016"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP16PsoDetails.errorDateOutOfRange"))
      }
    }

    "submitting an invalid set of PSO details - date in future for IP2016" should {

      val tomorrow = LocalDate.now.plusDays(1)
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", tomorrow.getDayOfMonth.toString),
        ("psoMonth", tomorrow.getMonthValue.toString),
        ("psoYear", tomorrow.getYear.toString),
        ("psoAmt", "1000"),
        ("protectionType", "ip2016"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP16PsoDetails.errorDateOutOfRange"))
      }
    }

    "submitting an invalid set of PSO details - date in future for IP2014" should {

      val tomorrow = LocalDate.now.plusDays(1)
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", tomorrow.getDayOfMonth.toString),
        ("psoMonth", tomorrow.getMonthValue.toString),
        ("psoYear", tomorrow.getYear.toString),
        ("psoAmt", "1000"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP14PsoDetails.errorDateOutOfRange"))
      }
    }

    "submitting an invalid set of PSO details - missing PSO amount" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", ""),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("error.real"))
      }
    }

    "submitting an invalid set of PSO details - amount negative" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "-1"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorNegative"))
      }
    }

    "submitting an invalid set of PSO details - amount too many decimal places" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "0.001"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorDecimalPlaces"))
      }
    }

    "submitting an invalid set of PSO details - amount too large" should {

      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitAmendPsoDetails,
        ("psoDay", "1"),
        ("psoMonth", "1"),
        ("psoYear", "2015"),
        ("psoAmt", "999999999999999"),
        ("protectionType", "ip2014"),
        ("status", "open"),
        ("existingPSO", "true")
      )
      "return 400" in { status(DataItem.result) shouldBe 400 }

      "fail with the correct error message" in {
        DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorMaximum"))
      }
    }
  }

  "Removing a recently added PSO" when {

    val testProtectionSinglePsoList = ProtectionModel (
      psaCheckReference = Some("psaRef"),
      protectionID = Some(1234),
      pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0)))
    )

    "there is no amend protection model fetched from keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.removePso("ip2016", "open"))

      "return 500" in {
        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldBe 500
      }
      "show the technical error page for existing protections" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.techError.pageHeading")
        DataItem.jsoupDoc.body.getElementById("tryAgainLink").attr("href") shouldEqual s"${controllers.routes.ReadProtectionsController.currentProtections()}"
      }
      "have the correct cache control" in {DataItem.result.header.headers.getOrElse(CACHE_CONTROL, "No-Cache-Control-Header-Set") shouldBe "no-cache" }
    }

    "a valid amend protection model is fetched from keystore" should {
      object DataItem extends AuthorisedFakeRequestTo(TestAmendsController.removePso("ip2016", "open"))

      "return 200" in {
        keystoreFetchCondition[AmendProtectionModel](Some(AmendProtectionModel(testProtectionSinglePsoList, testProtectionSinglePsoList)))
        status(DataItem.result) shouldBe 200
      }

      "show the remove pso page with correct details" in {
        DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
        DataItem.jsoupDoc.body.getElementById("protectionType").`val`() shouldEqual "ip2016"
        DataItem.jsoupDoc.body.getElementById("status").`val`() shouldEqual "open"
      }
    }

    "choosing remove on the remove page" should {

      "return 400 if the hidden form details were incorrect" in {
        object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitRemovePso)
        status(DataItem.result) shouldEqual 400
      }

      "return 500 if the an amend protection model could not be retrieved from keystore" in {
        object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitRemovePso,
          ("protectionType", "ip2016"),
          ("status", "open")
        )

        keystoreFetchCondition[AmendProtectionModel](None)
        status(DataItem.result) shouldEqual 500
      }

    }

    "Choosing remove with a valid amend protection model" should {
      val ip2016Protection = ProtectionModel(
        psaCheckReference = Some("testPSARef"),
        uncrystallisedRights = Some(100000.00),
        nonUKRights = Some(2000.00),
        preADayPensionInPayment = Some(2000.00),
        postADayBenefitCrystallisationEvents = Some(2000.00),
        notificationId = Some(12),
        protectionID = Some(12345),
        protectionType = Some("IP2016"),
        status = Some("open"),
        certificateDate = Some("2016-04-17"),
        pensionDebits = Some(List(PensionDebitModel("2016-12-23", 1000.0))),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"))

      val testAmendIP2016ProtectionModel = AmendProtectionModel(ip2016Protection, ip2016Protection)
      object DataItem extends AuthorisedFakeRequestToPost(TestAmendsController.submitRemovePso, ("protectionType", "ip2016"), ("status", "open"))


      "return 303" in {
        keystoreFetchCondition[AmendProtectionModel](Some(testAmendIP2016ProtectionModel))
        status(DataItem.result) shouldBe 303
      }

      "redirect location should be the amends summary page" in {
        redirectLocation(DataItem.result) shouldBe Some(s"${routes.AmendsController.amendsSummary("ip2016", "open")}")
      }
    }

    "choosing cancel on the remove page" should {}
  }
}
