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

package util

import play.api.Logging
import play.api.libs.json.*

trait JsonEnumFormat[E <: JsonEnum] extends Logging {

  protected def values: Array[E]

  private lazy val lookupMap = values.map(value => value.jsonString -> value).toMap

  def withName(string: String): Option[E] = lookupMap.get(string)

  given Reads[E] = {
    case JsString(str) =>
      withName(str) match {
        case Some(value) => JsSuccess(value)
        case None =>
          JsError(s"Received unknown ${getClass.getSimpleName}: $str")
      }
    case other =>
      JsError(s"Cannot create ${getClass.getSimpleName} instance from: ${other.toString}")
  }

  given Writes[E] = value => JsString(value.jsonString)

  given JsonEnumFormat[E] = this

}
