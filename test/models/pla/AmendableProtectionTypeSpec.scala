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

import models.pla.AmendableProtectionType._
import models.pla.response.ProtectionType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendableProtectionTypeSpec extends AnyWordSpec with Matchers {

  "AmendableProtectionType on tryFromProtectionType" should {

    "return Some containing correct AmendableProtectionType" when {

      val testScenarios = Seq(
        ProtectionType.IndividualProtection2014    -> IndividualProtection2014,
        ProtectionType.IndividualProtection2014LTA -> IndividualProtection2014LTA,
        ProtectionType.IndividualProtection2016    -> IndividualProtection2016,
        ProtectionType.IndividualProtection2016LTA -> IndividualProtection2016LTA
      )

      testScenarios.foreach { case (input, expectedType) =>
        s"provided with '$input' value" in {
          AmendableProtectionType.tryFromProtectionType(input) shouldBe Some(expectedType)
        }
      }
    }

    "return None" when {
      import models.pla.response.ProtectionType._

      val testValues = Seq(
        FixedProtection2016,
        FixedProtection2016LTA,
        FixedProtection2014,
        FixedProtection2014LTA,
        FixedProtection,
        FixedProtectionLTA,
        PrimaryProtection,
        PrimaryProtectionLTA,
        EnhancedProtection,
        EnhancedProtectionLTA,
        PensionCreditRights,
        InternationalEnhancementS221,
        InternationalEnhancementS224
      )

      testValues.foreach { input =>
        s"provided with '$input' value" in {
          AmendableProtectionType.tryFromProtectionType(input) shouldBe None
        }
      }
    }
  }

  "pathBindable" should {
    "have correct bind implementation".that {
      "returns Right" when
        Seq(
          "individual-protection-2014"     -> IndividualProtection2014,
          "ip2014"                         -> IndividualProtection2014,
          "individual-protection-2016"     -> IndividualProtection2016,
          "ip2016"                         -> IndividualProtection2016,
          "individual-protection-2014-lta" -> IndividualProtection2014LTA,
          "individual-protection-2016-lta" -> IndividualProtection2016LTA
        ).foreach { case (string, protectionType) =>
          s"provided with '$string'" in {
            AmendableProtectionType.pathBindable.bind("", string) shouldBe Right(protectionType)
          }
        }

      "returns Left" when
        (Seq("ip2014-lta", "ip2016-lta", "fp2014", "fp2016", "fixed", "enhanced", "primary") ++ ProtectionType.values
          .map(_.toString.toLowerCase)).foreach { string =>
          s"provided with '$string'" in {
            AmendableProtectionType.pathBindable.bind("", string) shouldBe Left(s"Unknown protection type '$string'")
          }
        }
    }

    "have correct unbind implementation".that {
      "returns correct value" when {
        val cases = Seq(
          IndividualProtection2014    -> "individual-protection-2014",
          IndividualProtection2016    -> "individual-protection-2016",
          IndividualProtection2014LTA -> "individual-protection-2014-lta",
          IndividualProtection2016LTA -> "individual-protection-2016-lta"
        )

        cases.foreach { case (protectionType, string) =>
          s"provided with '$protectionType'" in {
            AmendableProtectionType.pathBindable.unbind("", protectionType) shouldBe string
          }
        }
      }
    }
  }

}
