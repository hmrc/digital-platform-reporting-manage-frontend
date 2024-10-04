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

import models.UserAnswers
import models.subscription._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pages._
import queries.{GbUserQuery, IndividualQuery, TradingNameQuery}

class UserAnswersServiceSpec
  extends AnyFreeSpec
    with Matchers
    with GuiceOneAppPerSuite
    with TryValues
    with OptionValues
    with EitherValues {

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
  }

  "toSubscription" - {

    "must create a subscription request" - {

      "for an organisation" - {

        "when there is a second contact" - {

          "and phone numbers are present" in {
            
            val answers =
              UserAnswers("id")
                .set(GbUserQuery, true).success.value
                .set(PrimaryContactNamePage, "primary name").success.value
                .set(PrimaryContactEmailAddressPage, "primary email").success.value
                .set(CanPhonePrimaryContactPage, true).success.value
                .set(PrimaryContactPhoneNumberPage, "primary phone").success.value
                .set(HasSecondaryContactPage, true).success.value
                .set(SecondaryContactNamePage, "secondary name").success.value
                .set(SecondaryContactEmailAddressPage, "secondary email").success.value
                .set(CanPhoneSecondaryContactPage, true).success.value
                .set(SecondaryContactPhoneNumberPage, "secondary phone").success.value

            val expectedResult = SubscriptionInfo(
              "dprsId",
              true,
              None,
              OrganisationContact(Organisation("primary name"), "primary email", Some("primary phone")),
              Some(OrganisationContact(Organisation("secondary name"), "secondary email", Some("secondary phone")))
            )

            val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

            result.value mustEqual expectedResult
          }

          "and phone numbers are absent" in {

            val answers =
              UserAnswers("id")
                .set(GbUserQuery, true).success.value
                .set(PrimaryContactNamePage, "primary name").success.value
                .set(PrimaryContactEmailAddressPage, "primary email").success.value
                .set(CanPhonePrimaryContactPage, false).success.value
                .set(HasSecondaryContactPage, true).success.value
                .set(SecondaryContactNamePage, "secondary name").success.value
                .set(SecondaryContactEmailAddressPage, "secondary email").success.value
                .set(CanPhoneSecondaryContactPage, false).success.value

            val expectedResult = SubscriptionInfo(
              "dprsId",
              true,
              None,
              OrganisationContact(Organisation("primary name"), "primary email", None),
              Some(OrganisationContact(Organisation("secondary name"), "secondary email", None))
            )

            val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

            result.value mustEqual expectedResult
          }
        }

        "when there is no second contact" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(PrimaryContactNamePage, "primary name").success.value
              .set(PrimaryContactEmailAddressPage, "primary email").success.value
              .set(CanPhonePrimaryContactPage, true).success.value
              .set(PrimaryContactPhoneNumberPage, "primary phone").success.value
              .set(HasSecondaryContactPage, false).success.value

          val expectedResult = SubscriptionInfo(
            "dprsId",
            true,
            None,
            OrganisationContact(Organisation("primary name"), "primary email", Some("primary phone")),
            None
          )

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.value mustEqual expectedResult
        }

        "when there is a trading name" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(TradingNameQuery, "trading name").success.value
              .set(PrimaryContactNamePage, "primary name").success.value
              .set(PrimaryContactEmailAddressPage, "primary email").success.value
              .set(CanPhonePrimaryContactPage, true).success.value
              .set(PrimaryContactPhoneNumberPage, "primary phone").success.value
              .set(HasSecondaryContactPage, false).success.value

          val expectedResult = SubscriptionInfo(
            "dprsId",
            true,
            Some("trading name"),
            OrganisationContact(Organisation("primary name"), "primary email", Some("primary phone")),
            None
          )

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.value mustEqual expectedResult
        }
      }

      "for an individual" - {

        "when optional fields are present" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(IndividualQuery, Individual("first", "last")).success.value
              .set(IndividualEmailAddressPage, "email").success.value
              .set(CanPhoneIndividualPage, true).success.value
              .set(IndividualPhoneNumberPage, "phone").success.value

          val expectedResult = SubscriptionInfo(
            "dprsId",
            true,
            None,
            IndividualContact(Individual("first", "last"), "email", Some("phone")),
            None
          )

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.value mustEqual expectedResult
        }

        "when optional fields are absent" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(IndividualQuery, Individual("first", "last")).success.value
              .set(IndividualEmailAddressPage, "email").success.value
              .set(CanPhoneIndividualPage, false).success.value

          val expectedResult = SubscriptionInfo(
            "dprsId",
            true,
            None,
            IndividualContact(Individual("first", "last"), "email", None),
            None
          )

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.value mustEqual expectedResult
        }
      }
    }

    "must fail to build" - {

      "for an individual" - {

        "when mandatory fields are missing" in {

          val answers = UserAnswers("id").set(IndividualQuery, Individual("first", "last")).success.value

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain theSameElementsAs Seq(
            GbUserQuery, IndividualEmailAddressPage, CanPhoneIndividualPage
          )
        }

        "when we can phone the user but phone number is missing" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, false).success.value
              .set(IndividualQuery, Individual("first", "last")).success.value
              .set(IndividualEmailAddressPage, "email").success.value
              .set(CanPhoneIndividualPage, true).success.value

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain only IndividualPhoneNumberPage
        }
      }

      "for an organisation" - {

        "when mandatory fields are missing" in {

          val answers = UserAnswers("id")

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain theSameElementsAs Seq(
            GbUserQuery,
            PrimaryContactNamePage,
            PrimaryContactEmailAddressPage,
            CanPhonePrimaryContactPage,
            HasSecondaryContactPage
          )
        }

        "when we can phone the primary contact but the phone number is missing" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(PrimaryContactNamePage, "primary name").success.value
              .set(PrimaryContactEmailAddressPage, "primary email").success.value
              .set(CanPhonePrimaryContactPage, true).success.value
              .set(HasSecondaryContactPage, false).success.value

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain only PrimaryContactPhoneNumberPage
        }

        "when there is a secondary contact but their details are missing" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(PrimaryContactNamePage, "primary name").success.value
              .set(PrimaryContactEmailAddressPage, "primary email").success.value
              .set(CanPhonePrimaryContactPage, false).success.value
              .set(HasSecondaryContactPage, true).success.value

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain theSameElementsAs Seq(
            SecondaryContactNamePage, SecondaryContactEmailAddressPage, CanPhoneSecondaryContactPage
          )
        }

        "when we can phone the secondary contact but the phone number is missing" in {

          val answers =
            UserAnswers("id")
              .set(GbUserQuery, true).success.value
              .set(PrimaryContactNamePage, "primary name").success.value
              .set(PrimaryContactEmailAddressPage, "primary email").success.value
              .set(CanPhonePrimaryContactPage, false).success.value
              .set(HasSecondaryContactPage, true).success.value
              .set(SecondaryContactNamePage, "secondary name").success.value
              .set(SecondaryContactEmailAddressPage, "secondary email").success.value
              .set(CanPhoneSecondaryContactPage, true).success.value

          val result = userAnswersService.toSubscriptionInfo(answers, "dprsId")

          result.left.value.toChain.toList must contain only SecondaryContactPhoneNumberPage
        }
      }
    }
  }
}
