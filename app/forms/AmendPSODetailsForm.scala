/*
 * Copyright 2017 HM Revenue & Customs
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

package forms

import common.Exceptions
import common.Validation._
import common.Dates._
import models.amendModels.AmendPSODetailsModel
import utils.Constants
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object AmendPSODetailsForm {

  def validateForm(form: Form[AmendPSODetailsModel]): Form[AmendPSODetailsModel] = {
    val (day, month, year) = getFormDateValues(form)
    if(dateFieldsAlreadyInvalid(form)) form
    else if(!isValidDate(day, month, year)) form.withError("psoDay", Messages("pla.base.errors.invalidDate"))
    else if(futureDate(day, month, year)) formWithDateOutOfRangeError(form)
    else validateMinDate(form, day, month, year)
  }

  private def validateMinDate(form: Form[AmendPSODetailsModel], day: Int, month: Int, year: Int): Form[AmendPSODetailsModel] = {
    val pType = form("protectionType").value.getOrElse{throw new Exceptions.RequiredValueNotDefinedException("validateMinDate", "protectionType")}
    val (minDate, message) = pType.toLowerCase match {
      case "ip2016" => (Constants.minIP16PSODate, "IP16PsoDetails")
      case "ip2014" => (Constants.minIP14PSODate, "IP14PsoDetails")
      case other => {throw new Exceptions.RequiredValueNotDefinedException("validateMinDate", other)}
    }

    if(dateBefore(day, month, year, minDate)) form.withError("psoDay", Messages(s"pla.$message.errorDateOutOfRange"))
    else form
  }

  private def formWithDateOutOfRangeError(form: Form[AmendPSODetailsModel]): Form[AmendPSODetailsModel] = {
    val pType = form("protectionType").value.getOrElse{throw new Exceptions.RequiredValueNotDefinedException("validateMinDate", "protectionType")}
    pType.toLowerCase match {
      case "ip2016" => form.withError("psoDay", Messages(s"pla.IP16PsoDetails.errorDateOutOfRange"))
      case "ip2014" => form.withError("psoDay", Messages(s"pla.IP14PsoDetails.errorDateOutOfRange"))
    }
  }

  private def getFormDateValues(form: Form[AmendPSODetailsModel]): (Int, Int, Int) = {
    import common.Strings._
    (
      form("psoDay").value.getOrElse("0").toIntOpt.getOrElse(0),
      form("psoMonth").value.getOrElse("0").toIntOpt.getOrElse(0),
      form("psoYear").value.getOrElse("0").toIntOpt.getOrElse(0)
      )
  }

  // returns true if the passed form already contains an error with the key from any of the date fields
  private def dateFieldsAlreadyInvalid(form: Form[AmendPSODetailsModel]): Boolean = {
    form.errors.map(_.key).exists(List("psoDay","psoMonth","psoYear").contains(_))
  }

  val amendPsoDetailsForm = Form(
    mapping(
      "psoDay"    -> optional(number).verifying(Messages("pla.base.errors.dayEmpty"), {_.isDefined}),
      "psoMonth"  -> optional(number).verifying(Messages("pla.base.errors.monthEmpty"), {_.isDefined}),
      "psoYear"   -> optional(number).verifying(Messages("pla.base.errors.yearEmpty"), {_.isDefined}),
      "psoAmt"    -> optional(bigDecimal)
        .verifying(Messages("pla.psoDetails.errorMaximum"), psoAmt => isLessThanDouble(psoAmt.getOrElse(BigDecimal(0.0)).toDouble, Constants.npsMaxCurrency))
        .verifying(Messages("pla.psoDetails.errorNegative"), psoAmt => isPositive(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying(Messages("pla.psoDetails.errorDecimalPlaces"), psoAmt => isMaxTwoDecimalPlaces(psoAmt.getOrElse(BigDecimal(0.0)).toDouble))
        .verifying(Messages("pla.psoDetails.errorQuestion"), _.isDefined),

      "protectionType" -> text,
      "status"         -> text,
      "existingPSO"    -> boolean
    )(AmendPSODetailsModel.apply)(AmendPSODetailsModel.unapply)
  )
}
