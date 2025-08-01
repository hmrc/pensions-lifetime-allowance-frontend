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

import utils.{Enumerable, EnumerableInstance}

sealed abstract class AmendProtectionRequestStatus(value: String) extends EnumerableInstance(value)

object AmendProtectionRequestStatus extends Enumerable.Implicits {

  case object Open    extends AmendProtectionRequestStatus("OPEN")
  case object Dormant extends AmendProtectionRequestStatus("DORMANT")

  val allValues: Seq[AmendProtectionRequestStatus] = Seq(Open, Dormant)

  implicit val toEnumerable: Enumerable[AmendProtectionRequestStatus] =
    Enumerable(allValues.map(v => v.toString -> v): _*)

  def from(str: String): AmendProtectionRequestStatus =
    allValues
      .find(_.toString == str.toUpperCase)
      .getOrElse(throw new IllegalArgumentException(s"Cannot create AmendProtectionRequestStatus from String: $str"))

}
