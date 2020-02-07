/*
 * Copyright 2020 HM Revenue & Customs
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

package enums

import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.play.test.UnitSpec

class ApplicationTypeSpec extends UnitSpec {
  "ApplicationType" when {
    "translating from string to enum" should {

      "translate IP2014" in {
        ApplicationType.fromString("ip2014") shouldBe Some(ApplicationType.IP2014)
      }

      "translate IP2016" in {
        ApplicationType.fromString("ip2016") shouldBe Some(ApplicationType.IP2016)
      }

      "translate FP2016" in {
        ApplicationType.fromString("fp2016") shouldBe Some(ApplicationType.FP2016)
      }

      "translate existing protections" in {
        ApplicationType.fromString("existingprotections") shouldBe Some(ApplicationType.existingProtections)
      }

      "handle an incorrect protection string" in {
        ApplicationType.fromString("garble") shouldBe None
      }

    }
  }
}
