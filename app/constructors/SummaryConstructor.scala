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

import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import models._
import common.Display._
import common.Dates._
import controllers._
import uk.gov.hmrc.http.cache.client.CacheMap

object SummaryConstructor extends SummaryConstructor {
    
}

trait SummaryConstructor {

  def createSummaryData(data: CacheMap): Option[SummaryModel] = {
    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel]("pensionsTaken")

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel]("pensionsTakenBefore")
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel]("pensionsTakenBetween")
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel]("overseasPensions")
    val currentPensionsModel = data.getEntry[CurrentPensionsModel]("currentPensions")

    val pensionDebitsModel = data.getEntry[PensionDebitsModel]("pensionDebits")
    val numberOfPSOsModel = data.getEntry[NumberOfPSOsModel]("numberOfPSOs")



    def validData(): Boolean = {
      if(pensionsTakenModel.isEmpty || overseasPensionsModel.isEmpty || currentPensionsModel.isEmpty) false else {
        if(pensionsTakenModel.get.pensionsTaken.get == "yes") {
          pensionsTakenBeforeModel.isDefined && pensionsTakenBetweenModel.isDefined
        } else true
      }
    }

    def createSummaryModel(): SummaryModel = {
      val pensionContributionSeq = createPensionsTakenSeq() ::: createOverseasPensionsSeq() ::: createCurrentPensionsSeq() ::: createTotalPensionsSeq()
      val psoSeq = createPSOsSeq()
      SummaryModel(pensionContributionSeq, psoSeq)
    }

    def createPensionsTakenSeq(): List[SummaryRowModel] = {
      pensionsTakenModel.get.pensionsTaken match {
        case Some("no") => List(SummaryRowModel("pensionsTaken", Messages("pla.summary.questions.pensionsTaken"), Messages("pla.base.no"), Some(controllers.routes.IP2016Controller.pensionsTaken())))
        case Some("yes") => {
          val pensionsTaken = SummaryRowModel("pensionsTaken", Messages("pla.summary.questions.pensionsTaken"), Messages("pla.base.yes"), Some(controllers.routes.IP2016Controller.pensionsTaken()))
          List(pensionsTaken) ::: createPensionsTakenBeforeSeq() ::: createPensionsTakenBetweenSeq()
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBeforeSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBefore()
      pensionsTakenBeforeModel.get.pensionsTakenBefore match {
        case "no" => List(SummaryRowModel("pensionsTakenBefore", Messages("pla.summary.questions.pensionsTakenBefore"), Messages("pla.base.no"), Some(route)))
        case "yes" => {
          val pensionsTakenBefore = SummaryRowModel("pensionsTakenBefore", Messages("pla.summary.questions.pensionsTakenBefore"), Messages("pla.base.yes"), Some(route))
          val amt = currencyDisplayString(pensionsTakenBeforeModel.get.pensionsTakenBeforeAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBeforeAmt = SummaryRowModel("pensionsTakenBeforeAmt", Messages("pla.summary.questions.pensionsTakenBeforeAmt"), amt, Some(route))
          List(pensionsTakenBefore, pensionsTakenBeforeAmt)
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBetweenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBetween()
      pensionsTakenBetweenModel.get.pensionsTakenBetween match {
        case "no" => List(SummaryRowModel("pensionsTakenBetween", Messages("pla.summary.questions.pensionsTakenBetween"), Messages("pla.base.no"), Some(route)))
        case "yes" => {
          val pensionsTakenBetween = SummaryRowModel("pensionsTakenBetween", Messages("pla.summary.questions.pensionsTakenBetween"), Messages("pla.base.yes"), Some(route))
          val amt = currencyDisplayString(pensionsTakenBetweenModel.get.pensionsTakenBetweenAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBetweenAmt = SummaryRowModel("pensionsTakenBetweenAmt", Messages("pla.summary.questions.pensionsTakenBetweenAmt"), amt, Some(route))
          List(pensionsTakenBetween, pensionsTakenBetweenAmt)
        }
        case _ => List.empty
      }
    }

    def createOverseasPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.overseasPensions()
      overseasPensionsModel.get.overseasPensions match {
        case "no" => List(SummaryRowModel("overseasPensions", Messages("pla.summary.questions.overseasPensions"), Messages("pla.base.no"), Some(route)))
        case "yes" => {
          val overseasPensions = SummaryRowModel("overseasPensions", Messages("pla.summary.questions.overseasPensions"), Messages("pla.base.yes"), Some(route))
          val amt = currencyDisplayString(overseasPensionsModel.get.overseasPensionsAmt.getOrElse(BigDecimal(0)))
          val overseasPensionsAmt = SummaryRowModel("overseasPensionsAmt", Messages("pla.summary.questions.overseasPensionsAmt"), amt, Some(route))
          List(overseasPensions, overseasPensionsAmt)
        }
        case _ => List.empty
      }
    }

    def createCurrentPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.currentPensions()
      val amt = currencyDisplayString(currentPensionsModel.get.currentPensionsAmt.getOrElse(BigDecimal(0)))
      List(SummaryRowModel("currentPensionsAmt", Messages("pla.summary.questions.currentPensions"), amt, Some(route)))
    }

    def createTotalPensionsSeq(): List[SummaryRowModel] = {
      val totalAmt = currencyDisplayString(getCurrentPensionsAmount()
                      .+(getPensionsTakenBeforeAmt())
                      .+(getPensionsTakenBetweenAmt())
                      .+(getOverseasPensionsAmount())
                      )
      List(SummaryRowModel("totalPensionsAmt", Messages("pla.summary.questions.totalSavings"), totalAmt, None))
    }

    def getPensionsTakenBeforeAmt(): BigDecimal = {
      pensionsTakenModel.get.pensionsTaken.get match {
        case "yes" => {
          pensionsTakenBeforeModel.get.pensionsTakenBefore match {
            case "yes" => pensionsTakenBeforeModel.get.pensionsTakenBeforeAmt.getOrElse(BigDecimal(0))
            case _ => BigDecimal(0)
          }
        }
        case _ => BigDecimal(0)
      }
    }

    def getPensionsTakenBetweenAmt(): BigDecimal = {
      pensionsTakenModel.get.pensionsTaken.get match {
        case "yes" => {
          pensionsTakenBetweenModel.get.pensionsTakenBetween match {
            case "yes" => pensionsTakenBetweenModel.get.pensionsTakenBetweenAmt.getOrElse(BigDecimal(0))
            case _ => BigDecimal(0)
          }
        }
        case _ => BigDecimal(0)
      }
    }

    def getOverseasPensionsAmount(): BigDecimal = {
      overseasPensionsModel.get.overseasPensions match {
        case "yes" => overseasPensionsModel.get.overseasPensionsAmt.getOrElse(BigDecimal(0))
        case _ => BigDecimal(0)
      }
    }

    def getCurrentPensionsAmount(): BigDecimal = {
      currentPensionsModel.get.currentPensionsAmt.getOrElse(BigDecimal(0))
    }

    def createPSOsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionDebits()
      pensionDebitsModel.get.pensionDebits match {
        case Some("yes") => List(SummaryRowModel("pensionDebits", Messages("pla.summary.questions.pensionDebits"), Messages("pla.base.yes"), Some(route))) ::: createPSONumSeq()
        case _ => List(SummaryRowModel("pensionDebits", Messages("pla.summary.questions.pensionDebits"), Messages("pla.base.no"), Some(route)))
      }
    }

    def createPSONumSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.numberOfPSOs()
      numberOfPSOsModel.get.numberOfPSOs match {
        case Some(num) => List(SummaryRowModel("numberOfPSOsAmt", Messages("pla.summary.questions.numberOfPSOs"), num, Some(route))) ::: createPSODetailsSeq(num.toInt)
        case _ => List(SummaryRowModel("numberOfPSOsAmt", Messages("pla.summary.questions.numberOfPSOs"), "None", Some(route)))
      }
    }

    def createPSODetailsSeq(numPSOs: Int): List[SummaryRowModel] = {

      def loop(psoNum: Int): List[SummaryRowModel] = {
        val route = controllers.routes.IP2016Controller.psoDetails(psoNum.toString)
        val psoDetailsModel = data.getEntry[PSODetailsModel](s"psoDetails$psoNum").get
        val date = dateDisplayString(constructDate(psoDetailsModel.psoDay, psoDetailsModel.psoMonth, psoDetailsModel.psoYear))
        val amt = currencyDisplayString(psoDetailsModel.psoAmt)
        val on = Messages("pla.summary.onDate")
        val displayValue = s"$amt $on $date"
        val psoEntry = List(SummaryRowModel(s"psoDetails$psoNum", Messages(s"pla.summary.questions.psoDetails$psoNum"), displayValue, Some(route)))
        if(psoNum == numPSOs) psoEntry
        else psoEntry ::: loop(psoNum + 1)
      }

      loop(1)
    }

    if(!validData()) None else Some(createSummaryModel())

  }

}
