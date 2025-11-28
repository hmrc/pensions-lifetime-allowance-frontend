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

package controllers

import common.Strings
import models.amendModels._
import models.cache.CacheMap
import play.api.mvc.Results.Redirect
import play.api.mvc.{MessagesRequest, Result}
import services.SessionCacheService

import scala.concurrent.Future

trait AmendControllerCacheHelper {

  val sessionCacheService: SessionCacheService

  private def cacheKey(protectionType: String, status: String): String =
    Strings.protectionCacheKey(protectionType, status)

  def fetchAmendProtectionModel(protectionType: String, status: String)(
      implicit request: MessagesRequest[_]
  ): Future[Option[AmendProtectionModel]] =
    sessionCacheService.fetchAndGetFormData[AmendProtectionModel](cacheKey(protectionType, status))

  def saveAmendProtectionModel(protectionType: String, status: String, amendModel: AmendProtectionModel)(
      implicit request: MessagesRequest[_]
  ): Future[CacheMap] =
    sessionCacheService
      .saveFormData[AmendProtectionModel](cacheKey(protectionType, status), amendModel)

  def redirectToSummary(amendModel: AmendProtectionModel): Result = {
    val updatedProtectionType =
      Strings.protectionTypeUrlString(amendModel.updatedProtection.protectionType).toLowerCase
    val updatedStatus = Strings.statusString(amendModel.updatedProtection.status).toLowerCase

    Redirect(routes.AmendsController.amendsSummary(updatedProtectionType, updatedStatus))
  }

}
