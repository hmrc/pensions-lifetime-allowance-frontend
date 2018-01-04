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

import java.time.{LocalDate, LocalDateTime}

import common.Dates
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._

object WithdrawDateForm extends CommonBinders{

  def withdrawDateForm: Form[(Option[Int], Option[Int], Option[Int])] = Form(
    tuple(
      "withdrawDay" -> withdrawDateFormatter,
      "withdrawMonth" -> withdrawDatePartialFormatter("month"),
      "withdrawYear" -> withdrawDatePartialFormatter("year")
    )
  )

  def validateWithdrawDate(form: Form[(Option[Int], Option[Int], Option[Int])],
                           protectionStartDate: LocalDateTime): Form[(Option[Int], Option[Int], Option[Int])] = {
    if (form.hasErrors) form else {
      val day = form.get._1.get
      val month = form.get._2.get
      val year = form.get._3.get
      if (LocalDate.of(year, month, day) isBefore protectionStartDate.toLocalDate)
        form.withError("", "pla.withdraw.date-input.form.date-before-start-date")
      else form
    }
  }

  def getWithdrawDate(form: Form[(Option[Int], Option[Int], Option[Int])]) : String = {
    Dates.apiDateFormat(form.get._1.get,form.get._2.get,form.get._3.get)
  }

}
