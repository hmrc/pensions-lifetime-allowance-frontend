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

package models

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import scala.util.Try

case class TimeModel(time: LocalTime)

object TimeModel {

  private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")

  private def parse(timeString: String): Option[TimeModel] =
    Try(LocalTime.parse(timeString, timeFormat)).toOption.map(TimeModel(_))

  private def serialise(time: TimeModel): String = time.time.format(timeFormat)

  implicit val reads: Reads[TimeModel] = {
    case JsString(timeString) =>
      parse(timeString) match {
        case Some(time) => JsSuccess(time)
        case None       => JsError("invalid certificateTime")
      }
    case _ => JsError("certificateTime must be a string")
  }

  implicit val writes: Writes[TimeModel] = time => JsString(serialise(time))

  implicit val format: Format[TimeModel] = Format(reads, writes)

}
