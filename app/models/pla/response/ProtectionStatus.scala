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

import utils.{Enumerable, WithName}

sealed trait ProtectionStatus

object ProtectionStatus extends Enumerable.Implicits {

  override val jsonErrorMessage: String = "error.protectionStatus"

  case object Open         extends WithName("OPEN") with ProtectionStatus
  case object Dormant      extends WithName("DORMANT") with ProtectionStatus
  case object Withdrawn    extends WithName("WITHDRAWN") with ProtectionStatus
  case object Expired      extends WithName("EXPIRED") with ProtectionStatus
  case object Unsuccessful extends WithName("UNSUCCESSFUL") with ProtectionStatus
  case object Rejected     extends WithName("REJECTED") with ProtectionStatus

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
