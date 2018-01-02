/*
 * Copyright 2018 HM Revenue & Customs
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

import common.Validation._
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{Lang, Messages}
import play.api.i18n.Messages.Implicits._
import utils.Constants
import uk.gov.hmrc.http.HeaderCarrier

object OverseasPensionsForm {

  implicit val hc = new HeaderCarrier()

  def validateForm(form: Form[OverseasPensionsModel])(implicit lang: Lang): Form[OverseasPensionsModel] = {
    if(!validationNeeded(form)) form else {
      if(!validateFieldCompleted(form)) form.withError("overseasPensionsAmt", "pla.base.errors.errorQuestion")
      else if(!validateMinimum(form)) form.withError("overseasPensionsAmt", "pla.base.errors.errorNegative")
      else if(!validateMaximum(form)) form.withError("overseasPensionsAmt", "pla.base.errors.errorMaximum")
      else if(!validateTwoDec(form)) form.withError("overseasPensionsAmt", "pla.base.errors.errorDecimalPlaces")
      else form
    }
  }

  private def validationNeeded(data: Form[OverseasPensionsModel]): Boolean = {
    data("overseasPensions").value.get match {
      case "yes" => true
      case "no" => false
    }
  }

  private def validateFieldCompleted(data: Form[OverseasPensionsModel]) = data("overseasPensionsAmt").value.isDefined

  private def validateMinimum(data: Form[OverseasPensionsModel]) = isPositive(data("overseasPensionsAmt").value.getOrElse("0").toDouble)

  private def validateMaximum(data: Form[OverseasPensionsModel]) = isLessThanDouble(data("overseasPensionsAmt").value.getOrElse("0").toDouble, Constants.npsMaxCurrency)

  private def validateTwoDec(data: Form[OverseasPensionsModel]) = isMaxTwoDecimalPlaces(data("overseasPensionsAmt").value.getOrElse("0").toDouble)

  def overseasPensionsForm(implicit lang: Lang) = Form (
    mapping(
      "overseasPensions" -> nonEmptyText,
      "overseasPensionsAmt" -> optional(bigDecimal)
    )(OverseasPensionsModel.apply)(OverseasPensionsModel.unapply)
  )
}
