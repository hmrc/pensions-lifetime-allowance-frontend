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

import java.time.LocalDate
import java.util.UUID

import auth._
import com.kenshoo.play.metrics.PlayModule
import config.FrontendAuthConnector
import connectors.KeyStoreConnector
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class IP2016ControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfterEach with KeystoreTestHelper {
    override def bindModules = Seq(new PlayModule)

    val mockKeyStoreConnector = mock[KeyStoreConnector]

    override def beforeEach = {
        reset(mockKeyStoreConnector)
    }

    implicit val hc=new HeaderCarrier()
    object TestIP2016Controller extends IP2016Controller {
        override lazy val applicationConfig = MockConfig
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
//        when(mockKeyStoreConnector.fetchAndGetFormData[NumberOfPSOsModel](Matchers.eq("numberOfPSOs"))(Matchers.any(), Matchers.any()))
//          .thenReturn(Future.successful(data))
//    }

    def psoDetailsKeystoreSetup(data: Option[PSODetailsModel]) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PSODetailsModel](Matchers.eq(s"psoDetails"))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(data))
    }

    def pensionDebitsKeystoreSetup(data: Option[PensionDebitsModel]) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PensionDebitsModel](Matchers.eq("pensionDebits"))(Matchers.any(), Matchers.any()))
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTaken.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTaken.title")
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
            "redirect to pensions taken before" in {
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBefore()}")
            }
        }

        "Submitting 'no' in pensionsTakenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", "no"))
            "redirect to overseas pensions" in {
              keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
              status(DataItem.result) shouldBe 303
             redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions()}")
            }
        }

        "Submitting pensionsTakenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBefore.title")
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

            "valid data is submitted" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "1"))
                "redirect to pensions taken between" in {
                    keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                    status(DataItem.result) shouldBe 303
                    redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBetween()}") }
            }

            "invalid data is submitted" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionsTakenBetween.title")
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

            "submitting valid data" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "1"))
                "redirect to overseas pensions" in {
                    keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                    status(DataItem.result) shouldBe 303
                    redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}") }
            }

            "submitting invalid data" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", ""))
                "return 400" in {status(DataItem.result) shouldBe 400}
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.overseasPensions.title")
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

    "Submitting Overseas Pensions data" when {


        "Submitting valid data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "no"), ("overseasPensionsAmt", "") )
            "redirect to Current Pensions" in {
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.currentPensions()}") }
        }


        "Submitting invalid data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", ""), ("overseasPensionsAmt", "") )
            "return 400" in { status(DataItem.result) shouldBe 400 }
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.title")
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
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.currentPensions.title")
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

        "valid data is submitted" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "100000") )
            "redirect to Pension Debits page" in {
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionDebits()}") }
        }

        "invalid data is submitted" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", ""))
            "return 400" in {status(DataItem.result) shouldBe 400}
        }
    }



    ///////////////////////////////////////////////
    // PENSION DEBITS
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionDebits action" when {

        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionDebits)
            "return 200" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pension debits page" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionDebits.title")
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionDebitsModel(Some("yes"))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.pensionDebits)

            "return 200" in {
                keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                status(DataItem.result) shouldBe 200
            }

            "take the user to the pension debits page" in {
                keystoreFetchCondition[PensionDebitsModel](None)
                DataItem.jsoupDoc.body.getElementsByTag("h1").text shouldEqual Messages("pla.pensionDebits.title")
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

    "Submitting Pensions Debits data" when {

        "Submitting 'yes' in pensionDebitsForm" should {
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", "yes"))
            "redirect to number of pension sharing orders" in {
                keystoreSaveCondition[PensionDebitsModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.psoDetails()}")
            }
        }

        "Submitting 'no' in pensionDebitsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", "no"))
            "redirect to summary" in {
                keystoreSaveCondition[PensionDebitsModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16()}") }
        }

        "Submitting pensionDebitsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", ""))
            "return 400" in { status(DataItem.result) shouldBe 400 }
        }
    }

    ///////////////////////////////////////////////
    // PENSION DETAILS
    ///////////////////////////////////////////////

    "In IP2016Controller calling the .psoDetails action" when {


        "not supplied with a stored model" should {

            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.psoDetails)

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
            val testModel = PSODetailsModel(Some(1), Some(8), Some(2016), BigDecimal(1234))
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.psoDetails)

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
                    DataItem.jsoupDoc.body.getElementById("psoYear").`val`() shouldBe "2016"
                    DataItem.jsoupDoc.body.getElementById("psoAmt").`val`() shouldBe "1234"
                }
            }
        }

    }

    "Submitting valid PSO details data" when {

        "submitting valid PSO details" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPSODetails,
                
                ("psoDay", "6"),
                ("psoMonth", "4"),
                ("psoYear", "2016"),
                ("psoAmt", "100000")
            )

            "redirect to the summary page with a valid PSO" in {
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16()}")
            }
        }

        "submitting an invalid set of PSO details" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPSODetails,
                
                ("psoDay", ""),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 400" in { status(DataItem.result) shouldBe 400 }

        }
    }

    "In IP2016Controller calling the .removePsoDetails action" when {

        "supplied with a stored model" should {
            object DataItem extends AuthorisedFakeRequestTo(TestIP2016Controller.removePsoDetails)

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
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitRemovePsoDetails)

            "return 303" in {
                status(DataItem.result) shouldBe 303
            }

            "redirect location should be the summary page" in {
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16()}")
            }
        }
    }
}
