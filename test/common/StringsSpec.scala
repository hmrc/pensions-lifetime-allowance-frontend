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

package common

import uk.gov.hmrc.play.test.UnitSpec
import enums.ApplicationType

class StringsSpec extends UnitSpec {

  "nameString" should {

    "correctly create an IP14 keystore name string" in {
      implicit val protectionType = ApplicationType.IP2014
      Strings.nameString("testString") shouldBe "ip14TestString"
    }

    "leave an IP16 keystore name string unchanged" in {
      implicit val protectionType = ApplicationType.IP2016
      Strings.nameString("testString") shouldBe "testString"
    }

    "correctly create an FP16 keystore name string" in {
      implicit val protectionType = ApplicationType.FP2016
      Strings.nameString("testString") shouldBe "fp16TestString"
    }
  }

  "statusString" should {
    "Populate the protection status string" when {
      "the protection is open" in {
        Strings.statusString(Some("Open")) shouldBe "open"
      }
      "the protection is dormant" in {
        Strings.statusString(Some("Dormant")) shouldBe "dormant"
      }
      "the protection is withdrawn" in {
        Strings.statusString(Some("Withdrawn")) shouldBe "withdrawn"
      }
      "the protection is expired" in {
        Strings.statusString(Some("Expired")) shouldBe "expired"
      }
      "the protection is unsuccessful" in {
        Strings.statusString(Some("Unsuccessful")) shouldBe "unsuccessful"
      }
      "the protection is rejected" in {
        Strings.statusString(Some("Rejected")) shouldBe "rejected"
      }
      "there is no status recorded" in {
        Strings.statusString(None) shouldBe "notRecorded"
      }
    }
  }

  "protectionTypeString" should {
    "Populate the protection type string" when {
      "the protection is FP2016" in {
        Strings.protectionTypeString(Some("FP2016")) shouldBe "FP2016"
      }
      "the protection is IP2014" in {
        Strings.protectionTypeString(Some("IP2014")) shouldBe "IP2014"
      }
      "the protection is IP2016" in {
        Strings.protectionTypeString(Some("IP2016")) shouldBe "IP2016"
      }
      "the protection is primary" in {
        Strings.protectionTypeString(Some("Primary")) shouldBe "primary"
      }
      "the protection is enhanced" in {
        Strings.protectionTypeString(Some("Enhanced")) shouldBe "enhanced"
      }
      "the protection is fixed" in {
        Strings.protectionTypeString(Some("Fixed")) shouldBe "fixed"
      }
      "the protection is FP2014" in {
        Strings.protectionTypeString(Some("FP2014")) shouldBe "FP2014"
      }
      "the protection type is not recorded" in {
        Strings.protectionTypeString(None) shouldBe "notRecorded"
      }
    }
  }
}
