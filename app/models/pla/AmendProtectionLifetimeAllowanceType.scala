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

sealed abstract class AmendProtectionLifetimeAllowanceType(
    name: String,
    override val jsonValue: String,
    val urlValue: String
) extends EnumerableInstance(name)

object AmendProtectionLifetimeAllowanceType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendProtectionLifetimeAllowanceType("IndividualProtection2014", "INDIVIDUAL PROTECTION 2014", "ip2014")

  case object IndividualProtection2016
      extends AmendProtectionLifetimeAllowanceType("IndividualProtection2016", "INDIVIDUAL PROTECTION 2016", "ip2016")

  case object IndividualProtection2014LTA
      extends AmendProtectionLifetimeAllowanceType(
        "IndividualProtection2014LTA",
        "INDIVIDUAL PROTECTION 2014 LTA",
        "ip2014-lta"
      )

  case object IndividualProtection2016LTA
      extends AmendProtectionLifetimeAllowanceType(
        "IndividualProtection2016LTA",
        "INDIVIDUAL PROTECTION 2016 LTA",
        "ip2016-lta"
      )

  val values: Seq[AmendProtectionLifetimeAllowanceType] = Seq(
    IndividualProtection2014,
    IndividualProtection2016,
    IndividualProtection2014LTA,
    IndividualProtection2016LTA
  )

  implicit val toEnumerable: Enumerable[AmendProtectionLifetimeAllowanceType] =
    Enumerable(values.map(v => v.jsonValue -> v): _*)

  def from(str: String): AmendProtectionLifetimeAllowanceType = fromOption(str)
    .getOrElse(
      throw new IllegalArgumentException(s"Cannot create AmendProtectionLifetimeAllowanceType from String: $str")
    )

  private val valuesLowerCase =
    values.map(protectionType => protectionType.toString.toLowerCase -> protectionType).toMap

  def fromOption(str: String): Option[AmendProtectionLifetimeAllowanceType] =
    str.toLowerCase match {
      case "ip2014"     => Some(IndividualProtection2014)
      case "ip2016"     => Some(IndividualProtection2016)
      case "ip2014-lta" => Some(IndividualProtection2014LTA)
      case "ip2016-lta" => Some(IndividualProtection2016LTA)
      case str          => valuesLowerCase.get(str)
    }

}
