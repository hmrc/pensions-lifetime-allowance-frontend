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

package forms

import java.time.{LocalDate, LocalDateTime}

import models.WithdrawDateFormModel
import play.api.data.Form
import play.api.data.Forms._

object WithdrawDateForm extends CommonBinders {

  def withdrawDateForm: Form[WithdrawDateFormModel] = Form(
    mapping(
      "withdrawDate.day" -> withdrawDateFormatter,
      "withdrawDate.month" -> withdrawDatePartialFormatter("month"),
      "withdrawDate.year" -> withdrawDatePartialFormatter("year")
    )(WithdrawDateFormModel.apply)(WithdrawDateFormModel.unapply)
  )

  def validateWithdrawDate(form: Form[WithdrawDateFormModel],
                           protectionStartDate: LocalDateTime): Form[WithdrawDateFormModel] = {
    if (form.hasErrors) form
    else {
      val day = form.get.withdrawDay.get
      val month = form.get.withdrawMonth.get
      val year = form.get.withdrawYear.get
      if (LocalDate.of(year, month, day) isBefore protectionStartDate.toLocalDate)
        form.withError("withdrawDate.day", "pla.withdraw.date-input.form.date-before-start-date")
      else form
    }
  }
}
