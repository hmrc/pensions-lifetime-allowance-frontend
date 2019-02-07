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

import com.google.inject.AbstractModule
import connectors.CitizenDetailsConnector
import controllers._
import connectors._


class Module extends AbstractModule {

  override def configure(): Unit = {
    bindControllers()
    bindConnectors()
  }

  private def bindControllers() = {
    bind(classOf[ReadProtectionsController]).to(classOf[ReadProtectionsControllerImpl])
    bind(classOf[AmendsController]).to(classOf[AmendsControllerImpl])
  }



  private def bindConnectors() = {
    bind(classOf[CitizenDetailsConnector]).to(classOf[CitizenDetailsConnectorImpl])
    bind(classOf[IdentityVerificationConnector]).to(classOf[IdentityVerificationConnectorImpl])
    bind(classOf[KeyStoreConnector]).to(classOf[KeyStoreConnectorImpl])
    bind(classOf[PLAConnector]).to(classOf[PLAConnectorImpl])
  }
}