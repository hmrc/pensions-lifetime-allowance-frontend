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

package models.pla.request

import models.pla.response.ProtectionStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendProtectionRequestStatusSpec extends AnyWordSpec with Matchers {

  "tryFromProtectionStatus" should {

    val statuses = Seq(
      ProtectionStatus.Open    -> AmendProtectionRequestStatus.Open,
      ProtectionStatus.Dormant -> AmendProtectionRequestStatus.Dormant
    )

    "return Some containing correct value" when
      statuses.foreach { case (status, amendStatus) =>
        s"status is '$status'" in {
          AmendProtectionRequestStatus.tryFromProtectionStatus(status) shouldBe Some(amendStatus)
        }
      }

    "return None" when
      ProtectionStatus.values.diff(statuses.map(_._1)).foreach { status =>
        s"status is '$status'" in {
          AmendProtectionRequestStatus.tryFromProtectionStatus(status) shouldBe None
        }
      }
  }

  "pathBindable" should {
    "have correct bind implementation".that {
      "returns Right" when
        Seq(
          "open"    -> AmendProtectionRequestStatus.Open,
          "Open"    -> AmendProtectionRequestStatus.Open,
          "dormant" -> AmendProtectionRequestStatus.Dormant,
          "Dormant" -> AmendProtectionRequestStatus.Dormant
        ).foreach { case (string, value) =>
          s"provided with '$string'" in {
            AmendProtectionRequestStatus.pathBindable.bind("", string) shouldBe Right(value)
          }
        }

      "returns Left" when {
        val inputs = Seq(
          "closed",
          "unknown",
          "",
          "gfdsfdsfhkjh"
        ) ++ ProtectionStatus.values
          .diff(Seq(ProtectionStatus.Open, ProtectionStatus.Dormant))
          .map(_.toString.toLowerCase)

        inputs.foreach { string =>
          s"provided with '$string'" in {
            AmendProtectionRequestStatus.pathBindable.bind("", string) shouldBe Left(
              s"Unknown protection status '$string'"
            )
          }
        }
      }
    }

    "have correct unbind implementation".that {
      "returns correct value" when
        Seq(
          AmendProtectionRequestStatus.Open    -> "open",
          AmendProtectionRequestStatus.Dormant -> "dormant"
        ).foreach { case (status, string) =>
          s"provided with '$status'" in {
            AmendProtectionRequestStatus.pathBindable.unbind("", status) shouldBe string
          }
        }
    }
  }

}
