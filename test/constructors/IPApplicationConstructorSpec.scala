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

class IPApplicationConstructorSpec extends UnitSpec with WithFakeApplication {
  
  val tstId = "testUserID"
  "IP Application Constructor" should {

    val positivePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
    val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))

    val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes", Some(BigDecimal(1000))))
    val negativePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("no", None))

    val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes", Some(BigDecimal(1100))))
    val negativePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("no", None))

    val positiveOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010))))
    val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))

    val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))

    val positivePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsAmtModel(Some("yes")))
    val negativePensionDebitsTuple =  "pensionDebits" -> Json.toJson(PensionDebitsAmtModel(Some("no")))

    def numberOfPSOsTuple(numberOfPSOs: Int): (String, JsValue) = "numberOfPSOs" -> Json.toJson(NumberOfPSOsModel(Some(numberOfPSOs.toString)))

    val psoDetails1Tuple = "psoDetails1" -> Json.toJson(PSODetailsModel(1, Some(1), Some(2), Some(2016), BigDecimal(10000)))
    val psoDetails2Tuple = "psoDetails2" -> Json.toJson(PSODetailsModel(2, Some(2), Some(3), Some(2016), BigDecimal(11000)))
    val psoDetails3Tuple = "psoDetails3" -> Json.toJson(PSODetailsModel(3, Some(3), Some(4), Some(2016), BigDecimal(10100)))
    val psoDetails4Tuple = "psoDetails4" -> Json.toJson(PSODetailsModel(4, Some(4), Some(5), Some(2016), BigDecimal(10010)))
    val psoDetails5Tuple = "psoDetails5" -> Json.toJson(PSODetailsModel(5, Some(5), Some(6), Some(2016), BigDecimal(10001)))

    "Create an application object" when {
        implicit val protectionType = ApplicationType.IP2016

      "all answers are negative" in {
        val tstMap = CacheMap(tstId, Map(negativePensionsTakenTuple,
                                        positivePensionsTakenBetweenTuple,
                                        negativeOverseasPensionsTuple,
                                        validCurrentPensionsTuple,
                                        negativePensionDebitsTuple))

        val tstModel = IPApplicationModel(protectionType = "IP2016",
                                        relevantAmount = 1001,
                                        uncrystallisedRights = Some(1001),
                                        preADayPensionInPayment = Some(0),
                                        postADayBenefitCrystallisationEvents = Some(0),
                                        nonUKRights = Some(0))
        IPApplicationConstructor.createIPApplication(tstMap) shouldBe tstModel
      }

      "all answers are positive" in {
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
                                        psoDetails5Tuple))

        val tstModel = IPApplicationModel(protectionType = "IP2016",
                                        relevantAmount = 4111,
                                        uncrystallisedRights = Some(1001),
                                        preADayPensionInPayment = Some(1000),
                                        postADayBenefitCrystallisationEvents = Some(1100),
                                        nonUKRights = Some(1010),
                                        pensionDebits = Some(List(
                                            PensionDebit("2016-02-01", 10000),
                                            PensionDebit("2016-03-02", 11000),
                                            PensionDebit("2016-04-03", 10100),
                                            PensionDebit("2016-05-04", 10010),
                                            PensionDebit("2016-06-05", 10001)
                                            ))
                                        )
        IPApplicationConstructor.createIPApplication(tstMap) shouldBe tstModel
      }
    }

    "Throw an assertion error" when {

      "passed incomplete data" in {
        implicit val protectionType = ApplicationType.IP2016
        val tstMap = CacheMap(tstId, Map(positivePensionsTakenTuple,
          positivePensionsTakenBeforeTuple))
        val thrown = intercept[Error] {
          IPApplicationConstructor.createIPApplication(tstMap)
        }

        thrown.getMessage shouldBe """assertion failed: Invalid application data provided to createIPApplication for IP2016. Data: CacheMap(testUserID,Map(pensionsTaken -> {"pensionsTaken":"yes"}, pensionsTakenBefore -> {"pensionsTakenBefore":"yes","pensionsTakenBeforeAmt":1000}))"""
      }
    }
  }
}
