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

sealed abstract class ProtectionStatus(name: String, override val jsonValue: String) extends EnumerableInstance(name)

object ProtectionStatus extends Enumerable.Implicits {

  case object Open         extends ProtectionStatus("Open", "OPEN")
  case object Dormant      extends ProtectionStatus("Dormant", "DORMANT")
  case object Withdrawn    extends ProtectionStatus("Withdrawn", "WITHDRAWN")
  case object Expired      extends ProtectionStatus("Expired", "EXPIRED")
  case object Unsuccessful extends ProtectionStatus("Unsuccessful", "UNSUCCESSFUL")
  case object Rejected     extends ProtectionStatus("Rejected", "REJECTED")

  val values: Seq[ProtectionStatus] = Seq(
    Open,
    Dormant,
    Withdrawn,
    Expired,
    Unsuccessful,
    Rejected
  )

  implicit val enumerable: Enumerable[ProtectionStatus] =
    Enumerable(values.map(v => v.jsonValue -> v): _*)

  private val valuesLowerCase = values.map(status => status.toString.toLowerCase -> status).toMap

  def from(str: String): Option[ProtectionStatus] = valuesLowerCase.get(str.toLowerCase)

}
