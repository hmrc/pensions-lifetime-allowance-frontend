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

package config

import auth.{AuthFunction, AuthFunctionImpl}
import com.google.inject.AbstractModule
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}
import utils.{ActionWithSessionId, ActionWithSessionImpl}


class Module extends AbstractModule {

  override def configure(): Unit = {
    bindControllers()
  }

  private def bindControllers() = {
    bind(classOf[ActionWithSessionId]).to(classOf[ActionWithSessionImpl])
    bind(classOf[PlaContext]).to(classOf[PlaContextImpl])
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
    bind(classOf[HttpClient]).to(classOf[DefaultHttpClient])
    bind(classOf[AuthFunction]).to(classOf[AuthFunctionImpl])
  }
}