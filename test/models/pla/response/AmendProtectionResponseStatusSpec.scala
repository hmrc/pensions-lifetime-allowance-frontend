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

package models.pla.response

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendProtectionResponseStatusSpec extends AnyWordSpec with Matchers {

  "toProtectionStatus" should {
    "return correct protection status" when
      Seq(
        AmendProtectionResponseStatus.Open      -> ProtectionStatus.Open,
        AmendProtectionResponseStatus.Dormant   -> ProtectionStatus.Dormant,
        AmendProtectionResponseStatus.Withdrawn -> ProtectionStatus.Withdrawn
      ).foreach { case (responseStatus, status) =>
        s"provided with '$responseStatus'" in {
          responseStatus.toProtectionStatus shouldBe status
        }
      }
  }

}
