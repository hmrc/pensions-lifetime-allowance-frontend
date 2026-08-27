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

package models.pla.request

import models.pla.response.ProtectionStatus
import play.api.mvc.PathBindable
import util.{JsonEnum, JsonEnumFormat}

enum AmendProtectionRequestStatus(override val toString: String, override val jsonString: String) extends JsonEnum {

  case Open extends AmendProtectionRequestStatus("Open", "OPEN")

  case Dormant extends AmendProtectionRequestStatus("Dormant", "DORMANT")
}

object AmendProtectionRequestStatus extends JsonEnumFormat[AmendProtectionRequestStatus] {

  def tryFromProtectionStatus(protectionStatus: ProtectionStatus): Option[AmendProtectionRequestStatus] =
    protectionStatus match {
      case ProtectionStatus.Open    => Some(Open)
      case ProtectionStatus.Dormant => Some(Dormant)
      case _                        => None
    }

  given pathBindable: PathBindable[AmendProtectionRequestStatus] =
    new PathBindable[AmendProtectionRequestStatus] {

      override def bind(key: String, value: String): Either[String, AmendProtectionRequestStatus] =
        value.toLowerCase match {
          case UrlString.Open    => Right(Open)
          case UrlString.Dormant => Right(Dormant)
          case s                 => Left(s"Unknown protection status '$s'")
        }

      override def unbind(key: String, value: AmendProtectionRequestStatus): String = value match {
        case Open    => UrlString.Open
        case Dormant => UrlString.Dormant
      }

      object UrlString {
        val Open    = "open"
        val Dormant = "dormant"
      }
    }

}
