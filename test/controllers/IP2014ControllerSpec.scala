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

//    def psoNumKeystoreSetup(data: Option[NumberOfPSOsModel]) = {
//        when(mockKeyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel](Matchers.eq("ip14NumberOfPSOs"))(Matchers.any(), Matchers.any()))
//          .thenReturn(Future.successful(data))
//    }

    def psoDeetsKeystoreSetup(data: Option[PSODetailsModel], deetsNum: Int) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PSODetailsModel](Matchers.eq(s"ip14PsoDetails$deetsNum"))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(data))
    }

    def pensionDebitsKeystoreSetup(data: Option[PensionDebitsModel]) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PensionDebitsModel](Matchers.eq("ip14PensionDebits"))(Matchers.any(), Matchers.any()))
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTaken.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTaken.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTakenBetween.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionsTakenBetween.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14CurrentPensions.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14CurrentPensions.title")
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
            "redirect to Pension Debits page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PensionDebits()}") }
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


    ///////////////////////////////////////////////
    // IP14 PENSION DEBITS
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .pensionDebits action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionDebits)
            "return 200" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pension debits page" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionDebits.title")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionDebitsModel(Some("yes"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PensionDebits)

            "return 200" in {
                keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pension debits page" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionDebits.title")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("pensionDebits-yes").parent.classNames().contains("selected") shouldBe true
                }
            }
        }
    }

    "Submitting IP 2014 Pensions Debits data" when {

        "Submitting 'yes' in pensionDebitsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionDebits, ("pensionDebits", "yes"))
            "return 303" in {status(DataItem.result) shouldBe 303}
            "redirect to ip14 pso details page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PsoDetails}") }
        }

        "Submitting 'no' in pensionDebitsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionDebits, ("pensionDebits", "no"))
            "return 303" in { status(DataItem.result) shouldBe 303 }
            "redirect to ip14 summary page" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP14()}") }
        }

        "Submitting pensionDebitsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PensionDebits, ("pensionDebits", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.pensionDebits.mandatoryErr"))
            }
        }
    }

    ///////////////////////////////////////////////
    // PENSION DETAILS
    ///////////////////////////////////////////////

    "In IP2014Controller calling the .psoDetails action" when {


        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails)

            "return 200" in {
                keystoreFetchCondition[PSODetailsModel](None)
                status(DataItem.result) shouldBe 200
            }
            "take the user to the pso details page" in {
                keystoreFetchCondition[PSODetailsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
            }
        }

        "supplied with a stored test model" should {
            val testModel = PSODetailsModel(Some(1), Some(8), Some(2014), BigDecimal(1234))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails)

            "return 200" in {
                keystoreFetchCondition[PSODetailsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pso details page" in {
                keystoreFetchCondition[PSODetailsModel](Some(testModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
            }

            "return some HTML that" should {
                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PSODetailsModel](Some(testModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the input values set as default" in {
                    keystoreFetchCondition[PSODetailsModel](Some(testModel))
                    DataItem.jsoupDoc.body.getElementById("psoDay").`val`() shouldBe "1"
                    DataItem.jsoupDoc.body.getElementById("psoMonth").`val`() shouldBe "8"
                    DataItem.jsoupDoc.body.getElementById("psoYear").`val`() shouldBe "2014"
                    DataItem.jsoupDoc.body.getElementById("psoAmt").`val`() shouldBe "1234"
                }
            }
        }

    }

    "Submitting valid PSO details data" when {

        "submitting valid PSO details on first possible day" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "6"),
                ("psoMonth", "4"),
                ("psoYear", "2016"),
                ("psoAmt", "100000")
            )
            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect to the summary page" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP14}")
            }
        }

        "submitting valid PSO details on today's date" should {

            val todaysDate = LocalDate.now()
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", todaysDate.getDayOfMonth.toString),
                ("psoMonth", todaysDate.getMonthValue.toString),
                ("psoYear", todaysDate.getYear.toString),
                ("psoAmt", "1000000")
            )
            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect to the psoDetails controller action with a psoNum of 4" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP14()}")
            }
        }

        "submitting an invalid set of PSO details - missing day" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", ""),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.dayEmpty"))
            }
        }

        "submitting an invalid set of PSO details - missing month" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", ""),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.monthEmpty"))
            }
        }

        "submitting an invalid set of PSO details - missing year" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", ""),
                ("psoAmt", "100000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.yearEmpty"))
            }
        }

        "submitting an invalid set of PSO details - invalid date" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "29"),
                ("psoMonth", "2"),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.base.errors.invalidDate"))
            }
        }

        "submitting an invalid set of PSO details - date before 6 April 2014" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "5"),
                ("psoMonth", "4"),
                ("psoYear", "2014"),
                ("psoAmt", "1000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP14PsoDetails.errorDateOutOfRange"))
            }
        }

        "submitting an invalid set of PSO details - date in future" should {

            val tomorrow = LocalDate.now.plusDays(1)
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", tomorrow.getDayOfMonth.toString),
                ("psoMonth", tomorrow.getMonthValue.toString),
                ("psoYear", tomorrow.getYear.toString),
                ("psoAmt", "1000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.IP14PsoDetails.errorDateOutOfRange"))
            }
        }

        "submitting an invalid set of PSO details - missing PSO amount" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("error.real"))
            }
        }

        "submitting an invalid set of PSO details - amount negative" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "-1")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorNegative"))
            }
        }

        "submitting an invalid set of PSO details - amount too many decimal places" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "0.001")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorDecimalPlaces"))
            }
        }

        "submitting an invalid set of PSO details - amount too large" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "999999999999999")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.psoDetails.errorMaximum"))
            }
        }
    }

    "In IP2014Controller calling the .removePsoDetails action" when {

        "supplied with a stored model" should {
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.removeIp14PsoDetails)

            "return 200" in {
                status(DataItem.result) shouldBe 200
            }

            "take the user to the remove PSO page" in {
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.title")
            }
        }
    }

    "Submitting a pso for removal from application" when {

        "not supplied with a stored model" should {
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitRemoveIp14PsoDetails)

            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect location should be the summary page" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP14()}")
            }
        }
    }
}
