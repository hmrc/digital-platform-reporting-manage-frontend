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

import models.subscription._
import play.api.libs.json.{JsObject, Json, OWrites}

final case class ChangeDetailsAuditEvent(original: SubscriptionInfo, updated: SubscriptionInfo) {

  val auditType = "ChangeContactDetails"
}

object ChangeDetailsAuditEvent {

  implicit lazy val writes: OWrites[ChangeDetailsAuditEvent] = new OWrites[ChangeDetailsAuditEvent] {
    override def writes(o: ChangeDetailsAuditEvent): JsObject = {

      val originalJson = toJson(o.original)
      val updatedJson = toJson(o.updated)

      val changedFieldsInOriginal = JsObject(originalJson.fieldSet.diff(updatedJson.fieldSet).toSeq)
      val changedFieldsInUpdated = JsObject(updatedJson.fieldSet.diff(originalJson.fieldSet).toSeq)

      val userJourneyJson = o.original.primaryContact match {
        case _: OrganisationContact => Json.obj("userJourney" -> "organisation")
        case _: IndividualContact => Json.obj("userJourney" -> "individual")
      }

      Json.obj(
        "from" -> (changedFieldsInOriginal ++ userJourneyJson),
        "to" -> (changedFieldsInUpdated ++ userJourneyJson)
      )
    }
  }

  private def toJson(info: SubscriptionInfo): JsObject =
    info.primaryContact match {
      case contact: IndividualContact =>

        val phoneJson = contact.phone
          .map(phone => Json.obj(
            "canContactIndividualByPhone" -> true,
            "individualPhoneNumber" -> phone
          ))
          .getOrElse(Json.obj("canContactIndividualByPhone" -> false))

        Json.obj(
          "individualEmailAddress" -> contact.email
        ) ++ phoneJson

      case contact: OrganisationContact =>

        val primaryPhoneJson = contact.phone
          .map(phone => Json.obj(
            "canPhonePrimaryContact" -> true,
            "primaryContactPhoneNumber" -> phone
          ))
          .getOrElse(Json.obj("canPhonePrimaryContact" -> false))

        val secondaryContactJson = info.secondaryContact.map { secondaryContact =>

          val phoneJson = secondaryContact.phone
            .map(phone => Json.obj(
              "canPhoneSecondaryContact" -> true,
              "secondaryContactPhoneNumber" -> phone
            ))
            .getOrElse(Json.obj("canPhoneSecondaryContact" -> false))

          Json.obj(
            "hasSecondaryContact" -> true,
            "secondaryContactName" -> secondaryContact.organisation.name,
            "secondaryContactEmailAddress" -> secondaryContact.email
          ) ++ phoneJson
        }.getOrElse(Json.obj("hasSecondaryContact" -> false))

        Json.obj(
          "primaryContactName" -> contact.organisation.name,
          "primaryContactEmailAddress" -> contact.email
        ) ++ primaryPhoneJson ++ secondaryContactJson
    }
}
