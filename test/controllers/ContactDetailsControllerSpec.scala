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

package controllers

import base.SpecBase
import connectors.SubscriptionConnector
import models.requests.subscription.responses.SubscriptionInfo
import models.requests.subscription.{Individual, IndividualContact, Organisation, OrganisationContact}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.govuk.SummaryListFluency
import viewmodels.{ContactDetailsIndividualViewModel, ContactDetailsOrganisationViewModel}
import views.html.{ContactDetailsIndividualView, ContactDetailsOrganisationView}

import scala.concurrent.Future

class ContactDetailsControllerSpec
  extends SpecBase
    with SummaryListFluency
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockConnector = mock[SubscriptionConnector]
  private val mockRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector, mockRepository)
    super.beforeEach()
  }

  "Check Your Answers Controller" - {

    "must get subscription information, save it to user answers, and show the correct view" - {

      "for an individual" in {

        val contact = IndividualContact(Individual("first", "last"), "email", Some("phone"))
        val subscriptionInfo = SubscriptionInfo("id", gbUser = true, None, contact, None)

        when(mockConnector.getSubscription(any())) thenReturn Future.successful(subscriptionInfo)
        when(mockRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[SubscriptionConnector].toInstance(mockConnector),
              bind[SessionRepository].toInstance(mockRepository)
            )
            .build

        running(application) {
          val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ContactDetailsIndividualView]
          val userAnswersService = application.injector.instanceOf[UserAnswersService]
          val userAnswers = userAnswersService.fromSubscription("id", subscriptionInfo).success.value
          implicit val msgs: Messages = messages(application)

          val viewModel = ContactDetailsIndividualViewModel(userAnswers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, implicitly).toString

          verify(mockConnector, times(1)).getSubscription(any())
          verify(mockRepository, times(1)).set(any())
        }
      }

      "for an organisation" in {

        val primaryContact = OrganisationContact(Organisation("name"), "email", Some("phone"))
        val secondaryContact = OrganisationContact(Organisation("second name"), "second email", Some("second phone"))
        val subscriptionInfo = SubscriptionInfo("id", gbUser = true, None, primaryContact, Some(secondaryContact))

        when(mockConnector.getSubscription(any())) thenReturn Future.successful(subscriptionInfo)
        when(mockRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[SubscriptionConnector].toInstance(mockConnector),
              bind[SessionRepository].toInstance(mockRepository)
            )
            .build

        running(application) {
          val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ContactDetailsOrganisationView]
          val userAnswersService = application.injector.instanceOf[UserAnswersService]
          val userAnswers = userAnswersService.fromSubscription("id", subscriptionInfo).success.value
          implicit val msgs: Messages = messages(application)

          val viewModel = ContactDetailsOrganisationViewModel(userAnswers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, implicitly).toString

          verify(mockConnector, times(1)).getSubscription(any())
          verify(mockRepository, times(1)).set(any())
        }
      }
    }

    "must return a failed future when subscription information cannot be retrieved" in {

      when(mockConnector.getSubscription(any())) thenReturn Future.failed(new Exception("foo"))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SubscriptionConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockRepository)
          )
          .build

      running(application) {
        val request = FakeRequest(GET, routes.ContactDetailsController.onPageLoad().url)

        route(application, request).value.failed.futureValue
      }
    }
  }
}
