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
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}
import models.pla.response.{ProtectionStatus, ProtectionType}

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
    cacheAmendmentKey(protection.protectionType, protection.status)

  def cacheAmendFetchString(protectionType: String, status: String): String =
    cacheAmendmentKey(Some(protectionType), Some(status))

  private def cacheAmendmentKey(protectionType: Option[String], status: Option[String]): String =
    statusString(status) + protectionTypeString(protectionType) + "Amendment"

  def protectionTypeString(modelProtectionType: Option[String]): String =
    modelProtectionType.flatMap(ProtectionType.tryFrom).map(_.toString).getOrElse("notRecorded")

  def protectionTypeUrlString(modelProtectionType: Option[String]): String = {
    import AmendProtectionLifetimeAllowanceType._

    modelProtectionType
      .flatMap(AmendProtectionLifetimeAllowanceType.tryFrom) match {
      case Some(IndividualProtection2014)    => ProtectionTypeURL.IndividualProtection2014
      case Some(IndividualProtection2016)    => ProtectionTypeURL.IndividualProtection2016
      case Some(IndividualProtection2014LTA) => ProtectionTypeURL.IndividualProtection2014LTA
      case Some(IndividualProtection2016LTA) => ProtectionTypeURL.IndividualProtection2016LTA
      case _                                 => "notRecorded"
    }
  }

  object ProtectionTypeURL {
    val IndividualProtection2014: String    = "individual-protection-2014"
    val IndividualProtection2016: String    = "individual-protection-2016"
    val IndividualProtection2014LTA: String = "individual-protection-2014-lta"
    val IndividualProtection2016LTA: String = "individual-protection-2016-lta"
  }

  def statusString(modelStatus: Option[String]): String =
    modelStatus.flatMap(ProtectionStatus.tryFrom).map(_.toString).getOrElse("notRecorded")

  def statusUrlString(modelStatus: Option[String]): String = {
    import AmendProtectionRequestStatus._

    modelStatus.flatMap(AmendProtectionRequestStatus.tryFrom) match {
      case Some(Open)    => StatusURL.Open
      case Some(Dormant) => StatusURL.Dormant
      case _             => "notRecorded"
    }
  }

  object StatusURL {
    val Open: String    = "open"
    val Dormant: String = "dormant"
  }

}
