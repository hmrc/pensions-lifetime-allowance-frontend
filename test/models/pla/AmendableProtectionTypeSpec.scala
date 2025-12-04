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

    "return correct AmendableProtectionType" when {

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

    "throw IllegalArgumentException" when {
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

}
