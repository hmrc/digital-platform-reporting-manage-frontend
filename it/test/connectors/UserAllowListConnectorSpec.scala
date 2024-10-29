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

import connectors.UserAllowListConnector.{CheckRequest, UnexpectedResponseException}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.test.WireMockSupport
import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.http.HeaderCarrier

class UserAllowListConnectorSpec extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with WireMockSupport
  with GuiceOneAppPerSuite {

  implicit protected lazy val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("microservice.services.user-allow-list.port" -> wireMockPort)
    .build()

  private lazy val connector = app.injector.instanceOf[UserAllowListConnector]

  ".check" - {

    "must return true when the server returns OK" in {

      val checkRequest = CheckRequest("value")

      wireMockServer.stubFor(
        post(urlMatching("/user-allow-list/digital-platform-reporting/feature-name/check"))
          .withRequestBody(equalTo(Json.toJson(checkRequest).toString))
          .willReturn(ok())
      )

      val result = connector.check("feature-name", "value").futureValue
      result mustEqual true
    }

    "must return false when the server returns NOT_FOUND" in {

      val checkRequest = CheckRequest("value")

      wireMockServer.stubFor(
        post(urlMatching("/user-allow-list/digital-platform-reporting/feature-name/check"))
          .withRequestBody(equalTo(Json.toJson(checkRequest).toString))
          .willReturn(notFound())
      )

      val result = connector.check("feature-name", "value").futureValue
      result mustEqual false
    }

    "must return a failed future when the server returns an error" in {

      val checkRequest = CheckRequest("value")

      wireMockServer.stubFor(
        post(urlMatching("/user-allow-list/digital-platform-reporting/feature-name/check"))
          .withRequestBody(equalTo(Json.toJson(checkRequest).toString))
          .willReturn(serverError())
      )

      val result = connector.check("feature-name", "value").failed.futureValue
      result mustBe an[UnexpectedResponseException]

      val failure = result.asInstanceOf[UnexpectedResponseException]
      failure.status mustEqual 500
    }
  }
}
