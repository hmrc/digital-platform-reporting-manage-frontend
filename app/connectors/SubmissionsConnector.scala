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

import config.Service
import connectors.SubmissionsConnector.{AssumedReportsExistFailure, SubmissionsExistFailure}
import models.submissions.ViewSubmissionsRequest
import play.api.Configuration
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionsConnector @Inject()(configuration: Configuration,
                                     httpClient: HttpClientV2)(implicit ec: ExecutionContext) {

  private val digitalPlatformReporting: Service = configuration.get[Service]("microservice.services.digital-platform-reporting")

  def submissionsExist(implicit hc: HeaderCarrier): Future[Boolean] = {

    val request = ViewSubmissionsRequest(assumedReporting = false)

    httpClient.post(url"$digitalPlatformReporting/digital-platform-reporting/submission/delivered/list")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK        => Future.successful(true)
          case NOT_FOUND => Future.successful(false)
          case status    => Future.failed(SubmissionsExistFailure(status))
        }
      }
  }
  
  def assumedReportsExist(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient.get(url"$digitalPlatformReporting/digital-platform-reporting/submission/assumed")
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK        => Future.successful(true)
          case NOT_FOUND => Future.successful(false)
          case status    => Future.failed(AssumedReportsExistFailure(status))
        }
      }
}

object SubmissionsConnector {

  final case class SubmissionsExistFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Call to check if submissions exist failed with status: $status"
  }
  
  final case class AssumedReportsExistFailure(status: Int) extends Throwable {
    override def getMessage: String = s"Call to check if manual assumed reports exist failed with status: $status"
  }
}