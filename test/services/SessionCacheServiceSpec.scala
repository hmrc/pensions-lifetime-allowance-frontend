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

import constructors.display.DisplayConstructorsTestData
import models.amend.{AmendProtectionModel, AmendsGAModel}
import models.cache.CacheMap
import models.pla.AmendableProtectionType
import models.pla.request.AmendProtectionRequestStatus
import models.{AmendResponseModel, DateModel, PensionDebitModel, ProtectionModel}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Format, Json, Reads, Writes}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import testdata.AmendProtectionOutcomeViewsTestData.amendsGAModel
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.Future

class SessionCacheServiceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach
    with DisplayConstructorsTestData {

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]

  private val sessionCacheService = new SessionCacheService(mockSessionRepository)

  private val testKey      = "cache-key"
  private val testCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson("")))

  private given fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  private val testPensionDebit                               = PensionDebitModel(DateModel.of(2025, 12, 4), 10_000)
  private val testProtectionModel: ProtectionModel           = tstProtectionModel
  private val testAmendProtectionModel: AmendProtectionModel = tstNoPsoAmendProtectionModel
  private val testAmendsGAModel: AmendsGAModel               = amendsGAModel
  private val testAmendResponseModel: AmendResponseModel     = amendResponseModel
  private val testPreviousTechnicalIssues: Boolean           = false

  private val pensionDebitModelFormat: Format[PensionDebitModel]       = summon[Format[PensionDebitModel]]
  private val protectionModelFormat: Format[ProtectionModel]           = summon[Format[ProtectionModel]]
  private val amendProtectionModelFormat: Format[AmendProtectionModel] = summon[Format[AmendProtectionModel]]
  private val amendsGAModelFormat: Format[AmendsGAModel]               = summon[Format[AmendsGAModel]]
  private val amendResponseModelFormat: Format[AmendResponseModel]     = summon[Format[AmendResponseModel]]

  override def beforeEach(): Unit =
    reset(mockSessionRepository)

  "fetchAndGetFormData" should {
    "fetch and get from repo" in {
      when(
        mockSessionRepository.getFromSession[PensionDebitModel](DataKey[PensionDebitModel](any()))(using any(), any())
      )
        .thenReturn(Future.successful(Option(testPensionDebit)))

      val result: Future[Option[PensionDebitModel]] =
        sessionCacheService.fetchAndGetFormData[PensionDebitModel](testKey)

      await(result) shouldBe Some(testPensionDebit)
      verify(mockSessionRepository)
        .getFromSession[PensionDebitModel](DataKey[PensionDebitModel](testKey))(
          using pensionDebitModelFormat,
          fakeRequest
        )
    }
  }

  "saveFormData" should {
    "save form data to repo" in {
      when(
        mockSessionRepository
          .putInSession[PensionDebitModel](DataKey[PensionDebitModel](any()), any())(using any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveFormData(testKey, testPensionDebit)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository)
        .putInSession[PensionDebitModel](DataKey[PensionDebitModel](testKey), testPensionDebit)(
          using pensionDebitModelFormat,
          fakeRequest
        )
    }
  }

  "saveOpenProtection" should {
    "call saveFormData with correct key" in {
      when(
        mockSessionRepository
          .putInSession[ProtectionModel](DataKey[ProtectionModel](any()), any())(using any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveOpenProtection(tstProtectionModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[ProtectionModel](
        DataKey[ProtectionModel]("openProtection"),
        tstProtectionModel
      )(using protectionModelFormat, fakeRequest)
    }
  }

  "saveAmendProtectionModel" should {
    "call saveFormData with correct key" when {
      val cases = for {
        protectionType <- AmendableProtectionType.values
        status         <- AmendProtectionRequestStatus.values
      } yield (protectionType, status)

      cases.foreach { case (protectionType, status) =>
        s"protectionType is '$protectionType' and status is '$status'" in {
          when(
            mockSessionRepository
              .putInSession[AmendProtectionModel](DataKey[AmendProtectionModel](any()), any())(using any(), any())
          )
            .thenReturn(Future.successful(testCacheMap))

          val amendModel = testAmendProtectionModel.copy(
            protectionType = protectionType,
            status = status
          )

          val result = sessionCacheService.saveAmendProtectionModel(amendModel)

          val key = status.toString + protectionType.toString + "Amendment"

          await(result) shouldBe testCacheMap

          verify(mockSessionRepository).putInSession[AmendProtectionModel](
            DataKey[AmendProtectionModel](key),
            amendModel
          )(using amendProtectionModelFormat, fakeRequest)
        }
      }
    }
  }

  "saveAmendsGAModel" should {
    "call saveFormData with correct key" in {

      when(
        mockSessionRepository
          .putInSession[AmendsGAModel](DataKey[AmendsGAModel](any()), any())(using any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveAmendsGAModel(amendsGAModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[AmendsGAModel](DataKey[AmendsGAModel]("AmendsGA"), amendsGAModel)(
        using amendsGAModelFormat,
        fakeRequest
      )
    }
  }

  "saveAmendResponseModel" should {
    "call saveFormData with correct key" in {

      when(
        mockSessionRepository
          .putInSession[AmendResponseModel](DataKey[AmendResponseModel](any()), any())(using any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveAmendResponseModel(testAmendResponseModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[AmendResponseModel](
        DataKey[AmendResponseModel]("amendResponseModel"),
        testAmendResponseModel
      )(using amendResponseModelFormat, fakeRequest)
    }
  }

  "savePreviousTechnicalIssues" should {
    "call saveFormData with correct key" in {

      when(mockSessionRepository.putInSession[Boolean](DataKey[Boolean](any()), any())(using any(), any()))
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.savePreviousTechnicalIssues(testPreviousTechnicalIssues)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository)
        .putInSession[Boolean](DataKey[Boolean]("previous-technical-issues"), testPreviousTechnicalIssues)(
          using Writes.BooleanWrites,
          fakeRequest
        )
    }
  }

  "fetchOpenProtection" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[ProtectionModel](DataKey[ProtectionModel](any()))(using any(), any()))
        .thenReturn(Future.successful(Some(testProtectionModel)))

      val result = sessionCacheService.fetchOpenProtection

      await(result) shouldBe Some(testProtectionModel)

      verify(mockSessionRepository).getFromSession[ProtectionModel](DataKey[ProtectionModel]("openProtection"))(
        using protectionModelFormat,
        fakeRequest
      )
    }
  }

  "fetchAmendProtectionModel" should {
    "call getFromSession with correct key" when {
      val cases = for {
        protectionType <- AmendableProtectionType.values
        status         <- AmendProtectionRequestStatus.values
      } yield (protectionType, status)

      cases.foreach { case (protectionType, status) =>
        s"protectionType is '$protectionType' and status is $status'" in {
          val amendModel = testAmendProtectionModel.copy(
            protectionType = protectionType,
            status = status
          )

          when(
            mockSessionRepository
              .getFromSession[AmendProtectionModel](DataKey[AmendProtectionModel](any()))(using any(), any())
          )
            .thenReturn(Future.successful(Some(amendModel)))

          val result = sessionCacheService.fetchAmendProtectionModel(protectionType, status)

          val key = status.toString + protectionType.toString + "Amendment"

          await(result) shouldBe Some(amendModel)

          verify(mockSessionRepository).getFromSession[AmendProtectionModel](DataKey[AmendProtectionModel](key))(
            amendProtectionModelFormat,
            fakeRequest
          )
        }
      }
    }
  }

  "fetchAmendsGAModel" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[AmendsGAModel](DataKey[AmendsGAModel](any()))(using any(), any()))
        .thenReturn(Future.successful(Some(testAmendsGAModel)))

      val result = sessionCacheService.fetchAmendsGAModel

      await(result) shouldBe Some(testAmendsGAModel)

      verify(mockSessionRepository)
        .getFromSession[AmendsGAModel](DataKey[AmendsGAModel]("AmendsGA"))(amendsGAModelFormat, fakeRequest)
    }
  }

  "fetchAmendResponseModel" should {
    "call getFromSession with correct key" in {
      when(
        mockSessionRepository.getFromSession[AmendResponseModel](DataKey[AmendResponseModel](any()))(using any(), any())
      )
        .thenReturn(Future.successful(Some(testAmendResponseModel)))

      val result = sessionCacheService.fetchAmendResponseModel

      await(result) shouldBe Some(testAmendResponseModel)

      verify(mockSessionRepository).getFromSession[AmendResponseModel](
        DataKey[AmendResponseModel]("amendResponseModel")
      )(
        amendResponseModelFormat,
        fakeRequest
      )
    }
  }

  "fetchPreviousTechnicalIssues" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[Boolean](DataKey[Boolean](any()))(using any(), any()))
        .thenReturn(Future.successful(Some(testPreviousTechnicalIssues)))

      val result = sessionCacheService.fetchPreviousTechnicalIssues

      await(result) shouldBe Some(testPreviousTechnicalIssues)

      verify(mockSessionRepository)
        .getFromSession[Boolean](DataKey[Boolean]("previous-technical-issues"))(Reads.BooleanReads, fakeRequest)
    }
  }

  "remove" should {
    "call clearSession" in {
      when(mockSessionRepository.clearSession()(using any())).thenReturn(Future.successful((): Unit))

      sessionCacheService.remove()

      verify(mockSessionRepository).clearSession()(using fakeRequest)
    }
  }

}
