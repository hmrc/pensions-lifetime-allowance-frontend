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

package models.pla

import utils.{Enumerable, EnumerableInstanceWithKey}

sealed abstract class AmendProtectionRequestStatus(readsWrites: String, toString: String)
    extends EnumerableInstanceWithKey(readsWrites, toString)

object AmendProtectionRequestStatus extends Enumerable.Implicits {

  case object Open    extends AmendProtectionRequestStatus("OPEN", "open")
  case object Dormant extends AmendProtectionRequestStatus("DORMANT", "dormant")

  val allValues: Seq[AmendProtectionRequestStatus] = Seq(Open, Dormant)

  implicit val toEnumerable: Enumerable[AmendProtectionRequestStatus] =
    Enumerable(allValues.map(v => v.readsWrites -> v): _*)

  def from(str: String): AmendProtectionRequestStatus =
    allValues
      .find(_.toString.equalsIgnoreCase(str.toLowerCase))
      .getOrElse(throw new IllegalArgumentException(s"Cannot create AmendProtectionRequestStatus from String: $str"))

}
