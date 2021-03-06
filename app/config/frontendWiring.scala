/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient


class PLASessionCache @Inject()(appConfig: FrontendAppConfig, WSHttp: DefaultHttpClient ) extends SessionCache {
  override lazy val domain: String = appConfig.servicesConfig.getConfString("cachable.session-cache.domain",
    throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
  override lazy val baseUri: String = appConfig.servicesConfig.baseUrl("cachable.session-cache")
  override lazy val defaultSource: String = "pensions-lifetime-allowance-frontend"
  override lazy val http =  WSHttp
}
