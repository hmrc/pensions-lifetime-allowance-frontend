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

import models.PSALookupRequest
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object PSALookupRequestForm {

  def pSALookupRequestForm: Form[PSALookupRequest] = {
    Form(mapping(
      "pensionSchemeAdministratorCheckReference" -> text.verifying(psaRefConstraint),
      "lifetimeAllowanceReference" -> text.verifying(ltaRefConstraint)
    )(PSALookupRequest.apply)(PSALookupRequest.unapply))
  }

  private val psaRefRegex = """^PSA[0-9]{8}[A-Z]$""".r
  private val npsRefRegex = """^(IP14|IP16|FP16)[0-9]{10}[ABCDEFGHJKLMNPRSTXYZ]$""".r
  private val tpssRefRegex = """^[1-9A][0-9]{6}[ABCDEFHXJKLMNYPQRSTZW]$""".r

  val psaRefConstraint: Constraint[String] = Constraint("constraints.psarefcheck")({
    psaRef =>
      val errors = psaRef match {
        case "" => Seq(ValidationError(Messages("psa.lookup.form.psaref.required")))
        case psaRefRegex() => Nil
        case _ => Seq(ValidationError(Messages("psa.lookup.form.psaref.invalid")))
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

  val ltaRefConstraint: Constraint[String] = Constraint("constraints.ltarefcheck")({
    ltaRef =>
      val errors = ltaRef match {
        case "" => Seq(ValidationError(Messages("psa.lookup.form.ltaref.required")))
        case npsRefRegex(_) => Nil
        case tpssRefRegex() => Nil
        case _ => Seq(ValidationError(Messages("psa.lookup.form.ltaref.invalid")))
      }
      if (errors.isEmpty) Valid else Invalid(errors)
  })

}
