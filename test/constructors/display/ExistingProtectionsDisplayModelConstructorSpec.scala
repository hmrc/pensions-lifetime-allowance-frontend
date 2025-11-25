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

package constructors.display

import common.Strings
import models.{ProtectionModel, TransformedReadResponseModel}
import models.display.{
  ExistingInactiveProtectionsByType,
  ExistingInactiveProtectionsDisplayModel,
  ExistingProtectionDisplayModel,
  ExistingProtectionsDisplayModel
}
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType
import models.pla.response.ProtectionType._
import play.api.i18n.Messages

class ExistingProtectionsDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createExistingProtectionsDisplayModel" should {

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstExistingProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2014,
            Strings.StatusUrl.Open
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val tstProtectionModelDormant = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstExistingProtectionDisplayModelDormant = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2014,
            Strings.StatusUrl.Dormant
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None
      )
      val tstTransformedReadResponseModel =
        TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = Some(tstExistingProtectionDisplayModelOpen),
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(IndividualProtection2014.toString -> List(tstExistingProtectionDisplayModelDormant))
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType.empty,
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(
        tstTransformedReadResponseModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with the same status" in {
      val tstProtectionModelOpen = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Open.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456"),
        withdrawnDate = None
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Open.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2014,
            Strings.StatusUrl.Open
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm"),
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(EnhancedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantPrimary = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(PrimaryProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantIP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2016,
            Strings.StatusUrl.Dormant
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2016.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Dormant.toString,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            Strings.ProtectionTypeUrl.IndividualProtection2014,
            Strings.StatusUrl.Dormant
          )
        ),
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstExistingProtectionModel = TransformedReadResponseModel(
        Some(tstProtectionModelOpen),
        List(
          tstProtectionModelDormantIP2016,
          tstProtectionModelDormantFP2014,
          tstProtectionModelDormantFP2016,
          tstProtectionModelDormantFixed,
          tstProtectionModelDormantIP2014,
          tstProtectionModelDormantEnhanced,
          tstProtectionModelDormantPrimary
        )
      )

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = Some(tstProtectionDisplayModelOpen),
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2016.toString -> List(
                tstProtectionDisplayModelDormantIP2016
              ),
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelDormantIP2014
              ),
              FixedProtection2016.toString -> List(
                tstProtectionDisplayModelDormantFP2016
              ),
              FixedProtection2014.toString -> List(
                tstProtectionDisplayModelDormantFP2014
              ),
              PrimaryProtection.toString -> List(
                tstProtectionDisplayModelDormantPrimary
              ),
              EnhancedProtection.toString -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection.toString -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType.empty,
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(
        tstExistingProtectionModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }

    "Correctly order existing protections with a variety of statuses" in {

      val tstProtectionModelExpiredFP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2014.toString),
        status = Some(Expired.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014.toString,
        status = Expired.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelExpiredFP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection2016.toString),
        status = Some(Expired.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelExpiredFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016.toString,
        status = Expired.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelRejectedIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Rejected.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelRejectedIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Rejected.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelUnsuccessfulIP2014 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2014.toString),
        status = Some(Unsuccessful.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelUnsuccessfulIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014.toString,
        status = Unsuccessful.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelWithdrawnPrimary = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(PrimaryProtection.toString),
        status = Some(Withdrawn.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection.toString,
        status = Withdrawn.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelWithdrawnIP2016 = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(IndividualProtection2016.toString),
        status = Some(Withdrawn.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelWithdrawnIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016.toString,
        status = Withdrawn.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(FixedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some(EnhancedProtection.toString),
        status = Some(Dormant.toString),
        certificateDate = None,
        protectedAmount = None,
        protectionReference = None,
        withdrawnDate = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection.toString,
        status = Dormant.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        withdrawnDate = None
      )

      val tstExistingProtectionModel = TransformedReadResponseModel(
        None,
        List(
          tstProtectionModelWithdrawnIP2016,
          tstProtectionModelExpiredFP2016,
          tstProtectionModelDormantFixed,
          tstProtectionModelRejectedIP2014,
          tstProtectionModelDormantEnhanced,
          tstProtectionModelExpiredFP2014,
          tstProtectionModelUnsuccessfulIP2014,
          tstProtectionModelWithdrawnPrimary
        )
      )

      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = None,
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(
              EnhancedProtection.toString -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection.toString -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2016.toString -> List(
                tstProtectionDisplayModelWithdrawnIP2016
              ),
              PrimaryProtection.toString -> List(
                tstProtectionDisplayModelWithdrawnPrimary
              )
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelUnsuccessfulIP2014
              )
            )
          ),
          rejectedProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014.toString -> List(
                tstProtectionDisplayModelRejectedIP2014
              )
            )
          ),
          expiredProtections = ExistingInactiveProtectionsByType(
            Seq(
              FixedProtection2016.toString -> List(
                tstProtectionDisplayModelExpiredFP2016
              ),
              FixedProtection2014.toString -> List(
                tstProtectionDisplayModelExpiredFP2014
              )
            )
          )
        )
      )

      ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(
        tstExistingProtectionModel
      ) shouldBe tstExistingProtectionsDisplayModel
    }

    "Handle all protection types" when
      ProtectionType.values.foreach(protectionType =>
        s"protection type is $protectionType" in {
          val protectionModel = ProtectionModel(
            psaCheckReference = Some(tstPSACheckRef),
            protectionID = Some(12345),
            protectionType = Some(protectionType.toString),
            status = Some(Withdrawn.toString),
            certificateDate = Some("2016-04-17T15:14:00"),
            protectedAmount = Some(1250000),
            protectionReference = Some("PSA654321"),
            notificationId = Some(1)
          )

          val transformedReadResponseModel = TransformedReadResponseModel(
            activeProtection = None,
            inactiveProtections = Seq(protectionModel)
          )

          val existingProtectionDisplayModel = ExistingProtectionDisplayModel(
            protectionType = protectionType.toString,
            status = Withdrawn.toString,
            amendCall = None,
            psaCheckReference = Some(tstPSACheckRef),
            protectionReference = "PSA654321",
            protectedAmount = Some("£1,250,000"),
            certificateDate = Some("17 April 2016"),
            certificateTime = Some("3:14pm")
          )

          val existingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
            activeProtection = None,
            inactiveProtections = ExistingInactiveProtectionsDisplayModel(
              dormantProtections = ExistingInactiveProtectionsByType.empty,
              withdrawnProtections = ExistingInactiveProtectionsByType(
                Seq(
                  protectionType.toString -> Seq(existingProtectionDisplayModel)
                )
              ),
              unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
              rejectedProtections = ExistingInactiveProtectionsByType.empty,
              expiredProtections = ExistingInactiveProtectionsByType.empty
            )
          )

          ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(
            transformedReadResponseModel
          ) shouldBe existingProtectionsDisplayModel
        }
      )

    "Handle an unknown protection type as notRecorded" in {
      val protectionModel = ProtectionModel(
        psaCheckReference = Some(tstPSACheckRef),
        protectionID = Some(12345),
        protectionType = Some("unknown protection type"),
        status = Some(Withdrawn.toString),
        certificateDate = Some("2016-04-17T15:14:00"),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA654321"),
        notificationId = Some(1)
      )

      val transformedReadResponseModel = TransformedReadResponseModel(
        activeProtection = None,
        inactiveProtections = Seq(protectionModel)
      )

      val existingProtectionDisplayModel = ExistingProtectionDisplayModel(
        protectionType = "notRecorded",
        status = Withdrawn.toString,
        amendCall = None,
        psaCheckReference = Some(tstPSACheckRef),
        protectionReference = "PSA654321",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val existingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = None,
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType.empty,
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              "notRecorded" -> Seq(existingProtectionDisplayModel)
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType.empty,
          rejectedProtections = ExistingInactiveProtectionsByType.empty,
          expiredProtections = ExistingInactiveProtectionsByType.empty
        )
      )

      ExistingProtectionsDisplayModelConstructor.createExistingProtectionsDisplayModel(
        transformedReadResponseModel
      ) shouldBe existingProtectionsDisplayModel
    }

  }

  "shouldDisplayLumpSumAmount" should {
    "return true" when {
      val types = Seq(
        PrimaryProtection,
        PrimaryProtectionLTA
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayLumpSumAmount(protectionType) shouldBe true
        }
      )
    }

    "return false" when {
      val types = Seq(
        EnhancedProtection,
        EnhancedProtectionLTA,
        FixedProtection,
        FixedProtection2014,
        FixedProtection2014LTA,
        FixedProtection2016,
        FixedProtection2016LTA,
        FixedProtectionLTA,
        IndividualProtection2014,
        IndividualProtection2014LTA,
        IndividualProtection2016,
        IndividualProtection2016LTA,
        InternationalEnhancementS221,
        InternationalEnhancementS224,
        PensionCreditRights
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayLumpSumAmount(protectionType) shouldBe false
        }
      )
    }

  }

  "shouldDisplayLumpSumPercentage" should {
    "return true" when {
      val types = Seq(
        EnhancedProtection,
        EnhancedProtectionLTA
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayLumpSumPercentage(protectionType) shouldBe true
        }
      )
    }

    "return false" when {
      val types = Seq(
        PrimaryProtection,
        PrimaryProtectionLTA,
        FixedProtection,
        FixedProtection2014,
        FixedProtection2014LTA,
        FixedProtection2016,
        FixedProtection2016LTA,
        FixedProtectionLTA,
        IndividualProtection2014,
        IndividualProtection2014LTA,
        IndividualProtection2016,
        IndividualProtection2016LTA,
        InternationalEnhancementS221,
        InternationalEnhancementS224,
        PensionCreditRights
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayLumpSumPercentage(protectionType) shouldBe false
        }
      )
    }

  }

  "shouldDisplayEnhancementFactor" should {
    "return true" when {
      val types = Seq(
        PensionCreditRights,
        InternationalEnhancementS221,
        InternationalEnhancementS224
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayEnhancementFactor(protectionType) shouldBe true
        }
      )
    }

    "return false" when {
      val types = Seq(
        EnhancedProtection,
        EnhancedProtectionLTA,
        FixedProtection,
        FixedProtection2014,
        FixedProtection2014LTA,
        FixedProtection2016,
        FixedProtection2016LTA,
        FixedProtectionLTA,
        IndividualProtection2014,
        IndividualProtection2014LTA,
        IndividualProtection2016,
        IndividualProtection2016LTA,
        PrimaryProtection,
        PrimaryProtectionLTA
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayEnhancementFactor(protectionType) shouldBe false
        }
      )
    }

  }

  "shouldDisplayFactor" should {
    "return true" when {
      val types = Seq(
        PrimaryProtection,
        PrimaryProtectionLTA
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayFactor(protectionType) shouldBe true
        }
      )
    }

    "return false" when {
      val types = Seq(
        EnhancedProtection,
        EnhancedProtectionLTA,
        FixedProtection,
        FixedProtection2014,
        FixedProtection2014LTA,
        FixedProtection2016,
        FixedProtection2016LTA,
        FixedProtectionLTA,
        IndividualProtection2014,
        IndividualProtection2014LTA,
        IndividualProtection2016,
        IndividualProtection2016LTA,
        InternationalEnhancementS221,
        InternationalEnhancementS224,
        PensionCreditRights
      ).map(_.toString)

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayFactor(protectionType) shouldBe false
        }
      )
    }

  }

}
