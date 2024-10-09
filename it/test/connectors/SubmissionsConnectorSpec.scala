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

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.SubmissionsConnector.SubmissionsExistFailure
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

class SubmissionsConnectorSpec extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with WireMockSupport
  with MockitoSugar
  with BeforeAndAfterEach
  with EitherValues {

  private lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
    .build()

  private lazy val connector = app.injector.instanceOf[SubmissionsConnector]

  private implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("authToken")))

  "submissionsExist" - {

    "must return true when the server returns OK" in {

      wireMockServer.stubFor(
        post(urlPathEqualTo("/digital-platform-reporting/submission/list"))
          .withHeader("Authorization", equalTo("authToken"))
          .willReturn(ok())
      )

      val result = connector.submissionsExist(assumedReporting = true).futureValue
      result mustEqual true
    }

    "must return false when the server returns NOT_FOUND" in {

      wireMockServer.stubFor(
        post(urlPathEqualTo("/digital-platform-reporting/submission/list"))
          .withHeader("Authorization", equalTo("authToken"))
          .willReturn(notFound())
      )

      val result = connector.submissionsExist(assumedReporting = true).futureValue
      result mustEqual false
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        post(urlPathEqualTo("/digital-platform-reporting/submission/list"))
          .withHeader("Authorization", equalTo("authToken"))
          .willReturn(serverError())
      )

      val result = connector.submissionsExist(assumedReporting = true).failed.futureValue
      result mustBe a[SubmissionsExistFailure]

      val failure = result.asInstanceOf[SubmissionsExistFailure]
      failure.status mustEqual INTERNAL_SERVER_ERROR
    }
  }
}
