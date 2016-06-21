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

import java.util.UUID
import connectors.KeyStoreConnector
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import testHelpers._
import org.mockito.Matchers
import org.mockito.Mockito._
import scala.concurrent.Future
import config.{FrontendAppConfig,FrontendAuthConnector}
import models._
import auth._

class IP2014ControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

    val mockKeyStoreConnector = mock[KeyStoreConnector]

    object TestIP2014Controller extends IP2014Controller {
        override lazy val applicationConfig = FrontendAppConfig
        override lazy val authConnector = MockAuthConnector
        override lazy val postSignInRedirectUrl = "http://localhost:9012/protect-your-lifetime-allowance/apply-ip"
        override val keyStoreConnector: KeyStoreConnector = mockKeyStoreConnector
    }

    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest()

    val mockUsername = "mockuser"
    val mockUserId = "/auth/oid/" + mockUsername

    def keystoreFetchCondition[T](data: Option[T]): Unit = {
        when(mockKeyStoreConnector.fetchAndGetFormData[T](Matchers.anyString())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(data))
    }

    def psoNumKeystoreSetup(data: Option[NumberOfPSOsModel]) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel](Matchers.eq("numberOfPSOs"))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(data))
    }

    def psoDeetsKeystoreSetup(data: Option[PSODetailsModel], deetsNum: Int) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PSODetailsModel](Matchers.eq(s"psoDetails$deetsNum"))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(data))
    }



    ///////////////////////////////////////////////
    // Initial Setup
    ///////////////////////////////////////////////
    "IP2014Controller should be correctly initialised" in {
        IP2014Controller.keyStoreConnector shouldBe KeyStoreConnector
        IP2014Controller.authConnector shouldBe FrontendAuthConnector
    }

    ///////////////////////////////////////////////
    // IP14 Pensions Taken
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14PensionsTaken action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTaken)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 pensions taken page" in {
                keystoreFetchCondition[PensionsTakenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTaken.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenModel(Some("yes"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTaken)

            "return 200" in {
                keystoreFetchCondition[PensionsTakenModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken page" in {
                keystoreFetchCondition[PensionsTakenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTaken.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PensionsTakenModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    keystoreFetchCondition[PensionsTakenModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionsTaken-yes").parent.classNames().contains("selected") shouldBe true
                }
            }
        }
    }

    "Submitting Pensions Taken data" when {

        "Submitting 'yes' in pensionsTakenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTaken, ("pensionsTaken", "yes"))
            "return 303" in {status(DataItem.result) shouldBe 303}
            "redirect to ip14 pensions taken before" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PensionsTakenBefore()}") }
        }

        "Submitting 'no' in pensionsTakenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTaken, ("pensionsTaken", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to ip14 overseas pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14OverseasPensions()}") }
        }

        "Submitting pensionsTakenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTaken, ("pensionsTaken", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTaken.mandatoryErr"))
            }
        }
    }


    ///////////////////////////////////////////////
    // IP14 Pensions Taken Before
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14PensionsTakenBefore action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTakenBefore)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 pensions taken before page" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBeforeModel("yes", Some(1))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTakenBefore)

            "return 200" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 pensions taken before page" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionsTakenBefore-yes").parent.classNames().contains("selected") shouldBe true
                }

                "have the amount £1 completed by default" in {
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionsTakenBeforeAmt").attr("value") shouldBe "1"
                }
            }
        }
    }

    "Submitting Pensions Taken Before data" when {

        "Submitting 'yes' in pensionsTakenBeforeForm" when {

            "amount is set as '1'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "1"))
                "return 303" in {status(DataItem.result) shouldBe 303}
                "redirect to ip14 pensions taken between" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PensionsTakenBetween()}") }
            }

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorMaximum"))
                }
            }
        }

        "Submitting 'no' in pensionsTakenBeforeForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to ip14 pensions taken between" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PensionsTakenBetween()}") }
        }

        "Submitting pensionsTakenBeforeForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBefore, ("pensionsTakenBefore", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }
    }

    ///////////////////////////////////////////////
    // Pensions Taken Between
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14PensionsTakenBetween action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTakenBetween)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 pensions taken between page" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTakenBetween.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBetweenModel("yes", Some(1))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionsTakenBetween)

            "return 200" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 pensions taken between page" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTakenBetween.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionsTakenBetween-yes").parent.classNames().contains("selected") shouldBe true
                }

                "have the amount £1 completed by default" in {
                    keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionsTakenBetweenAmt").attr("value") shouldBe "1"
                }
            }
        }
    }

    "Submitting IP14 Pensions Taken Between data" when {

        "Submitting 'yes' in pensionsTakenBetweenForm" when {

            "amount is set as '1'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "1"))
                "return 303" in {status(DataItem.result) shouldBe 303}
                "redirect to ip14 overseas pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14OverseasPensions()}") }
            }

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorMaximum"))
                }
            }
        }

        "Submitting 'no' in pensionsTakenBetweenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "temporarily to ip14 overseas pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14OverseasPensions()}") }
        }

        "Submitting pensionsTakenBetweenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionsTakenBetween, ("pensionsTakenBetween", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }
    }


    ///////////////////////////////////////////////
    // IP14 Overseas Pensions
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14OverseasPensions action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14OverseasPensions)
            "return 200" in {
                keystoreFetchCondition[OverseasPensionsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 overseas pensions page" in {
                keystoreFetchCondition[OverseasPensionsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.pageHeading")
            }
        }

        "supplied with a stored test model (yes, £100000)" should {
            val testModel = new OverseasPensionsModel("yes", Some(100000))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14OverseasPensions)

            "return 200" in {
                keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 current pensions page" in {
                keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("overseasPensions-yes").parent.classNames().contains("selected") shouldBe true
                }

                "have the value 100000 completed in the amount input by default" in {
                    keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("overseasPensionsAmt").attr("value") shouldBe "100000"
                }
            }
        }
    }

    "Submitting IP14 Overseas Pensions data" when {


        "Submitting 'no' in overseasPensionsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "no"), ("overseasPensionsAmt", "") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to ip14 current pensions page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14CurrentPensions()}") }
        }

        "Submitting 'yes', '£100,000' in overseasPensionForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "100000") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to Current Pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14CurrentPensions()}") }
        }

        "Submitting overseasPensionsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", ""), ("overseasPensionsAmt", "") )
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }

        "Submitting 'yes' in overseasPensionsForm" when {

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14OverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorMaximum"))
                }
            }
        }
    }


    ///////////////////////////////////////////////
    // IP14 CURRENT PENSIONS
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14CurrentPensions action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14CurrentPensions)
            "return 200" in {
                keystoreFetchCondition[CurrentPensionsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14 current pensions page" in {
                keystoreFetchCondition[CurrentPensionsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.pageHeading")
            }
        }

        "supplied with a stored test model (£100000)" should {
            val testModel = new CurrentPensionsModel(Some(100000))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14CurrentPensions)

            "return 200" in {
                keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the ip14Current pensions page" in {
                keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the value 100000 completed in the amount input by default" in {
                    keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("currentPensionsAmt").attr("value") shouldBe "100000"
                }
            }
        }
    }

    "Submitting IP14 Current Pensions data" when {

        "amount is set as '100,000'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14CurrentPensions, ("currentPensionsAmt", "100000") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "temporarily redirect to Introduction page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}") }
        }

        "no amount is set" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14CurrentPensions, ("currentPensionsAmt", ""))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorQuestion"))
            }
        }

        "amount is set as '5.001'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14CurrentPensions, ("currentPensionsAmt", "5.001"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorDecimalPlaces"))
            }
        }

        "amount is set as '-25'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14CurrentPensions, ("currentPensionsAmt", "-25"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorNegative"))
            }
        }

        "amount is set as '99999999999999.99'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14CurrentPensions, ("currentPensionsAmt", "99999999999999.99"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorMaximum"))
            }
        }
        
    }
}