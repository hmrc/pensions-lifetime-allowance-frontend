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

package models.pla.response

import utils.{Enumerable, EnumerableInstance}

sealed abstract class ProtectionStatus(value: String, _key: String) extends EnumerableInstance(value) {
  val key = _key
}

object ProtectionStatus extends Enumerable.Implicits {

  case object Open         extends ProtectionStatus("OPEN", "open")
  case object Dormant      extends ProtectionStatus("DORMANT", "dormant")
  case object Withdrawn    extends ProtectionStatus("WITHDRAWN", "withdrawn")
  case object Expired      extends ProtectionStatus("EXPIRED", "expired")
  case object Unsuccessful extends ProtectionStatus("UNSUCCESSFUL", "unsuccessful")
  case object Rejected     extends ProtectionStatus("REJECTED", "rejected")

  val values: Seq[ProtectionStatus] = Seq(
    Open,
    Dormant,
    Withdrawn,
    Expired,
    Unsuccessful,
    Rejected
  )

  implicit val enumerable: Enumerable[ProtectionStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
