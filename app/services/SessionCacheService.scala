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
import models.{AmendResponseModel, ProtectionModel, PsaLookupRequest, PsaLookupResult}
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

  private val openProtectionKey: String = "openProtection"

  private def amendProtectionModelKey(
      protectionType: AmendableProtectionType,
      status: AmendProtectionRequestStatus
  ): String =
    status.toString + protectionType.toString + "Amendment"

  private val amendsGAModelKey: String = "AmendsGA"

  private val amendResponseModelKey: String = "amendResponseModel"
  private val psaLookupRequestKey           = "psa-lookup-request"
  private val psaLookupResultKey            = "psa-lookup-result"
  private val previousTechnicalIssuesKey    = "previous-technical-issues"

  def saveOpenProtection(openProtection: ProtectionModel)(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[ProtectionModel](openProtectionKey, openProtection)

  def saveAmendProtectionModel(
      amendProtectionModel: AmendProtectionModel
  )(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[AmendProtectionModel](
      amendProtectionModelKey(amendProtectionModel.protectionType, amendProtectionModel.status),
      amendProtectionModel
    )

  def saveAmendsGAModel(amendsGAModel: AmendsGAModel)(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[AmendsGAModel](amendsGAModelKey, amendsGAModel)

  def saveAmendResponseModel(amendResponseModel: AmendResponseModel)(
      implicit request: Request[_]
  ): Future[CacheMap] =
    saveFormData[AmendResponseModel](amendResponseModelKey, amendResponseModel)

  def savePsaLookupRequest(psaLookupRequest: PsaLookupRequest)(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[PsaLookupRequest](psaLookupRequestKey, psaLookupRequest)

  def savePsaLookupResult(psaLookupResult: PsaLookupResult)(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[PsaLookupResult](psaLookupResultKey, psaLookupResult)

  def savePreviousTechnicalIssues(previousTechnicalIssues: Boolean)(implicit request: Request[_]): Future[CacheMap] =
    saveFormData[Boolean](previousTechnicalIssuesKey, previousTechnicalIssues)

  private[services] def saveFormData[T](
      key: String,
      data: T
  )(implicit request: Request[_], formats: Writes[T]): Future[CacheMap] = {
    logger.info(s"Saving $key as ${Json.toJson(data)}")
    sessionRepository.putInSession(DataKey(key), data)
  }

  def fetchOpenProtection(implicit request: Request[_]): Future[Option[ProtectionModel]] =
    fetchAndGetFormData[ProtectionModel](openProtectionKey)

  def fetchAmendProtectionModel(protectionType: AmendableProtectionType, status: AmendProtectionRequestStatus)(
      implicit request: Request[_]
  ): Future[Option[AmendProtectionModel]] =
    fetchAndGetFormData[AmendProtectionModel](amendProtectionModelKey(protectionType, status))

  def fetchAmendsGAModel(implicit request: Request[_]): Future[Option[AmendsGAModel]] =
    fetchAndGetFormData[AmendsGAModel](amendsGAModelKey)

  def fetchAmendResponseModel(implicit request: Request[_]): Future[Option[AmendResponseModel]] =
    fetchAndGetFormData[AmendResponseModel](amendResponseModelKey)

  def fetchPsaLookupRequest(implicit request: Request[_]): Future[Option[PsaLookupRequest]] =
    fetchAndGetFormData[PsaLookupRequest](psaLookupRequestKey)

  def fetchPsaLookupResult(implicit request: Request[_]): Future[Option[PsaLookupResult]] =
    fetchAndGetFormData[PsaLookupResult](psaLookupResultKey)

  def fetchPreviousTechnicalIssues(implicit request: Request[_]): Future[Option[Boolean]] =
    fetchAndGetFormData[Boolean](previousTechnicalIssuesKey)

  private[services] def fetchAndGetFormData[T](
      key: String
  )(implicit request: Request[_], formats: Reads[T]): Future[Option[T]] = {
    logger.info(s"Fetching $key")
    sessionRepository.getFromSession[T](DataKey(key))
  }

  def remove(implicit request: Request[_]): Future[Unit] =
    sessionRepository.clearSession

}
