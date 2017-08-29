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

import common.Dates.futureDate
import common.Validation.isValidDate
import forms.AmendPSODetailsForm.validateMinDate
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{Lang, Messages}

object WithdrawDateForm {

  def withdrawDate(implicit lang: Lang): Form[(Int, Int, Int)] = Form(
    tuple(
      "withdrawDay" -> number.verifying(withdrawDayConstraint),
      "withdrawMonth" -> number,
      "withdrawYear" -> number
    )
  )

  val withdrawDayConstraint: Constraint[Int] = Constraint("")({
    day =>
      val errors = day match {
        case a if a < 0 => Seq(ValidationError(Messages("date too low")))
        case b if b > 31 => Seq(ValidationError(Messages("date too high")))
        case _ => Seq()
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

//  def validateForm(form: Form[(Int, Int, Int)])(implicit lang: Lang): Form[(Int, Int, Int)] = {
//    val day = form.get._1
//    val month = form.get._2
//    val year = form.get._3
//    if (!isValidDate(day, month, year)) form.withError("psoDay", Messages("pla.base.errors.invalidDate"))
//    else if (futureDate(day, month, year)) form.withError("withdrawDay", Messages(""))
//    else validateMinDate(form, day, month, year)
//  }
}
