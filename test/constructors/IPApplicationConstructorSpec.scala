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
import play.api.libs.json.Json
import testHelpers.FakeApplication
import models.cache.CacheMap

import java.time.LocalDate

class IPApplicationConstructorSpec extends FakeApplication {

  val tstId = "testUserID"

  "IP Application Constructor" should {

    val positivePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("yes")))
    val negativePensionsTakenTuple = "pensionsTaken" -> Json.toJson(PensionsTakenModel(Some("no")))

    val positivePensionsTakenBeforeTuple = "pensionsTakenBefore" -> Json.toJson(PensionsTakenBeforeModel("yes"))
    val validPensionsWorthBeforeTuple =
      "pensionsWorthBefore" -> Json.toJson(PensionsWorthBeforeModel(Some(BigDecimal(1000))))

    val positivePensionsTakenBetweenTuple = "pensionsTakenBetween" -> Json.toJson(PensionsTakenBetweenModel("yes"))
    val validPensionUsedBetweenTuple =
      "pensionsUsedBetween" -> Json.toJson(PensionsUsedBetweenModel(Some(BigDecimal(1100))))

    val positiveOverseasPensionsTuple =
      "overseasPensions" -> Json.toJson(OverseasPensionsModel("yes", Some(BigDecimal(1010))))
    val negativeOverseasPensionsTuple = "overseasPensions" -> Json.toJson(OverseasPensionsModel("no", None))

    val validCurrentPensionsTuple = "currentPensions" -> Json.toJson(CurrentPensionsModel(Some(BigDecimal(1001))))

    val positivePensionDebitsTuple = "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("yes")))
    val negativePensionDebitsTuple = "pensionDebits" -> Json.toJson(PensionDebitsModel(Some("no")))

    val psoDetailsTuple =
      "psoDetails" -> Json.toJson(PSODetailsModel(LocalDate.of(2016, 2, 1), Some(BigDecimal(10000))))

    "Create an application object" when {
      implicit val protectionType = ApplicationType.IP2016

      "all answers are negative" in {
        val tstMap = CacheMap(
          tstId,
          Map(
            negativePensionsTakenTuple,
            positivePensionsTakenBetweenTuple,
            validPensionUsedBetweenTuple,
            negativeOverseasPensionsTuple,
            validCurrentPensionsTuple,
            negativePensionDebitsTuple
          )
        )

        val tstModel = IPApplicationModel(
          protectionType = "IP2016",
          relevantAmount = 1001,
          uncrystallisedRights = Some(1001),
          preADayPensionInPayment = Some(0),
          postADayBenefitCrystallisationEvents = Some(0),
          nonUKRights = Some(0)
        )
        IPApplicationConstructor.createIPApplication(tstMap) shouldBe tstModel
      }

      "all answers are positive" in {
        val tstMap = CacheMap(
          tstId,
          Map(
            positivePensionsTakenTuple,
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

        val tstModel = IPApplicationModel(
          protectionType = "IP2016",
          relevantAmount = 4111,
          uncrystallisedRights = Some(1001),
          preADayPensionInPayment = Some(1000),
          postADayBenefitCrystallisationEvents = Some(1100),
          nonUKRights = Some(1010),
          pensionDebits = Some(
            List(
              PensionDebit("2016-02-01", 10000)
            )
          )
        )
        IPApplicationConstructor.createIPApplication(tstMap) shouldBe tstModel
      }
    }

    "Throw an assertion error" when {

      "passed incomplete data" in {
        implicit val protectionType: ApplicationType.Value = ApplicationType.IP2016
        val tstMap = CacheMap(
          tstId,
          Map(positivePensionsTakenTuple, positivePensionsTakenBeforeTuple, validPensionsWorthBeforeTuple)
        )
        val thrown = intercept[Error] {
          IPApplicationConstructor.createIPApplication(tstMap)
        }

        thrown.getMessage shouldBe """assertion failed: Invalid application data provided to createIPApplication for IP2016. Data: CacheMap(testUserID,Map(pensionsTaken -> {"pensionsTaken":"yes"}, pensionsTakenBefore -> {"pensionsTakenBefore":"yes"}, pensionsWorthBefore -> {"pensionsWorthBeforeAmt":1000}))"""
      }
    }
  }

}
