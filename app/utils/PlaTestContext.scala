/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import config.{FrontendAppConfig, PlaContext}
import javax.inject.Inject
import play.api.i18n.Messages

class PlaTestContext @Inject()(appConfig: FrontendAppConfig) extends PlaContext {

  override def getPageHelpPartial()(messages: Messages): String = s"${appConfig.servicesConfig.baseUrl("contact-frontend")}/contact/problem_reports"
  override def assetsUrl: String = s"${appConfig.servicesConfig.getString("assets.url")}${appConfig.servicesConfig.getString("assets.version")}/"

}
