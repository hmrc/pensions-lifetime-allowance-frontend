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

package models

import models.pla.response.ProtectionStatus.Open
import models.pla.response.{AmendProtectionResponse, ReadProtectionsResponse}
import play.api.libs.json.{Json, OFormat}

case class TransformedReadResponseModel(
    activeProtection: Option[ProtectionModel],
    inactiveProtections: Seq[ProtectionModel]
)

object TransformedReadResponseModel {
  implicit val format: OFormat[TransformedReadResponseModel] = Json.format[TransformedReadResponseModel]

  def from(respModel: ReadResponseModel): TransformedReadResponseModel = {
    val activeProtectionOpt = respModel.lifetimeAllowanceProtections.find(_.status.contains("Open")).map {
      _.copy(psaCheckReference = Some(respModel.psaCheckReference))
    }
    val otherProtections = respModel.lifetimeAllowanceProtections.filterNot(_.status.contains("Open")).map {
      _.copy(psaCheckReference = Some(respModel.psaCheckReference))
    }
    TransformedReadResponseModel(activeProtectionOpt, otherProtections)
  }

  def from(respModel: ReadProtectionsResponse): TransformedReadResponseModel = {

    val protectionRecords = respModel.protectionRecordsList.map(_.protectionRecord)

    val activeProtectionOpt = protectionRecords
      .find(_.status == Open)
      .map { protectionRecord =>
        ProtectionModel(
          respModel.pensionSchemeAdministratorCheckReference,
          protectionRecord
        )
      }

    val inactiveProtections = protectionRecords
      .filterNot(_.status == Open)
      .map { protectionRecord =>
        ProtectionModel(
          respModel.pensionSchemeAdministratorCheckReference,
          protectionRecord
        )
      }

    TransformedReadResponseModel(activeProtectionOpt, inactiveProtections)
  }

}

case class ReadResponseModel(psaCheckReference: String, lifetimeAllowanceProtections: Seq[ProtectionModel])

object ReadResponseModel {
  implicit val format: OFormat[ReadResponseModel] = Json.format[ReadResponseModel]
}

case class AmendResponseModel(
    protection: ProtectionModel
)

object AmendResponseModel {
  implicit val format: OFormat[AmendResponseModel] = Json.format[AmendResponseModel]

  def from(amendProtectionResponse: AmendProtectionResponse): AmendResponseModel =
    AmendResponseModel(ProtectionModel(None, None))

}
