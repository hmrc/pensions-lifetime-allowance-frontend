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
import enums.ApplicationType
import models.pla.response.{ProtectionStatus, ProtectionType}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StringsSpec extends AnyWordSpecLike with Matchers with OptionValues {

  "nameString" should {

    "correctly create an IP14 cache name string" in {
      implicit val protectionType = ApplicationType.IP2014
      Strings.nameString("testString") shouldBe "ip14TestString"
    }

    "leave an IP16 cache name string unchanged" in {
      implicit val protectionType = ApplicationType.IP2016
      Strings.nameString("testString") shouldBe "testString"
    }

    "correctly create an FP16 cache name string" in {
      implicit val protectionType = ApplicationType.FP2016
      Strings.nameString("testString") shouldBe "fp16TestString"
    }
  }

  "statusString" should {
    import ProtectionStatus._

    "Populate the protection status string" when {
      "the protection is open" in {
        Strings.statusString(Some(Open.toString)) shouldBe Open.toString
      }
      "the protection is dormant" in {
        Strings.statusString(Some(Dormant.toString)) shouldBe Dormant.toString
      }
      "the protection is withdrawn" in {
        Strings.statusString(Some(Withdrawn.toString)) shouldBe Withdrawn.toString
      }
      "the protection is expired" in {
        Strings.statusString(Some(Expired.toString)) shouldBe Expired.toString
      }
      "the protection is unsuccessful" in {
        Strings.statusString(Some(Unsuccessful.toString)) shouldBe Unsuccessful.toString
      }
      "the protection is rejected" in {
        Strings.statusString(Some(Rejected.toString)) shouldBe Rejected.toString
      }
      "there is no status recorded" in {
        Strings.statusString(None) shouldBe "notRecorded"
      }
    }
  }

  "protectionTypeString" should {
    "Populate the protection type string" when {

      ProtectionType.values.foreach(protectionType =>
        s"the protection is $protectionType" in {
          Strings.protectionTypeString(Some(protectionType.toString)) shouldBe protectionType.toString
        }
      )

      "the protection type is not recorded for an unknown value" in {
        Strings.protectionTypeString(Some("unknown protection type")) shouldBe "notRecorded"
      }

      "the protection type is not recorded for a missing value" in {
        Strings.protectionTypeString(None) shouldBe "notRecorded"
      }
    }
  }

}
