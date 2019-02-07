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

package common

import java.text.SimpleDateFormat

import models._
import enums.ApplicationType
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.Logger

object Validation {

  def isMaxTwoDecimalPlaces(amount: BigDecimal): Boolean = {
    amount match {
      case checkAmount if checkAmount.scale <= 2 => true
      case _ => false
    }
  }

  def isPositive(amount: BigDecimal): Boolean = {
    amount match {
      case checkAmount if checkAmount < 0 => false
      case _ => true
    }
  }

  def isLessThanDouble(amount: Double, target: Double): Boolean = {
    amount < target
  }

  def isValidDate(day:Int, month:Int, year:Int): Boolean = {
    try {
      val fmt = new SimpleDateFormat("dd/MM/yyyy")
      fmt.setLenient(false)
      fmt.parse(s"$day/$month/$year")
      true
    } catch {
      case e: Exception => false
    }
  }

  def validIPData(data: CacheMap)(implicit protectionType: ApplicationType.Value): Boolean = {
    import Strings.nameString
    val pensionsTakenModel: Option[PensionsTakenModel] = data.getEntry[PensionsTakenModel](nameString("pensionsTaken"))

    val pensionsTakenBeforeModel = data.getEntry[PensionsTakenBeforeModel](nameString("pensionsTakenBefore"))
    val pensionsTakenBetweenModel = data.getEntry[PensionsTakenBetweenModel](nameString("pensionsTakenBetween"))
    val overseasPensionsModel = data.getEntry[OverseasPensionsModel](nameString("overseasPensions"))
    val currentPensionsModel = data.getEntry[CurrentPensionsModel](nameString("currentPensions"))

    val pensionDebitsModel = data.getEntry[PensionDebitsModel](nameString("pensionDebits"))
    val PSODetailsModel = data.getEntry[PSODetailsModel](nameString("psoDetails"))

    def validPensionData(): Boolean = {
      if (pensionsTakenModel.isEmpty || overseasPensionsModel.isEmpty || currentPensionsModel.isEmpty) false
      else {
        if (pensionsTakenModel.get.pensionsTaken.get == "yes") {
          pensionsTakenBeforeModel.isDefined && pensionsTakenBetweenModel.isDefined
        } else true
      }
    }

    def validPSOData(): Boolean = {
      if (pensionDebitsModel.isEmpty) false
      else {
        if (pensionDebitsModel.get.pensionDebits.get == "no") true
        else {
            !invalidPSODetails()
        }
      }
    }

    def invalidPSODetails(): Boolean = {
      data.getEntry[PSODetailsModel](nameString(s"psoDetails")).isEmpty
    }

    validPensionData() && validPSOData()
  }
}
