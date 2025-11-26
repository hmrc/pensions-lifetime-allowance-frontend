/*
 * Copyright 2025 HM Revenue & Customs
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

package testHelpers

import models.{
  CurrentPensionsModel,
  OverseasPensionsModel,
  PensionDebitsModel,
  PensionsTakenBeforeModel,
  PensionsTakenBetweenModel,
  PensionsTakenModel,
  PensionsUsedBetweenModel,
  PensionsWorthBeforeModel,
  PsoDetailsModel
}
import play.api.libs.json.{Format, Json, Writes}

object Formats {

  implicit val pensionsTakenModelFormat: Format[PensionsTakenModel] = Json.format[PensionsTakenModel]

  implicit val currentPensionsWrites: Writes[CurrentPensionsModel]           = Json.writes[CurrentPensionsModel]
  implicit val overseasPensionsWrites: Writes[OverseasPensionsModel]         = Json.writes[OverseasPensionsModel]
  implicit val pensionsDebitsWrites: Writes[PensionDebitsModel]              = Json.writes[PensionDebitsModel]
  implicit val pensionsTakenBeforeWrites: Writes[PensionsTakenBeforeModel]   = Json.writes[PensionsTakenBeforeModel]
  implicit val pensionsTakenBetweenWrites: Writes[PensionsTakenBetweenModel] = Json.writes[PensionsTakenBetweenModel]
  implicit val pensionsUsedBetweenWrites: Writes[PensionsUsedBetweenModel]   = Json.writes[PensionsUsedBetweenModel]
  implicit val pensionsWorthBeforeWrites: Writes[PensionsWorthBeforeModel]   = Json.writes[PensionsWorthBeforeModel]
  implicit val psoDetailsWrites: Writes[PsoDetailsModel]                     = Json.writes[PsoDetailsModel]

}
