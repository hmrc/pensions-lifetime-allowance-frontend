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

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.{Messages, MessagesProvider}

object PSALookupSchemeAdministratorReferenceForm {

  def psaRefForm(implicit messagesProvider: MessagesProvider): Form[String] =
    Form(
      single(
        "pensionSchemeAdministratorCheckReference" -> text.verifying(psaRefConstraint)
      )
    )

  private val psaRefRegex = """^(?i)PSA[0-9]{8}[A-Z]$""".r

  def psaRefConstraint(implicit messagesProvider: MessagesProvider): Constraint[String] =
    Constraint("constraints.psarefcheck") { psaRef =>
      val errors = psaRef match {
        case ""            => Seq(ValidationError(Messages("psa.lookup.form.psaref.required")))
        case psaRefRegex() => Nil
        case _             => Seq(ValidationError(Messages("psa.lookup.form.psaref.invalid")))
      }
      if (errors.isEmpty) Valid else Invalid(errors)
    }

}
