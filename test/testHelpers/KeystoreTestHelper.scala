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

package testHelpers

import connectors.KeyStoreConnector
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

trait KeystoreTestHelper {

  def keystoreSaveCondition[T](mockKeyStoreConnector: KeyStoreConnector, key: Option[String] = None, returnedData: Option[CacheMap] = None): OngoingStubbing[Future[CacheMap]] = {
    val keyMatcher = key.map(ArgumentMatchers.contains).getOrElse(ArgumentMatchers.anyString())

    when(mockKeyStoreConnector.saveFormData[T](keyMatcher, ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any()))
      .thenReturn(Future.successful(returnedData.getOrElse(CacheMap("", Map.empty))))
  }


}
