/*
 * Copyright 2016 HM Revenue & Customs
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

package constructors

import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import models._
import uk.gov.hmrc.http.cache.client.CacheMap

object SummaryConstructor extends SummaryConstructor {
    
}

trait SummaryConstructor {

  def createSummary(data: CacheMap): Int = {
    21
  }
}
