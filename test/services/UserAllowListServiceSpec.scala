/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import config.FrontendAppConfig
import connectors.UserAllowListConnector
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserAllowListServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with ScalaFutures {

  private val mockConnector = mock[UserAllowListConnector]
  private val mockAppConfig = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    reset(mockConnector, mockAppConfig)
    super.beforeEach()
  }

  private val service = new UserAllowListService(mockConnector, mockAppConfig)
  private val emptyEnrolments = Enrolments(Set.empty)
  private val ctEnrolment = Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", "utr")), "activated")
  private val mtdEnrolment = Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "vrn")), "activated")
  private val vatEnrolment = Enrolment("HMCE-VATDEC-ORG", Seq(EnrolmentIdentifier("VATRegNo", "vatref")), "activated")
  private val fatcaEnrolment = Enrolment("HMRC-FATCA-ORG", Seq(EnrolmentIdentifier("FATCAID", "fatcaId")), "activated")

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "isUserAllowed" - {

    "when the allowlist feature is disabled" - {

      "must return true" in {

        when(mockAppConfig.userAllowListEnabled) thenReturn false

        service.isUserAllowed(emptyEnrolments).futureValue mustEqual true
        verify(mockConnector, never).check(any(), any())(any())
      }
    }

    "when the allowlist feature is enabled" - {

      "must return true" - {

        "when the user has a CT enrolment in the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("UTR"), any())(any())) thenReturn Future.successful(true)

          val enrolments = Enrolments(Set(ctEnrolment, mtdEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual true
        }

        "when the user has an HMRC_MTD-VAT enrolment in the VRN list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("UTR"), any())(any())) thenReturn Future.successful(false)
          when(mockConnector.check(eqTo("VRN"), any())(any())) thenReturn Future.successful(true)

          val enrolments = Enrolments(Set(ctEnrolment, mtdEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual true
        }

        "when the user has an HMCE-VATDEC-ORG enrolment in the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("UTR"), any())(any())) thenReturn Future.successful(false)
          when(mockConnector.check(eqTo("VRN"), any())(any())) thenReturn Future.successful(true)

          val enrolments = Enrolments(Set(ctEnrolment, vatEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual true
        }

        "when the user has an HMRC-FATCA-ORG enrolment in the list" in {
          when(mockAppConfig.userAllowListEnabled).thenReturn(true)
          when(mockConnector.check(eqTo("UTR"), any())(any())).thenReturn(Future.successful(false))
          when(mockConnector.check(eqTo("FATCAID"), any())(any())).thenReturn(Future.successful(true))

          val enrolments = Enrolments(Set(ctEnrolment, fatcaEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual true
        }
      }

      "must return false" - {

        "when the user has no CT or VAT enrolments" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true

          service.isUserAllowed(emptyEnrolments).futureValue mustEqual false
        }

        "when the user has a CT enrolment but it isn't on the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("UTR"), any())(any())) thenReturn Future.successful(false)

          val enrolments = Enrolments(Set(ctEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual false
        }

        "when the user has an HMRC-MTD-VAT enrolment but it isn't on the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("VRN"), any())(any())) thenReturn Future.successful(false)

          val enrolments = Enrolments(Set(mtdEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual false
        }

        "when the user has an HMCE-VATDEC-ORG enrolment but it isn't on the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("VRN"), any())(any())) thenReturn Future.successful(false)

          val enrolments = Enrolments(Set(vatEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual false
        }

        "when the user has a CT enrolment and VAT enrolment but neither is on the list" in {

          when(mockAppConfig.userAllowListEnabled) thenReturn true
          when(mockConnector.check(eqTo("UTR"), any())(any())) thenReturn Future.successful(false)
          when(mockConnector.check(eqTo("VRN"), any())(any())) thenReturn Future.successful(false)

          val enrolments = Enrolments(Set(ctEnrolment, mtdEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual false
        }

        "when the user has an HMRC-FATCA-ORG enrolment but it isn't on the list" in {
          when(mockAppConfig.userAllowListEnabled).thenReturn(true)
          when(mockConnector.check(eqTo("FATCAID"), any())(any())).thenReturn(Future.successful(false))

          val enrolments = Enrolments(Set(fatcaEnrolment))

          service.isUserAllowed(enrolments).futureValue mustEqual false
        }
      }
    }
  }
}
