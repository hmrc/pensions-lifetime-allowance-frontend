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

import common.Validation.{mandatoryCheck, yesNoCheck}
import models._
import play.api.data.Forms._
import play.api.data._

object PensionsTakenBetweenForm extends CommonBinders {

  def pensionsTakenBetweenForm(protectionType: String): Form[PensionsTakenBetweenModel] = Form(
    mapping(
      "pensionsTakenBetween" -> common.Validation
        .newText(s"pla.pensionsTakenBetween.errors.mandatoryError.$protectionType")
        .verifying(s"pla.pensionsTakenBetween.errors.mandatoryError.$protectionType", mandatoryCheck)
        .verifying(s"pla.pensionsTakenBetween.errors.mandatoryError.$protectionType", yesNoCheck)
    )(PensionsTakenBetweenModel.apply)(PensionsTakenBetweenModel.unapply)
  )

}
