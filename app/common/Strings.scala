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
    statusString(protection.status) + protectionTypeString(protection.protectionType) + "Amendment"

  def cacheAmendFetchString(protectionType: String, status: String): String =
    status.toLowerCase + protectionType.toUpperCase + "Amendment"

  private val protectionTypeStrings = ProtectionType.values.map(_.toString)

  def protectionTypeString(modelProtectionType: Option[String]): String =
    modelProtectionType.filter(protectionTypeStrings.contains(_)).getOrElse("notRecorded")

  private val statusStrings = ProtectionStatus.values.map(_.toString)

  def statusString(modelStatus: Option[String]): String =
    modelStatus.filter(statusStrings.contains(_)).getOrElse("notRecorded")

}
