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

package common

import java.text.SimpleDateFormat

import enums.ApplicationType
import models._
import play.api.data.{FieldMapping, FormError}
import play.api.data.Forms.of
import play.api.data.format.Formatter
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.Constants.npsMaxCurrency

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

  def isLessThanMax(amount: BigDecimal): Boolean = {
    amount <= npsMaxCurrency
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

  val mandatoryCheck: String => Boolean = input => input.trim != ""

  val yesNoCheck: String => Boolean = {
    case "yes" => true
    case "no" => true
    case "" => true
    case _ => false
  }

  def newText(errorKey: String = "error.required", optional: Boolean = false): FieldMapping[String] =
    of(stringFormatter(errorKey, optional))

  def stringFormatter(errorKey: String, optional: Boolean = false): Formatter[String] = new Formatter[String] {

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.length == 0 && optional == false => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.length == 0 && optional == true => Right(x.trim)
        case Some(s) => Right(s.trim)
      }

    def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)
  }
}
