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

package services

import models.requests.subscription.responses.SubscriptionInfo
import models.requests.subscription.{Individual, IndividualContact, Organisation, OrganisationContact}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages._
import queries.{GbUserQuery, IndividualQuery, TradingNameQuery}

class UserAnswersServiceSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite with TryValues with OptionValues {

  private lazy val userAnswersService = app.injector.instanceOf[UserAnswersService]

  "fromSubscription" - {

    "must return a UserAnswers populated from the subscription" - {

      "when the subscription is for an organisation" - {

        "when all optional fields are present" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = Some("tradingName"),
            primaryContact = OrganisationContact(
              organisation = Organisation("primaryContactName"),
              email = "primaryEmail",
              phone = Some("primaryPhone")
            ),
            secondaryContact = Some(
              OrganisationContact(
                organisation = Organisation("secondaryContactName"),
                email = "secondaryEmail",
                phone = Some("secondaryPhone")
              )
            )
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(GbUserQuery).value mustBe true
          result.get(TradingNameQuery).value mustEqual "tradingName"

          result.get(PrimaryContactNamePage).value mustEqual "primaryContactName"
          result.get(PrimaryContactEmailAddressPage).value mustEqual "primaryEmail"
          result.get(CanPhonePrimaryContactPage).value mustBe true
          result.get(PrimaryContactPhoneNumberPage).value mustBe "primaryPhone"

          result.get(HasSecondaryContactPage).value mustBe true
          result.get(SecondaryContactNamePage).value mustEqual "secondaryContactName"
          result.get(SecondaryContactEmailAddressPage).value mustEqual "secondaryEmail"
          result.get(CanPhoneSecondaryContactPage).value mustBe true
          result.get(SecondaryContactPhoneNumberPage).value mustBe "secondaryPhone"
        }

        "when there is no secondary contact" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = Some("tradingName"),
            primaryContact = OrganisationContact(
              organisation = Organisation("primaryContactName"),
              email = "primaryEmail",
              phone = Some("primaryPhone")
            ),
            secondaryContact = None
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(GbUserQuery).value mustBe true
          result.get(TradingNameQuery).value mustEqual "tradingName"

          result.get(PrimaryContactNamePage).value mustEqual "primaryContactName"
          result.get(PrimaryContactEmailAddressPage).value mustEqual "primaryEmail"
          result.get(CanPhonePrimaryContactPage).value mustBe true
          result.get(PrimaryContactPhoneNumberPage).value mustBe "primaryPhone"

          result.get(HasSecondaryContactPage).value mustBe false
          result.get(SecondaryContactNamePage) mustBe None
          result.get(SecondaryContactEmailAddressPage) mustBe None
          result.get(CanPhoneSecondaryContactPage) mustBe None
          result.get(SecondaryContactPhoneNumberPage) mustBe None
        }

        "when the contacts have no phone numbers" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = Some("tradingName"),
            primaryContact = OrganisationContact(
              organisation = Organisation("primaryContactName"),
              email = "primaryEmail",
              phone = None
            ),
            secondaryContact = Some(
              OrganisationContact(
                organisation = Organisation("secondaryContactName"),
                email = "secondaryEmail",
                phone = None
              )
            )
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(TradingNameQuery).value mustEqual "tradingName"

          result.get(PrimaryContactNamePage).value mustEqual "primaryContactName"
          result.get(PrimaryContactEmailAddressPage).value mustEqual "primaryEmail"
          result.get(CanPhonePrimaryContactPage).value mustBe false
          result.get(PrimaryContactPhoneNumberPage) mustBe None

          result.get(HasSecondaryContactPage).value mustBe true
          result.get(SecondaryContactNamePage).value mustEqual "secondaryContactName"
          result.get(SecondaryContactEmailAddressPage).value mustEqual "secondaryEmail"
          result.get(CanPhoneSecondaryContactPage).value mustBe false
          result.get(SecondaryContactPhoneNumberPage) mustBe None
        }

        "when there is no trading name" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = OrganisationContact(
              organisation = Organisation("primaryContactName"),
              email = "primaryEmail",
              phone = None
            ),
            secondaryContact = None
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(TradingNameQuery) mustBe None
        }
      }

      "when the subscription is for an individual" - {

        "when all optional fields are present" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = Some("tradingName"),
            primaryContact = IndividualContact(
              individual = Individual("first", "second"),
              email = "primaryEmail",
              phone = Some("primaryPhone")
            ),
            secondaryContact = None
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(GbUserQuery).value mustBe true
          result.get(TradingNameQuery).value mustEqual "tradingName"

          result.get(IndividualQuery).value mustEqual Individual("first", "second")
          result.get(IndividualEmailAddressPage).value mustEqual "primaryEmail"
          result.get(CanPhoneIndividualPage).value mustBe true
          result.get(IndividualPhoneNumberPage).value mustBe "primaryPhone"

          result.get(HasSecondaryContactPage).value mustBe false
        }

        "when the contact has no phone number" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = Some("tradingName"),
            primaryContact = IndividualContact(
              individual = Individual("first", "second"),
              email = "primaryEmail",
              phone = None
            ),
            secondaryContact = None
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.id mustEqual "id"
          result.get(GbUserQuery).value mustBe true
          result.get(TradingNameQuery).value mustEqual "tradingName"

          result.get(IndividualQuery).value mustEqual Individual("first", "second")
          result.get(IndividualEmailAddressPage).value mustEqual "primaryEmail"
          result.get(CanPhoneIndividualPage).value mustBe false
          result.get(IndividualPhoneNumberPage) mustBe None

          result.get(HasSecondaryContactPage).value mustBe false
        }

        "when there is no trading name" in {

          val subscription = SubscriptionInfo(
            id = "id",
            gbUser = true,
            tradingName = None,
            primaryContact = IndividualContact(
              individual = Individual("first", "second"),
              email = "primaryEmail",
              phone = Some("primaryPhone")
            ),
            secondaryContact = None
          )

          val result = userAnswersService.fromSubscription("id", subscription).success.value

          result.get(TradingNameQuery) mustBe None
        }
      }
    }

    "must fail when the secondary contact is an individual contact" in {

      val subscription = SubscriptionInfo(
        id = "id",
        gbUser = true,
        tradingName = Some("tradingName"),
        primaryContact = OrganisationContact(
          organisation = Organisation("primaryContactName"),
          email = "primaryEmail",
          phone = Some("primaryPhone")
        ),
        secondaryContact = Some(
          IndividualContact(
            individual = Individual("first", "last"),
            email = "secondaryEmail",
            phone = Some("secondaryPhone")
          )
        )
      )

      userAnswersService.fromSubscription("id", subscription).failed.success.value
    }
  }

  "toSubscription" - {

  }
}
