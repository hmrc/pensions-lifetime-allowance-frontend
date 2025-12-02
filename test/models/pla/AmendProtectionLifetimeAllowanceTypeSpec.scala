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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AmendableProtectionTypeSpec extends AnyWordSpec with Matchers {

  "AmendableProtectionType on from" should {

    "return correct AmendableProtectionType" when {

      val testScenarios = Seq(
        "IP2014" -> IndividualProtection2014,
        "IP2016" -> IndividualProtection2016
      ) ++ AmendableProtectionType.values.map(protectionType => protectionType.toString -> protectionType)

      testScenarios.foreach { case (input, expectedType) =>
        s"provided with '$input' value" in {
          AmendableProtectionType.fromProtectionType(input) shouldBe expectedType
        }
      }
    }

    "throw IllegalArgumentException" when {
      import models.pla.response.ProtectionType._

      val testValues = Seq(
        "Unknown",
        "FP2016",
        "Primary",
        "Enhanced",
        "Fixed",
        "FP2014"
      ) ++ Seq(
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
      ).map(_.toString)

      testValues.foreach { input =>
        s"provided with '$input' value" in {
          val exc =
            the[IllegalArgumentException] thrownBy AmendableProtectionType.fromProtectionType(input)

          exc.getMessage shouldBe s"Cannot create AmendableProtectionType from String: $input"
        }
      }
    }
  }

}
