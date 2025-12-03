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

package models.display

import models.pla.response.{ProtectionStatus, ProtectionType}

case class PrintDisplayModel(
    firstName: String,
    surname: String,
    nino: String,
    protectionType: ProtectionType,
    status: ProtectionStatus,
    psaCheckReference: String,
    protectionReference: String,
    protectedAmount: Option[String],
    certificateDate: Option[String],
    certificateTime: Option[String],
    lumpSumPercentage: Option[String] = None,
    lumpSumAmount: Option[String] = None,
    enhancementFactor: Option[String] = None,
    factor: Option[String] = None
) {

  def isFixedProtection2016: Boolean = protectionType.isFixedProtection2016

}
