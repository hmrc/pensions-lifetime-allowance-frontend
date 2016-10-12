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

    val helper = new SummaryConstructorHelper()

    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel](nameString("pensionsTaken"))

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore"))
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel](nameString("pensionsTakenBetween"))
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel](nameString("overseasPensions"))
    val currentPensionsModel = data.getEntry[CurrentPensionsModel](nameString("currentPensions"))

    val PensionDebitsModel = data.getEntry[PensionDebitsModel](nameString("pensionDebits"))
    val numberOfPSOsModel = data.getEntry[NumberOfPSOsModel](nameString("numberOfPSOs"))


    def relevantAmount: BigDecimal = {
      val (pensionsBeforeAmt, pensionsBetweenAmt) = if(helper.positiveAnswer(pensionsTakenModel)) (
        if(helper.positiveAnswer(pensionsTakenBeforeModel)) helper.amountValue(pensionsTakenBeforeModel) else None,
        if(helper.positiveAnswer(pensionsTakenBetweenModel)) helper.amountValue(pensionsTakenBetweenModel) else None
        )
      else (None, None)
      val overseasPensionsAmnt = if(helper.positiveAnswer(overseasPensionsModel)) helper.amountValue(overseasPensionsModel) else None
      val currentPensionsAmnt = helper.amountValue(currentPensionsModel)

      List(pensionsBeforeAmt,pensionsBetweenAmt,overseasPensionsAmnt,currentPensionsAmnt).flatten.sum
    }

    val pensionsTakenSection = helper.createYesNoSection("pensionsTaken", pensionsTakenModel, boldText = false)
    val (pensionsTakenBeforeSection, pensionsTakenBetweenSection) =
      if(helper.positiveAnswer(pensionsTakenModel)) (
            Some(helper.createYesNoAmountSection("pensionsTakenBefore", pensionsTakenBeforeModel, boldText = false)),
            Some(helper.createYesNoAmountSection("pensionsTakenBetween", pensionsTakenBetweenModel, boldText = false))
          )
      else (None, None)

    val overseasPensionsSection = Some(helper.createYesNoAmountSection("overseasPensions", overseasPensionsModel, boldText = false))
    val currentPensionsSection = Some(helper.createAmountSection("currentPensions", currentPensionsModel, boldText = false))

    val totalPensionsSection = Some(
      SummarySectionModel(List(
        SummaryRowModel(nameString("totalPensionsAmt"), None, boldText = true, currencyDisplayString(relevantAmount))
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

    val pensionDebitsSection = helper.createYesNoSection("pensionDebits", PensionDebitsModel, boldText = false)
    val (numPSOsSection, numberOfPSOs) = helper.createNumberOfPSOsSection(PensionDebitsModel, numberOfPSOsModel)
    val psoDetailsList = (1 to numberOfPSOs).flatMap{ psoNum =>
      data.getEntry[PSODetailsModel](nameString(s"psoDetails$psoNum"))
    }

    val psoDetailsSections = helper.createAllPSODetailsSections(psoDetailsList)

    val pensionDebits = List(
      pensionDebitsSection,
      numPSOsSection
    ).flatten ::: psoDetailsSections

    if(!Validation.validIPData(data)) {
      None
    } else Some(helper.createSummaryModel(relevantAmount, pensionContributions, pensionDebits))

  }

}

class SummaryConstructorHelper()(implicit protectionType: ApplicationType.Value) {

    def positiveAnswer(modelOption: Option[YesNoModel]): Boolean = {
     modelOption.exists{_.getYesNoValue == "yes"}
    }

    def amountDisplayValue(model: AmountModel): String = {
      currencyDisplayString(model.getAmount.getOrElse(BigDecimal(0)))
    }

    def amountValue(modelOption: Option[AmountModel]): Option[BigDecimal] = {
      modelOption.map{_.getAmount}.getOrElse(None)
    }

    def createYesNoSection(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean): Option[SummarySectionModel] = {
      createYesNoRow(dataName, modelOption, boldText).map { row =>
        SummarySectionModel(
          List(row)
        )
      }
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
          name+"Amt", call, boldText, amountDisplayValue(modelOption.get)
          )
        )
      else None
    }

    def createAmountRow(dataName: String, modelOption: Option[AmountModel],boldText: Boolean) = {
      val name = nameString(dataName)
      modelOption.map{ model =>
        val call = CallMap.get(name)
          Some(SummaryRowModel(
            name+"Amt", call, boldText, amountDisplayValue(model)
          ))
      }.getOrElse(None)
    }

    // returns an option on SummarySectionModel for number of PSOs and an Int value of the total number of PSOs
    // if no PSOs, returns (None, 0)
    def createNumberOfPSOsSection(debitsOptionModel: Option[YesNoModel], numPSOsOptionModel: Option[NumberOfPSOsModel]): (Option[SummarySectionModel], Int) = {
      val name = nameString("numberOfPSOs")
      if(positiveAnswer(debitsOptionModel)) {
        numPSOsOptionModel.map { model =>
          val numPSOs = model.numberOfPSOs
          (Some(SummarySectionModel(
            List(SummaryRowModel(
              name+"Amt", CallMap.get(name), boldText = false, numPSOs.getOrElse("0")
            )))), numPSOs.getOrElse("0").toInt)
        }.getOrElse((None, 0))
      }
      else (None, 0)
    }

    def createAllPSODetailsSections(psoModels: IndexedSeq[PSODetailsModel]): List[SummarySectionModel] = {
      psoModels.indices.map{index =>
        createPSODetailsSection(psoModels(index), index.+(1))
      }.toList
    }

    def createPSODetailsSection(model: PSODetailsModel, modelNum: Int): SummarySectionModel = {
      val name = nameString(s"psoDetails$modelNum")
      val call = CallMap.get(name)
      val date = dateDisplayString(constructDate(model.psoDay, model.psoMonth, model.psoYear))
      val amt = currencyDisplayString(model.psoAmt)
      SummarySectionModel(List(
        SummaryRowModel(name, call, boldText = false, amt, date)
      ))
    }

    def createSummaryModel(relevantAmount: BigDecimal,
                           pensionContributions: List[SummarySectionModel],
                           pensionDebits: List[SummarySectionModel])(implicit protectionType: ApplicationType.Value): SummaryModel = {
      val threshold = protectionType match {
        case ApplicationType.IP2016 => Constants.ip16RelevantAmountThreshold
        case ApplicationType.IP2014 => Constants.ip14RelevantAmountThreshold
      }
      val invalidRelevantAmount = relevantAmount < threshold
      SummaryModel(protectionType, invalidRelevantAmount, pensionContributions, pensionDebits)
    }
  
}
