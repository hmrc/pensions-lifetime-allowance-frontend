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

class IP2016ControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

    val mockKeyStoreConnector = mock[KeyStoreConnector]

    object TestIP2016Controller extends IP2016Controller {
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

    ///////////////////////////////////////////////
    // Initial Setup
    ///////////////////////////////////////////////
    "IP2016Controller should be correctly initialised" in {
        IP2016Controller.keyStoreConnector shouldBe KeyStoreConnector
        IP2016Controller.authConnector shouldBe FrontendAuthConnector
    }

    ///////////////////////////////////////////////
    // Pensions Taken
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTaken action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTaken)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken page" in {
                keystoreFetchCondition[PensionsTakenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTaken.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenModel(Some("yes"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTaken)

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

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", "yes"))
            "return 303" in {status(DataItem.result) shouldBe 303}
            "redirect to pensions taken before" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBefore}") }
        }

        "Submitting 'no' in pensionsTakenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            // "redirect to overseas pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}") }
        }

        "Submitting pensionsTakenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTaken.mandatoryErr"))
            }
        }
    }


    ///////////////////////////////////////////////
    // Pensions Taken Before
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTakenBefore action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTakenBefore)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken before page" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBeforeModel("yes", Some(1))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTakenBefore)

            "return 200" in {
                keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken before page" in {
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

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "1"))
                "return 303" in {status(DataItem.result) shouldBe 303}
                "redirect to pensions taken between" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBetween}") }
            }

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBefore.errorMaximum"))
                }
            }
        }

        "Submitting 'no' in pensionsTakenBeforeForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            // TODO: redirect location not yet implemented in controller
            //"redirect to somewhere" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.someAction}") }
        }

        "Submitting pensionsTakenBeforeForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }
    }

    ///////////////////////////////////////////////
    // Pensions Taken Between
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTakenBetween action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTakenBetween)
            "return 200" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken between page" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.pageHeading")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBetweenModel("yes", Some(1))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionsTakenBetween)

            "return 200" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken between page" in {
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.pageHeading")
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

    "Submitting Pensions Taken Between data" when {

        "Submitting 'yes' in pensionsTakenBetweenForm" when {

            "amount is set as '1'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "1"))
                "return 303" in {status(DataItem.result) shouldBe 303}
                //"redirect to pensions taken between" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}") }
            }

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionsTakenBetween.errorMaximum"))
                }
            }
        }

        "Submitting 'no' in pensionsTakenBetweenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            // TODO: redirect location not yet implemented in controller
            //"redirect to somewhere" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.someAction}") }
        }

        "Submitting pensionsTakenBetweenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }
    }




    ///////////////////////////////////////////////
    // Overseas Pensions
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .overseasPensions action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.overseasPensions)
            "return 200" in {
                keystoreFetchCondition[OverseasPensionsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the overseas pensions page" in {
                keystoreFetchCondition[OverseasPensionsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.pageHeading")
            }
        }

        "supplied with a stored test model (yes, £100000)" should {
            val testModel = new OverseasPensionsModel("yes", Some(100000))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.overseasPensions)

            "return 200" in {
                keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pensions taken page" in {
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

    "Submitting Overseas Pensions data" when {


        "Submitting 'no' in overseasPensionsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "no"), ("overseasPensionsAmt", "") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to Current Pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.currentPensions()}") }
        }

        "Submitting 'yes', '£100,000' in overseasPensionForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "100000") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to Current Pensions" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.currentPensions()}") }
        }

        "Submitting overseasPensionsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", ""), ("overseasPensionsAmt", "") )
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include ("This field is required")
            }
        }

        "Submitting 'yes' in overseasPensionsForm" when {

            "no amount is set" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorQuestion"))
                }
            }

            "amount is set as '5.001'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "5.001"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorDecimalPlaces"))
                }
            }

            "amount is set as '-25'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "-25"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorNegative"))
                }
            }

            "amount is set as '99999999999999.99'" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "99999999999999.99"))
                "return 400" in {status(DataItem.result) shouldBe 400}
                "fail with the correct error message" in {
                    DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.overseasPensions.errorMaximum"))
                }
            }
        }
    }



    ///////////////////////////////////////////////
    // CURRENT PENSIONS
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .currentPensions action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.currentPensions)
            "return 200" in {
                keystoreFetchCondition[CurrentPensionsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the current pensions page" in {
                keystoreFetchCondition[CurrentPensionsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.pageHeading")
            }
        }

        "supplied with a stored test model (£100000)" should {
            val testModel = new CurrentPensionsModel(Some(100000))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.currentPensions)

            "return 200" in {
                keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the current pensions page" in {
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

    "Submitting Current Pensions data" when {

        "amount is set as '100,000'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "100000") )
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "temporarily redirect to Introduction" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IntroductionController.introduction()}") }
        }

        "no amount is set" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", ""))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorQuestion"))
            }
        }

        "amount is set as '5.001'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "5.001"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorDecimalPlaces"))
            }
        }

        "amount is set as '-25'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "-25"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorNegative"))
            }
        }

        "amount is set as '99999999999999.99'" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "99999999999999.99"))
            "return 400" in {status(DataItem.result) shouldBe 400}
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.currentPensions.errorMaximum"))
            }
        }
        
    }
}
