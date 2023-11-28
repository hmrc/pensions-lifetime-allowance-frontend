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

import common.Dates.apiDateFormat
import common.Strings.nameString
import common.Validation
import enums.ApplicationType
import models._
import models.cache.CacheMap

object IPApplicationConstructor {

  def createIPApplication(data: CacheMap)(implicit protectionType: ApplicationType.Value) : IPApplicationModel = {

    assert(Validation.validIPData(data), s"Invalid application data provided to createIPApplication for $protectionType. Data: $data")

    // uncrystallised Rights- current pensions
    val uncrystallisedRightsAmount = data.getEntry[CurrentPensionsModel](nameString("currentPensions")).get.currentPensionsAmt


    def getPensionsTakenBeforeAmt() = {
      data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore")) match {
        case Some(model) => model.pensionsTakenBefore match {
          case "yes" => data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore")).get.pensionsTakenBeforeAmt
          case _ => Some(BigDecimal(0))
        }
        case _ => Some(BigDecimal(0))
      }
    }

    val pensionsUsedBetweenAmount = data.getEntry[PensionsUsedBetweenModel](nameString("pensionsUsedBetween")).get.pensionsUsedBetweenAmt

    // preADay - Pensions taken before
    val preADayPensionInPayment: Option[BigDecimal] = data.getEntry[PensionsTakenModel](nameString("pensionsTaken")) match {
      case Some(model) => model.pensionsTaken match {
        case Some("yes") => getPensionsTakenBeforeAmt()
        case _ => Some(BigDecimal(0))
      }
      case _ => Some(BigDecimal(0))
    }

    // postADay - Pensions taken between
    val postADayBenefitCrystallisationEvents = data.getEntry[PensionsTakenModel](nameString("pensionsTaken")) match {
      case Some(model) => model.pensionsTaken match {
        case Some("yes") => pensionsUsedBetweenAmount
        case _ => Some(BigDecimal(0))
      }
      case _ => Some(BigDecimal(0))
    }

    // nonUKRights - Overseas pensions
    val nonUKRights = data.getEntry[OverseasPensionsModel](nameString("overseasPensions")) match {
      case Some(model) => model.overseasPensions match {
        case "yes" => data.getEntry[OverseasPensionsModel](nameString("overseasPensions")).get.overseasPensionsAmt
        case _ => Some(BigDecimal(0))
      }
      case _ => Some(BigDecimal(0))
    }

    val amounts: List[Option[BigDecimal]] = List(uncrystallisedRightsAmount,preADayPensionInPayment, postADayBenefitCrystallisationEvents, nonUKRights)
    val relevantAmount = amounts.flatten.sum

    
    val hasPso = data.getEntry[PensionDebitsModel](nameString("pensionDebits")) match {
      case Some(pdModel) => pdModel.pensionDebits match {
        case Some("yes")  =>  true
        case _            => false
      }
      case None => false
    }

    lazy val pensionDebits = if(!hasPso) None else {
      Option(List(createPensionDebit(data.getEntry[PSODetailsModel](nameString(s"psoDetails")).get)))
    }

    def createPensionDebit(model: PSODetailsModel): PensionDebit = {
      PensionDebit(apiDateFormat(model.psoDay, model.psoMonth, model.psoYear), model.psoAmt.getOrElse(BigDecimal(0.0)).toDouble)
    }

    val protectionString = protectionType match {
      case ApplicationType.IP2016 => "IP2016"
      case ApplicationType.IP2014 => "IP2014"
    }

    IPApplicationModel(
      protectionString,
      relevantAmount.toDouble,
      optionBigDecToOptionDouble(uncrystallisedRightsAmount),
      optionBigDecToOptionDouble(preADayPensionInPayment),
      optionBigDecToOptionDouble(postADayBenefitCrystallisationEvents),
      optionBigDecToOptionDouble(nonUKRights),
      pensionDebits)
  }

  def optionBigDecToOptionDouble(opt: Option[BigDecimal]) = {
    opt match {
      case Some(value) => Some(value.toDouble)
      case _ => None
    }
  }


}
