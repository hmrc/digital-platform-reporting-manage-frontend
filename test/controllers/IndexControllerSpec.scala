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
import connectors.PlatformOperatorConnector
import models.operator.{AddressDetails, ContactDetails, NotificationType}
import models.operator.responses.{NotificationDetails, PlatformOperator, ViewPlatformOperatorsResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.{CardState, IndexViewModel}
import views.html.IndexView

import java.time.Instant
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockConnector = mock[PlatformOperatorConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  "Index Controller" - {

    "when platform operators are disabled" - {

      "must hide the platform operator and reporting notification cards" in {

        val application =
          applicationBuilder(userAnswers = None)
            .configure("features.platform-operators" -> false)
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[IndexView]

          status(result) mustEqual OK

          val viewModel = IndexViewModel(CardState.Hidden, CardState.Hidden)
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }

    "when platform operators are enabled" - {

      "and the user has not set up any platform operators" - {

        "must set the platform operators care to Add and the reporting notifications card to Inactive" in {

          when(mockConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Nil))

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(bind[PlatformOperatorConnector].toInstance(mockConnector))
              .configure("features.platform-operators" -> true)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]

            status(result) mustEqual OK

            val viewModel = IndexViewModel(CardState.AddOnly, CardState.Inactive)
            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
          }
        }
      }

      "and the user has set up platform operators, but none have a reporting notification" - {

        "must set the platform operators care to Add or View and the reporting notifications card to Add" in {

          val platformOperator = PlatformOperator(
            operatorId = "operatorId",
            operatorName = "operatorName",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Nil
          )

          when(mockConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Seq(platformOperator)))

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(bind[PlatformOperatorConnector].toInstance(mockConnector))
              .configure("features.platform-operators" -> true)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]

            status(result) mustEqual OK

            val viewModel = IndexViewModel(CardState.AddAndView, CardState.AddOnly)
            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
          }
        }
      }

      "and the user has set up platform operators, and at least one has a reporting notification" - {

        "must set the platform operators care to Add or View and the reporting notifications card to Add or View" in {

          val platformOperator1 = PlatformOperator(
            operatorId = "operatorId1",
            operatorName = "operatorName1",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Nil
          )

          val platformOperator2 = PlatformOperator(
            operatorId = "operatorId2",
            operatorName = "operatorName2",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Seq(NotificationDetails(NotificationType.Epo, None, None, 2024, Instant.now))
          )
          val platformOperatorResponse = ViewPlatformOperatorsResponse(Seq(platformOperator1, platformOperator2))

          when(mockConnector.viewPlatformOperators(any())) thenReturn Future.successful(platformOperatorResponse)

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(bind[PlatformOperatorConnector].toInstance(mockConnector))
              .configure("features.platform-operators" -> true)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]

            status(result) mustEqual OK

            val viewModel = IndexViewModel(CardState.AddAndView, CardState.AddAndView)
            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
          }
        }
      }
    }
  }
}
