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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProtectionModelSpec extends AnyWordSpec with Matchers {

  "ProtectionModel on isAmendable" should {

    "return true" when {

      val amendableStatuses        = Seq("open", "dormant")
      val amendableProtectionTypes = Seq("ip2014", "ip2016")
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

      "ProtectionModel contains status: 'open' and protectionType: 'other'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = Some("open"),
          protectionType = Some("other")
        )

        protectionModel.isAmendable shouldBe false
      }

      "ProtectionModel contains status: 'dormant' and protectionType: 'other'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = Some("dormant"),
          protectionType = Some("other")
        )

        protectionModel.isAmendable shouldBe false
      }

      "ProtectionModel contains status: 'open' and empty protectionType'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = Some("open"),
          protectionType = None
        )

        protectionModel.isAmendable shouldBe false
      }

      "ProtectionModel contains status: 'other' and protectionType: 'ip2014'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = Some("other"),
          protectionType = Some("ip2014")
        )

        protectionModel.isAmendable shouldBe false
      }

      "ProtectionModel contains status: 'other' and protectionType: 'ip2016'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = Some("other"),
          protectionType = Some("ip2016")
        )

        protectionModel.isAmendable shouldBe false
      }

      "ProtectionModel contains empty status and protectionType: 'ip2016'" in {
        val protectionModel = ProtectionModel(
          psaCheckReference = None,
          protectionID = None,
          status = None,
          protectionType = Some("ip2016")
        )

        protectionModel.isAmendable shouldBe false
      }
    }

  }

}
