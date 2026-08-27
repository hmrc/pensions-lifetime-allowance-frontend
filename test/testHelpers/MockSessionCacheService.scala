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

package testHelpers

import models.AmendResponseModel
import models.amend.{AmendProtectionModel, AmendsGAModel}
import models.cache.CacheMap
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import services.SessionCacheService

import scala.concurrent.Future

trait MockSessionCacheService extends BeforeAndAfterEach { this: Suite with MockitoSugar =>

  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockSessionCacheService)
  }

  def mockFetchAmendProtectionModel(protectionType: AmendableProtectionType, status: AmendProtectionRequestStatus)(
      result: Option[AmendProtectionModel]
  ): OngoingStubbing[Future[Option[AmendProtectionModel]]] =
    when(mockSessionCacheService.fetchAmendProtectionModel(protectionType, status)(using any()))
      .thenReturn(Future.successful(result))

  def mockFetchAmendsGAModel(result: Option[AmendsGAModel]): OngoingStubbing[Future[Option[AmendsGAModel]]] =
    when(mockSessionCacheService.fetchAmendsGAModel(using any())).thenReturn(Future.successful(result))

  def mockFetchAmendResponseModel(
      result: Option[AmendResponseModel]
  ): OngoingStubbing[Future[Option[AmendResponseModel]]] =
    when(mockSessionCacheService.fetchAmendResponseModel(using any())).thenReturn(Future.successful(result))

  private val emptyCacheMap = CacheMap("", Map.empty)

  def mockSaveAmendProtectionModel(result: CacheMap = emptyCacheMap): OngoingStubbing[Future[CacheMap]] =
    when(mockSessionCacheService.saveAmendProtectionModel(any())(using any())).thenReturn(Future.successful(result))

  def mockSaveAmendsGAModel(result: CacheMap = emptyCacheMap): OngoingStubbing[Future[CacheMap]] =
    when(mockSessionCacheService.saveAmendsGAModel(any())(using any())).thenReturn(Future.successful(result))

  def mockSaveAmendResponseModel(result: CacheMap = emptyCacheMap): OngoingStubbing[Future[CacheMap]] =
    when(mockSessionCacheService.saveAmendResponseModel(any())(using any())).thenReturn(Future.successful(result))

}
