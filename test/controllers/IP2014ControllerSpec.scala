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
        when(mockKeyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel](Matchers.eq("ip14NumberOfPSOs"))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(data))
    }

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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionDebits.pageHeading")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.ip14PensionDebits.pageHeading")
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
            "redirect to ip14 number of pension sharing orders" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14NumberOfPSOs()}") }
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
    // IP14 NUMBER OF PENSION SHARING ORDERS
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14NumberOfPSOs action" when {

        "not supplied with a stored model" should {

            val testModel = PensionDebitsModel(Some("yes"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14NumberOfPSOs)
            "return 200" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the number of PSOs page" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(Some(testModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.numberOfPSOs.pageHeading")
            }
        }

        "the user has not declared any pension sharing orders" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14NumberOfPSOs)
            "return 303" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(None)
                status(DataItem.result) shouldBe 303
            }

            "redirect the user to the technical error page" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(None)
                redirectLocation(DataItem.result) shouldBe Some(""+routes.FallbackController.technicalError("IP2014"))
            }
        }

        "the user has declared they have no pension sharing orders" should {

            val testModel = PensionDebitsModel(Some("no"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14NumberOfPSOs)
            "return 303" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(Some(testModel))
                status(DataItem.result) shouldBe 303
            }

            "redirect the user to the technical error page" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                pensionDebitsKeystoreSetup(Some(testModel))
                redirectLocation(DataItem.result) shouldBe Some(""+routes.FallbackController.technicalError("IP2014"))
            }
        }

        "supplied with a pre-existing stored model" should {

            val testPensionDebitsModel = PensionDebitsModel(Some("yes"))
            val testModel = NumberOfPSOsModel(Some("3"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14NumberOfPSOs)
            "return 200" in {
                keystoreFetchCondition[NumberOfPSOsModel](Some(testModel))
                pensionDebitsKeystoreSetup(Some(testPensionDebitsModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the number of PSOs page" in {
                keystoreFetchCondition[NumberOfPSOsModel](Some(testModel))
                pensionDebitsKeystoreSetup(Some(testPensionDebitsModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.numberOfPSOs.pageHeading")
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[NumberOfPSOsModel](Some(testModel))
                    pensionDebitsKeystoreSetup(Some(testPensionDebitsModel))
                    contentType(DataItem.result) shouldBe Some("text/html")
                    charset(DataItem.result) shouldBe Some("utf-8")
                }

                "have the radio option `3` selected by default" in {
                    keystoreFetchCondition[NumberOfPSOsModel](Some(testModel))
                    pensionDebitsKeystoreSetup(Some(testPensionDebitsModel))
                    DataItem.jsoupDoc.body.getElementById("numberOfPSOs-3").parent.classNames().contains("selected") shouldBe true
                }
            }
        }

    }

    "Submitting number of Pension Sharing Orders data" when {

        "Submitting '1' in numberOfPSOsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14NumberOfPSOs, ("numberOfPSOs", "1"))
            "return 303" in {status(DataItem.result) shouldBe 303}
            "redirect to PSO details" in { redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PsoDetails("1")}") }
        }

        "Submitting numberOfPSOsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14NumberOfPSOs, ("numberOfPSOs", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
            "fail with the correct error message" in {
                DataItem.jsoupDoc.getElementsByClass("error-notification").text should include (Messages("pla.numberOfPSOs.mandatoryErr"))
            }
        }
    }



    ///////////////////////////////////////////////
    // IP14 PENSION SHARING ORDER DETAILS
    ///////////////////////////////////////////////
    "In IP2014Controller calling the .ip14PsoDetails action" when {

        "there is no total PSOs number stored in keystore" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails("1"))
            "return 303" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                status(DataItem.result) shouldBe 303
            }

            "redirect the user to the technical error page" in {
                keystoreFetchCondition[NumberOfPSOsModel](None)
                redirectLocation(DataItem.result) shouldBe Some(""+routes.FallbackController.technicalError("IP2014"))
            }
        }

        "a PSO number higher than the total number of PSOs is passed in" should {

            val testModel = new NumberOfPSOsModel(Some("2"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails("3"))
            "return 303" in {
                psoNumKeystoreSetup(Some(testModel))
                status(DataItem.result) shouldBe 303
            }

            "redirect the user to the ip14 summary page" in {
                psoNumKeystoreSetup(Some(testModel))
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP14()}")
            }
        }

        "a PSO number (2) less than the total number of PSOs is passed in" should {

            val testModel = new NumberOfPSOsModel(Some("3"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails("2"))
            "return 200" in {
                psoNumKeystoreSetup(Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the second PSO details page" in {
                psoNumKeystoreSetup(Some(testModel))
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.pageHeading2")
            }
        }

        "a PSO number (2) equal the total number of PSOs is passed in with a stored model" should {

            val day = 13
            val month = 5
            val year = 2016
            val psoAmt = 100000
            val testModel = new NumberOfPSOsModel(Some("2"))
            val testDetailsModel = new PSODetailsModel(2, day, month, year, psoAmt)
            object DataItem extends AuthorisedFakeRequestTo(TestIP2014Controller.ip14PsoDetails("2"))
            "return 200" in {
                psoNumKeystoreSetup(Some(testModel))
                psoDeetsKeystoreSetup(Some(testDetailsModel), 2)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the second PSO details page" in {
                psoNumKeystoreSetup(Some(testModel))
                psoDeetsKeystoreSetup(Some(testDetailsModel), 2)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.psoDetails.pageHeading2")
            }

            "have the PSO date fields completed correctly" in {
                psoNumKeystoreSetup(Some(testModel))
                psoDeetsKeystoreSetup(Some(testDetailsModel), 2)
                DataItem.jsoupDoc.body.getElementById("psoDay").attr("value") shouldEqual "13"
                DataItem.jsoupDoc.body.getElementById("psoMonth").attr("value") shouldEqual "5"
                DataItem.jsoupDoc.body.getElementById("psoYear").attr("value") shouldEqual "2016"
            }

            "have the PSO amount field completed correctly" in {
                psoNumKeystoreSetup(Some(testModel))
                psoDeetsKeystoreSetup(Some(testDetailsModel), 2)
                DataItem.jsoupDoc.body.getElementById("psoAmt").attr("value") shouldEqual "100000"
            }
        }
    }

    "Submitting valid PSO details data" when {

        "submitting a valid 4th PSO's details" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoNumber", "4"),
                ("psoDay", "1"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect to the psoDetails controller action with a psoNum of 5" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2014Controller.ip14PsoDetails("5")}")
            }
        }

        "submitting an invalid set of PSO details - missing day" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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

        "submitting an invalid set of PSO details - date out of range" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoNumber", "4"),
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

        "submitting an invalid set of PSO details - missing PSO amount" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2014Controller.submitIP14PSODetails,
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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
                ("psoNumber", "4"),
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
}
