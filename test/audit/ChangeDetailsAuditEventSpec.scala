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

      "with minimal details" in {

        val info = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "email", None),
          secondaryContact = None
        )

        val request = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "new email", None),
          secondaryContact = None
        )

        val auditEvent = ChangeDetailsAuditEvent(info, request)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "email",
            "canContactIndividualByPhone" -> false
          ),
          "to" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "new email",
            "canContactIndividualByPhone" -> false
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }

      "with complete details" in {

        val info = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "email", Some("phone")),
          secondaryContact = None
        )

        val request = SubscriptionInfo(
          id = "id",
          gbUser = true,
          tradingName = None,
          primaryContact = IndividualContact(Individual("first", "last"), "new email", Some("new phone")),
          secondaryContact = None
        )

        val auditEvent = ChangeDetailsAuditEvent(info, request)
        val expectedJson = Json.obj(
          "from" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "email",
            "canContactIndividualByPhone" -> true,
            "individualPhoneNumber" -> "phone"
          ),
          "to" -> Json.obj(
            "userJourney" -> "individual",
            "individualEmailAddress" -> "new email",
            "canContactIndividualByPhone" -> true,
            "individualPhoneNumber" -> "new phone"
          )
        )

        Json.toJson(auditEvent) mustEqual expectedJson
      }
    }

    "must write details for an organisation" - {

      "with no secondary contact" - {

        "with minimal details" in {

          val info = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("name"), "email", None),
            secondaryContact = None
          )

          val request = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("new name"), "new email", None),
            secondaryContact = None
          )

          val auditEvent = ChangeDetailsAuditEvent(info, request)
          val expectedJson = Json.obj(
            "from" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "name",
              "primaryContactEmailAddress" -> "email",
              "canPhonePrimaryContact" -> false,
              "hasSecondaryContact" -> false
            ),
            "to" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "new name",
              "primaryContactEmailAddress" -> "new email",
              "canPhonePrimaryContact" -> false,
              "hasSecondaryContact" -> false
            )
          )

          Json.toJson(auditEvent) mustEqual expectedJson
        }

        "with complete details" in {

          val info = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("name"), "email", Some("phone")),
            secondaryContact = None
          )

          val request = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("new name"), "new email", Some("new phone")),
            secondaryContact = None
          )

          val auditEvent = ChangeDetailsAuditEvent(info, request)
          val expectedJson = Json.obj(
            "from" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "name",
              "primaryContactEmailAddress" -> "email",
              "canPhonePrimaryContact" -> true,
              "primaryContactPhoneNumber" -> "phone",
              "hasSecondaryContact" -> false
            ),
            "to" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "new name",
              "primaryContactEmailAddress" -> "new email",
              "canPhonePrimaryContact" -> true,
              "primaryContactPhoneNumber" -> "new phone",
              "hasSecondaryContact" -> false
            )
          )

          Json.toJson(auditEvent) mustEqual expectedJson
        }
      }

      "with a secondary contact" - {

        "with minimal details" in {

          val info = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("name"), "email", None),
            secondaryContact = Some(OrganisationContact(Organisation("name 2"), "email 2", None))
          )

          val request = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("new name"), "new email", None),
            secondaryContact = Some(OrganisationContact(Organisation("new name 2"), "new email 2", None))
          )

          val auditEvent = ChangeDetailsAuditEvent(info, request)
          val expectedJson = Json.obj(
            "from" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "name",
              "primaryContactEmailAddress" -> "email",
              "canPhonePrimaryContact" -> false,
              "hasSecondaryContact" -> true,
              "secondaryContactName" -> "name 2",
              "secondaryContactEmailAddress" -> "email 2",
              "canPhoneSecondaryContact" -> false,
            ),
            "to" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "new name",
              "primaryContactEmailAddress" -> "new email",
              "canPhonePrimaryContact" -> false,
              "hasSecondaryContact" -> true,
              "secondaryContactName" -> "new name 2",
              "secondaryContactEmailAddress" -> "new email 2",
              "canPhoneSecondaryContact" -> false,
            )
          )

          Json.toJson(auditEvent) mustEqual expectedJson
        }

        "with complete details" in {

          val info = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("name"), "email", Some("phone")),
            secondaryContact = Some(OrganisationContact(Organisation("name 2"), "email 2", Some("phone 2")))
          )

          val request = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(Organisation("new name"), "new email", Some("new phone")),
            secondaryContact = Some(OrganisationContact(Organisation("new name 2"), "new email 2", Some("new phone 2")))
          )

          val auditEvent = ChangeDetailsAuditEvent(info, request)
          val expectedJson = Json.obj(
            "from" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "name",
              "primaryContactEmailAddress" -> "email",
              "canPhonePrimaryContact" -> true,
              "primaryContactPhoneNumber" -> "phone",
              "hasSecondaryContact" -> true,
              "secondaryContactName" -> "name 2",
              "secondaryContactEmailAddress" -> "email 2",
              "canPhoneSecondaryContact" -> true,
              "secondaryContactPhoneNumber" -> "phone 2"
            ),
            "to" -> Json.obj(
              "userJourney" -> "organisation",
              "primaryContactName" -> "new name",
              "primaryContactEmailAddress" -> "new email",
              "canPhonePrimaryContact" -> true,
              "primaryContactPhoneNumber" -> "new phone",
              "hasSecondaryContact" -> true,
              "secondaryContactName" -> "new name 2",
              "secondaryContactEmailAddress" -> "new email 2",
              "canPhoneSecondaryContact" -> true,
              "secondaryContactPhoneNumber" -> "new phone 2"
            )
          )

          Json.toJson(auditEvent) mustEqual expectedJson

        }
      }
    }
  }
}
