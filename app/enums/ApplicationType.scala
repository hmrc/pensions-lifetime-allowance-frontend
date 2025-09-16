/*
 * Copyright 2023 HM Revenue & Customs
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

package enums

object ApplicationType extends Enumeration {

  type ApplicationType = Value
  val IP2014              = Value
  val IP2016              = Value
  val FP2016              = Value
  val existingProtections = Value

  def fromString(applicationType: String): Option[Value] =
    applicationType.toLowerCase match {
      case "ip2014"                   => Some(IP2014)
      case "individualprotection2014" => Some(IP2014)
      case "ip2016"                   => Some(IP2016)
      case "individualprotection2016" => Some(IP2016)
      case "fp2016"                   => Some(FP2016)
      case "fixedprotection2016"      => Some(FP2016)
      case "existingprotections"      => Some(existingProtections)
      case _                          => None
    }

}
