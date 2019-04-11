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

package constructors

import play.api.libs.json.JsValue
import models._
import enums.ApplicationType

class ResponseConstructors {

    def createApplyResponseModelFromJson(json: JsValue)(implicit protectionType: ApplicationType.Value): Option[ApplyResponseModel] = {
        val psaReference = (json \ "psaCheckReference").asOpt[String]

        json.validate[ProtectionModel].fold(
            errors => None,
            success => Some(ApplyResponseModel(success.copy(psaCheckReference = psaReference)))
        )
    }

    def createTransformedReadResponseModelFromJson(json: JsValue): Option[TransformedReadResponseModel] = {
        val responseModel = json.validate[ReadResponseModel]
        responseModel.fold (
            errors => None,
            valid = success => Some(transformReadResponseModel(success))
        )
    }

    def transformReadResponseModel(respModel: ReadResponseModel): TransformedReadResponseModel = {
        val activeProtectionOpt = respModel.lifetimeAllowanceProtections.find(_.status.contains("Open")).map{_.copy(psaCheckReference = Some(respModel.psaCheckReference))}
        val otherProtections = respModel.lifetimeAllowanceProtections.filterNot(_.status.contains("Open")).map{_.copy(psaCheckReference = Some(respModel.psaCheckReference))}
        TransformedReadResponseModel(activeProtectionOpt, otherProtections)
    }

    def createAmendResponseModelFromJson(json: JsValue): Option[AmendResponseModel] = {
        json.validate[ProtectionModel].fold(
        errors => None,
        success => Some(AmendResponseModel(success))
        )
    }
}
