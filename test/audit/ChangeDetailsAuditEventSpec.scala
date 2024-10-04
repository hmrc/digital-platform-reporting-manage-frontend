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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class ChangeDetailsAuditEventSpec extends AnyFreeSpec with Matchers {

  ".writes" - {

    "must write details for an individual" - {

      val baseContact = IndividualContact(Individual("first", "last"), "email", None)
      val baseInfo = SubscriptionInfo(
        id = "id",
        gbUser = true,
        tradingName = None,
        primaryContact = baseContact,
        secondaryContact = None
      )

      "when email has changed" in {

        val original = baseInfo
        val updated = baseInfo.copy(primaryContact = baseContact.copy(email = "new email"))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "email"
          ),
          "to" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "new email"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone` has from true to false" in {

        val original = baseInfo.copy(primaryContact = baseContact.copy(phone = Some("phone")))
        val updated = baseInfo

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "individual",
            "canContactIndividualByPhone" -> true,
            "individualPhoneNumber" -> "phone"
          ),
          "to" -> Json.obj(
            "userJourney" -> "individual",
            "canContactIndividualByPhone" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone` has from false to true" in {

        val original = baseInfo
        val updated = baseInfo.copy(primaryContact = baseContact.copy(phone = Some("phone")))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "individual",
            "canContactIndividualByPhone" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "individual",
            "canContactIndividualByPhone" -> true,
            "individualPhoneNumber" -> "phone"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }
    }

    "must write details for an organisation" - {

      val baseContact = OrganisationContact(Organisation("name"), "email", None)
      val baseContact2 = OrganisationContact(Organisation("name 2"), "email 2", None)
      val baseInfo = SubscriptionInfo(
        id = "id",
        gbUser = true,
        tradingName = None,
        primaryContact = baseContact,
        secondaryContact = None
      )

      "when basic details change" in {

        val original = baseInfo.copy(secondaryContact = Some(baseContact2))
        val updated = baseInfo.copy(
          primaryContact = baseContact.copy(organisation = Organisation("new name"), email = "new email"),
          secondaryContact = Some(baseContact2.copy(organisation = Organisation("new name 2"), email = "new email 2"))
        )

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "primaryContactName" -> "name",
            "primaryContactEmailAddress" -> "email",
            "secondaryContactName" -> "name 2",
            "secondaryContactEmailAddress" -> "email 2"
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "primaryContactName" -> "new name",
            "primaryContactEmailAddress" -> "new email",
            "secondaryContactName" -> "new name 2",
            "secondaryContactEmailAddress" -> "new email 2"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone primary contact` changes from true to false" in {

        val original = baseInfo.copy(primaryContact = baseContact.copy(phone = Some("phone")))
        val updated = baseInfo

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhonePrimaryContact" -> true,
            "primaryContactPhoneNumber" -> "phone"
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhonePrimaryContact" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone primary contact` changes from false to true" in {

        val original = baseInfo
        val updated = baseInfo.copy(primaryContact = baseContact.copy(phone = Some("phone")))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhonePrimaryContact" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhonePrimaryContact" -> true,
            "primaryContactPhoneNumber" -> "phone"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `has secondary contact` changes from true to false" in {

        val original = baseInfo.copy(secondaryContact = Some(baseContact2))
        val updated = baseInfo

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "hasSecondaryContact" -> true,
            "secondaryContactName" -> "name 2",
            "secondaryContactEmailAddress" -> "email 2",
            "canPhoneSecondaryContact" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "hasSecondaryContact" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `has secondary contact` changes from false to true" in {

        val original = baseInfo
        val updated = baseInfo.copy(secondaryContact = Some(baseContact2))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "hasSecondaryContact" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "hasSecondaryContact" -> true,
            "secondaryContactName" -> "name 2",
            "secondaryContactEmailAddress" -> "email 2",
            "canPhoneSecondaryContact" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone secondary contact` goes from true to false" in {

        val original = baseInfo.copy(secondaryContact = Some(baseContact2.copy(phone = Some("phone 2"))))
        val updated = baseInfo.copy(secondaryContact = Some(baseContact2))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhoneSecondaryContact" -> true,
            "secondaryContactPhoneNumber" -> "phone 2"
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhoneSecondaryContact" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "when `can phone secondary contact` goes from false to true" in {

        val original = baseInfo.copy(secondaryContact = Some(baseContact2))
        val updated = baseInfo.copy(secondaryContact = Some(baseContact2.copy(phone = Some("phone 2"))))

        val auditEvent = ChangeDetailsAuditEvent(original, updated)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhoneSecondaryContact" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "organisation",
            "canPhoneSecondaryContact" -> true,
            "secondaryContactPhoneNumber" -> "phone 2"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }
    }
  }
}
