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

import builders.GroupEnrolmentBuilder.aGroupEnrolment
import builders.UpsertKnownFactsBuilder.anUpsertKnownFacts
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.TaxEnrolmentConnector.{AllocateEnrolmentToGroupTaxEnrolmentFailure, UpsertTaxEnrolmentFailure}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

class TaxEnrolmentConnectorSpec extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with WireMockSupport
  with GuiceOneAppPerSuite {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("microservice.services.tax-enrolments.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[TaxEnrolmentConnector]

  ".allocateEnrolmentToGroup" - {
    "must succeed when the server returns CREATED" in {
      wireMockServer.stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
        .willReturn(created()))

      underTest.allocateEnrolmentToGroup(aGroupEnrolment).futureValue
    }

    "must succeed when the server returns CONFLICT" in {
      wireMockServer.stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
        .willReturn(aResponse.withStatus(409)))

      underTest.allocateEnrolmentToGroup(aGroupEnrolment).futureValue
    }

    "must return a failed future when the server returns an error" in {
      wireMockServer
        .stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
          .willReturn(serverError()))

      val result = underTest.allocateEnrolmentToGroup(aGroupEnrolment).failed.futureValue
      result.asInstanceOf[AllocateEnrolmentToGroupTaxEnrolmentFailure].status mustBe 500
    }
  }

  ".upsert" - {
    "must succeed when the server returns NO_CONTENT" in {
      wireMockServer
        .stubFor(put(urlMatching(s"/tax-enrolments/enrolments/${anUpsertKnownFacts.enrolmentKey}"))
          .willReturn(noContent()))

      underTest.upsert(anUpsertKnownFacts).futureValue
    }

    "must return a failed future when the server returns an error" in {
      wireMockServer
        .stubFor(put(urlMatching(s"/tax-enrolments/enrolments/${anUpsertKnownFacts.enrolmentKey}"))
          .willReturn(badRequest()))

      val result = underTest.upsert(anUpsertKnownFacts).failed.futureValue
      result.asInstanceOf[UpsertTaxEnrolmentFailure].status mustBe 400
    }
  }
}