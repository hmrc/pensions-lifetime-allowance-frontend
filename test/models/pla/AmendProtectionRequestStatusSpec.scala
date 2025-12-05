/*
 * Copyright 2025 HM Revenue & Customs
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

package models.pla

import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.ProtectionStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendProtectionRequestStatusSpec extends AnyWordSpec with Matchers {

  "AmendProtectionRequestStatus on tryFromProtectionStatus" should {

    "return correct AmendProtectionRequestStatus" when {

      val testScenarios = Seq(
        ProtectionStatus.Open    -> AmendProtectionRequestStatus.Open,
        ProtectionStatus.Dormant -> AmendProtectionRequestStatus.Dormant
      )

      testScenarios.foreach { case (input, expectedStatus) =>
        s"provided with '$input' value" in {
          AmendProtectionRequestStatus.tryFromProtectionStatus(input) shouldBe Some(expectedStatus)
        }
      }
    }

    "throw IllegalArgumentException" when
      Seq(
        ProtectionStatus.Withdrawn,
        ProtectionStatus.Expired,
        ProtectionStatus.Unsuccessful,
        ProtectionStatus.Rejected
      ).foreach { input =>
        s"provided with '$input' value" in {
          AmendProtectionRequestStatus.tryFromProtectionStatus(input) shouldBe None
        }
      }
  }

}
