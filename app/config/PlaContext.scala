/*
 * Copyright 2019 HM Revenue & Customs
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

package config

import javax.inject.Inject
import play.api.i18n.Messages
import uk.gov.hmrc.play.config.ServicesConfig

trait PlaContext {
  def getPageHelpPartial()(messages: Messages): String
  def assetsUrl: String
}

object PlaContextImpl extends PlaContext with ServicesConfig with PlaConfig {
  override def getPageHelpPartial()(messages: Messages): String = s"${baseUrl("contact-frontend")}/contact/problem_reports"

  override def assetsUrl: String = s"${getString("assets.url")}${getString("assets.version")}/"
}
