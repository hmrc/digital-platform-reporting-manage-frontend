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

package connectors

import config.FrontendAppConfig
import connectors.TaxEnrolmentConnector.{AllocateEnrolmentToGroupTaxEnrolmentFailure, UpsertTaxEnrolmentFailure}
import models.eacd.requests.{GroupEnrolment, UpsertKnownFacts}
import org.apache.pekko.Done
import play.api.http.Status.{CONFLICT, CREATED, NO_CONTENT}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import services.UuidService
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxEnrolmentConnector @Inject()(appConfig: FrontendAppConfig,
                                      httpClient: HttpClientV2,
                                      uuidService: UuidService)
                                     (implicit ec: ExecutionContext) {

  def upsert(upsertKnownFacts: UpsertKnownFacts)
            (implicit hc: HeaderCarrier): Future[Done] =
    httpClient.put(url"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments/enrolments/${upsertKnownFacts.enrolmentKey}")
      .setHeader("X-Correlation-ID" -> uuidService.generate())
      .setHeader("X-Conversation-ID" -> uuidService.generate())
      .setHeader("X-Forwarded-Host" -> appConfig.appName)
      .withBody(Json.toJson(upsertKnownFacts))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NO_CONTENT => Future.successful(Done)
          case code => Future.failed(UpsertTaxEnrolmentFailure(code))
        }
      }

  def allocateEnrolmentToGroup(groupEnrolment: GroupEnrolment)
                              (implicit hc: HeaderCarrier): Future[Done] =
    httpClient.post(url"${appConfig.taxEnrolmentsBaseUrl}/tax-enrolments/groups/${groupEnrolment.groupId}/enrolments/${groupEnrolment.enrolmentKey}")
      .setHeader("X-Correlation-ID" -> uuidService.generate())
      .setHeader("X-Conversation-ID" -> uuidService.generate())
      .setHeader("X-Forwarded-Host" -> appConfig.appName)
      .withBody(Json.toJson(groupEnrolment))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case CREATED | CONFLICT => Future.successful(Done)
          case code => Future.failed(AllocateEnrolmentToGroupTaxEnrolmentFailure(code))
        }
      }
}

object TaxEnrolmentConnector {

  final case class UpsertTaxEnrolmentFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Upsert tax enrolment failed with status: $status"
  }

  final case class AllocateEnrolmentToGroupTaxEnrolmentFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Allocate enrolment to group tax enrolment failed with status: $status"
  }
}
