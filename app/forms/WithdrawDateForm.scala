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

import java.time.{LocalDate, LocalDateTime}

import common.Dates
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

  def withdrawDateForm(implicit lang: Lang): Form[(Option[Int], Option[Int], Option[Int])] = Form(
    tuple(
      "withdrawDay" -> optional(number).verifying(withdrawDayConstraint),
      "withdrawMonth" -> optional(number).verifying(withdrawMonthConstraint),
      "withdrawYear" -> optional(number).verifying(withdrawYearConstraint)
    )
  )

  val withdrawDayConstraint: Constraint[Option[Int]] = Constraint {
    day =>
      val errors = day match {
        case None => Seq(ValidationError(Messages("pla.withdraw.date-input.form.day-empty")))
        case Some(a) if a <= 0 => Seq(ValidationError(Messages("pla.withdraw.date-input.form.day-too-low")))
        case Some(b) if b > 31 => Seq(ValidationError(Messages("pla.withdraw.date-input.form.day-too-high")))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  }

  val withdrawMonthConstraint: Constraint[Option[Int]] = Constraint {
    day =>
      val errors = day match {
        case None => Seq(ValidationError(Messages("pla.withdraw.date-input.form.month-empty")))
        case Some(a) if a <= 0 => Seq(ValidationError(Messages("pla.withdraw.date-input.form.month-too-low")))
        case Some(b) if b > 12 => Seq(ValidationError(Messages("pla.withdraw.date-input.form.month-too-high")))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  }

  val withdrawYearConstraint: Constraint[Option[Int]] = Constraint {
    day =>
      val errors = day match {
        case None => Seq(ValidationError(Messages("pla.withdraw.date-input.form.year-empty")))
        case Some(a) if a > LocalDate.now().getYear => Seq(ValidationError(Messages("pla.withdraw.date-input.form.year-too-high")))
        case _ => Nil
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  }

  def validateWithdrawDate(form: Form[(Option[Int], Option[Int], Option[Int])],
                           protectionStartDate: LocalDateTime)(implicit lang: Lang): Form[(Option[Int], Option[Int], Option[Int])] = {
    if (form.hasErrors) form else {
      val day = form.get._1.get
      val month = form.get._2.get
      val year = form.get._3.get
      if (!isValidDate(day, month, year)) form.withError("", Messages("pla.base.errors.invalidDate"))
      else if (LocalDate.of(year, month, day) isAfter LocalDate.now) form.withError("", Messages("pla.withdraw.date-input.form.date-in-future"))
      else if (LocalDate.of(year, month, day) isBefore protectionStartDate.toLocalDate)
        form.withError("", Messages("pla.withdraw.date-input.form.date-before-start-date"))
      else form
    }
  }

  def getWithdrawDate(form: Form[(Option[Int], Option[Int], Option[Int])]) : String = {
    Dates.apiDateFormat(form.get._1.get,form.get._2.get,form.get._3.get)
  }

}
