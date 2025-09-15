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

package utils

import play.api.libs.json._

trait Enumerable[A] {

  def findByName(str: String): Option[A]
}

object Enumerable {

  def apply[A](entries: (String, A)*): Enumerable[A] =
    (str: String) => entries.toMap.get(str)

  trait Implicits { self =>

    implicit def reads[A](implicit ev: Enumerable[A]): Reads[A] =
      Reads {
        case JsString(str) =>
          ev.findByName(str)
            .map(s => JsSuccess(s))
            .getOrElse(JsError(s"Received unknown ${self.getClass.getSimpleName}: $str"))
        case other =>
          JsError(s"Cannot create ${self.getClass.getSimpleName} instance from: ${other.toString}")
      }

    implicit def writes[A <: EnumerableInstance]: Writes[A] =
      Writes(value => JsString(value.readsWrites))

  }

}

abstract class EnumerableInstance(name: String) {
  val readsWrites: String       = name
  override val toString: String = name
}

abstract class EnumerableInstanceWithKey(readsWrites: String, _toString: String)
    extends EnumerableInstance(readsWrites) {
  override val toString: String = _toString
}
