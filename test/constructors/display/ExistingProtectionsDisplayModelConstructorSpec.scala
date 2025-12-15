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

import models.display.{
  ExistingInactiveProtectionsByType,
  ExistingInactiveProtectionsDisplayModel,
  ExistingProtectionDisplayModel,
  ExistingProtectionsDisplayModel
}
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType
import models.pla.response.ProtectionType._
import models.{DateModel, ProtectionModel, TimeModel, TransformedReadResponseModel}
import play.api.i18n.Messages

class ExistingProtectionsDisplayModelConstructorSpec extends DisplayConstructorsTestData {

  "createExistingProtectionsDisplayModel" should {

    "Create an ExistingProtectionsDisplayModel" in {
      val tstProtectionModelOpen = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Open,
        certificateDate = Some(DateModel.of(2016, 4, 17)),
        certificateTime = Some(TimeModel.of(15, 14, 0)),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstExistingProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Open,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Open
          )
        ),
        psaCheckReference = tstPsaCheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val tstProtectionModelDormant = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Dormant,
        certificateDate = None,
        certificateTime = None
      )

      val tstExistingProtectionDisplayModelDormant = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Dormant,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Dormant
          )
        ),
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstTransformedReadResponseModel =
        TransformedReadResponseModel(Some(tstProtectionModelOpen), List(tstProtectionModelDormant))
      val tstExistingProtectionsDisplayModel = ExistingProtectionsDisplayModel(
        activeProtection = Some(tstExistingProtectionDisplayModelOpen),
        inactiveProtections = ExistingInactiveProtectionsDisplayModel(
          dormantProtections = ExistingInactiveProtectionsByType(
            Seq(IndividualProtection2014 -> List(tstExistingProtectionDisplayModelDormant))
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
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Open,
        certificateDate = Some(DateModel.of(2016, 4, 17)),
        certificateTime = Some(TimeModel.of(15, 14, 0)),
        protectedAmount = Some(1250000),
        protectionReference = Some("PSA123456")
      )
      val tstProtectionDisplayModelOpen = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Open,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Open
          )
        ),
        psaCheckReference = tstPsaCheckRef,
        protectionReference = "PSA123456",
        protectedAmount = Some("£1,250,000"),
        certificateDate = Some("17 April 2016"),
        certificateTime = Some("3:14pm")
      )

      val tstProtectionModelDormantFP2014 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection2014,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )

      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = EnhancedProtection,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantPrimary = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = PrimaryProtection,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantIP2016 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2016,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016,
        status = Dormant,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            AmendableProtectionType.IndividualProtection2016,
            AmendProtectionRequestStatus.Dormant
          )
        ),
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantFP2016 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection2016,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantIP2014 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )

      val tstProtectionDisplayModelDormantIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Dormant,
        amendCall = Some(
          controllers.routes.AmendsController.amendsSummary(
            AmendableProtectionType.IndividualProtection2014,
            AmendProtectionRequestStatus.Dormant
          )
        ),
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
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
              IndividualProtection2016 -> List(
                tstProtectionDisplayModelDormantIP2016
              ),
              IndividualProtection2014 -> List(
                tstProtectionDisplayModelDormantIP2014
              ),
              FixedProtection2016 -> List(
                tstProtectionDisplayModelDormantFP2016
              ),
              FixedProtection2014 -> List(
                tstProtectionDisplayModelDormantFP2014
              ),
              PrimaryProtection -> List(
                tstProtectionDisplayModelDormantPrimary
              ),
              EnhancedProtection -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection -> List(
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
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection2014,
        status = Expired,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelExpiredFP2014 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2014,
        status = Expired,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelExpiredFP2016 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection2016,
        status = Expired,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelExpiredFP2016 = ExistingProtectionDisplayModel(
        protectionType = FixedProtection2016,
        status = Expired,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelRejectedIP2014 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Rejected,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelRejectedIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Rejected,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelUnsuccessfulIP2014 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2014,
        status = Unsuccessful,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelUnsuccessfulIP2014 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2014,
        status = Unsuccessful,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelWithdrawnPrimary = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = PrimaryProtection,
        status = Withdrawn,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelWithdrawnPrimary = ExistingProtectionDisplayModel(
        protectionType = PrimaryProtection,
        status = Withdrawn,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelWithdrawnIP2016 = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = IndividualProtection2016,
        status = Withdrawn,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelWithdrawnIP2016 = ExistingProtectionDisplayModel(
        protectionType = IndividualProtection2016,
        status = Withdrawn,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantFixed = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = FixedProtection,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantFixed = ExistingProtectionDisplayModel(
        protectionType = FixedProtection,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
      )

      val tstProtectionModelDormantEnhanced = ProtectionModel(
        psaCheckReference = tstPsaCheckRef,
        identifier = 12345,
        sequenceNumber = 1,
        protectionType = EnhancedProtection,
        status = Dormant,
        certificateDate = None,
        certificateTime = None,
        protectedAmount = None,
        protectionReference = None
      )
      val tstProtectionDisplayModelDormantEnhanced = ExistingProtectionDisplayModel(
        protectionType = EnhancedProtection,
        status = Dormant,
        amendCall = None,
        psaCheckReference = tstPsaCheckRef,
        protectionReference = Messages("pla.protection.protectionReference"),
        protectedAmount = None,
        certificateDate = None,
        certificateTime = None
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
              EnhancedProtection -> List(
                tstProtectionDisplayModelDormantEnhanced
              ),
              FixedProtection -> List(
                tstProtectionDisplayModelDormantFixed
              )
            )
          ),
          withdrawnProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2016 -> List(
                tstProtectionDisplayModelWithdrawnIP2016
              ),
              PrimaryProtection -> List(
                tstProtectionDisplayModelWithdrawnPrimary
              )
            )
          ),
          unsuccessfulProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014 -> List(
                tstProtectionDisplayModelUnsuccessfulIP2014
              )
            )
          ),
          rejectedProtections = ExistingInactiveProtectionsByType(
            Seq(
              IndividualProtection2014 -> List(
                tstProtectionDisplayModelRejectedIP2014
              )
            )
          ),
          expiredProtections = ExistingInactiveProtectionsByType(
            Seq(
              FixedProtection2016 -> List(
                tstProtectionDisplayModelExpiredFP2016
              ),
              FixedProtection2014 -> List(
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
            psaCheckReference = tstPsaCheckRef,
            identifier = 12345,
            sequenceNumber = 1,
            protectionType = protectionType,
            status = Withdrawn,
            certificateDate = Some(DateModel.of(2016, 4, 17)),
            certificateTime = Some(TimeModel.of(15, 14, 0)),
            protectedAmount = Some(1250000),
            protectionReference = Some("PSA654321")
          )

          val transformedReadResponseModel = TransformedReadResponseModel(
            activeProtection = None,
            inactiveProtections = Seq(protectionModel)
          )

          val existingProtectionDisplayModel = ExistingProtectionDisplayModel(
            protectionType = protectionType,
            status = Withdrawn,
            amendCall = None,
            psaCheckReference = tstPsaCheckRef,
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
                  protectionType -> Seq(existingProtectionDisplayModel)
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

  }

  "shouldDisplayLumpSumAmount" should {
    "return true" when {
      val types = Seq(
        PrimaryProtection,
        PrimaryProtectionLTA
      )

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
      )

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
      )

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
      )

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
      )

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
      )

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
      )

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
      )

      types.foreach(protectionType =>
        s"the protection type is $protectionType" in {
          ExistingProtectionsDisplayModelConstructor.shouldDisplayFactor(protectionType) shouldBe false
        }
      )
    }

  }

}
