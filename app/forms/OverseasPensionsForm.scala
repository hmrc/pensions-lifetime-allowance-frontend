/*
 * Copyright 2019 HM Revenue & Customs
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

object OverseasPensionsForm extends CommonBinders{

  def overseasPensionsForm(implicit lang: Lang) = Form (
    mapping(
      "overseasPensions" -> nonEmptyText,
      "overseasPensionsAmt" -> yesNoOptionalBigDecimal
    )(OverseasPensionsModel.apply)(OverseasPensionsModel.unapply)
  )
}
