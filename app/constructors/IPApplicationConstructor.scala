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

import common.Validation
import common.Strings.nameString
import common.Dates.apiDateFormat
import models._
import enums.ApplicationType
import play.api.Logger
import uk.gov.hmrc.http.cache.client.CacheMap

object IPApplicationConstructor {

  def createIPApplication(data: CacheMap)(implicit protectionType: ApplicationType.Value) : IPApplicationModel = {

    assert(Validation.validIPData(data), Logger.error(s"Invalid application data provided to createIPApplication for $protectionType. Data: $data"))

    val relevantAmount = data.getEntry[CurrentPensionsModel](nameString("currentPensions")).get.currentPensionsAmt

    val preADayPensionInPayment = data.getEntry[PensionsTakenModel](nameString("pensionsTaken")) match {
      case Some(model) => model.pensionsTaken match {
        case Some("yes") => data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore")).get.pensionsTakenBeforeAmt
        case _ => None
      }
      case _ => None
    }

    val postADayBenefitCrystallisationEvents = data.getEntry[PensionsTakenModel](nameString("pensionsTaken")) match {
      case Some(model) => model.pensionsTaken match {
        case Some("yes") => data.getEntry[PensionsTakenBetweenModel](nameString("pensionsTakenBetween")).get.pensionsTakenBetweenAmt
        case _ => None
      }
      case _ => None
    }

    val nonUKRights = data.getEntry[OverseasPensionsModel](nameString("overseasPensions")).get.overseasPensionsAmt


    val numPSOs = data.getEntry[PensionDebitsModel](nameString("pensionDebits")) match {
      case Some(pdModel) => pdModel.pensionDebits match {
        case Some("yes") =>  data.getEntry[NumberOfPSOsModel](nameString("numberOfPSOs")) match {
                                case Some(model) => model.numberOfPSOs.getOrElse("0").toInt
                                case _ => 0
                              }
        case _ => 0
      }
      case _ => 0
    }


    lazy val pensionDebits = if(numPSOs == 0) None else {
      Some((1 to numPSOs).map(psoNum => createPensionDebit(data.getEntry[PSODetailsModel](nameString(s"psoDetails$psoNum")).get)).toList)
    }

    def createPensionDebit(model: PSODetailsModel): PensionDebit = {
      PensionDebit(apiDateFormat(model.psoDay, model.psoMonth, model.psoYear), model.psoAmt.toDouble)
    }

    val protectionString = protectionType match {
      case ApplicationType.IP2016 => "IP2016"
      case ApplicationType.IP2014 => "IP2014"
    }

    IPApplicationModel(
      protectionString,
      optionBigDecToOptionDouble(relevantAmount),
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
