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

import forms.formatters.DateFormatter
import forms.mappings.CurrencyMappings
import models.amend.AmendPsoDetailsModel
import models.pla.AmendableProtectionType
import models.pla.AmendableProtectionType._
import play.api.data.Forms.{mapping, of}
import play.api.data.Form
import play.api.i18n.Messages
import utils.Constants

import java.time.LocalDate

object AmendPsoDetailsForm extends CurrencyMappings {

  val key    = "pso"
  val amount = "psoAmt"

  def amendPsoDetailsForm(
      protectionType: AmendableProtectionType
  )(implicit messages: Messages): Form[AmendPsoDetailsModel] = Form(
    mapping(
      key -> of(
        DateFormatter(
          key,
          optMinDate = Some(
            protectionType match {
              case IndividualProtection2014 | IndividualProtection2014LTA => Constants.minIP14PsoDate
              case IndividualProtection2016 | IndividualProtection2016LTA => Constants.minIP16PsoDate
            }
          ),
          optMaxDate = Some(LocalDate.now.plusDays(1))
        )
      ),
      amount -> currencyMappingFromPrefix("pla.psoDetails.amount.errors")
    )((date, amount) => AmendPsoDetailsModel(date, amount))(model => Some((model.startDate, model.enteredAmount)))
  )

}
