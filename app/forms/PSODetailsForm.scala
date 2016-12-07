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

package forms

import models._
import common.Validation._
import common.Dates._
import utils.Constants
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.play.http.HeaderCarrier

object PSODetailsForm {

  val hc = new HeaderCarrier()

  def validateForm(form: Form[PSODetailsModel])(implicit lang:Lang): Form[PSODetailsModel] = {
    val (day, month, year) = getFormDateValues(form)
    if(dateFieldsAlreadyInvalid(form)) form
    else if(!isValidDate(day, month, year)) form.withError("psoDay", Messages("pla.base.errors.invalidDate"))
    else if(dateBefore(day, month, year, Constants.minIP16PSODate)) form.withError("psoDay", Messages("pla.IP16PsoDetails.errorDateOutOfRange"))
    else if(futureDate(day, month, year)) form.withError("psoDay", Messages("pla.IP16PsoDetails.errorDateOutOfRange"))
    else form
  }

  private def getFormDateValues(form: Form[PSODetailsModel]): (Int, Int, Int) = {
    import common.Strings._
    (
      form("psoDay").value.getOrElse("0").toIntOpt.getOrElse(0),
      form("psoMonth").value.getOrElse("0").toIntOpt.getOrElse(0),
      form("psoYear").value.getOrElse("0").toIntOpt.getOrElse(0)
      )
  }

  // returns true if the passed form already contains an error with the key from any of the date fields
  private def dateFieldsAlreadyInvalid(form: Form[PSODetailsModel]): Boolean = {
    form.errors.map(_.key).exists(List("psoDay","psoMonth","psoYear").contains(_))
  }

//  private def invalidPsoAmt(psoAmt: BigDecimal) {
//    if(!isLessThanDouble(psoAmt.toDouble, Constants.npsMaxCurrency)) Messages("pla.psoDetails.errorMaximum")
//    if(!isPositive(psoAmt.toDouble)) Messages("pla.psoDetails.errorNegative")
//
//  }

  def psoDetailsForm(implicit lang: Lang) = Form(
  mapping(
    "psoDay"    -> optional(number).verifying(Messages("pla.base.errors.dayEmpty"), {_.isDefined}),
    "psoMonth"  -> optional(number).verifying(Messages("pla.base.errors.monthEmpty"), {_.isDefined}),
    "psoYear"   -> optional(number).verifying(Messages("pla.base.errors.yearEmpty"), {_.isDefined}),
    "psoAmt"    -> bigDecimal
      .verifying(Messages("pla.psoDetails.errorMaximum"), psoAmt => isLessThanDouble(psoAmt.toDouble, Constants.npsMaxCurrency))
      .verifying(Messages("pla.psoDetails.errorNegative"), psoAmt => isPositive(psoAmt.toDouble))
      .verifying(Messages("pla.psoDetails.errorDecimalPlaces"), psoAmt => isMaxTwoDecimalPlaces(psoAmt.toDouble))
    )(PSODetailsModel.apply)(PSODetailsModel.unapply)
  )
}


