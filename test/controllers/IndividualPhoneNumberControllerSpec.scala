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

import audit.{AuditService, ChangeDetailsAuditEvent}
import base.SpecBase
import connectors.SubscriptionConnector
import forms.IndividualPhoneNumberFormProvider
import models.UserAnswers
import models.subscription._
import navigation.{FakeNavigator, Navigator}
import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CanPhoneIndividualPage, IndividualEmailAddressPage, IndividualPhoneNumberPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.{GbUserQuery, IndividualQuery, OriginalSubscriptionInfoQuery}
import repositories.SessionRepository
import views.html.IndividualPhoneNumberView

import scala.concurrent.Future

class IndividualPhoneNumberControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/foo")

  private val formProvider = new IndividualPhoneNumberFormProvider()
  private val form = formProvider()

  private lazy val individualPhoneNumberRoute = routes.IndividualPhoneNumberController.onPageLoad.url

  "IndividualPhoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualPhoneNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndividualPhoneNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IndividualPhoneNumberPage, "07777 777777").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, individualPhoneNumberRoute)

        val view = application.injector.instanceOf[IndividualPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("07777 777777"))(request, messages(application)).toString
      }
    }

    "must update the subscription, save user answers, audit the event, and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[SubscriptionConnector]
      val mockAuditService = mock[AuditService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockConnector.updateSubscription(any())(any())) thenReturn Future.successful(Done)

      val originalContact = IndividualContact(Individual("first", "last"), "foo@example.com", Some("07777 777777"))
      val originalInfo = SubscriptionInfo("dprsId", gbUser = true, None, originalContact, None)

      val answers =
        emptyUserAnswers
          .set(GbUserQuery, true).success.value
          .set(IndividualQuery, Individual("first", "last")).success.value
          .set(IndividualEmailAddressPage, "foo@example.com").success.value
          .set(CanPhoneIndividualPage, true).success.value
          .set(IndividualPhoneNumberPage, "07777 777777").success.value
          .set(OriginalSubscriptionInfoQuery, originalInfo).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionConnector].toInstance(mockConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "07777 888888"))

        val expectedContact = IndividualContact(Individual("first", "last"), "foo@example.com", Some("07777 888888"))
        val expectedRequest = SubscriptionInfo("dprsId", gbUser = true, None, expectedContact, None)
        val expectedAuditEvent = ChangeDetailsAuditEvent(originalInfo, expectedRequest)
        val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockConnector, times(1)).updateSubscription(eqTo(expectedRequest))(any())
        verify(mockSessionRepository, times(1)).set(answersCaptor.capture())
        verify(mockAuditService, times(1)).sendAudit(eqTo(expectedAuditEvent))(any())

        val savedAnswers = answersCaptor.getValue
        savedAnswers.get(IndividualPhoneNumberPage).value mustEqual "07777 888888"
      }
    }

    "must return a failed future and not save user answers or audit the event when valid data is submitted but the update fails" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[SubscriptionConnector]
      val mockAuditService = mock[AuditService]

      when(mockConnector.updateSubscription(any())(any())) thenReturn Future.failed(new Exception("foo"))

      val originalContact = IndividualContact(Individual("first", "last"), "foo@example.com", Some("07777 777777"))
      val originalInfo = SubscriptionInfo("dprsId", gbUser = true, None, originalContact, None)

      val answers =
        emptyUserAnswers
          .set(GbUserQuery, true).success.value
          .set(IndividualQuery, Individual("first", "last")).success.value
          .set(IndividualEmailAddressPage, "foo@example.com").success.value
          .set(CanPhoneIndividualPage, true).success.value
          .set(IndividualPhoneNumberPage, "07777 777777").success.value
          .set(OriginalSubscriptionInfoQuery, originalInfo).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[SubscriptionConnector].toInstance(mockConnector),
            bind[AuditService].toInstance(mockAuditService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, individualPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "07777 888888"))

        val expectedContact = IndividualContact(Individual("first", "last"), "foo@example.com", Some("07777 888888"))
        val expectedRequest = SubscriptionInfo("dprsId", gbUser = true, None, expectedContact, None)
        route(application, request).value.failed.futureValue

        verify(mockConnector, times(1)).updateSubscription(eqTo(expectedRequest))(any())
        verify(mockSessionRepository, never()).set(any())
        verify(mockAuditService, never()).sendAudit(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, individualPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IndividualPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, individualPhoneNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, individualPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "07777 777777"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
