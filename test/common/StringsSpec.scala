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

import enums.ApplicationType
import models.ProtectionModel
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}
import models.pla.response.{ProtectionStatus, ProtectionType}
import org.scalatest.OptionValues
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

  "cache key" should {
    "correctly generate the cache key" when {
      val testValues = Seq(
        ("ip2014", "open")        -> "OpenIP2014Amendment",
        ("ip2016", "dormant")     -> "DormantIP2016Amendment",
        ("ip2014-lta", "dormant") -> "DormantIP2014-LTAAmendment",
        ("ip2016-lta", "open")    -> "OpenIP2016-LTAAmendment"
      )

      "saving a protection model to cache" when
        testValues.foreach { case ((protectionType, status), expected) =>
          s"protection type is $protectionType and status is $status" in {
            val protectionModel = ProtectionModel(
              protectionType = Some(protectionType),
              status = Some(status),
              psaCheckReference = None,
              version = None,
              protectionID = None
            )

            Strings.cacheProtectionName(protectionModel) shouldBe expected
          }
        }

      "retrieving a protection from the cache" when
        testValues.foreach { case ((protectionType, status), expected) =>
          s"protection type is $protectionType and status is $status" in {
            Strings.cacheAmendFetchString(protectionType, status) shouldBe expected
          }
        }
    }

    "use same cache key for saving and retrieving" when {

      val testValues = for {
        protectionType <- AmendProtectionLifetimeAllowanceType.values.map(_.urlValue)
        status         <- AmendProtectionRequestStatus.values.map(_.urlValue)
      } yield (protectionType, status)

      testValues.foreach { case (protectionType, status) =>
        s"protection type is $protectionType and status is $status" in {
          val protectionModel = ProtectionModel(
            protectionType = Some(protectionType),
            status = Some(status),
            psaCheckReference = None,
            protectionID = None,
            version = None
          )

          Strings.cacheAmendFetchString(protectionType, status) shouldBe Strings.cacheProtectionName(protectionModel)
        }
      }
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
      import ProtectionType._

      "the protection is FP2016" in {
        Strings.protectionTypeString(Some("FP2016")) shouldBe FixedProtection2016.toString
      }
      "the protection is IP2014" in {
        Strings.protectionTypeString(Some("IP2014")) shouldBe IndividualProtection2014.toString
      }
      "the protection is IP2014-LTA" in {
        Strings.protectionTypeString(Some("IP2014-LTA")) shouldBe IndividualProtection2014LTA.toString
      }
      "the protection is IP2016" in {
        Strings.protectionTypeString(Some("IP2016")) shouldBe IndividualProtection2016.toString
      }
      "the protection is IP2016-LTA" in {
        Strings.protectionTypeString(Some("IP2016-LTA")) shouldBe IndividualProtection2016LTA.toString
      }
      "the protection is primary" in {
        Strings.protectionTypeString(Some("Primary")) shouldBe PrimaryProtection.toString
      }
      "the protection is enhanced" in {
        Strings.protectionTypeString(Some("Enhanced")) shouldBe EnhancedProtection.toString
      }
      "the protection is fixed" in {
        Strings.protectionTypeString(Some("Fixed")) shouldBe FixedProtection.toString
      }
      "the protection is FP2014" in {
        Strings.protectionTypeString(Some("FP2014")) shouldBe FixedProtection2014.toString
      }

      ProtectionType.values.foreach(protectionType =>
        s"the protection is $protectionType" in {
          Strings.protectionTypeString(Some(protectionType.toString)) shouldBe protectionType.toString
        }
      )
    }

    "Populate the protection type string with notRecorded" when {
      "the protection type is an unknown value" in {
        Strings.protectionTypeString(Some("unknown protection type")) shouldBe "notRecorded"
      }

      "the protection type is missing" in {
        Strings.protectionTypeString(None) shouldBe "notRecorded"
      }
    }
  }

  "protectionTypeUrlString" should {
    "Populate the protection type URL string" when {
      import AmendProtectionLifetimeAllowanceType._

      "the protection is IP2014" in {
        Strings.protectionTypeUrlString(Some("IP2014")) shouldBe IndividualProtection2014.urlValue
      }
      "the protection is IP2014-LTA" in {
        Strings.protectionTypeUrlString(Some("IP2014-LTA")) shouldBe IndividualProtection2014LTA.urlValue
      }
      "the protection is IP2016" in {
        Strings.protectionTypeUrlString(Some("IP2016")) shouldBe IndividualProtection2016.urlValue
      }
      "the protection is IP2016-LTA" in {
        Strings.protectionTypeUrlString(Some("IP2016-LTA")) shouldBe IndividualProtection2016LTA.urlValue
      }

      AmendProtectionLifetimeAllowanceType.values.foreach(protectionType =>
        s"the protection is $protectionType" in {
          Strings.protectionTypeUrlString(Some(protectionType.toString)) shouldBe protectionType.urlValue
        }
      )
    }

    "Populate the protection type URL string with notRecorded" when {
      "the protection type is an unknown value" in {
        Strings.protectionTypeUrlString(Some("unknown protection type")) shouldBe "notRecorded"
      }

      "the protection type is missing" in {
        Strings.protectionTypeUrlString(None) shouldBe "notRecorded"
      }
    }
  }

  "protectionTypeElementIdString" should {
    "Populate the protection type element Id string" when {
      import AmendProtectionLifetimeAllowanceType._

      "the protection is IP2014" in {
        Strings.protectionTypeElementIdString(Some("IP2014")) shouldBe IndividualProtection2014.urlValue.toUpperCase
      }
      "the protection is IP2014-LTA" in {
        Strings.protectionTypeElementIdString(
          Some("IP2014-LTA")
        ) shouldBe IndividualProtection2014LTA.urlValue.toUpperCase
      }
      "the protection is IP2016" in {
        Strings.protectionTypeElementIdString(Some("IP2016")) shouldBe IndividualProtection2016.urlValue.toUpperCase
      }
      "the protection is IP2016-LTA" in {
        Strings.protectionTypeElementIdString(
          Some("IP2016-LTA")
        ) shouldBe IndividualProtection2016LTA.urlValue.toUpperCase
      }

      AmendProtectionLifetimeAllowanceType.values.foreach(protectionType =>
        s"the protection is $protectionType" in {
          Strings.protectionTypeElementIdString(
            Some(protectionType.toString)
          ) shouldBe protectionType.urlValue.toUpperCase
        }
      )
    }

    "Populate the protection type element Id string with notRecorded" when {
      "the protection type is an unknown value" in {
        Strings.protectionTypeElementIdString(Some("unknown protection type")) shouldBe "NOTRECORDED"
      }

      "the protection type is missing" in {
        Strings.protectionTypeElementIdString(None) shouldBe "NOTRECORDED"
      }
    }
  }

}
