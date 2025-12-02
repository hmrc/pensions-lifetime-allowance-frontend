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

package services

import models.cache.CacheMap
import play.api.Logging
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.Request
import repositories.SessionRepository
import uk.gov.hmrc.mongo.cache.DataKey

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionCacheService @Inject() (sessionRepository: SessionRepository)(
    implicit executionContext: ExecutionContext
) extends Logging {

  def saveFormData[T](key: String, data: T)(implicit request: Request[_], formats: Writes[T]): Future[CacheMap] = {
    logger.info(s"Saving $key as ${Json.toJson(data)}")
    sessionRepository.putInSession(DataKey(key), data)
  }

  def fetchAndGetFormData[T](key: String)(implicit request: Request[_], formats: Reads[T]): Future[Option[T]] = {
    logger.info(s"Fetching $key")
    sessionRepository.getFromSession[T](DataKey(key))
  }

  def remove(implicit request: Request[_]): Future[Unit] =
    sessionRepository.clearSession

}
