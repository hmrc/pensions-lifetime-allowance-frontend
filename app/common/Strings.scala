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

package common

import enums.ApplicationType
import models.ProtectionModel
import models.pla.response.ProtectionStatus._
import models.pla.response.ProtectionType._

object Strings {

  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._
    def toIntOpt = catching(classOf[NumberFormatException]).opt(s.toInt)
  }

  def nameString(name: String)(implicit protectionType: ApplicationType.Value): String =
    protectionType match {
      case ApplicationType.FP2016 => "fp16" + name.capitalize
      case ApplicationType.IP2014 => "ip14" + name.capitalize
      case _                      => name
    }

  def cacheProtectionName(protection: ProtectionModel): String =
    statusString(protection.status) + protectionTypeString(protection.protectionType) + "Amendment"

  def cacheAmendFetchString(protectionType: String, status: String): String =
    status.toLowerCase + protectionType.toUpperCase + "Amendment"

  def protectionTypeString(modelProtectionType: Option[String]): String =
    modelProtectionType match {
      case Some("FP2016") | Some(FixedProtection2016.toString)      => "FP2016"
      case Some("IP2014") | Some(IndividualProtection2014.toString) => "IP2014"
      case Some("IP2016") | Some(IndividualProtection2016.toString) => "IP2016"
      case Some("Primary") | Some(PrimaryProtection.toString)       => "primary"
      case Some("Enhanced") | Some(EnhancedProtection.toString)     => "enhanced"
      case Some("Fixed") | Some(FixedProtection.toString)           => "fixed"
      case Some("FP2014") | Some(FixedProtection2014.toString)      => "FP2014"
      case _                                                        => "notRecorded"
    }

  def statusString(modelStatus: Option[String]): String =
    modelStatus match {
      case Some("Open") | Some(Open.toString)                 => "open"
      case Some("Dormant") | Some(Dormant.toString)           => "dormant"
      case Some("Withdrawn") | Some(Withdrawn.toString)       => "withdrawn"
      case Some("Expired") | Some(Expired.toString)           => "expired"
      case Some("Unsuccessful") | Some(Unsuccessful.toString) => "unsuccessful"
      case Some("Rejected") | Some(Rejected.toString)         => "rejected"
      case _                                                  => "notRecorded"
    }

}
