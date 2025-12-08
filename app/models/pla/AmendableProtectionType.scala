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
import play.api.mvc.PathBindable
import utils.{Enumerable, EnumerableInstance}

sealed abstract class AmendableProtectionType(
    name: String,
    override val jsonValue: String
) extends EnumerableInstance(name) {}

object AmendableProtectionType extends Enumerable.Implicits {

  case object IndividualProtection2014
      extends AmendableProtectionType(
        "IndividualProtection2014",
        "INDIVIDUAL PROTECTION 2014"
      )

  case object IndividualProtection2016
      extends AmendableProtectionType(
        "IndividualProtection2016",
        "INDIVIDUAL PROTECTION 2016"
      )

  case object IndividualProtection2014LTA
      extends AmendableProtectionType(
        "IndividualProtection2014LTA",
        "INDIVIDUAL PROTECTION 2014 LTA"
      )

  case object IndividualProtection2016LTA
      extends AmendableProtectionType(
        "IndividualProtection2016LTA",
        "INDIVIDUAL PROTECTION 2016 LTA"
      )

  val values: Seq[AmendableProtectionType] = Seq(
    IndividualProtection2014,
    IndividualProtection2016,
    IndividualProtection2014LTA,
    IndividualProtection2016LTA
  )

  implicit val toEnumerable: Enumerable[AmendableProtectionType] =
    Enumerable(values.map(v => v.jsonValue -> v): _*)

  def tryFromProtectionType(protectionType: ProtectionType): Option[AmendableProtectionType] =
    protectionType match {
      case ProtectionType.IndividualProtection2014    => Some(IndividualProtection2014)
      case ProtectionType.IndividualProtection2014LTA => Some(IndividualProtection2014LTA)
      case ProtectionType.IndividualProtection2016    => Some(IndividualProtection2016)
      case ProtectionType.IndividualProtection2016LTA => Some(IndividualProtection2016LTA)
      case _                                          => None
    }

  implicit val pathBindable: PathBindable[AmendableProtectionType] =
    new PathBindable[AmendableProtectionType] {

      override def bind(key: String, value: String): Either[String, AmendableProtectionType] =
        value.toLowerCase match {
          case "ip2014"                              => Right(IndividualProtection2014)
          case UrlString.IndividualProtection2014    => Right(IndividualProtection2014)
          case "ip2016"                              => Right(IndividualProtection2016)
          case UrlString.IndividualProtection2016    => Right(IndividualProtection2016)
          case UrlString.IndividualProtection2014LTA => Right(IndividualProtection2014LTA)
          case UrlString.IndividualProtection2016LTA => Right(IndividualProtection2016LTA)
          case p                                     => Left(s"Unknown protection type '$p'")
        }

      override def unbind(key: String, value: AmendableProtectionType): String = value match {
        case IndividualProtection2014    => UrlString.IndividualProtection2014
        case IndividualProtection2016    => UrlString.IndividualProtection2016
        case IndividualProtection2014LTA => UrlString.IndividualProtection2014LTA
        case IndividualProtection2016LTA => UrlString.IndividualProtection2016LTA
      }

      object UrlString {
        val IndividualProtection2014: String    = "individual-protection-2014"
        val IndividualProtection2016: String    = "individual-protection-2016"
        val IndividualProtection2014LTA: String = "individual-protection-2014-lta"
        val IndividualProtection2016LTA: String = "individual-protection-2016-lta"
      }
    }

}
