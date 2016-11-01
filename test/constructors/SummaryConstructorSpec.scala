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

package constructors

import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{WithFakeApplication, UnitSpec}
import play.api.libs.json.{JsValue, Json}
import models._
import enums.ApplicationType

class SummaryConstructorSpec extends UnitSpec with WithFakeApplication {
  object TestSummaryConstructor extends SummaryConstructor
  val tstId = "testUserID"
  "Summary Constructor" should {

    val positivePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
    val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
    val negativePensionsTakenSummaryRow = SummaryRowModel("pensionsTaken", Some(controllers.routes.IP2016Controller.pensionsTaken()), None, false, "No")
    val positivePensionsTakenSummaryRow = SummaryRowModel("pensionsTaken", Some(controllers.routes.IP2016Controller.pensionsTaken()), None,  false, "Yes")

    val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes", Some(BigDecimal(1001000))))
    val negativePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no", None))
    val positivePensionsTakenBeforeSummaryRow = SummaryRowModel("pensionsTakenBefore", Some(controllers.routes.IP2016Controller.pensionsTakenBefore()), None,  false, "Yes")
    val positivePensionsTakenBeforeAmtSummaryRow = SummaryRowModel("pensionsTakenBeforeAmt", Some(controllers.routes.IP2016Controller.pensionsTakenBefore()), None,  false, "£1,001,000")
    val negativePensionsTakenBeforeSummaryRow = SummaryRowModel("pensionsTakenBefore", Some(controllers.routes.IP2016Controller.pensionsTakenBefore()), None,  false, "No")

    val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes", Some(BigDecimal(1100))))
    val negativePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no", None))
    val positivePensionsTakenBetweenSummaryRow = SummaryRowModel("pensionsTakenBetween", Some(controllers.routes.IP2016Controller.pensionsTakenBetween()), None,  false, "Yes")
    val positivePensionsTakenBetweenAmtSummaryRow = SummaryRowModel("pensionsTakenBetweenAmt", Some(controllers.routes.IP2016Controller.pensionsTakenBetween()), None,  false, "£1,100")
    val negativePensionsTakenBetweenSummaryRow = SummaryRowModel("pensionsTakenBetween", Some(controllers.routes.IP2016Controller.pensionsTakenBetween()), None,  false, "No")

    val positiveOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010))))
    val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
    val negativeOverseasPensionsSummaryRow = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "No")
    val positiveOverseasPensionsSummaryRow = SummaryRowModel("overseasPensions", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "Yes")
    val positiveOverseasPensionsAmtSummaryRow = SummaryRowModel("overseasPensionsAmt", Some(controllers.routes.IP2016Controller.overseasPensions()), None,  false, "£1,010")

    val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
    val currentPensionsSummaryRow = SummaryRowModel("currentPensionsAmt", Some(controllers.routes.IP2016Controller.currentPensions()), None, false, "£1,001")

    val positivePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))
    val negativePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))
    val negativePensionDebitsSummaryRow = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits()), None,  false, "No")
    val positivePensionDebitsSummaryRow = SummaryRowModel("pensionDebits", Some(controllers.routes.IP2016Controller.pensionDebits()), None,  false, "Yes")

    def totalPensionsAmountSummaryRow(totalAmount: String) = SummaryRowModel("totalPensionsAmt", None, None, true, totalAmount)

    val psoDetailsTuple = "psoDetails" -> Json.toJson(PSODetailsModel(Some(1), Some(2), Some(2016), BigDecimal(10000)))

    val psoDetailsSummaryRow = SummaryRowModel("psoDetails", Some(controllers.routes.IP2016Controller.psoDetails), Some(controllers.routes.IP2016Controller.removePsoDetails), false, "£10,000", "1 February 2016")

    "handle invalid summary data" when {
        implicit val protectionType = ApplicationType.IP2016

      "there is no data" in {
        val tstMap = CacheMap(tstId, Map.empty)
        TestSummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no pensions taken model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
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
        implicit val protectionType = ApplicationType.IP2016

      "all answers are negative" in {
        val testSummaryModel = SummaryModel(protectionType, true, List(
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
        val testSummaryModel = SummaryModel(protectionType, false,
                                        List(
                                          SummarySectionModel(List(
                                            positivePensionsTakenSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsTakenBeforeSummaryRow, positivePensionsTakenBeforeAmtSummaryRow)),
                                          SummarySectionModel(List(
                                            positivePensionsTakenBetweenSummaryRow, positivePensionsTakenBetweenAmtSummaryRow)),
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
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple,
                                        psoDetailsTuple
                                        )
                            )

        TestSummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }

      "pensions taken 'yes', pensions taken before 'no', pensions taken between 'no'" in {
        val testSummaryModel = SummaryModel(protectionType, true,
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
