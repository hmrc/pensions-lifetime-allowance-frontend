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

package models.display

import models.pla.response.ProtectionType
import models.pla.response.ProtectionType._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PrintDisplayModelSpec extends AnyWordSpec with Matchers {

  "PrintDisplayModel on isFixedProtection2016" should {

    val nino = "AB123456A"
    val model = PrintDisplayModel(
      firstName = "Jim",
      surname = "Davis",
      nino = nino,
      protectionType = "IP2016",
      status = "active",
      psaCheckReference = "PSA33456789",
      protectionReference = "IP123456789001",
      protectedAmount = Some("1,200,000"),
      certificateDate = Some("23/02/2015"),
      certificateTime = Some("12:34:56")
    )

    val fixedProtectionTypes = Seq(FixedProtection2016.toString, FixedProtection2016LTA.toString, "FP2016")

    "return true" when
      fixedProtectionTypes.foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          val protectionModel = model.copy(protectionType = protectionType)

          protectionModel.isFixedProtection2016 shouldBe true
        }
      }

    "return false" when {

      val testScenarios = (ProtectionType.values.map(_.toString) ++ Seq("IP2014", "IP2016")).diff(fixedProtectionTypes)

      testScenarios.foreach { protectionType =>
        s"ProtectionModel contains protectionType: '$protectionType'" in {
          val protectionModel = model.copy(protectionType = protectionType)

          protectionModel.isFixedProtection2016 shouldBe false
        }
      }
    }
  }

}
