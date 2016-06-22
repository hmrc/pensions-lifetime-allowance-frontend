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



    def validData(): Boolean = {
      if(pensionsTakenModel.isEmpty || overseasPensionsModel.isEmpty || currentPensionsModel.isEmpty) false else {
        if(pensionsTakenModel.get.pensionsTaken.get == "yes") {
          pensionsTakenBeforeModel.isDefined && pensionsTakenBetweenModel.isDefined
        } else true
      }
    }

    def createSummaryModel(): SummaryModel = {
      val pensionsTakenSeq = createPensionsTakenSeq()
      SummaryModel(pensionsTakenSeq)
    }

    def createPensionsTakenSeq(): List[SummaryRowModel] = {
      pensionsTakenModel.get.pensionsTaken match {
        case Some("no") => List(SummaryRowModel(Messages("pla.summary.questions.pensionsTaken"), Messages("pla.base.no"), Some(controllers.routes.IP2016Controller.pensionsTaken())))
        case Some("yes") => {
          val pensionsTaken = SummaryRowModel(Messages("pla.summary.questions.pensionsTaken"), Messages("pla.base.yes"), Some(controllers.routes.IP2016Controller.pensionsTaken()))
          List(pensionsTaken) ::: createPensionsTakenBeforeSeq() ::: createPensionsTakenBetweenSeq()
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBeforeSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBefore()
      pensionsTakenBeforeModel.get.pensionsTakenBefore match {
        case "no" => List(SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBefore"), Messages("pla.base.no"), Some(route)))
        case "yes" => {
          val pensionsTakenBefore = SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBefore"), Messages("pla.base.yes"), Some(route))
          val amt = currencyDisplayString(pensionsTakenBeforeModel.get.pensionsTakenBeforeAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBeforeAmt = SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBeforeAmt"), amt, Some(route))
          List(pensionsTakenBefore, pensionsTakenBeforeAmt)
        }
        case _ => List.empty
      }
    }

    def createPensionsTakenBetweenSeq(): List[SummaryRowModel] = {
      val route = controllers.routes.IP2016Controller.pensionsTakenBetween()
      pensionsTakenBetweenModel.get.pensionsTakenBetween match {
        case "no" => List(SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBetween"), Messages("pla.base.no"), Some(route)))
        case "yes" => {
          val pensionsTakenBetween = SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBetween"), Messages("pla.base.yes"), Some(route))
          val amt = currencyDisplayString(pensionsTakenBetweenModel.get.pensionsTakenBetweenAmt.getOrElse(BigDecimal(0)))
          val pensionsTakenBetweenAmt = SummaryRowModel(Messages("pla.summary.questions.pensionsTakenBetweenAmt"), amt, Some(route))
          List(pensionsTakenBetween, pensionsTakenBetweenAmt)
        }
        case _ => List.empty
      }
    }

    if(!validData()) None else Some(createSummaryModel())

  }

}
