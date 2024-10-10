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

import builders.EnrolmentDetailsBuilder.anEnrolmentDetails
import connectors.TaxEnrolmentConnector
import models.eacd.requests.{GroupEnrolment, UpsertKnownFacts}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentServiceSpec extends AnyFreeSpec
  with Matchers
  with MockitoSugar
  with ScalaFutures
  with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    Mockito.reset(mockTaxEnrolmentConnector)
    super.beforeEach()
  }

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val mockTaxEnrolmentConnector = mock[TaxEnrolmentConnector]

  private val underTest = new EnrolmentService(mockTaxEnrolmentConnector)

  ".enrol(...)" - {
    "must upsert and allocate enrolment to a group" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.successful(Done))
      when(mockTaxEnrolmentConnector.allocateEnrolmentToGroup(GroupEnrolment(anEnrolmentDetails))).thenReturn(Future.successful(Done))

      underTest.enrol(anEnrolmentDetails).futureValue
    }

    "must error when when upsert fails" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.failed(new RuntimeException()))

      underTest.enrol(anEnrolmentDetails).failed

      verify(mockTaxEnrolmentConnector, never()).allocateEnrolmentToGroup(any())(any())
    }

    "must error when allocation fails" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.successful(Done))
      when(mockTaxEnrolmentConnector.allocateEnrolmentToGroup(GroupEnrolment(anEnrolmentDetails))).thenReturn(Future.failed(new RuntimeException()))

      underTest.enrol(anEnrolmentDetails).failed
    }
  }
}
