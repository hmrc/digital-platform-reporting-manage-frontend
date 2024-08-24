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

import cats.data.{EitherNec, StateT}
import cats.implicits._
import models.UserAnswers
import models.requests.subscription.requests.SubscriptionRequest
import models.requests.subscription.responses.SubscriptionInfo
import models.requests.subscription.{Contact, IndividualContact, Organisation, OrganisationContact}
import pages._
import play.api.libs.json.Writes
import queries.{GbUserQuery, IndividualQuery, Query, Settable, TradingNameQuery}
import services.UserAnswersService.InvalidSecondaryContact

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Try}

@Singleton
class UserAnswersService @Inject() {

  def fromSubscription(userId: String, subscription: SubscriptionInfo): Try[UserAnswers] = {

    val transformation = for {
      _ <- setOptional(TradingNameQuery, subscription.tradingName)
      _ <- set(GbUserQuery, subscription.gbUser)
      _ <- setContacts(subscription.primaryContact, subscription.secondaryContact)
    } yield ()

    transformation.runS(UserAnswers(userId))
  }

  private def setContacts(primary: Contact, secondary: Option[Contact]): StateT[Try, UserAnswers, Unit] =
    primary match {
      case contact: OrganisationContact =>
        for {
          _ <- setPrimaryContact(contact)
          _ <- set(HasSecondaryContactPage, secondary.isDefined)
          _ <- setSecondaryContact(secondary)
        } yield ()
      case contact: IndividualContact =>
        setIndividualContact(contact)
    }

  private def setPrimaryContact(contact: OrganisationContact): StateT[Try, UserAnswers, Unit] =
    for {
      _ <- set(PrimaryContactNamePage, contact.organisation.name)
      _ <- set(PrimaryContactEmailAddressPage, contact.email)
      _ <- set(CanPhonePrimaryContactPage, contact.phone.isDefined)
      _ <- setOptional(PrimaryContactPhoneNumberPage, contact.phone)
    } yield ()

  private def setSecondaryContact(optionalContact: Option[Contact]): StateT[Try, UserAnswers, Unit] = {
    optionalContact.map {
      case contact: OrganisationContact =>
        for {
          _ <- set(SecondaryContactNamePage, contact.organisation.name)
          _ <- set(SecondaryContactEmailAddressPage, contact.email)
          _ <- set(CanPhoneSecondaryContactPage, contact.phone.isDefined)
          _ <- setOptional(SecondaryContactPhoneNumberPage, contact.phone)
        } yield ()
      case _ => StateT.liftF[Try, UserAnswers, Unit](Failure(InvalidSecondaryContact))
    }.getOrElse(StateT.pure(()))
  }

  private def setIndividualContact(contact: IndividualContact): StateT[Try, UserAnswers, Unit] =
    for {
      _ <- set(IndividualQuery, contact.individual)
      _ <- set(IndividualEmailAddressPage, contact.email)
      _ <- set(CanPhoneIndividualPage, contact.phone.isDefined)
      _ <- setOptional(IndividualPhoneNumberPage, contact.phone)
      _ <- set(HasSecondaryContactPage, false)
    } yield ()

  private def set[A](settable: Settable[A], value: A)(implicit writes: Writes[A]): StateT[Try, UserAnswers, Unit] =
    StateT.modifyF[Try, UserAnswers](_.set(settable, value))

  private def setOptional[A](settable: Settable[A], optionalValue: Option[A])(implicit writes: Writes[A]): StateT[Try, UserAnswers, Unit] =
    optionalValue.map { value =>
      set(settable, value)
    }.getOrElse(StateT.pure(()))

  def toSubscriptionRequest(answers: UserAnswers, dprsId: String): EitherNec[Query, SubscriptionRequest] =
    (
      answers.getEither(GbUserQuery),
      getPrimaryContact(answers),
      getSecondaryContact(answers)
    ).parMapN { (gbUser, primaryContact, secondaryContact) =>
      SubscriptionRequest(dprsId, gbUser, answers.get(TradingNameQuery), primaryContact, secondaryContact)
    }

  private def getPrimaryContact(answers: UserAnswers): EitherNec[Query, Contact] =
    answers.get(IndividualQuery).map { individual =>
      (
        answers.getEither(IndividualEmailAddressPage),
        getIndividualPhoneNumber(answers)
      ).parMapN { (email, phone) =>
        IndividualContact(individual, email, phone)
      }
    }.getOrElse {
      (
        answers.getEither(PrimaryContactNamePage),
        answers.getEither(PrimaryContactEmailAddressPage),
        getPrimaryContactPhoneNumber(answers)
      ).parMapN { (name, email, phone) =>
        OrganisationContact(Organisation(name), email, phone)
      }
    }

  private def getSecondaryContact(answers: UserAnswers): EitherNec[Query, Option[Contact]] =
    answers.get(IndividualQuery).map { _ =>
      Right(None)
    }.getOrElse {
      answers.getEither(HasSecondaryContactPage).flatMap {
        case false => Right(None)
        case true =>
          (
            answers.getEither(SecondaryContactNamePage),
            answers.getEither(SecondaryContactEmailAddressPage),
            getSecondaryContactPhoneNumber(answers)
          ).parMapN { (name, email, phone) =>
            Some(OrganisationContact(Organisation(name), email, phone))
          }
      }
    }

  private def getIndividualPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhoneIndividualPage).flatMap {
      case true  => answers.getEither(IndividualPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }

  private def getPrimaryContactPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhonePrimaryContactPage).flatMap {
      case true  => answers.getEither(PrimaryContactPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }

  private def getSecondaryContactPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhoneSecondaryContactPage).flatMap {
      case true  => answers.getEither(SecondaryContactPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }
}

object UserAnswersService {

  final case object InvalidSecondaryContact extends Throwable {
    override def getMessage: String = "Provided secondary contact is an individual contact"
  }
}
