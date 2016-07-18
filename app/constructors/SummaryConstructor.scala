/*
 * Copyright 2016 HM Revenue & Customs
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

import java.io.Serializable

import enums.ApplicationType
import play.api.Logger
import play.api.i18n.Messages
import models._
import common.Display._
import common.Dates._
import common.Validation
import common.Strings.nameString
import utils.{CallMap, Constants}
import uk.gov.hmrc.http.cache.client.CacheMap

object SummaryConstructor extends SummaryConstructor {
    
}

trait SummaryConstructor {

  def createSummaryData(data: CacheMap)(implicit protectionType: ApplicationType.Value): Option[SummaryModel] = {

    val yesValue = Messages("pla.base.yes")

    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel]("pensionsTaken")

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel]("pensionsTakenBefore")
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel]("pensionsTakenBetween")
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel]("overseasPensions")
    val currentPensionsModel = data.getEntry[CurrentPensionsModel]("currentPensions")

    val pensionDebitsModel = data.getEntry[PensionDebitsModel]("pensionDebits")
    val numberOfPSOsModel = data.getEntry[NumberOfPSOsModel]("numberOfPSOs")

    val pensionsTakenSection = Some(createYesNoSection("pensionsTaken", pensionsTakenModel, boldText = false))
    val (pensionsTakenBeforeSection, pensionsTakenBetweenSection) =
      if(positiveAnswer(pensionsTakenModel)) (
            Some(createYesNoAmountSection("pensionsTakenBefore", pensionsTakenBeforeModel, boldText = false)),
            Some(createYesNoAmountSection("pensionsTakenBetween", pensionsTakenBetweenModel, boldText = false))
          )
      else (None, None)

    val overseasPensionsSection = Some(createYesNoAmountSection("overseasPensions", overseasPensionsModel, boldText = false))
    val currentPensionsSection = Some(createAmountSection("currentPensions", currentPensionsModel, boldText = false))

    val totalPensionsSection = Some(
      SummarySectionModel(List(
        SummaryRowModel("totalPensionsAmnt", None, boldText = true, currencyDisplayString(relevantAmount))
      ))
    )

    val pensionContributions = List(
      pensionsTakenSection,
      pensionsTakenBeforeSection,
      pensionsTakenBetweenSection,
      overseasPensionsSection,
      currentPensionsSection,
      totalPensionsSection
      ).flatten

    val pensionDebitsSection = Some(createYesNoSection("pensionDebits", pensionDebitsModel, boldText = false))
    val numPSOsSection = createNumberOfPSOsSection(pensionDebitsModel, numberOfPSOsModel)
    val psoDetailsSections = createPSODetailsSections()

    val pensionDebits = List(
      pensionDebitsSection,
      numPSOsSection
    ).flatten

    def positiveAnswer(modelOption: Option[YesNoModel]): Boolean = {
     modelOption.exists{_.getYesNoValue == yesValue}
    }

    def createYesNoSection(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean) = {
      SummarySectionModel(
        List(
          createYesNoRow(dataName, modelOption, boldText)
        )
      )
    }

    def createAmountSection[T <: AmountModel](dataName: String, modelOption: Option[AmountModel], boldText: Boolean) = {
      SummarySectionModel(
        List(
          createAmountRow(dataName, modelOption, boldText)
        ).flatten
      )
    }

    def createYesNoAmountSection(dataName: String, modelOption: Option[YesNoAmountModel], boldText: Boolean) = {
      SummarySectionModel(
        List(
          createYesNoRow(dataName, modelOption, boldText),
          createYesNoAmountRow(dataName, modelOption, boldText)
        ).flatten
      )
    }

    def createYesNoRow(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean) = {
      modelOption.map { model =>
        val name = nameString(dataName)
        val call = CallMap.get(name)
        val displayValue = yesNoValue(model)
        SummaryRowModel(
          name, call, boldText, displayValue
        )
      }
    }

    def yesNoValue(model: YesNoModel): String = {
      Messages(s"pla.base.${model.getYesNoValue}")
    }

    def createYesNoAmountRow(dataName: String, modelOption: Option[YesNoAmountModel], boldText: Boolean) = {
      val name = nameString(dataName)
      val call = CallMap.get(name)
      if(positiveAnswer(modelOption))
        Some(SummaryRowModel(
          name, call, boldText, amountDisplayValue(modelOption.get)
          )
        )
      else None
    }

    def createAmountRow(dataName: String, modelOption: Option[AmountModel],boldText: Boolean) = {
      val name = nameString(dataName)
      modelOption.map{ model =>
        val call = CallMap.get(name)
          Some(SummaryRowModel(
            name, call, boldText, amountDisplayValue(model)
          ))
      }.getOrElse(None)
    }

    def amountDisplayValue(model: AmountModel): String = {
      currencyDisplayString(model.getAmount.getOrElse(BigDecimal(0)))
    }

    def amountValue(modelOption: Option[AmountModel]): Option[BigDecimal] = {
      modelOption.map{_.getAmount}.getOrElse(None)
    }

    def relevantAmount: BigDecimal = {
      val (pensionsBeforeAmt, pensionsBetweenAmt) = if(positiveAnswer(pensionsTakenModel)) (
        if(positiveAnswer(pensionsTakenBeforeModel)) amountValue(pensionsTakenBeforeModel) else None,
        if(positiveAnswer(pensionsTakenBetweenModel)) amountValue(pensionsTakenBetweenModel) else None
        )
      else (None, None)
      val overseasPensionsAmnt = if(positiveAnswer(overseasPensionsModel)) amountValue(overseasPensionsModel) else None
      val currentPensionsAmnt = amountValue(currentPensionsModel)

      List(pensionsBeforeAmt,pensionsBetweenAmt,overseasPensionsAmnt,currentPensionsAmnt).flatten.sum
    }

    def createNumberOfPSOsSection(debitsOptionModel: Option[YesNoModel], numPSOsOptionModel: Option[NumberOfPSOsModel]) = {
      val name = nameString("numberOfPSOs")
      if(positiveAnswer(debitsOptionModel)) {
        numPSOsOptionModel.map { model =>
          val numPSOs = model.numberOfPSOs
          Some(SummarySectionModel(
            List(SummaryRowModel(
              name, CallMap.get(name), boldText = false, numPSOs.getOrElse("0")
            ))))
        }.getOrElse(None)
      }
      else None
    }

    def createPSODetailsSections(): List[SummarySectionModel] = {
      List.empty
    }

    def createSummaryModel(): SummaryModel = {
      val invalidRelevantAmount = relevantAmount < Constants.ip16RelevantAmountThreshold
      SummaryModel(protectionType, invalidRelevantAmount, pensionContributions, pensionDebits)
    }

    if(!Validation.validIPData(data)) {
      Logger.warn(s"Unable to create summary model from user data for ${protectionType.toString}. Data: ${data.data}")
      None
    } else Some(createSummaryModel())

  }

}
