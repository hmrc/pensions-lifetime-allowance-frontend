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

package constructors

import models._
import play.api.libs.json.JsValue

class ResponseConstructors {

  def createApplyResponseModelFromJson(json: JsValue): Option[ApplyResponseModel] = {
    val psaReference = (json \ "psaCheckReference").asOpt[String]

    json
      .validate[ProtectionModel]
      .fold(
        errors => None,
        success => Some(ApplyResponseModel(success.copy(psaCheckReference = psaReference)))
      )
  }

  def createTransformedReadResponseModelFromJson(json: JsValue): Option[TransformedReadResponseModel] = {
    val responseModel = json.validate[ReadResponseModel]
    responseModel.fold(
      errors => None,
      valid = success => Some(TransformedReadResponseModel.from(success))
    )
  }

  def createAmendResponseModelFromJson(json: JsValue): Option[AmendResponseModel] =
    json
      .validate[ProtectionModel]
      .fold(
        errors => None,
        success => Some(AmendResponseModel(success))
      )

}
