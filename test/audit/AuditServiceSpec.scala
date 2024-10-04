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

package audit

import config.FrontendAppConfig
import models.subscription.{Individual, IndividualContact, SubscriptionInfo}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private val mockAuditConnector: AuditConnector = mock[AuditConnector]
  private val mockAppConfig = mock[FrontendAppConfig]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  private val auditSource = "audit-source"

  private val service = new AuditService(mockAuditConnector, mockAppConfig)

  ".sendAudit" - {

    "must send an event to the audit connector" in {

      val eventModel = ChangeDetailsAuditEvent(
        original = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "phone", None),
          secondaryContact = None
        ),
        updated = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "new phone", None),
          secondaryContact = None
        )
      )

      when(mockAppConfig.auditSource).thenReturn(auditSource)

      service.sendAudit(eventModel)
      val eventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      verify(mockAuditConnector, times(1)).sendExtendedEvent(eventCaptor.capture())(any(), any())

      val event = eventCaptor.getValue
      event.auditSource mustEqual auditSource
      event.auditType mustEqual "ChangeContactDetails"
      event.detail mustEqual Json.toJson(eventModel)
    }
  }
}
