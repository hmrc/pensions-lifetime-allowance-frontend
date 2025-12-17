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

import models.pla.response.ProtectionType._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtectionTypeSpec extends AnyWordSpec with Matchers {

  "isFixedProtection2016" should {
    val fixedProtection2016Types = Seq(
      FixedProtection2016,
      FixedProtection2016LTA
    )

    "return true" when
      fixedProtection2016Types.foreach { protectionType =>
        s"provided with '$protectionType'" in {
          protectionType.isFixedProtection2016 shouldBe true
        }
      }

    "return false" when
      ProtectionType.values.diff(fixedProtection2016Types).foreach { protectionType =>
        s"provided with '$protectionType'" in {
          protectionType.isFixedProtection2016 shouldBe false
        }
      }
  }

}
