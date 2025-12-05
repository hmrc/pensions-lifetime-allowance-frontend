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

import common._
import enums.ApplicationStage
import models.amend.AmendProtectionModel
import models.display.{AmendDisplayModel, AmendDisplayRowModel, AmendDisplaySectionModel}
import play.api.Logging
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Call

object AmendDisplayModelConstructor extends Logging {

  def createAmendDisplayModel(
      model: AmendProtectionModel
  )(implicit lang: Lang, messages: Messages): AmendDisplayModel = {
    val amended = model.hasChanges

    val totalAmount = Display.currencyDisplayString(BigDecimal(model.updatedRelevantAmount))

    val protectionType = model.protectionType

    val pcSections: Seq[AmendDisplaySectionModel] = createAmendPensionContributionSectionsFromProtection(model)

    val pensionDebitAdded = model.updated.pensionDebit.isDefined

    val psoSections: Seq[AmendDisplaySectionModel] = createCurrentPsoSection(model).getOrElse(Seq())

    AmendDisplayModel(
      protectionType = protectionType,
      amended = amended,
      pensionContributionSections = pcSections,
      psoAdded = pensionDebitAdded,
      psoSections = psoSections,
      totalAmount = totalAmount
    )
  }

  private def createPreviousPsoSection(model: AmendProtectionModel)(
      implicit messages: Messages
  ): AmendDisplaySectionModel =
    createNoChangeSection(ApplicationStage.CurrentPsos, model.pensionDebitTotalAmount)

  private def createCurrentPsoSection(
      model: AmendProtectionModel
  )(implicit lang: Lang, messages: Messages): Option[Seq[AmendDisplaySectionModel]] =
    model.updated.pensionDebit.map { pensionDebit =>
      val psoAmendCall  = Helpers.createAmendCall(model, ApplicationStage.CurrentPsos)
      val psoRemoveCall = Helpers.createPsoRemoveCall(model)
      Seq(
        AmendDisplaySectionModel(
          "pensionDebits",
          Seq(
            AmendDisplayRowModel(
              s"${ApplicationStage.CurrentPsos.toString}-psoDetails",
              changeLinkCall = Some(psoAmendCall),
              removeLinkCall = Some(psoRemoveCall),
              Display.currencyDisplayString(pensionDebit.amount),
              Display.dateDisplayString(pensionDebit.startDate)
            )
          )
        )
      )
    }

  private def createAmendPensionContributionSectionsFromProtection(
      protection: AmendProtectionModel
  )(implicit messages: Messages): Seq[AmendDisplaySectionModel] = {
    val currentPensionsSection = createCurrentPensionsSection(protection, ApplicationStage.CurrentPensions)
    val pensionsTakenBeforeSection = createSection(
      protection,
      ApplicationStage.PensionsTakenBefore,
      protection.updated.preADayPensionInPayment,
      displayYesNoOnly = true
    )
    val pensionsWorthBeforeSection = createSection(
      protection,
      ApplicationStage.PensionsWorthBefore,
      protection.updated.preADayPensionInPayment,
      displayAmountOnly = true
    )
    val pensionsTakenBetweenSection = createSection(
      protection,
      ApplicationStage.PensionsTakenBetween,
      protection.updated.postADayBenefitCrystallisationEvents,
      displayYesNoOnly = true
    )
    val pensionsUsedBetweenSection = createSection(
      protection,
      ApplicationStage.PensionsUsedBetween,
      protection.updated.postADayBenefitCrystallisationEvents,
      displayAmountOnly = true
    )
    val overseasPensionsSection =
      createSection(protection, ApplicationStage.OverseasPensions, protection.updated.nonUKRights)
    val previousPsoSection = createPreviousPsoSection(protection)

    (protection.updated.postADayBenefitCrystallisationEvents, protection.updated.preADayPensionInPayment) match {
      case (Some(postAmt), None) =>
        if (postAmt < 0.01) {
          Seq(
            pensionsTakenBeforeSection,
            pensionsTakenBetweenSection,
            overseasPensionsSection,
            currentPensionsSection,
            previousPsoSection
          )
        } else {
          Seq(
            pensionsTakenBeforeSection,
            pensionsTakenBetweenSection,
            pensionsUsedBetweenSection,
            overseasPensionsSection,
            currentPensionsSection,
            previousPsoSection
          )
        }
      case (None, Some(preAmt)) =>
        if (preAmt < 0.01) {
          Seq(
            pensionsTakenBeforeSection,
            pensionsWorthBeforeSection,
            pensionsTakenBetweenSection,
            overseasPensionsSection,
            currentPensionsSection,
            previousPsoSection
          )
        } else {
          Seq(
            pensionsTakenBeforeSection,
            pensionsTakenBetweenSection,
            overseasPensionsSection,
            currentPensionsSection,
            previousPsoSection
          )
        }
      case (Some(postAmt), Some(preAmt)) =>
        (postAmt < 0.01, preAmt < 0.01) match {
          case (true, true) =>
            Seq(
              pensionsTakenBeforeSection,
              pensionsTakenBetweenSection,
              overseasPensionsSection,
              currentPensionsSection,
              previousPsoSection
            )
          case (true, false) =>
            Seq(
              pensionsTakenBeforeSection,
              pensionsWorthBeforeSection,
              pensionsTakenBetweenSection,
              overseasPensionsSection,
              currentPensionsSection,
              previousPsoSection
            )
          case (false, true) =>
            Seq(
              pensionsTakenBeforeSection,
              pensionsTakenBetweenSection,
              pensionsUsedBetweenSection,
              overseasPensionsSection,
              currentPensionsSection,
              previousPsoSection
            )
          case (false, false) =>
            Seq(
              pensionsTakenBeforeSection,
              pensionsWorthBeforeSection,
              pensionsTakenBetweenSection,
              pensionsUsedBetweenSection,
              overseasPensionsSection,
              currentPensionsSection,
              previousPsoSection
            )
        }
      case _ =>
        Seq(
          pensionsTakenBeforeSection,
          pensionsTakenBetweenSection,
          overseasPensionsSection,
          currentPensionsSection,
          previousPsoSection
        )
    }
  }

  private def createSection(
      protection: AmendProtectionModel,
      applicationStage: ApplicationStage,
      amountOption: Option[Double],
      displayYesNoOnly: Boolean = false,
      displayAmountOnly: Boolean = false
  )(implicit messages: Messages): AmendDisplaySectionModel = {
    val amendCall = Helpers.createAmendCall(protection, applicationStage)

    createYesNoSection(applicationStage.toString, Some(amendCall), amountOption, displayYesNoOnly, displayAmountOnly)
  }

  private def createNoChangeSection(
      applicationStage: ApplicationStage,
      amountOption: Option[Double]
  )(implicit messages: Messages): AmendDisplaySectionModel =
    createNoChangeYesNoSection(applicationStage.toString, amountOption)

  private def createCurrentPensionsSection(
      protection: AmendProtectionModel,
      applicationStage: ApplicationStage
  ): AmendDisplaySectionModel = {
    val amendCall       = Helpers.createAmendCall(protection, applicationStage)
    val currentPensions = protection.updated.uncrystallisedRights
    AmendDisplaySectionModel(
      applicationStage.toString,
      Seq(
        AmendDisplayRowModel(
          "Amt",
          Some(amendCall),
          removeLinkCall = None,
          Display.currencyDisplayString(currentPensions)
        )
      )
    )
  }

  private def createNoChangeYesNoSection(stage: String, amountOption: Option[Double])(
      implicit messages: Messages
  ): AmendDisplaySectionModel =
    amountOption.fold(
      AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.no"))))
    )(amt =>
      if (amt < 0.01) {
        AmendDisplaySectionModel(stage, Seq(AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.no"))))
      } else {
        AmendDisplaySectionModel(
          stage,
          Seq(
            AmendDisplayRowModel("YesNo", None, None, Messages("pla.base.yes")),
            AmendDisplayRowModel("Amt", None, None, Display.currencyDisplayString(amt))
          )
        )
      }
    )

  private def createYesNoSection(
      stage: String,
      amendCall: Option[Call],
      amountOption: Option[Double],
      displayYesNoOnly: Boolean,
      displayAmountOnly: Boolean
  )(implicit messages: Messages): AmendDisplaySectionModel =
    amountOption.fold(
      AmendDisplaySectionModel(
        stage,
        Seq(AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.no")))
      )
    )(amt =>
      if (amt < 0.01) {
        AmendDisplaySectionModel(
          stage,
          Seq(AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.no")))
        )
      } else {
        if (displayYesNoOnly) {
          AmendDisplaySectionModel(
            stage,
            Seq(
              AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.yes"))
            )
          )
        } else if (displayAmountOnly) {
          AmendDisplaySectionModel(
            stage,
            Seq(
              AmendDisplayRowModel("Amt", amendCall, removeLinkCall = None, Display.currencyDisplayString(amt))
            )
          )
        } else {
          AmendDisplaySectionModel(
            stage,
            Seq(
              AmendDisplayRowModel("YesNo", amendCall, removeLinkCall = None, Messages("pla.base.yes")),
              AmendDisplayRowModel("Amt", amendCall, removeLinkCall = None, Display.currencyDisplayString(amt))
            )
          )
        }
      }
    )

}
