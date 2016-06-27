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
import uk.gov.hmrc.http.cache.client.CacheMap

object SummaryConstructor extends SummaryConstructor {
    
}

trait SummaryConstructor {

  def createSummaryData(data: CacheMap)(implicit protectionType: ApplicationType.Value): Option[SummaryModel] = {
    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel]("pensionsTaken")

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel]("pensionsTakenBefore")
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel]("pensionsTakenBetween")
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel]("overseasPensions")
    val currentPensionsModel = data.getEntry[CurrentPensionsModel]("currentPensions")

    val pensionDebitsModel = data.getEntry[PensionDebitsModel]("pensionDebits")
    val numberOfPSOsModel = data.getEntry[NumberOfPSOsModel]("numberOfPSOs")

    def createSummaryModel(): SummaryModel = {
      val pensionContributionSeq = createPensionsTakenSeq() ::: createOverseasPensionsSeq() ::: createCurrentPensionsSeq() ::: createTotalPensionsSeq()
      val psoSeq = createPSOsSeq()
      SummaryModel(pensionContributionSeq, psoSeq)
    }

    def createPensionsTakenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTaken()
      pensionsTakenModel.get.pensionsTaken match {
        case Some("no") => List(SummaryRowModel("pensionsTaken", Some(route), Messages("pla.base.no")))
        case Some("yes") => {
          val pensionsTaken = SummaryRowModel("pensionsTaken", Some(route), Messages("pla.base.yes"))
          List(pensionsTaken) ::: createPensionsTakenBeforeSeq() ::: createPensionsTakenBetweenSeq()
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBeforeSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBefore()
      pensionsTakenBeforeModel.get.pensionsTakenBefore match {
        case "no" => List(SummaryRowModel("pensionsTakenBefore", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val pensionsTakenBefore = SummaryRowModel("pensionsTakenBefore", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(pensionsTakenBeforeModel.get.pensionsTakenBeforeAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBeforeAmt = SummaryRowModel("pensionsTakenBeforeAmt", Some(route), amt)
          List(pensionsTakenBefore, pensionsTakenBeforeAmt)
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBetweenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBetween()
      pensionsTakenBetweenModel.get.pensionsTakenBetween match {
        case "no" => List(SummaryRowModel("pensionsTakenBetween", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val pensionsTakenBetween = SummaryRowModel("pensionsTakenBetween", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(pensionsTakenBetweenModel.get.pensionsTakenBetweenAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBetweenAmt = SummaryRowModel("pensionsTakenBetweenAmt", Some(route), amt)
          List(pensionsTakenBetween, pensionsTakenBetweenAmt)
        }
        case _ => List.empty
      }
    }

    def createOverseasPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.overseasPensions()
      overseasPensionsModel.get.overseasPensions match {
        case "no" => List(SummaryRowModel("overseasPensions", Some(route), Messages("pla.base.no")))
        case "yes" => {
          val overseasPensions = SummaryRowModel("overseasPensions", Some(route), Messages("pla.base.yes"))
          val amt = currencyDisplayString(overseasPensionsModel.get.overseasPensionsAmt.getOrElse(BigDecimal(0)))
          val overseasPensionsAmt = SummaryRowModel("overseasPensionsAmt", Some(route), amt)
          List(overseasPensions, overseasPensionsAmt)
        }
        case _ => List.empty
      }
    }

    def createCurrentPensionsSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.currentPensions()
      val amt = currencyDisplayString(currentPensionsModel.get.currentPensionsAmt.getOrElse(BigDecimal(0)))
      List(SummaryRowModel("currentPensionsAmt", Some(route), amt))
    }

    def createTotalPensionsSeq(): List[SummaryRowModel] = {
      val totalAmt = currencyDisplayString(getCurrentPensionsAmount()
                      .+(getPensionsTakenBeforeAmt())
                      .+(getPensionsTakenBetweenAmt())
                      .+(getOverseasPensionsAmount())
                      )
      List(SummaryRowModel("totalPensionsAmt", None, totalAmt))
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
        case Some("yes") => List(SummaryRowModel("pensionDebits", Some(route), Messages("pla.base.yes"))) ::: createPSONumSeq()
        case _ => List(SummaryRowModel("pensionDebits", Some(route), Messages("pla.base.no")))
      }
    }

    def createPSONumSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.numberOfPSOs()
      numberOfPSOsModel.get.numberOfPSOs match {
        case Some(num) => List(SummaryRowModel("numberOfPSOsAmt", Some(route), num)) ::: createPSODetailsSeq(num.toInt)
        case _ => List(SummaryRowModel("numberOfPSOsAmt", Some(route), "None"))
      }
    }

    def createPSODetailsSeq(numPSOs: Int): List[SummaryRowModel] = {

      def loop(psoNum: Int): List[SummaryRowModel] = {
        val route = controllers.routes.IP2016Controller.psoDetails(psoNum.toString)
        val psoDetailsModel = data.getEntry[PSODetailsModel](s"psoDetails$psoNum").get
        val date = dateDisplayString(constructDate(psoDetailsModel.psoDay, psoDetailsModel.psoMonth, psoDetailsModel.psoYear))
        val amt = currencyDisplayString(psoDetailsModel.psoAmt)
        val psoEntry = List(SummaryRowModel(s"psoDetails$psoNum", Some(route), amt, date))
        if(psoNum == numPSOs) psoEntry
        else psoEntry ::: loop(psoNum + 1)
      }

      loop(1)
    }

    if(!Validation.validIPData(data)) None else Some(createSummaryModel())

  }

}
