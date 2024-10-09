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

package models.eacd.requests

import models.eacd.{EnrolmentDetails, Identifier}
import play.api.libs.json.{Json, Writes}

case class UpsertKnownFacts(verifierKey: String,
                            verifierValue: String,
                            identifier: Identifier) extends EnrolmentRequest

object UpsertKnownFacts {

  implicit val writes: Writes[UpsertKnownFacts] = (o: UpsertKnownFacts) => Json.obj(
    "verifiers" -> Json.arr(Json.obj(
      "key" -> o.verifierKey,
      "value" -> o.verifierValue
    ))
  )

  def apply(enrolmentDetails: EnrolmentDetails): UpsertKnownFacts = UpsertKnownFacts(
    verifierKey = enrolmentDetails.verifierKey,
    verifierValue = enrolmentDetails.verifierValue,
    identifier = enrolmentDetails.identifier,
  )
}
