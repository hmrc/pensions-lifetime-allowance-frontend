/*
 * Copyright 2020 HM Revenue & Customs
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

package constructors

import common._
import enums.{ApplicationStage, ApplicationType}
import javax.inject.Inject
import models._
import models.amendModels.AmendProtectionModel
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call
import utils.Constants


class DisplayConstructors @Inject()() {

  implicit val lang: Lang = Lang.defaultLang

  // PRINT PAGE
  def createPrintDisplayModel(personalDetailsModelOpt: Option[PersonalDetailsModel],
                              protectionModel: ProtectionModel,
                              nino: String
                             )(implicit lang: Lang): PrintDisplayModel = {

    val personalDetailsModel = personalDetailsModelOpt.getOrElse {
      throw new Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "personalDetailsModel")
    }

    val firstName = personalDetailsModel.person.firstName.toLowerCase.capitalize
    val surname = personalDetailsModel.person.lastName.toLowerCase.capitalize

    val protectionType = Strings.protectionTypeString(protectionModel.protectionType)
    val status = Strings.statusString(protectionModel.status)
    val psaCheckReference = protectionModel.psaCheckReference.getOrElse {
      throw new Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "psaCheckReference")
    }
    val protectionReference = protectionModel.protectionReference.getOrElse {
      throw new Exceptions.RequiredValueNotDefinedException("createPrintDisplayModel", "protectionReference")
    }

    val protectedAmountOption = protectionModel.protectedAmount.map { amt =>
      Display.currencyDisplayString(BigDecimal(amt))
    }

    val protectedAmount = protectionModel.protectionType match {
      case Some("FP2016") => Some(Display.currencyDisplayString(Constants.fpProtectedAmount))
      case _ => protectedAmountOption
    }

    val certificateDate = protectionModel.certificateDate.map { cDate =>
      Display.dateDisplayString(Dates.constructDateFromAPIString(cDate))
    }

    PrintDisplayModel(
      firstName, surname, nino,
      protectionType,
      status,
      psaCheckReference,
      protectionReference,
      protectedAmount,
      certificateDate
    )
  }

  // EXISTING PROTECTIONS
  def createExistingProtectionsDisplayModel(model: TransformedReadResponseModel)(implicit lang: Lang): ExistingProtectionsDisplayModel = {
    val activeProtection = model.activeProtection.map(createExistingProtectionDisplayModel)
    val otherProtectionsList = model.inactiveProtections.map(createExistingProtectionDisplayModel).sortWith(sortByStatus)

    ExistingProtectionsDisplayModel(activeProtection, otherProtectionsList)

  }

  def sortByStatus(s1: models.ExistingProtectionDisplayModel, s2: models.ExistingProtectionDisplayModel): Boolean = {
    if (s1.status == s2.status) {
      val typeMap: Map[String, Int] = Map("IP2014" -> 1, "FP2016" -> 2, "IP2016" -> 3, "primary" -> 4, "enhanced" -> 5, "fixed" -> 6, "FP2014" -> 7)
      if (typeMap(s1.protectionType) < typeMap(s2.protectionType)) true else false
    }
    else {
      val statusMap: Map[String, Int] = Map("dormant" -> 1, "withdrawn" -> 2, "unsuccessful" -> 3, "rejected" -> 4, "expired" -> 5)
      if (statusMap(s1.status) < statusMap(s2.status)) true else false
    }
  }

  def createExistingProtectionDisplayModel(model: ProtectionModel)(implicit lang: Lang): ExistingProtectionDisplayModel = {

    val status = Strings.statusString(model.status)
    val protectionType = Strings.protectionTypeString(model.protectionType)
    val protectionReference = model.protectionReference.getOrElse(Messages("pla.protection.protectionReference"))

    val protectedAmount = model.protectedAmount.map { amt =>
      Display.currencyDisplayString(BigDecimal(amt))
    }

    val certificateDate = model.certificateDate.map { cDate =>
      Display.dateDisplayString(Dates.constructDateFromAPIString(cDate))
    }

    val strippedPsaRef = model.psaCheckReference.map {
      _.stripPrefix(""""""").stripSuffix(""""""")
    }

    val withdrawnDate = model.withdrawnDate.map { wDate =>
      Display.dateDisplayString(Dates.constructDateFromAPIString(wDate))
    }
    val amendCall = Helpers.createAmendCallIfRequired(model)

    ExistingProtectionDisplayModel(
      protectionType,
      status,
      amendCall,
      strippedPsaRef,
      protectionReference,
      protectedAmount,
      certificateDate,
      withdrawnDate)
  }

  // AMENDS
  def createAmendDisplayModel(model: AmendProtectionModel)(implicit lang: Lang): AmendDisplayModel = {
    val amended = modelsDiffer(model.originalProtection, model.updatedProtection)
    val totalAmount = Display.currencyDisplayString(BigDecimal(
      model.updatedProtection.relevantAmount.getOrElse(0.0)
    ))
    val protectionType = Strings.protectionTypeString(model.updatedProtection.protectionType)

    val pcSections: Seq[AmendDisplaySectionModel] = createAmendPensionContributionSectionsFromProtection(model.updatedProtection)

    val pensionDebitAdded = model.updatedProtection.pensionDebits.isDefined

    val psoSecs: Seq[AmendDisplaySectionModel] = createCurrentPsoSection(model.updatedProtection).getOrElse(Seq())

    AmendDisplayModel(
      protectionType = protectionType,
      amended = amended,
      pensionContributionSections = pcSections,
      psoAdded = pensionDebitAdded,
      psoSections = psoSecs,
      totalAmount = totalAmount
    )
  }

  def createPreviousPsoSection(model: ProtectionModel): AmendDisplaySectionModel = {
    createNoChangeSection(model, ApplicationStage.CurrentPsos, model.pensionDebitTotalAmount)
  }

  def createCurrentPsoSection(model: ProtectionModel)(implicit lang: Lang): Option[Seq[AmendDisplaySectionModel]] = {

    model.pensionDebits.flatMap { psoList =>
      if (psoList.length > 1) {
        Logger.warn("More than one PSO amendment was found in the protection model, where only one is permitted.")
        None
      } else {
        val psoAmendCall = Helpers.createAmendCall(model, ApplicationStage.CurrentPsos)
        val psoRemoveCall = Helpers.createPsoRemoveCall(model)
        psoList.headOption.map { debit =>
          Seq(
            AmendDisplaySectionModel("pensionDebits",
              Seq(AmendDisplayRowModel(s"${ApplicationStage.CurrentPsos.toString}-psoDetails",
                changeLinkCall = Some(psoAmendCall),
                removeLinkCall = psoRemoveCall,
                Display.currencyDisplayString(BigDecimal(debit.amount)),
                Display.dateDisplayString(Dates.constructDateFromAPIString(debit.startDate))))
            )
          )
        }
      }
    }
  }

  def createAmendPensionContributionSectionsFromProtection(protection: ProtectionModel): Seq[AmendDisplaySectionModel] = {
    val currentPensionsSection = createCurrentPensionsSection(protection, ApplicationStage.CurrentPensions)
    val pensionsTakenBeforeSection = createSection(protection, ApplicationStage.PensionsTakenBefore, protection.preADayPensionInPayment)
    val pensionsTakenBetweenSection = createSection(protection, ApplicationStage.PensionsTakenBetween, protection.postADayBenefitCrystallisationEvents)
    val overseasPensionsSection = createSection(protection, ApplicationStage.OverseasPensions, protection.nonUKRights)
    val previousPsoSection = createPreviousPsoSection(protection)

    Seq(pensionsTakenBeforeSection, pensionsTakenBetweenSection, overseasPensionsSection, currentPensionsSection, previousPsoSection)
  }

  def createSection(protection: ProtectionModel, applicationStage: ApplicationStage.Value, amountOption: Option[Double]): AmendDisplaySectionModel = {
    val amendCall = Helpers.createAmendCall(protection, applicationStage)

    createYesNoSection(applicationStage.toString, Some(amendCall), amountOption)
  }

  def createNoChangeSection(protection: ProtectionModel, applicationStage: ApplicationStage.Value, amountOption: Option[Double]): AmendDisplaySectionModel = {
    createNoChangeYesNoSection(applicationStage.toString, amountOption)
  }

  def createCurrentPensionsSection(protection: ProtectionModel, applicationStage: ApplicationStage.Value): AmendDisplaySectionModel = {
    val amendCall = Helpers.createAmendCall(protection, applicationStage)
    val currentPensions = protection.uncrystallisedRights.getOrElse(throw new Exceptions.OptionNotDefinedException("createCurrentPensionsSection", "currentPensions", protection.protectionType.getOrElse("No protection type")))
    AmendDisplaySectionModel(applicationStage.toString, Seq(AmendDisplayRowModel("Amt", Some(amendCall), removeLinkCall = None, Display.currencyDisplayString(BigDecimal(currentPensions)))))
  }

  def createNoChangeYesNoSection(stage: String, amountOption: Option[Double]) = {
    amountOption.fold(
      AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.no"))))
    )(amt =>
      if (amt < 0.01) {
        AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.no"))))
      } else {
        AmendDisplaySectionModel(stage, Seq(
          AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.yes")),
          AmendDisplayRowModel("Amt", None, None, Display.currencyDisplayString(amt))
        ))
      }
    )
  }

  def createYesNoSection(stage: String, amendCall: Option[Call], amountOption: Option[Double]): AmendDisplaySectionModel = {
    amountOption.fold(
      AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.no"))))
    )(amt =>
      if (amt < 0.01) {
        AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.no"))))
      } else {
        AmendDisplaySectionModel(stage, Seq(
          AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.yes")),
          AmendDisplayRowModel("Amt", amendCall, removeLinkCall = None, Display.currencyDisplayString(amt))
        ))
      }
    )
  }

  def modelsDiffer(modelA: ProtectionModel, modelB: ProtectionModel): Boolean = {
    modelA match {
      case `modelB` => false
      case _ => true
    }
  }

  // ACTIVE AMEND RESPONSE
  def createActiveAmendResponseDisplayModel(model: AmendResponseModel): ActiveAmendResultDisplayModel = {
    val protectionDetails = createProtectionDetailsFromProtection(model.protection)
    val protectionType = getProtectionTypeFromProtection(model.protection)

    val protectedAmount = model.protection.protectedAmount.getOrElse {
      throw new Exceptions.OptionNotDefinedException("createActiveAmendResponseDisplayModel", "protectedAmount",
        model.protection.protectionType.getOrElse("No protection type in response"))
    }
    val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

    val notificationId = model.protection.notificationId.getOrElse(
      throw new Exceptions.OptionNotDefinedException("createActiveAmendResponseDisplayModel", "notificationId",
        model.protection.protectionType.getOrElse("No protection type in response")))

    ActiveAmendResultDisplayModel(protectionType, notificationId.toString, protectedAmountString, Some(protectionDetails))
  }

  // INACTIVE AMEND RESPONSE
  def createInactiveAmendResponseDisplayModel(model: AmendResponseModel): InactiveAmendResultDisplayModel = {
    val notificationId = model.protection.notificationId.getOrElse(
      throw new Exceptions.OptionNotDefinedException("createInactiveAmendResponseDisplayModel", "notificationId",
        model.protection.protectionType.getOrElse("No protection type in response")))
    val additionalInfo = getAdditionalInfo("amendResultCode", notificationId)
    InactiveAmendResultDisplayModel(notificationId.toString, additionalInfo)
  }

  def getProtectionTypeFromProtection(protection: ProtectionModel): ApplicationType.Value = {
    val protectionTypeString = protection.protectionType.getOrElse(
      throw new Exceptions.RequiredValueNotDefinedException("getProtectionTypeFromProtection", "protectionType")
    )
    ApplicationType.fromString(protectionTypeString).getOrElse {
      throw new Exception("Invalid protection type passed to getProtectionTypeFromProtection")
    }
  }

  // SUCCESSFUL APPLICATION RESPONSE
  def createSuccessDisplayModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value, lang: Lang): SuccessDisplayModel = {
    val notificationId = model.protection.notificationId.getOrElse(throw new Exceptions.OptionNotDefinedException("CreateSuccessDisplayModel", "notification ID", protectionType.toString))
    val protectedAmount = model.protection.protectedAmount.getOrElse(if (protectionType == ApplicationType.FP2016) Constants.fpProtectedAmount else throw new Exceptions.OptionNotDefinedException("ApplyResponseModel", "protected amount", protectionType.toString))
    val printable = Constants.activeProtectionCodes.contains(notificationId)

    val details = if (Constants.successCodesRequiringProtectionInfo.contains(notificationId)) {
      Some(createProtectionDetailsFromProtection(model.protection))
    } else None

    val protectedAmountString = Display.currencyDisplayString(BigDecimal(protectedAmount))

    val additionalInfo = getAdditionalInfo("resultCode", notificationId)

    SuccessDisplayModel(protectionType, notificationId.toString, protectedAmountString, printable, details, additionalInfo)
  }

  // REJECTED APPLICATION RESPONSE
  def createRejectionDisplayModel(model: ApplyResponseModel)(implicit protectionType: ApplicationType.Value): RejectionDisplayModel = {
    val notificationId = model.protection.notificationId.getOrElse(throw new Exceptions.OptionNotDefinedException("CreateRejectionDisplayModel", "notification ID", protectionType.toString))
    val additionalInfo = getAdditionalInfo("resultCode", notificationId)
    RejectionDisplayModel(notificationId.toString, additionalInfo, protectionType)
  }

  private def createProtectionDetailsFromProtection(protection: ProtectionModel)(implicit lang: Lang): ProtectionDetailsDisplayModel = {
    val protectionReference = protection.protectionReference
    val psaReference = protection.psaCheckReference.getOrElse(throw new Exceptions.OptionNotDefinedException("createProtectionDetailsFromModel", "psaCheckReference", protection.protectionType.getOrElse("No protection type in response")))
    val applicationDate = protection.certificateDate.map { dt => Display.dateDisplayString(Dates.constructDateFromAPIString(dt)) }
    ProtectionDetailsDisplayModel(protectionReference, psaReference, applicationDate)
  }

  def createWithdrawSummaryTable(protection: ProtectionModel): AmendDisplayModel = {
    val currentPensionsSection = AmendDisplaySectionModel(ApplicationStage.CurrentPensions.toString,
      Seq(AmendDisplayRowModel("Amt", None, None,
        Display.currencyDisplayString(BigDecimal(protection.uncrystallisedRights.getOrElse(0.0))))))
    val pensionsTakenBeforeSection = createNoChangeYesNoSection(ApplicationStage.PensionsTakenBefore.toString,
      protection.preADayPensionInPayment)
    val pensionsTakenBetweenSection = createNoChangeYesNoSection(ApplicationStage.PensionsTakenBetween.toString,
      protection.postADayBenefitCrystallisationEvents)
    val overseasPensionsSection = createNoChangeYesNoSection(ApplicationStage.OverseasPensions.toString,
      protection.nonUKRights)
    val previousPsoSection = createNoChangeYesNoSection(ApplicationStage.CurrentPsos.toString,
      protection.pensionDebitTotalAmount)

    val pcSections = Seq(pensionsTakenBeforeSection, pensionsTakenBetweenSection, overseasPensionsSection, currentPensionsSection, previousPsoSection)

    val totalAmount = Display.currencyDisplayString(BigDecimal(protection.relevantAmount.getOrElse(0.0)))

    val protectionType = Strings.protectionTypeString(protection.protectionType)

    val psoSecs: Seq[AmendDisplaySectionModel] = createAmendPensionContributionSectionsFromProtection(protection)

    val pensionDebitAdded = protection.pensionDebits.isDefined

    AmendDisplayModel(
      protectionType = protectionType,
      amended = false,
      pensionContributionSections = pcSections,
      psoAdded = pensionDebitAdded,
      psoSections = psoSecs,
      totalAmount = totalAmount
    )
  }

  // HELPER FUNCTIONS
  def getAdditionalInfo(messagesPrefix: String, notificationId: Int): List[String] = {

    def loop(notificationId: Int, i: Int = 1, paragraphs: List[String] = List.empty): List[String] = {
      val x: String = s"$messagesPrefix.$notificationId.$i"
      if (Messages(x) == x) {
        paragraphs
      } else {
        loop(notificationId, i + 1, paragraphs :+ i.toString)
      }
    }

    loop(notificationId)
  }
}
