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

import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.ws.{WSDelete, WSGet, WSPost, WSPut}

object FrontendAuditConnector extends AppName with AuditConnector {
  protected def appNameConfiguration: Configuration = Play.current.configuration
  protected def mode: Mode = Play.current.mode

  override lazy val auditingConfig = LoadAuditingConfig(appNameConfiguration, mode, "auditing")
}

trait PlaConfig {
  protected def appNameConfiguration: Configuration = Play.current.configuration
  protected def runModeConfiguration: Configuration = Play.current.configuration
  protected def mode: Mode = Play.current.mode
  def auditConnector: AuditConnector = FrontendAuditConnector
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with
  HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName with PlaConfig {
  override val configuration = Some(appNameConfiguration.underlying)
}

object WSHttp extends WSHttp {
}

object PLASessionCache extends SessionCache with ServicesConfig with AppName with PlaConfig {
  override lazy val domain: String = getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  override lazy val baseUri: String = baseUrl("cachable.session-cache")
  override lazy val defaultSource: String = appName
  override lazy val http: WSHttp.type =  WSHttp
}

object AuthClientConnector extends PlayAuthConnector with ServicesConfig with PlaConfig {
  override val serviceUrl: String = baseUrl("auth")

  override def http: CorePost = WSHttp
}
