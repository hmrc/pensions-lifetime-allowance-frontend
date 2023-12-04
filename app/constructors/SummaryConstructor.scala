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

package constructors

import common.Dates._
import common.Display._
import common.Strings.nameString
import common.Validation
import enums.ApplicationType
import models._
import play.api.i18n.{Lang, MessagesProvider}
import models.cache.CacheMap
import utils.{CallMap, Constants}

object SummaryConstructor extends SummaryConstructor {

}

trait SummaryConstructor {

  def createSummaryData(data: CacheMap)(implicit protectionType: ApplicationType.Value,
                                        lang: Lang, provider: MessagesProvider) : Option[SummaryModel] = {

    val helper = new SummaryConstructorHelper()

    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel](nameString("pensionsTaken"))

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore"))
    val pensionsWorthBeforeModel = data.getEntry[PensionsWorthBeforeModel](nameString("pensionsWorthBefore"))
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel](nameString("pensionsTakenBetween"))
    val pensionsUsedBetweenModel = data.getEntry[PensionsUsedBetweenModel](nameString("pensionsUsedBetween"))
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel](nameString("overseasPensions"))
    val currentPensionsModel = data.getEntry[CurrentPensionsModel](nameString("currentPensions"))

    val pensionDebitsModel = data.getEntry[PensionDebitsModel](nameString("pensionDebits"))
    val psoDetails = data.getEntry[PSODetailsModel](nameString("psoDetails"))


    def relevantAmount(): BigDecimal = {
      val (pensionsBeforeAmt, pensionsBetweenAmt) = if(helper.positiveAnswer(pensionsTakenModel)) (
        if(helper.positiveAnswer(pensionsTakenBeforeModel)) helper.amountValue(pensionsWorthBeforeModel) else None,
        if(helper.positiveAnswer(pensionsTakenBetweenModel)) helper.amountValue(pensionsUsedBetweenModel) else None
      )
      else (None, None)
      val overseasPensionsAmnt = if(helper.positiveAnswer(overseasPensionsModel)) helper.amountValue(overseasPensionsModel) else None
      val currentPensionsAmnt = helper.amountValue(currentPensionsModel)

      List(pensionsBeforeAmt,pensionsBetweenAmt,overseasPensionsAmnt,currentPensionsAmnt).flatten.sum
    }

    val pensionsTakenSection = helper.createYesNoSection("pensionsTaken", pensionsTakenModel, boldText = false)
    val pensionsTakenBeforeSection = if(helper.positiveAnswer(pensionsTakenModel)) {
      helper.createYesNoSection("pensionsTakenBefore", pensionsTakenBeforeModel, boldText = false)
    } else None
    val pensionsWorthBeforeSection = if(helper.positiveAnswer(pensionsTakenModel) && helper.positiveAnswer(pensionsTakenBeforeModel)) {
      Some(helper.createAmountSection("pensionsWorthBefore", pensionsWorthBeforeModel, boldText = false))
    } else None

    val pensionsTakenBetweenSection = if (helper.positiveAnswer(pensionsTakenModel)) {
      helper.createYesNoSection("pensionsTakenBetween", pensionsTakenBetweenModel, boldText = false)
    } else None
    val pensionsUsedBetweenSection = if (helper.positiveAnswer(pensionsTakenModel) && helper.positiveAnswer(pensionsTakenBetweenModel)) {
      Some(helper.createAmountSection("pensionsUsedBetween", pensionsUsedBetweenModel, boldText = false))
    } else None

    val overseasPensionsSection = Some(helper.createYesNoAmountSection("overseasPensions", overseasPensionsModel, boldText = false))
    val currentPensionsSection = Some(helper.createAmountSection("currentPensions", currentPensionsModel, boldText = false))
    
    val totalPensionsSection = Some(
      SummarySectionModel(List(
        SummaryRowModel(nameString("totalPensionsAmt"), None, None, boldText = false, currencyDisplayString(relevantAmount()))
      ))
    )

    val pensionContributions = List(
      pensionsTakenSection,
      pensionsTakenBeforeSection,
      pensionsWorthBeforeSection,
      pensionsTakenBetweenSection,
      pensionsUsedBetweenSection,
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
    } else {
      Some(helper.createSummaryModel(relevantAmount(), pensionContributions, pensionDebits))
    }

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

  def createYesNoSection(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean)
                        (implicit provider: MessagesProvider): Option[SummarySectionModel] = {
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

  def createYesNoAmountSection(dataName: String, modelOption: Option[YesNoAmountModel], boldText: Boolean)
                              (implicit provider: MessagesProvider) = {
    SummarySectionModel(
      List(
        createYesNoRow(dataName, modelOption, boldText),
        createYesNoAmountRow(dataName, modelOption, boldText)
      ).flatten
    )
  }

  def createYesNoRow(dataName: String, modelOption: Option[YesNoModel], boldText: Boolean)
                    (implicit provider: MessagesProvider) = {
    modelOption.map { model =>
      val name = nameString(dataName)
      val call = CallMap.get(name)
      val displayValue = yesNoValue(model)
      SummaryRowModel(
        name, call, None, boldText, displayValue
      )
    }
  }

  def yesNoValue(model: YesNoModel)(implicit provider: MessagesProvider): String = {
    provider.messages(s"pla.base.${model.getYesNoValue}")
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

  def createPSODetailsSection(model: Option[PSODetailsModel])(implicit lang: Lang, provider: MessagesProvider) = {
    model match {
      case Some(m) =>
        val name = nameString(s"psoDetails")
        val changeCall = CallMap.get(name)
        val removeCall = CallMap.get("remove"+name.capitalize)
        val date = dateDisplayString(constructDate(m.psoDay, m.psoMonth, m.psoYear))
        val amt = currencyDisplayString(m.psoAmt.getOrElse(BigDecimal(0.0)))
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
