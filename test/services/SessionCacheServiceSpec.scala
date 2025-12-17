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
import models.{AmendResponseModel, DateModel, PensionDebitModel, ProtectionModel, PsaLookupRequest, PsaLookupResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json, Reads, Writes}
import play.api.test.Helpers._
import repositories.SessionRepository
import testHelpers.FakeApplication
import testdata.AmendProtectionOutcomeViewsTestData.amendsGAModel
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class SessionCacheServiceSpec
    extends FakeApplication
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach
    with DisplayConstructorsTestData {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  implicit val executionContext: ExecutionContext = inject[ExecutionContext]

  val sessionCacheService = new SessionCacheService(mockSessionRepository)

  val testKey      = "cache-key"
  val testCacheMap = CacheMap("haveAddedToPension", Map("data" -> Json.toJson("")))

  val testPensionDebit                               = PensionDebitModel(DateModel.of(2025, 12, 4), 10_000)
  val testProtectionModel: ProtectionModel           = tstProtectionModel
  val testAmendProtectionModel: AmendProtectionModel = tstNoPsoAmendProtectionModel
  val testAmendsGAModel: AmendsGAModel               = amendsGAModel
  val testAmendResponseModel: AmendResponseModel     = amendResponseModel
  val testPsaLookupRequest                 = PsaLookupRequest("psaCheckReference", Some("protectionReference"))
  val testPsaLookupResult                  = PsaLookupResult("protectionReference", 6, 7, None, None)
  val testPreviousTechnicalIssues: Boolean = false

  override def beforeEach(): Unit =
    reset(mockSessionRepository)

  "fetchAndGetFormData" should {
    "fetch and get from repo" in {
      when(mockSessionRepository.getFromSession[PensionDebitModel](DataKey[PensionDebitModel](any()))(any(), any()))
        .thenReturn(Future.successful(Option(testPensionDebit)))

      val result = sessionCacheService.fetchAndGetFormData[PensionDebitModel](testKey)
      await(result) shouldBe Some(testPensionDebit)
      verify(mockSessionRepository)
        .getFromSession[PensionDebitModel](DataKey[PensionDebitModel](testKey))(PensionDebitModel.format, fakeRequest)
    }
  }

  "saveFormData" should {
    "save form data to repo" in {
      when(
        mockSessionRepository
          .putInSession[PensionDebitModel](DataKey[PensionDebitModel](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveFormData(testKey, testPensionDebit)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository)
        .putInSession[PensionDebitModel](DataKey[PensionDebitModel](testKey), testPensionDebit)(
          PensionDebitModel.format,
          fakeRequest,
          executionContext
        )
    }
  }

  "saveOpenProtection" should {
    "call saveFormData with correct key" in {
      when(
        mockSessionRepository.putInSession[ProtectionModel](DataKey[ProtectionModel](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveOpenProtection(tstProtectionModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[ProtectionModel](
        DataKey[ProtectionModel]("openProtection"),
        tstProtectionModel
      )(ProtectionModel.format, fakeRequest, executionContext)
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
              .putInSession[AmendProtectionModel](DataKey[AmendProtectionModel](any()), any())(any(), any(), any())
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
          )(AmendProtectionModel.format, fakeRequest, executionContext)
        }
      }
    }
  }

  "saveAmendsGAModel" should {
    "call saveFormData with correct key" in {

      when(mockSessionRepository.putInSession[AmendsGAModel](DataKey[AmendsGAModel](any()), any())(any(), any(), any()))
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveAmendsGAModel(amendsGAModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[AmendsGAModel](DataKey[AmendsGAModel]("AmendsGA"), amendsGAModel)(
        AmendsGAModel.format,
        fakeRequest,
        executionContext
      )
    }
  }

  "saveAmendResponseModel" should {
    "call saveFormData with correct key" in {

      when(
        mockSessionRepository
          .putInSession[AmendResponseModel](DataKey[AmendResponseModel](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.saveAmendResponseModel(testAmendResponseModel)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[AmendResponseModel](
        DataKey[AmendResponseModel]("amendResponseModel"),
        testAmendResponseModel
      )(AmendResponseModel.format, fakeRequest, executionContext)
    }
  }

  "savePsaLookupRequest" should {
    "call saveFormData with correct key" in {

      when(
        mockSessionRepository
          .putInSession[PsaLookupRequest](DataKey[PsaLookupRequest](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.savePsaLookupRequest(testPsaLookupRequest)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[PsaLookupRequest](
        DataKey[PsaLookupRequest]("psa-lookup-request"),
        testPsaLookupRequest
      )(PsaLookupRequest.format, fakeRequest, executionContext)
    }
  }

  "savePsaLookupResult" should {
    "call saveFormData with correct key" in {

      when(
        mockSessionRepository.putInSession[PsaLookupResult](DataKey[PsaLookupResult](any()), any())(any(), any(), any())
      )
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.savePsaLookupResult(testPsaLookupResult)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository).putInSession[PsaLookupResult](
        DataKey[PsaLookupResult]("psa-lookup-result"),
        testPsaLookupResult
      )(PsaLookupResult.format, fakeRequest, executionContext)
    }
  }

  "savePreviousTechnicalIssues" should {
    "call saveFormData with correct key" in {

      when(mockSessionRepository.putInSession[Boolean](DataKey[Boolean](any()), any())(any(), any(), any()))
        .thenReturn(Future.successful(testCacheMap))

      val result = sessionCacheService.savePreviousTechnicalIssues(testPreviousTechnicalIssues)

      await(result) shouldBe testCacheMap

      verify(mockSessionRepository)
        .putInSession[Boolean](DataKey[Boolean]("previous-technical-issues"), testPreviousTechnicalIssues)(
          Writes.BooleanWrites,
          fakeRequest,
          executionContext
        )
    }
  }

  "fetchOpenProtection" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[ProtectionModel](DataKey[ProtectionModel](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testProtectionModel)))

      val result = sessionCacheService.fetchOpenProtection

      await(result) shouldBe Some(testProtectionModel)

      verify(mockSessionRepository).getFromSession[ProtectionModel](DataKey[ProtectionModel]("openProtection"))(
        ProtectionModel.format,
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
              .getFromSession[AmendProtectionModel](DataKey[AmendProtectionModel](any()))(any(), any())
          )
            .thenReturn(Future.successful(Some(amendModel)))

          val result = sessionCacheService.fetchAmendProtectionModel(protectionType, status)

          val key = status.toString + protectionType.toString + "Amendment"

          await(result) shouldBe Some(amendModel)

          verify(mockSessionRepository).getFromSession[AmendProtectionModel](DataKey[AmendProtectionModel](key))(
            AmendProtectionModel.format,
            fakeRequest
          )
        }
      }
    }
  }

  "fetchAmendsGAModel" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[AmendsGAModel](DataKey[AmendsGAModel](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testAmendsGAModel)))

      val result = sessionCacheService.fetchAmendsGAModel

      await(result) shouldBe Some(testAmendsGAModel)

      verify(mockSessionRepository)
        .getFromSession[AmendsGAModel](DataKey[AmendsGAModel]("AmendsGA"))(AmendsGAModel.format, fakeRequest)
    }
  }

  "fetchAmendResponseModel" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[AmendResponseModel](DataKey[AmendResponseModel](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testAmendResponseModel)))

      val result = sessionCacheService.fetchAmendResponseModel

      await(result) shouldBe Some(testAmendResponseModel)

      verify(mockSessionRepository).getFromSession[AmendResponseModel](
        DataKey[AmendResponseModel]("amendResponseModel")
      )(
        AmendResponseModel.format,
        fakeRequest
      )
    }
  }

  "fetchPsaLookupRequest" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[PsaLookupRequest](DataKey[PsaLookupRequest](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testPsaLookupRequest)))

      val result = sessionCacheService.fetchPsaLookupRequest

      await(result) shouldBe Some(testPsaLookupRequest)

      verify(mockSessionRepository).getFromSession[PsaLookupRequest](DataKey[PsaLookupRequest]("psa-lookup-request"))(
        PsaLookupRequest.format,
        fakeRequest
      )
    }
  }

  "fetchPsaLookupResult" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[PsaLookupResult](DataKey[PsaLookupResult](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testPsaLookupResult)))

      val result = sessionCacheService.fetchPsaLookupResult

      await(result) shouldBe Some(testPsaLookupResult)

      verify(mockSessionRepository).getFromSession[PsaLookupResult](DataKey[PsaLookupResult]("psa-lookup-result"))(
        PsaLookupResult.format,
        fakeRequest
      )
    }
  }

  "fetchPreviousTechnicalIssues" should {
    "call getFromSession with correct key" in {
      when(mockSessionRepository.getFromSession[Boolean](DataKey[Boolean](any()))(any(), any()))
        .thenReturn(Future.successful(Some(testPreviousTechnicalIssues)))

      val result = sessionCacheService.fetchPreviousTechnicalIssues

      await(result) shouldBe Some(testPreviousTechnicalIssues)

      verify(mockSessionRepository)
        .getFromSession[Boolean](DataKey[Boolean]("previous-technical-issues"))(Reads.BooleanReads, fakeRequest)
    }
  }

  "remove" should {
    "call clearSession" in {
      when(mockSessionRepository.clearSession(any())).thenReturn(Future.successful((): Unit))

      sessionCacheService.remove

      verify(mockSessionRepository).clearSession(fakeRequest)
    }
  }

}
