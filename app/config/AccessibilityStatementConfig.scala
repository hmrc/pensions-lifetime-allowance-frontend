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

package config

import play.api.Configuration
import play.api.mvc.RequestHeader

import java.net.URLEncoder
import javax.inject.Inject

class AccessibilityStatementConfig @Inject() (config: Configuration) {

  val platformHost: Option[String] =
    config.getOptional[String]("platform.frontend.host")

  val accessibilityStatementHost: Option[String] =
    platformHost.orElse(config.getOptional[String]("accessibility-statement.host"))

  val accessibilityStatementPath: Option[String] =
    config.getOptional[String]("accessibility-statement.path")

  val accessibilityStatementServicePath: Option[String] =
    config.getOptional[String]("accessibility-statement.service-path")

  def url(implicit request: RequestHeader): Option[String] =
    for {
      host        <- accessibilityStatementHost
      path        <- accessibilityStatementPath
      servicePath <- accessibilityStatementServicePath
    } yield s"$host$path$servicePath$query"

  private def query(implicit request: RequestHeader): String = {
    val referrerUrl = URLEncoder.encode(s"${platformHost.getOrElse("")}${request.path}", "UTF-8")
    s"?referrerUrl=$referrerUrl"
  }

}
