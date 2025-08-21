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

package views

import config.FrontendAppConfig
import play.api.i18n.Messages

object HeaderContent {

  def getProtectedLifetimeAllowance(frontendAppConfig: FrontendAppConfig)(implicit messages: Messages): String =
    if (frontendAppConfig.hipMigrationEnabled) {
      messages("common.header.protectedAmount")
    } else {
      messages("common.header.protectedLifetimeAllowance")
    }

  def getProtectionNotificatioNumber(frontendAppConfig: FrontendAppConfig)(implicit messages: Messages): String =
    if (frontendAppConfig.hipMigrationEnabled) {
      messages("common.header.protectionReferenceNumber")
    } else {
      messages("common.header.protectionNotificationNumber")
    }

  def getSchemeAdministratorReference(frontendAppConfig: FrontendAppConfig)(implicit messages: Messages): String =
    if (frontendAppConfig.hipMigrationEnabled) {
      messages("common.header.pensionSchemeAdministratorCheckReference")
    } else {
      messages("common.header.schemeAdministratorReference")
    }

  def getProtectionReferenceNumber(frontendAppConfig: FrontendAppConfig)(implicit messages: Messages): String =
    if (frontendAppConfig.hipMigrationEnabled) {
      messages("common.header.protectionRef")
    } else {
      messages("pla.resultSuccess.protectionRef")
    }

  def getPensionSchemeAdministratorCheckReference(frontendAppConfig: FrontendAppConfig)(implicit messages: Messages): String =
    if (frontendAppConfig.hipMigrationEnabled) {
      messages("common.header.psaRef")
    } else {
      messages("pla.resultSuccess.psaRef")
    }

}
