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

import java.util.UUID

import config.wiring.PlaFormPartialRetriever
import config.{AuthClientConnector, LocalTemplateRenderer}
import connectors.KeyStoreConnector
import mocks.AuthMock
import models._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testHelpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IP2016ControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with KeystoreTestHelper with AuthMock with WithFakeApplication {

    val mockKeyStoreConnector = mock[KeyStoreConnector]
    implicit val templateRenderer: LocalTemplateRenderer = MockTemplateRenderer.renderer
    implicit val partialRetriever: PlaFormPartialRetriever = mock[PlaFormPartialRetriever]

    override def beforeEach = {
        reset(mockKeyStoreConnector)
        reset(mockAuthConnector)
    }

    implicit val hc = HeaderCarrier()

//    lazy val TestIP2016Controller = fakeApplication.injector.instanceOf[IP2016Controller]
    object TestIP2016Controller extends IP2016Controller(mockKeyStoreConnector, partialRetriever, templateRenderer) {
        override lazy val authConnector = mockAuthConnector
    }

    val sessionId = UUID.randomUUID.toString
    val fakeRequest = FakeRequest()

    val mockUsername = "mockuser"
    val mockUserId = "/auth/oid/" + mockUsername

    def keystoreFetchCondition[T](data: Option[T]): Unit = {
        when(mockKeyStoreConnector.fetchAndGetFormData[T](ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(data))
    }


    def psoDetailsKeystoreSetup(data: Option[PSODetailsModel]) = {
        when(mockKeyStoreConnector.fetchAndGetFormData[PSODetailsModel](ArgumentMatchers.eq(s"psoDetails"))(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(data))
    }

    def pensionsDebitsSaveData(data: Option[PensionDebitsModel]) = {
        when(mockKeyStoreConnector.saveData(anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(CacheMap("tstId", Map.empty[String, JsValue])))
    }



    ///////////////////////////////////////////////
    // Initial Setup
    ///////////////////////////////////////////////
    "IP2016Controller should be correctly initialised" in {
        TestIP2016Controller.keyStoreConnector shouldBe mockKeyStoreConnector
        TestIP2016Controller.authConnector shouldBe mockAuthConnector
    }

    ///////////////////////////////////////////////
    // Pensions Taken
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTaken action" when {

        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.pensionsTaken(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenModel](None)
                status(result) shouldBe 200
            }
        }

        "supplied with a stored test model" should {
            mockAuthConnector(Future.successful({}))
            val testModel = new PensionsTakenModel(Some("yes"))
            lazy val result = await(TestIP2016Controller.pensionsTaken(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenModel](Some(testModel))
                status(result) shouldBe 200
            }

            "return some HTML that" should {
                mockAuthConnector(Future.successful({}))
                "contain some text and use the character set utf-8" in {
                    keystoreFetchCondition[PensionsTakenModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }
            }
        }
    }

    "Submitting Pensions Taken data" when {

        "Submitting 'yes' in pensionsTakenForm" should {
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", "yes"))
            "redirect to pensions taken before" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBefore()}")
            }
        }

        "Submitting 'no' in pensionsTakenForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", "no"))
            "redirect to overseas pensions" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions()}")
            }
        }

        "Submitting pensionsTakenForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTaken, ("pensionsTaken", ""))
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }
        }
    }


    ///////////////////////////////////////////////
    // Pensions Taken Before
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTakenBefore action" when {

        "not supplied with a stored model" should {
            lazy val result = await(TestIP2016Controller.pensionsTakenBefore(fakeRequest))
            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
                status(result) shouldBe 200
            }

            "take the user to the pensions taken before page" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBeforeModel("yes", Some(1))
            lazy val result = await(TestIP2016Controller.pensionsTakenBefore(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                status(result) shouldBe 200
            }

            "take the user to the pensions taken before page" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBeforeModel](None)
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }

                "have the radio option `yes` selected by default" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                }

                "have the amount £1 completed by default" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PensionsTakenBeforeModel](Some(testModel))
                }
            }
        }
    }

    "Submitting Pensions Taken Before data" when {

        "Submitting 'yes' in pensionsTakenBeforeForm" when {

            "valid data is submitted" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", "1"))
                "redirect to pensions taken between" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                    status(DataItem.result) shouldBe 303
                    redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionsTakenBetween()}")
                }
            }

            "invalid data is submitted" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", ""), ("pensionsTakenBeforeAmt", ""))
                "return 400" in {
                    mockAuthConnector(Future.successful({}))
                    status(DataItem.result) shouldBe 400
                }
            }

            "invalid data is submitted that fails additional validation" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBefore, ("pensionsTakenBefore", "yes"), ("pensionsTakenBeforeAmt", ""))
                "return 400" in {
                    mockAuthConnector(Future.successful({}))
                    status(DataItem.result) shouldBe 400
                }
            }
        }
    }

    ///////////////////////////////////////////////
    // Pensions Taken Between
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionsTakenBetween action" when {

        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.pensionsTakenBetween(fakeRequest))
            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBetweenModel](None)
                status(result) shouldBe 200
            }
        }

        "supplied with a stored test model" should {
            val testModel = new PensionsTakenBetweenModel("yes", Some(1))
            lazy val result = await(TestIP2016Controller.pensionsTakenBetween(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                status(result) shouldBe 200
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PensionsTakenBetweenModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }
            }
        }
    }

    "Submitting Pensions Taken Between data" when {

        "Submitting 'yes' in pensionsTakenBetweenForm" when {

            "submitting valid data" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", "1"))
                "redirect to overseas pensions" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                    status(DataItem.result) shouldBe 303
                    redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.overseasPensions}") }
            }

            "submitting invalid data" should {
                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", ""), ("pensionsTakenBetweenAmt", ""))
                "return 400" in {
                    mockAuthConnector(Future.successful({}))
                    status(DataItem.result) shouldBe 400
                }
            }

            "submitting invalid data that fails additional validation" should {

                object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionsTakenBetween, ("pensionsTakenBetween", "yes"), ("pensionsTakenBetweenAmt", ""))
                "return 400" in {
                    mockAuthConnector(Future.successful({}))
                    status(DataItem.result) shouldBe 400
                }
            }
        }
    }




    ///////////////////////////////////////////////
    // Overseas Pensions
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .overseasPensions action" when {

        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.overseasPensions(fakeRequest))
            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[OverseasPensionsModel](None)
                status(result) shouldBe 200
            }
        }

        "supplied with a stored test model (yes, £100000)" should {
            val testModel = new OverseasPensionsModel("yes", Some(100000))
            lazy val result = await(TestIP2016Controller.overseasPensions(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                status(result) shouldBe 200
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[OverseasPensionsModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }
            }
        }
    }

    "Submitting Overseas Pensions data" when {


        "Submitting valid data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "no"), ("overseasPensionsAmt", "") )
            "redirect to Current Pensions" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.currentPensions()}") }
        }
        "Submitting invalid data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", ""), ("overseasPensionsAmt", "") )
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }
        }

        "Submitting invalid data that fails additional validation" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitOverseasPensions, ("overseasPensions", "yes"), ("overseasPensionsAmt", "") )
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }
        }
    }



    ///////////////////////////////////////////////
    // CURRENT PENSIONS
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .currentPensions action" when {

        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.currentPensions(fakeRequest))
            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[CurrentPensionsModel](None)
                status(result) shouldBe 200
            }
        }

        "supplied with a stored test model (£100000)" should {
            val testModel = new CurrentPensionsModel(Some(100000))
            lazy val result = await(TestIP2016Controller.currentPensions(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                status(result) shouldBe 200
            }


            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[CurrentPensionsModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }

            }
        }
    }

    "Submitting Current Pensions data" when {

        "valid data is submitted" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", "100000") )
            "redirect to Pension Debits page" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionsTakenModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.pensionDebits()}") }
        }

        "invalid data is submitted" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitCurrentPensions, ("currentPensionsAmt", ""))
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
                }
        }
    }



    ///////////////////////////////////////////////
    // PENSION DEBITS
    ///////////////////////////////////////////////
    "In IP2016Controller calling the .pensionDebits action" when {

        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.pensionDebits(fakeRequest))
            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionDebitsModel](None)
                status(result) shouldBe 200
            }

        }

        "supplied with a stored test model" should {
            val testModel = new PensionDebitsModel(Some("yes"))
            lazy val result = await(TestIP2016Controller.pensionDebits(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                status(result) shouldBe 200
            }

            "return some HTML that" should {

                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PensionDebitsModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
                }
            }
        }
    }

    "Submitting Pensions Debits data" when {

        "Submitting 'yes' in pensionDebitsForm" should {
            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", "yes"))
            "redirect to number of pension sharing orders" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionDebitsModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.IP2016Controller.psoDetails()}")
            }
        }

        "Submitting 'no' in pensionDebitsForm" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", "no"))
            "redirect to summary" in {
                mockAuthConnector(Future.successful({}))
                keystoreSaveCondition[PensionDebitsModel](mockKeyStoreConnector)
                status(DataItem.result) shouldBe 303
                redirectLocation(DataItem.result) shouldBe Some(s"${routes.SummaryController.summaryIP16()}") }
        }

        "Submitting pensionDebitsForm with no data" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPensionDebits, ("pensionDebits", ""))
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }
        }
    }

    ///////////////////////////////////////////////
    // PENSION DETAILS
    ///////////////////////////////////////////////

    "In IP2016Controller calling the .psoDetails action" when {


        "not supplied with a stored model" should {

            lazy val result = await(TestIP2016Controller.psoDetails(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PSODetailsModel](None)
                status(result) shouldBe 200
            }
        }

        "supplied with a stored test model" should {
            val testModel = PSODetailsModel(1, 8, 2016, BigDecimal(1234))
            lazy val result = await(TestIP2016Controller.psoDetails(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                keystoreFetchCondition[PSODetailsModel](Some(testModel))
                status(result) shouldBe 200
            }


            "return some HTML that" should {
                "contain some text and use the character set utf-8" in {
                    mockAuthConnector(Future.successful({}))
                    keystoreFetchCondition[PSODetailsModel](Some(testModel))
                    contentType(result) shouldBe Some("text/html")
                    charset(result) shouldBe Some("utf-8")
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
                mockAuthConnector(Future.successful({}))
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
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }

        }

        "submitting an invalid set of PSO details that fails additional validation" should {

            object DataItem extends AuthorisedFakeRequestToPost(TestIP2016Controller.submitPSODetails,
                ("psoDay", "35"),
                ("psoMonth", "1"),
                ("psoYear", "2015"),
                ("psoAmt", "100000")
            )
            "return 400" in {
                mockAuthConnector(Future.successful({}))
                status(DataItem.result) shouldBe 400
            }
        }
    }

    "In IP2016Controller calling the .removePsoDetails action" when {

        "supplied with a stored model" should {
            lazy val result = await(TestIP2016Controller.removePsoDetails(fakeRequest))

            "return 200" in {
                mockAuthConnector(Future.successful({}))
                status(result) shouldBe 200
            }
        }
    }

    "Submitting a pso for removal from application" when {

        "not supplied with a stored model" should {
            lazy val result = await(TestIP2016Controller.submitRemovePsoDetails(fakeRequest))
            val testModel = new PensionDebitsModel(Some("yes"))

            "return 303" in {
                mockAuthConnector(Future.successful({}))
                pensionsDebitsSaveData(Some(testModel))
                status(result) shouldBe 303
            }

            "redirect location should be the summary page" in {
                mockAuthConnector(Future.successful({}))
                redirectLocation(result) shouldBe Some(s"${routes.SummaryController.summaryIP16()}")
            }
        }
    }
}
