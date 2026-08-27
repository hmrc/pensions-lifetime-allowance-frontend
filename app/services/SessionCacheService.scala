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

import models.amend.{AmendProtectionModel, AmendsGAModel}
import models.cache.CacheMap
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{AmendResponseModel, ProtectionModel}
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.RequestHeader
import repositories.SessionRepository
import uk.gov.hmrc.mongo.cache.DataKey

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SessionCacheService @Inject() (sessionRepository: SessionRepository) {

  private val openProtectionKey: String = "openProtection"

  private def amendProtectionModelKey(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): String =
    status.toString + protectionType.toString + "Amendment"

  private val amendsGAModelKey: String = "AmendsGA"

  private val amendResponseModelKey: String = "amendResponseModel"
  private val previousTechnicalIssuesKey    = "previous-technical-issues"

  def saveOpenProtection(openProtection: ProtectionModel)(using RequestHeader): Future[CacheMap] =
    saveFormData[ProtectionModel](openProtectionKey, openProtection)

  def saveAmendProtectionModel(
      amendProtectionModel: AmendProtectionModel
  )(using RequestHeader): Future[CacheMap] =
    saveFormData[AmendProtectionModel](
      amendProtectionModelKey(amendProtectionModel.protectionType, amendProtectionModel.status),
      amendProtectionModel
    )

  def saveAmendsGAModel(amendsGAModel: AmendsGAModel)(using RequestHeader): Future[CacheMap] =
    saveFormData[AmendsGAModel](amendsGAModelKey, amendsGAModel)

  def saveAmendResponseModel(amendResponseModel: AmendResponseModel)(
      using RequestHeader
  ): Future[CacheMap] =
    saveFormData[AmendResponseModel](amendResponseModelKey, amendResponseModel)

  def savePreviousTechnicalIssues(previousTechnicalIssues: Boolean)(using RequestHeader): Future[CacheMap] =
    saveFormData[Boolean](previousTechnicalIssuesKey, previousTechnicalIssues)

  private[services] def saveFormData[T](
      key: String,
      data: T
  )(using request: RequestHeader, formats: Writes[T]): Future[CacheMap] =
    sessionRepository.putInSession(DataKey(key), data)

  def fetchOpenProtection(using RequestHeader): Future[Option[ProtectionModel]] =
    fetchAndGetFormData[ProtectionModel](openProtectionKey)

  def fetchAmendProtectionModel(protectionType: AmendableProtectionType, status: AmendProtectionRequestStatus)(
      using RequestHeader
  ): Future[Option[AmendProtectionModel]] =
    fetchAndGetFormData[AmendProtectionModel](amendProtectionModelKey(protectionType, status))

  def fetchAmendsGAModel(using RequestHeader): Future[Option[AmendsGAModel]] =
    fetchAndGetFormData[AmendsGAModel](amendsGAModelKey)

  def fetchAmendResponseModel(using RequestHeader): Future[Option[AmendResponseModel]] =
    fetchAndGetFormData[AmendResponseModel](amendResponseModelKey)

  def fetchPreviousTechnicalIssues(using RequestHeader): Future[Option[Boolean]] =
    fetchAndGetFormData[Boolean](previousTechnicalIssuesKey)

  private[services] def fetchAndGetFormData[T](
      key: String
  )(using request: RequestHeader, formats: Reads[T]): Future[Option[T]] =
    sessionRepository.getFromSession[T](DataKey(key))

  def remove()(using RequestHeader): Future[Unit] =
    sessionRepository.clearSession()

}
