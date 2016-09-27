/*
 * Copyright 2016 HM Revenue & Customs
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
import models.{AmendProtectionModel, ProtectionModel}

object Strings {

  implicit class StringImprovements(val s: String) {
    import scala.util.control.Exception._
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
  }

  def nameString(name: String)(implicit protectionType: ApplicationType.Value): String = {
    protectionType match {
      case ApplicationType.FP2016 => "fp16"+name.capitalize
      case ApplicationType.IP2014 => "ip14"+name.capitalize
    case _ => name
    }
  }

  def keyStoreProtectionName(protection: ProtectionModel): String = {
    statusString(protection.status)+protectionTypeString(protection.protectionType)+"Amendment"
  }

  def keyStoreProtectionName(amendment: AmendProtectionModel): String = {
    keyStoreProtectionName(amendment.originalProtection)
  }

  def keyStoreAmendFetchString(protectionType: String, status: String): String = {
    status.toLowerCase + protectionType.toUpperCase + "Amendment"
  }

  def protectionTypeString(modelProtectionType: Option[String]) = {
    modelProtectionType match {
      case Some("FP2016") => "FP2016"
      case Some("IP2014") => "IP2014"
      case Some("IP2016") => "IP2016"
      case Some("Primary") => "primary"
      case Some("Enhanced") => "enhanced"
      case Some("Fixed") => "fixed"
      case Some("FP2014") => "FP2014"
      case _ => "notRecorded"
    }
  }

  def statusString(modelStatus: Option[String]): String = {
    modelStatus match {
      case Some("Open") => "open"
      case Some("Dormant") => "dormant"
      case Some("Withdrawn") => "withdrawn"
      case Some("Expired") => "expired"
      case Some("Unsuccessful") => "unsuccessful"
      case Some("Rejected") => "rejected"
      case _ => "notRecorded"
    }
  }
}
