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

import models.pla.response.ProtectionType
import utils.{Enumerable, EnumerableInstance}

sealed abstract class AmendedProtectionType(name: String, val toProtectionType: ProtectionType)
    extends EnumerableInstance(name) {

  def isFixedProtection2016: Boolean =
    this match {
      case AmendedProtectionType.FixedProtection2016    => true
      case AmendedProtectionType.FixedProtection2016LTA => true
      case _                                            => false
    }

}

object AmendedProtectionType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendedProtectionType("IndividualProtection2014", ProtectionType.IndividualProtection2014)

  case object IndividualProtection2014LTA
      extends AmendedProtectionType("IndividualProtection2014LTA", ProtectionType.IndividualProtection2014LTA)

  case object IndividualProtection2016
      extends AmendedProtectionType("IndividualProtection2016", ProtectionType.IndividualProtection2016)

  case object IndividualProtection2016LTA
      extends AmendedProtectionType("IndividualProtection2016LTA", ProtectionType.IndividualProtection2016LTA)

  case object FixedProtection2016
      extends AmendedProtectionType("FixedProtection2016", ProtectionType.FixedProtection2016)

  case object FixedProtection2016LTA
      extends AmendedProtectionType("FixedProtection2016LTA", ProtectionType.FixedProtection2016LTA)

  val values = Seq(
    IndividualProtection2014,
    IndividualProtection2014LTA,
    IndividualProtection2016,
    IndividualProtection2016LTA,
    FixedProtection2016,
    FixedProtection2016LTA
  )

  implicit val toEnumerable: Enumerable[AmendedProtectionType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def from(protectionType: AmendableProtectionType): AmendedProtectionType =
    protectionType match {
      case AmendableProtectionType.IndividualProtection2014    => IndividualProtection2014
      case AmendableProtectionType.IndividualProtection2014LTA => IndividualProtection2014LTA
      case AmendableProtectionType.IndividualProtection2016    => IndividualProtection2016
      case AmendableProtectionType.IndividualProtection2016LTA => IndividualProtection2016LTA
    }

  def tryFrom(protectionType: ProtectionType): Option[AmendedProtectionType] =
    protectionType match {
      case ProtectionType.IndividualProtection2014    => Some(IndividualProtection2014)
      case ProtectionType.IndividualProtection2014LTA => Some(IndividualProtection2014LTA)
      case ProtectionType.IndividualProtection2016    => Some(IndividualProtection2016)
      case ProtectionType.IndividualProtection2016LTA => Some(IndividualProtection2016LTA)
      case ProtectionType.FixedProtection2016         => Some(FixedProtection2016LTA)
      case ProtectionType.FixedProtection2016LTA      => Some(FixedProtection2016LTA)
      case _                                          => None
    }

}
