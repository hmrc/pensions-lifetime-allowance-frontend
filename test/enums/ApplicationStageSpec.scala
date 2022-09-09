/*
 * Copyright 2022 HM Revenue & Customs
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

package enums

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ApplicationStageSpec extends AnyFlatSpec with Matchers {

  import ApplicationStage._

  "ApplicationStage.fromString" should "return Some(PensionsTakenBefore) " in {
    fromString("pensionstakenbefore") shouldBe Some(PensionsTakenBefore)
  }

  it should "return Some(PensionsTakenBetween)" in {
    fromString("pensionstakenbetween") shouldBe Some(PensionsTakenBetween)
  }
  it should "return Some(OverseasPensions)" in {
    fromString("overseaspensions") shouldBe Some(OverseasPensions)
  }
  it should "return Some(CurrentPensions)" in {
    fromString("currentpensions") shouldBe Some(CurrentPensions)
  }
  it should "return Some(PreviousPsos)" in {
    fromString("previouspsos") shouldBe Some(PreviousPsos)
  }
  it should "return Some(CurrentPsos)" in {
    fromString("currentpsos") shouldBe Some(CurrentPsos)
  }
  it should "return None" in {
    fromString("") shouldBe None
  }


}
