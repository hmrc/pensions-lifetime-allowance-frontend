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

package constructors

import enums.ApplicationType
import models._
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.FakeRequest
import testHelpers.FakeApplication
import models.cache.CacheMap
import utils.CallMap

import java.time.LocalDate

class SummaryConstructorSpec extends FakeApplication with MockitoSugar {

  implicit val mockLang: Lang = mock[Lang]

  lazy implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit lazy val mockMessage: Messages = fakeApplication().injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  object TestSummaryConstructor extends SummaryConstructor
  val tstId = "testUserID"
  "Summary Constructor" should {

    val positivePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
    val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
    val negativePensionsTakenSummaryRow = SummaryRowModel("pensionsTaken", CallMap.get("pensionsTaken"), None, false, "No")
    val positivePensionsTakenSummaryRow = SummaryRowModel("pensionsTaken", Some(controllers.routes.IP2016Controller.pensionsTaken), None,  false, "Yes")

    val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes"))
    val negativePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no"))
    val validPensionsWorthBeforeTuple = "pensionsWorthBefore" -> Json.toJson(PensionsWorthBeforeModel(Some(BigDecimal(1001000))))
    val positivePensionsTakenBeforeSummaryRow = SummaryRowModel("pensionsTakenBefore", Some(controllers.routes.IP2016Controller.pensionsTakenBefore), None,  false, "Yes")
    val positivePensionsWorthBeforeSummaryRow = SummaryRowModel("pensionsWorthBeforeAmt", Some(controllers.routes.IP2016Controller.pensionsWorthBefore), None,  false, "£1,001,000")
    val negativePensionsTakenBeforeSummaryRow = SummaryRowModel("pensionsTakenBefore", Some(controllers.routes.IP2016Controller.pensionsTakenBefore), None,  false, "No")

    val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes"))
    val negativePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no"))
    val validPensionUsedBetweenTuple = "pensionsUsedBetween" -> Json.toJson(PensionsUsedBetweenModel(Some(BigDecimal(1100))))

    val positivePensionsTakenBetweenSummaryRow = SummaryRowModel("pensionsTakenBetween", Some(controllers.routes.IP2016Controller.pensionsTakenBetween), None,  false, "Yes")
    val positivePensionsTakenBetweenAmtSummaryRow = SummaryRowModel("pensionsUsedBetweenAmt", Some(controllers.routes.IP2016Controller.pensionsUsedBetween), None,  false, "£1,100")
    val negativePensionsTakenBetweenSummaryRow = SummaryRowModel("pensionsTakenBetween", Some(controllers.routes.IP2016Controller.pensionsTakenBetween), None,  false, "No")

    val positiveOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010))))
    val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
    val negativeOverseasPensionsSummaryRow = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions), None,  false, "No")
    val positiveOverseasPensionsSummaryRow = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions), None,  false, "Yes")
    val positiveOverseasPensionsAmtSummaryRow = SummaryRowModel("overseasPensionsAmt", Some(controllers.routes.IP2016Controller.overseasPensions), None,  false, "£1,010")

    val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
    val currentPensionsSummaryRow = SummaryRowModel("currentPensionsAmt", Some(controllers.routes.IP2016Controller.currentPensions), None, false, "£1,001")

    val positivePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))
    val negativePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))
    val negativePensionDebitsSummaryRow = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits), None,  false, "No")
    val positivePensionDebitsSummaryRow = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits), None,  false, "Yes")

    def totalPensionsAmountSummaryRow(totalAmount: String) = SummaryRowModel("totalPensionsAmt", None, None, false, totalAmount)

    val psoDetailsTuple = "psoDetails" -> Json.toJson(PSODetailsModel(LocalDate.of(2016, 2, 1), Some(BigDecimal(10000))))

    val psoDetailsSummaryRow = SummaryRowModel("psoDetails", Some(controllers.routes.IP2016Controller.psoDetails), Some(controllers.routes.IP2016Controller.removePsoDetails), false, "£10,000", "1 February 2016")

    "handle invalid summary data" when {
        implicit val protectionType: ApplicationType.Value = ApplicationType.IP2016

      "there is no data" in {
        val tstMap = CacheMap(tstId, Map.empty)
        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no pensions taken model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenBeforeTuple,
                                        validPensionsWorthBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
                                        validPensionUsedBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no overseas pensions model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no current pensions model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positiveOverseasPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pensions taken 'yes' and no pensions taken before model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pensions taken 'yes' and no pensions taken between model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBeforeTuple,
                                        validPensionsWorthBeforeTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pension debits 'yes' and no Pso Details Model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }
    }

    "handle valid summary data" when {
        implicit val protectionType: ApplicationType.Value = ApplicationType.IP2016

      "all answers are negative" in {
        val testSummaryModel = SummaryModel(protectionType, invalidRelevantAmount = true, List(
                                                                    SummarySectionModel(List(
                                                                      negativePensionsTakenSummaryRow)),
                                                                    SummarySectionModel(List(
                                                                      negativeOverseasPensionsSummaryRow)),
                                                                    SummarySectionModel(List(
                                                                      currentPensionsSummaryRow)),
                                                                    SummarySectionModel(List(
                                                                      totalPensionsAmountSummaryRow("£1,001")))
                                                                    ),
                                                                    List(
                                                                      SummarySectionModel(
                                                                        List(negativePensionDebitsSummaryRow))
                                                                    )
        )

        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        negativeOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }

      "all answers are positive" in {
        val testSummaryModel = SummaryModel(protectionType, invalidRelevantAmount = false,
                                        List(
                                          SummarySectionModel(List(
                                            positivePensionsTakenSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsTakenBeforeSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsWorthBeforeSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsTakenBetweenSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsTakenBetweenAmtSummaryRow)),
                                          SummarySectionModel(List(
                                            positiveOverseasPensionsSummaryRow, positiveOverseasPensionsAmtSummaryRow)),
                                          SummarySectionModel(List(
                                            currentPensionsSummaryRow)),
                                          SummarySectionModel(List(
                                            totalPensionsAmountSummaryRow("£1,004,111")))
                                            ),
                                        List(
                                          SummarySectionModel(List(
                                            positivePensionDebitsSummaryRow)),
                                          SummarySectionModel(List(
                                            psoDetailsSummaryRow))
                                            )
                                        )

        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBeforeTuple,
                                        validPensionsWorthBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
                                        validPensionUsedBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple,
                                        psoDetailsTuple
                                        )
                            )

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }

      "pensions taken 'yes', pensions taken before 'no', pensions taken between 'no'" in {
        val testSummaryModel = SummaryModel(protectionType, invalidRelevantAmount = true,
                                        List(
                                          SummarySectionModel(List(
                                            positivePensionsTakenSummaryRow)),
                                          SummarySectionModel(List(
                                            negativePensionsTakenBeforeSummaryRow)),
                                          SummarySectionModel(List(
                                            negativePensionsTakenBetweenSummaryRow)),
                                          SummarySectionModel(List(
                                            positiveOverseasPensionsSummaryRow, positiveOverseasPensionsAmtSummaryRow)),
                                          SummarySectionModel(List(
                                            currentPensionsSummaryRow)),
                                          SummarySectionModel(List(
                                            totalPensionsAmountSummaryRow("£2,011")))
                                            ),
                                        List(
                                          SummarySectionModel(List(
                                            negativePensionDebitsSummaryRow)))
                                        )

        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        negativePensionsTakenBeforeTuple,
                                        negativePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple
                                        )
                            )

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }
    }
  }
}
