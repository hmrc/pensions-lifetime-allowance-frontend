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

import models.cache.CacheMap
import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import play.api.mvc.Request
import services.SessionCacheService

import scala.concurrent.Future

trait SessionCacheTestHelper {

  def cacheSaveCondition[T](mockSessionCacheService: SessionCacheService, key: Option[String] = None, returnedData: Option[CacheMap] = None): OngoingStubbing[Future[CacheMap]] = {
    val keyMatcher = key.map(Matchers.contains).getOrElse(Matchers.anyString())

    when(mockSessionCacheService.saveFormData[T](keyMatcher, Matchers.any())(Matchers.any[Request[_]](), Matchers.any()))
      .thenReturn(Future.successful(returnedData.getOrElse(CacheMap("", Map.empty))))
  }


}