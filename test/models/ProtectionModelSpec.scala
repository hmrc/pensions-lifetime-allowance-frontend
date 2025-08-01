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

import models.pla.AmendProtectionLifetimeAllowanceType._
import models.pla.AmendProtectionRequestStatus
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtectionModelSpec extends AnyWordSpec with Matchers {

  "ProtectionModel on isAmendable" should {

    "return true" when {

      val amendableStatuses =
        Seq(
          "open",
          "dormant",
          AmendProtectionRequestStatus.Open.toString,
          AmendProtectionRequestStatus.Dormant.toString
        )
      val amendableProtectionTypes = Seq(
        "ip2014",
        "ip2016",
        IndividualProtection2014.toString,
        IndividualProtection2016.toString,
        IndividualProtection2014Lta.toString,
        IndividualProtection2016Lta.toString
      )
      val allAmendableCombinations = for {
        status         <- amendableStatuses
        protectionType <- amendableProtectionTypes
      } yield (status, protectionType)

      allAmendableCombinations.foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: '$status' and protectionType: '$protectionType''" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            protectionID = None,
            status = Some(status),
            protectionType = Some(protectionType)
          )

          protectionModel.isAmendable shouldBe true
        }
      }
    }

    "return false" when {

      val testScenarios = Seq(
        Some("open")    -> Some("other"),
        Some("dormant") -> Some("other"),
        Some("open")    -> None,
        Some("dormant") -> None,
        Some("OPEN")    -> Some("other"),
        Some("DORMANT") -> Some("other"),
        Some("OPEN")    -> None,
        Some("DORMANT") -> None,
        Some("other")   -> Some("ip2014"),
        Some("other")   -> Some("ip2016"),
        None            -> Some("ip2014"),
        None            -> Some("ip2016"),
        Some("other")   -> Some(IndividualProtection2014.toString),
        Some("other")   -> Some(IndividualProtection2016.toString),
        Some("other")   -> Some(IndividualProtection2014Lta.toString),
        Some("other")   -> Some(IndividualProtection2016Lta.toString),
        None            -> Some(IndividualProtection2014.toString),
        None            -> Some(IndividualProtection2016.toString),
        None            -> Some(IndividualProtection2014Lta.toString),
        None            -> Some(IndividualProtection2016Lta.toString)
      )

      testScenarios.foreach { case (status, protectionType) =>
        s"ProtectionModel contains status: $status and protectionType: $protectionType" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = None,
            protectionID = None,
            status = status,
            protectionType = protectionType
          )

          protectionModel.isAmendable shouldBe false
        }
      }
    }

  }

}
