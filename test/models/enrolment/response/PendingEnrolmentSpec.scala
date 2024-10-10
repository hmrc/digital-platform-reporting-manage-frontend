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

package models.enrolment.response

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class PendingEnrolmentSpec extends AnyFreeSpec
  with Matchers {

  private val validJson = Json.obj(
    "userId" -> "some-user-id",
    "providerId" -> "some-provider-id",
    "groupIdentifier" -> "some-group-identifier",
    "verifierKey" -> "some-verifier-key",
    "verifierValue" -> "some-verifier-value",
    "dprsId" -> "some-dprs-id"
  )

  private val validModel = PendingEnrolment(
    userId = "some-user-id",
    providerId = "some-provider-id",
    groupIdentifier = "some-group-identifier",
    verifierKey = "some-verifier-key",
    verifierValue = "some-verifier-value",
    dprsId = "some-dprs-id"
  )

  "PendingEnrolment.format" - {
    "parse from json" in {
      validJson.as[PendingEnrolment] mustBe validModel
    }

    "parse to json" in {
      Json.toJson(validModel) mustBe validJson
    }
  }
}
