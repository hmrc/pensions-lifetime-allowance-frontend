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

import models.pla.response.{ProtectionStatus, ProtectionType}
import models.pla.{AmendProtectionLifetimeAllowanceType, AmendProtectionRequestStatus}

object Strings {

  def protectionCacheKey(protectionType: String, status: String): String =
    protectionCacheKey(Some(protectionType), Some(status))

  def protectionCacheKey(protectionType: Option[String], status: Option[String]): String =
    statusString(status) + protectionTypeString(protectionType) + "Amendment"

  def protectionTypeString(modelProtectionType: Option[String]): String =
    modelProtectionType.flatMap(ProtectionType.tryFrom).map(_.toString).getOrElse("notRecorded")

  def protectionTypeUrlString(modelProtectionType: Option[String]): String = {
    import AmendProtectionLifetimeAllowanceType._

    modelProtectionType
      .flatMap(AmendProtectionLifetimeAllowanceType.tryFrom) match {
      case Some(IndividualProtection2014)    => ProtectionTypeUrl.IndividualProtection2014
      case Some(IndividualProtection2016)    => ProtectionTypeUrl.IndividualProtection2016
      case Some(IndividualProtection2014LTA) => ProtectionTypeUrl.IndividualProtection2014LTA
      case Some(IndividualProtection2016LTA) => ProtectionTypeUrl.IndividualProtection2016LTA
      case _                                 => "notRecorded"
    }
  }

  object ProtectionTypeUrl {
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
      case Some(Open)    => StatusUrl.Open
      case Some(Dormant) => StatusUrl.Dormant
      case _             => "notRecorded"
    }
  }

  object StatusUrl {
    val Open: String    = "open"
    val Dormant: String = "dormant"
  }

}
