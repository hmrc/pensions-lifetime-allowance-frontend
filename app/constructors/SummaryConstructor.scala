/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

import enums.ApplicationType
import play.api.Logger
import play.api.i18n.{Lang, Messages}
import models._
import common.Display._
import common.Dates._
import common.Validation
import common.Strings.nameString
import utils.{CallMap, Constants}
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object SummaryConstructor extends SummaryConstructor {
    
}

trait SummaryConstructor {

  def createSummaryData(data: CacheMap)(implicit protectionType: ApplicationType.Value, lang: Lang) : Option[SummaryModel] = {

    val helper = new SummaryConstructorHelper()

    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel](nameString("pensionsTaken"))

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore"))
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel](nameString("pensionsTakenBetween"))
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel](nameString("overseasPensions"))
    val currentPensionsModel = data.getEntry[CurrentPensionsModel](nameString("currentPensions"))

    val pensionDebitsModel = data.getEntry[PensionDebitsModel](nameString("pensionDebits"))
    val psoDetails = data.getEntry[PSODetailsModel](nameString("psoDetails"))


    def relevantAmount(implicit lang: Lang): BigDecimal = {
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
        SummaryRowModel(nameString("totalPensionsAmt"), None, None, boldText = true, currencyDisplayString(relevantAmount))
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

    val pensionDebitsSection = helper.createYesNoSection("pensionDebits", pensionDebitsModel, boldText = false)

    val psoDetailsSection: Option[SummarySectionModel] = {
      pensionDebitsModel.flatMap {
        case s: PensionDebitsModel => s.getYesNoValue match {
          case "yes" => Some(helper.createPSODetailsSection(psoDetails))
          case _ => None
        }
        case _  => None
      }
    }

    val pensionDebits = List(
      pensionDebitsSection,
      psoDetailsSection
    ).flatten

    if(!Validation.validIPData(data)) {
      None
    } else {Some(helper.createSummaryModel(relevantAmount, pensionContributions, pensionDebits))}

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

    def createYesNoSection(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean)(implicit lang: Lang): Option[SummarySectionModel] = {
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

    def createYesNoAmountSection(dataName: String, modelOption: Option[YesNoAmountModel], boldText: Boolean)(implicit lang: Lang) = {
      SummarySectionModel(
        List(
          createYesNoRow(dataName, modelOption, boldText),
          createYesNoAmountRow(dataName, modelOption, boldText)
        ).flatten
      )
    }

    def createYesNoRow(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean)(implicit lang: Lang) = {
      modelOption.map { model =>
        val name = nameString(dataName)
        val call = CallMap.get(name)
        val displayValue = yesNoValue(model)
        SummaryRowModel(
          name, call, None, boldText, displayValue
        )
      }
    }

    def yesNoValue(model: YesNoModel)(implicit lang: Lang): String = {
      Messages(s"pla.base.${model.getYesNoValue}")
    }

    def createYesNoAmountRow(dataName: String, modelOption: Option[YesNoAmountModel], boldText: Boolean) = {
      val name = nameString(dataName)
      val call = CallMap.get(name)
      if(positiveAnswer(modelOption))
        Some(SummaryRowModel(
          name+"Amt", call, None, boldText, amountDisplayValue(modelOption.get)
          )
        )
      else None
    }

    def createAmountRow(dataName: String, modelOption: Option[AmountModel],boldText: Boolean) = {
      val name = nameString(dataName)
      modelOption.map{ model =>
        val call = CallMap.get(name)
          Some(SummaryRowModel(
            name+"Amt", call, None, boldText, amountDisplayValue(model)
          ))
      }.getOrElse(None)
    }

    def createPSODetailsSection(model: Option[PSODetailsModel])(implicit lang: Lang) = {
      model match {
        case Some(m) =>
          val name = nameString(s"psoDetails")
          val changeCall = CallMap.get(name)
          val removeCall = CallMap.get("remove"+name.capitalize)
          val date = dateDisplayString(constructDate(m.psoDay, m.psoMonth, m.psoYear))
          val amt = currencyDisplayString(m.psoAmt)
          SummarySectionModel(List(
            SummaryRowModel(name, changeCall, removeCall, boldText = false, amt, date)
          ))
        case None => SummarySectionModel(List.empty)
      }

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
