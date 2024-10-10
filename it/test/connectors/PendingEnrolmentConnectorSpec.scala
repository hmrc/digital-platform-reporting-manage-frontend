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

import builders.PendingEnrolmentBuilder.aPendingEnrolment
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.PendingEnrolmentConnector.{GetPendingEnrolmentFailure, RemovePendingEnrolmentFailure}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import java.time.Instant

class PendingEnrolmentConnectorSpec extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with WireMockSupport
  with GuiceOneAppPerSuite {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[PendingEnrolmentConnector]

  ".getPendingEnrolment" - {
    "must succeed when the server returns OK" in {
      val responsePayload = Json.toJsObject(aPendingEnrolment) + ("created" -> Json.toJson(Instant.now()))

      wireMockServer.stubFor(get(urlMatching("/digital-platform-reporting/pending-enrolment"))
        .willReturn(ok(responsePayload.toString())))

      underTest.getPendingEnrolment().futureValue mustBe aPendingEnrolment
    }

    "must return GetPendingEnrolmentFailure when the server returns error" in {
      wireMockServer.stubFor(get(urlMatching("/digital-platform-reporting/pending-enrolment"))
        .willReturn(serverError()))

      val result = underTest.getPendingEnrolment().failed.futureValue
      result.asInstanceOf[GetPendingEnrolmentFailure].status mustBe 500
    }
  }

  ".remove" - {
    "must succeed when the server returns OK" in {
      wireMockServer.stubFor(delete(urlMatching("/digital-platform-reporting/pending-enrolment"))
        .willReturn(ok()))

      underTest.remove().futureValue
    }

    "must return RemovePendingEnrolmentFailure when the server returns error" in {
      wireMockServer.stubFor(delete(urlMatching("/digital-platform-reporting/pending-enrolment"))
        .willReturn(serverError()))

      val result = underTest.remove().failed.futureValue
      result.asInstanceOf[RemovePendingEnrolmentFailure].status mustBe 500
    }
  }
}