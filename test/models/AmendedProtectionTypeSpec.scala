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

package models

import models.pla.AmendableProtectionType
import models.pla.response.ProtectionType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendedProtectionTypeSpec extends AnyWordSpec with Matchers {

  "toProtectionType" should {
    "return the correct protection type" when
      Seq(
        AmendedProtectionType.IndividualProtection2014    -> ProtectionType.IndividualProtection2014,
        AmendedProtectionType.IndividualProtection2016    -> ProtectionType.IndividualProtection2016,
        AmendedProtectionType.IndividualProtection2014LTA -> ProtectionType.IndividualProtection2014LTA,
        AmendedProtectionType.IndividualProtection2016LTA -> ProtectionType.IndividualProtection2016LTA,
        AmendedProtectionType.FixedProtection2016         -> ProtectionType.FixedProtection2016,
        AmendedProtectionType.FixedProtection2016LTA      -> ProtectionType.FixedProtection2016LTA
      ).foreach { case (amendedProtectionType, protectionType) =>
        s"provided with '$protectionType'" in {
          amendedProtectionType.toProtectionType shouldBe protectionType
        }
      }
  }

  "from AmendableProtectionType" should {
    "return the correct protection type" when
      Seq(
        AmendableProtectionType.IndividualProtection2014    -> AmendedProtectionType.IndividualProtection2014,
        AmendableProtectionType.IndividualProtection2014LTA -> AmendedProtectionType.IndividualProtection2014LTA,
        AmendableProtectionType.IndividualProtection2016    -> AmendedProtectionType.IndividualProtection2016,
        AmendableProtectionType.IndividualProtection2016LTA -> AmendedProtectionType.IndividualProtection2016LTA
      ).foreach { case (amendableProtectionType, amendedProtectionType) =>
        s"provided with '$amendableProtectionType'" in {
          AmendedProtectionType.from(amendableProtectionType) shouldBe amendedProtectionType
        }
      }
  }

  "tryFrom ProtectionType" should {
    val cases = Seq(
      ProtectionType.IndividualProtection2014    -> AmendedProtectionType.IndividualProtection2014,
      ProtectionType.IndividualProtection2014LTA -> AmendedProtectionType.IndividualProtection2014LTA,
      ProtectionType.IndividualProtection2016    -> AmendedProtectionType.IndividualProtection2016,
      ProtectionType.IndividualProtection2016LTA -> AmendedProtectionType.IndividualProtection2016LTA,
      ProtectionType.FixedProtection2016         -> AmendedProtectionType.FixedProtection2016,
      ProtectionType.FixedProtection2016LTA      -> AmendedProtectionType.FixedProtection2016LTA
    )
    "return Some containing correct protection type" when
      cases.foreach { case (protectionType, amendedProtectionType) =>
        s"provided with '$protectionType'" in {
          AmendedProtectionType.tryFrom(protectionType) shouldBe Some(amendedProtectionType)
        }
      }

    "return None" when
      ProtectionType.values.diff(cases.map(_._1)).foreach { protectionType =>
        s"provided with '$protectionType'" in {
          AmendedProtectionType.tryFrom(protectionType) shouldBe None
        }
      }
  }

}
