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
import play.api.i18n.Messages
import models._
import common.Display._
import common.Dates._
import common.Validation
import utils.Constants
import uk.gov.hmrc.http.cache.client.CacheMap

object IP14SummaryConstructor extends IP14SummaryConstructor {
    
}

trait IP14SummaryConstructor {

  def createSummaryData(data: CacheMap)(implicit protectionType: ApplicationType.Value): Option[SummaryModel] = {
    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel]("ip14PensionsTaken")

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel]("ip14PensionsTakenBefore")
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel]("ip14PensionsTakenBetween")
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel]("ip14OverseasPensions")
    val currentPensionsModel = data.getEntry[CurrentPensionsModel]("ip14CurrentPensions")

    val pensionDebitsModel = data.getEntry[PensionDebitsModel]("ip14PensionDebits")
    val numberOfPSOsModel = data.getEntry[NumberOfPSOsModel]("ip14NumberOfPSOs")

    def createSummaryModel(): SummaryModel = {
      val pensionContributionSeq = createPensionsTakenSeq() ::: createOverseasPensionsSeq() ::: createCurrentPensionsSeq() ::: createTotalPensionsSeq()
      val psoSeq = createPSOsSeq()
      val invalidRelevantAmount = relevantAmount() <= Constants.ipRelevantAmountThreshold
      SummaryModel(invalidRelevantAmount, pensionContributionSeq, psoSeq)
    }

    def createPensionsTakenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14PensionsTaken()
      pensionsTakenModel.get.pensionsTaken match {
        case Some("no") => List(SummaryRowModel("ip14PensionsTaken", Some(route), Messages("pla.base.no")))
        case Some("yes") => {
          val pensionsTaken = SummaryRowModel("ip14PensionsTaken", Some(route), Messages("pla.base.yes"))
          List(pensionsTaken) ::: createPensionsTakenBeforeSeq() ::: createPensionsTakenBetweenSeq()
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBeforeSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14PensionsTakenBefore()
      pensionsTakenBeforeModel.get.pensionsTakenBefore match {
        case "no" => List(SummaryRowModel("ip14PensionsTakenBefore", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val pensionsTakenBefore = SummaryRowModel("ip14PensionsTakenBefore", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(pensionsTakenBeforeModel.get.pensionsTakenBeforeAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBeforeAmt = SummaryRowModel("ip14PensionsTakenBeforeAmt", Some(route), amt)
          List(pensionsTakenBefore, pensionsTakenBeforeAmt)
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBetweenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14PensionsTakenBetween()
      pensionsTakenBetweenModel.get.pensionsTakenBetween match {
        case "no" => List(SummaryRowModel("ip14PensionsTakenBetween", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val pensionsTakenBetween = SummaryRowModel("ip14PensionsTakenBetween", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(pensionsTakenBetweenModel.get.pensionsTakenBetweenAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBetweenAmt = SummaryRowModel("ip14PensionsTakenBetweenAmt", Some(route), amt)
          List(pensionsTakenBetween, pensionsTakenBetweenAmt)
        }
        case _ => List.empty
      }
    }

    def createOverseasPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14OverseasPensions()
      overseasPensionsModel.get.overseasPensions match {
        case "no" => List(SummaryRowModel("ip14OverseasPensions", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val overseasPensions = SummaryRowModel("ip14OverseasPensions", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(overseasPensionsModel.get.overseasPensionsAmt.getOrElse(BigDecimal(0)))
          val overseasPensionsAmt = SummaryRowModel("ip14OverseasPensionsAmt", Some(route), amt)
          List(overseasPensions, overseasPensionsAmt)
        }
        case _ => List.empty
      }
    }

    def createCurrentPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14CurrentPensions()
      val amt = currencyDisplayString(currentPensionsModel.get.currentPensionsAmt.getOrElse(BigDecimal(0)))
      List(SummaryRowModel("ip14CurrentPensionsAmt", Some(route), amt))
    }

    def createTotalPensionsSeq(): List[SummaryRowModel] = {
      val totalAmt = currencyDisplayString(relevantAmount)
      List(SummaryRowModel("ip14TotalPensionsAmt", None, totalAmt))
    }

    def relevantAmount(): BigDecimal = {
      getCurrentPensionsAmount()
      .+(getPensionsTakenBeforeAmt())
      .+(getPensionsTakenBetweenAmt())
      .+(getOverseasPensionsAmount())
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
      val route = controllers.routes.IP2014Controller.ip14PensionDebits()
      pensionDebitsModel.get.pensionDebits match {
        case Some("yes") => List(SummaryRowModel("ip14PensionDebits", Some(route), Messages("pla.base.yes"))) ::: createPSONumSeq()
        case _ => List(SummaryRowModel("ip14PensionDebits", Some(route), Messages("pla.base.no")))
      }
    }

    def createPSONumSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2014Controller.ip14NumberOfPSOs()
      numberOfPSOsModel.get.numberOfPSOs match {
        case Some(num) => List(SummaryRowModel("ip14NumberOfPSOsAmt", Some(route), num)) ::: createPSODetailsSeq(num.toInt)
        case _ => List(SummaryRowModel("ip14NumberOfPSOsAmt", Some(route), "None"))
      }
    }

    def createPSODetailsSeq(numPSOs: Int): List[SummaryRowModel] = {

      def loop(psoNum: Int): List[SummaryRowModel] = {
        val route = controllers.routes.IP2014Controller.ip14PsoDetails(psoNum.toString)
        val psoDetailsModel = data.getEntry[PSODetailsModel](s"ip14PsoDetails$psoNum").get
        val date = dateDisplayString(constructDate(psoDetailsModel.psoDay, psoDetailsModel.psoMonth, psoDetailsModel.psoYear))
        val amt = currencyDisplayString(psoDetailsModel.psoAmt)
        val psoEntry = List(SummaryRowModel(s"ip14PsoDetails$psoNum", Some(route), amt, date))
        if(psoNum == numPSOs) psoEntry
        else psoEntry ::: loop(psoNum + 1)
      }

      loop(1)
    }

    if(!Validation.validIPData(data)) None else Some(createSummaryModel())

  }

}
