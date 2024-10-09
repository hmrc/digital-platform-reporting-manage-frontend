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
import connectors.PendingEnrolmentConnector.{GetPendingEnrolmentFailure, RemovePendingEnrolmentFailure}
import models.enrolment.response.PendingEnrolment
import org.apache.pekko.Done
import play.api.http.Status.OK
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PendingEnrolmentConnector @Inject()(appConfig: FrontendAppConfig, httpClient: HttpClientV2)
                                         (implicit ec: ExecutionContext) {

  def getPendingEnrolment()(implicit hc: HeaderCarrier): Future[PendingEnrolment] =
    httpClient.get(url"${appConfig.digitalPlatformReportingUrl}/digital-platform-reporting/pending-enrolment")
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK => Future.successful(response.json.as[PendingEnrolment])
          case status => Future.failed(GetPendingEnrolmentFailure(status))
        }
      }

  def remove()(implicit hc: HeaderCarrier): Future[Done] =
    httpClient.delete(url"${appConfig.digitalPlatformReportingUrl}/digital-platform-reporting/pending-enrolment")
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK => Future.successful(Done)
          case status => Future.failed(RemovePendingEnrolmentFailure(status))
        }
      }
}

object PendingEnrolmentConnector {

  final case class GetPendingEnrolmentFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Get pending enrolment failed with status: $status"
  }

  final case class RemovePendingEnrolmentFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Remove pending enrolment failed with status: $status"
  }
}