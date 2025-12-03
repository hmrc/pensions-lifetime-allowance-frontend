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

package common

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StringsSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "cache key" should {
    "correctly generate the cache key" when {
      import models.pla.AmendableProtectionType._
      import models.pla.request.AmendProtectionRequestStatus._

      val testValues = Seq(
        (IndividualProtection2014, Open)       -> "OpenIndividualProtection2014Amendment",
        (IndividualProtection2014, Dormant)    -> "DormantIndividualProtection2014Amendment",
        (IndividualProtection2016, Open)       -> "OpenIndividualProtection2016Amendment",
        (IndividualProtection2016, Dormant)    -> "DormantIndividualProtection2016Amendment",
        (IndividualProtection2014LTA, Open)    -> "OpenIndividualProtection2014LTAAmendment",
        (IndividualProtection2014LTA, Dormant) -> "DormantIndividualProtection2014LTAAmendment",
        (IndividualProtection2016LTA, Open)    -> "OpenIndividualProtection2016LTAAmendment",
        (IndividualProtection2016LTA, Dormant) -> "DormantIndividualProtection2016LTAAmendment"
      )

      testValues.foreach { case ((protectionType, status), expected) =>
        s"protection type is $protectionType and status is $status" in {
          Strings.protectionCacheKey(protectionType, status) shouldBe expected
        }
      }
    }
  }

}
