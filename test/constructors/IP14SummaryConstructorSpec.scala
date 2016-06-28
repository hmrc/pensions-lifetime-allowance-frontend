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

class IP14SummaryConstructorSpec extends UnitSpec with WithFakeApplication {
  object TestIP14SummaryConstructor extends IP14SummaryConstructor
  val tstId = "testUserID"
  "Summary Constructor" should {

    val positivePensionsTakenTuple = "ip14PensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
    val negativePensionsTakenTuple = "ip14PensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))
    val negativePensionsTakenSummaryRow = SummaryRowModel("ip14PensionsTaken", Some(controllers.routes.IP2014Controller.ip14PensionsTaken()), "No")
    val positivePensionsTakenSummaryRow = SummaryRowModel("ip14PensionsTaken", Some(controllers.routes.IP2014Controller.ip14PensionsTaken()), "Yes")

    val positivePensionsTakenBeforeTuple = "ip14PensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes", Some(BigDecimal(1000))))
    val negativePensionsTakenBeforeTuple = "ip14PensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no", None))
    val positivePensionsTakenBeforeSummaryRow = SummaryRowModel("ip14PensionsTakenBefore", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBefore()), "Yes")
    val positivePensionsTakenBeforeAmtSummaryRow = SummaryRowModel("ip14PensionsTakenBeforeAmt", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBefore()), "£1,000.00")
    val negativePensionsTakenBeforeSummaryRow = SummaryRowModel("ip14PensionsTakenBefore", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBefore()), "No")

    val positivePensionsTakenBetweenTuple = "ip14PensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes", Some(BigDecimal(1100))))
    val negativePensionsTakenBetweenTuple = "ip14PensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no", None))
    val positivePensionsTakenBetweenSummaryRow = SummaryRowModel("ip14PensionsTakenBetween", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBetween()), "Yes")
    val positivePensionsTakenBetweenAmtSummaryRow = SummaryRowModel("ip14PensionsTakenBetweenAmt", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBetween()), "£1,100.00")
    val negativePensionsTakenBetweenSummaryRow = SummaryRowModel("ip14PensionsTakenBetween", Some(controllers.routes.IP2014Controller.ip14PensionsTakenBetween()), "No")

    val positiveOverseasPensionsTuple = "ip14OverseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010))))
    val negativeOverseasPensionsTuple = "ip14OverseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))
    val negativeOverseasPensionsSummaryRow = SummaryRowModel("ip14OverseasPensions", Some(controllers.routes.IP2014Controller.ip14OverseasPensions()), "No")
    val positiveOverseasPensionsSummaryRow = SummaryRowModel("ip14OverseasPensions", Some(controllers.routes.IP2014Controller.ip14OverseasPensions()), "Yes")
    val positiveOverseasPensionsAmtSummaryRow = SummaryRowModel("ip14OverseasPensionsAmt", Some(controllers.routes.IP2014Controller.ip14OverseasPensions()), "£1,010.00")

    val validCurrentPensionsTuple = "ip14CurrentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))
    val currentPensionsSummaryRow = SummaryRowModel("ip14CurrentPensionsAmt", Some(controllers.routes.IP2014Controller.ip14CurrentPensions()), "£1,001.00")

    val positivePensionDebitsTuple =  "ip14PensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))
    val negativePensionDebitsTuple =  "ip14PensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))
    val negativePensionDebitsSummaryRow = SummaryRowModel("ip14PensionDebits", Some(controllers.routes.IP2014Controller.ip14PensionDebits()), "No")
    val positivePensionDebitsSummaryRow = SummaryRowModel("ip14PensionDebits", Some(controllers.routes.IP2014Controller.ip14PensionDebits()), "Yes")

    def totalPensionsAmountSummaryRow(totalAmount: String) = SummaryRowModel("ip14TotalPensionsAmt", None, totalAmount)

    def numberOfPSOsTuple(numberOfPSOs: Int): (String, JsValue) = "ip14NumberOfPSOs" -> Json.toJson(NumberOfPSOsModel(Some(numberOfPSOs.toString)))
    def numberOfPSOsSummaryRow(numberOfPSOs: Int) = SummaryRowModel("ip14NumberOfPSOsAmt", Some(controllers.routes.IP2014Controller.ip14NumberOfPSOs()), numberOfPSOs.toString)

    val psoDetails1Tuple = "ip14PsoDetails1" -> Json.toJson(PSODetailsModel(1, Some(1), Some(2), Some(2016), BigDecimal(10000)))
    val psoDetails2Tuple = "ip14PsoDetails2" -> Json.toJson(PSODetailsModel(2, Some(2), Some(3), Some(2016), BigDecimal(11000)))
    val psoDetails3Tuple = "ip14PsoDetails3" -> Json.toJson(PSODetailsModel(3, Some(3), Some(4), Some(2016), BigDecimal(10100)))
    val psoDetails4Tuple = "ip14PsoDetails4" -> Json.toJson(PSODetailsModel(4, Some(4), Some(5), Some(2016), BigDecimal(10010)))
    val psoDetails5Tuple = "ip14PsoDetails5" -> Json.toJson(PSODetailsModel(5, Some(5), Some(6), Some(2016), BigDecimal(10001)))

    val psoDetails1SummaryRow = SummaryRowModel("ip14PsoDetails1", Some(controllers.routes.IP2014Controller.ip14PsoDetails("1")), "£10,000.00", "1 February 2016")
    val psoDetails2SummaryRow = SummaryRowModel("ip14PsoDetails2", Some(controllers.routes.IP2014Controller.ip14PsoDetails("2")), "£11,000.00", "2 March 2016")
    val psoDetails3SummaryRow = SummaryRowModel("ip14PsoDetails3", Some(controllers.routes.IP2014Controller.ip14PsoDetails("3")), "£10,100.00", "3 April 2016")
    val psoDetails4SummaryRow = SummaryRowModel("ip14PsoDetails4", Some(controllers.routes.IP2014Controller.ip14PsoDetails("4")), "£10,010.00", "4 May 2016")
    val psoDetails5SummaryRow = SummaryRowModel("ip14PsoDetails5", Some(controllers.routes.IP2014Controller.ip14PsoDetails("5")), "£10,001.00", "5 June 2016")

    "handle invalid summary data" when {
        implicit val protectionType = ApplicationType.IP2014

      "there is no data" in {
        val tstMap = CacheMap(tstId, Map.empty)
        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no pensions taken model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no overseas pensions model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "there is no current pensions model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positiveOverseasPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pensions taken 'yes' and no pensions taken before model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pensions taken 'yes' and no pensions taken between model" in {
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBeforeTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pension debits 'yes' and no number of PSOs model" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }

      "pension debits 'yes', 3 PSOs and not enough PSO details models" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple,
                                        numberOfPSOsTuple(3),
                                        psoDetails1Tuple,
                                        psoDetails2Tuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe None
      }
    }

    "handle valid summary data" when {
        implicit val protectionType = ApplicationType.IP2014

      "all answers are negative" in {
        val testSummaryModel = SummaryModel(List(negativePensionsTakenSummaryRow, negativeOverseasPensionsSummaryRow, currentPensionsSummaryRow, totalPensionsAmountSummaryRow("£1,001.00")), List(negativePensionDebitsSummaryRow))
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        negativeOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }

      "all answers are positive" in {
        val testSummaryModel = SummaryModel(
                                        List(
                                            positivePensionsTakenSummaryRow,
                                            positivePensionsTakenBeforeSummaryRow, positivePensionsTakenBeforeAmtSummaryRow,
                                            positivePensionsTakenBetweenSummaryRow, positivePensionsTakenBetweenAmtSummaryRow,
                                            positiveOverseasPensionsSummaryRow, positiveOverseasPensionsAmtSummaryRow,
                                            currentPensionsSummaryRow,
                                            totalPensionsAmountSummaryRow("£4,111.00")
                                            ),
                                        List(
                                            positivePensionDebitsSummaryRow,
                                            numberOfPSOsSummaryRow(5),
                                            psoDetails1SummaryRow,
                                            psoDetails2SummaryRow,
                                            psoDetails3SummaryRow,
                                            psoDetails4SummaryRow,
                                            psoDetails5SummaryRow
                                            )
                                        )

        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        positivePensionsTakenBeforeTuple,
                                        positivePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        positivePensionDebitsTuple,
                                        numberOfPSOsTuple(5),
                                        psoDetails1Tuple,
                                        psoDetails2Tuple,
                                        psoDetails3Tuple,
                                        psoDetails4Tuple,
                                        psoDetails5Tuple
                                        )
                            )

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }

      "pensions taken 'yes', pensions taken before 'no', pensions taken between 'no'" in {
        val testSummaryModel = SummaryModel(
                                        List(
                                            positivePensionsTakenSummaryRow,
                                            negativePensionsTakenBeforeSummaryRow,
                                            negativePensionsTakenBetweenSummaryRow,
                                            positiveOverseasPensionsSummaryRow, positiveOverseasPensionsAmtSummaryRow,
                                            currentPensionsSummaryRow,
                                            totalPensionsAmountSummaryRow("£2,011.00")
                                            ),
                                        List(negativePensionDebitsSummaryRow)
                                        )

        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
                                        negativePensionsTakenBeforeTuple,
                                        negativePensionsTakenBetweenTuple,
                                        positiveOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple
                                        )
                            )

        TestIP14SummaryConstructor.createSummaryData(tstMap) shouldBe Some(testSummaryModel)
      }
    }
  }
}
