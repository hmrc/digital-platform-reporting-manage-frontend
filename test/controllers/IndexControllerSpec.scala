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
import config.FrontendAppConfig
import connectors.{PlatformOperatorConnector, SubmissionsConnector}
import models.operator.{AddressDetails, ContactDetails, NotificationType}
import models.operator.responses.{NotificationDetails, PlatformOperator, ViewPlatformOperatorsResponse}
import models.submissions.ViewSubmissionsRequest
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.{AssumedReportingCardViewModel, CardState, FileSubmissionsCardViewModel, IndexViewModel, PlatformOperatorCardViewModel, ReportingNotificationCardViewModel}
import views.html.IndexView

import java.time.Instant
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockPlatformOperatorConnector = mock[PlatformOperatorConnector]
  private val mockSubmissionsConnector = mock[SubmissionsConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockPlatformOperatorConnector, mockSubmissionsConnector)
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

          val operatorCard = PlatformOperatorCardViewModel(CardState.Hidden, Nil)
          val notificationCard = ReportingNotificationCardViewModel(CardState.Hidden, Nil)
          val fileSubmissionsCard = FileSubmissionsCardViewModel(CardState.Hidden, Nil)
          val assumedReportingCard = AssumedReportingCardViewModel(CardState.Hidden, Nil)
          val viewModel = IndexViewModel(operatorCard, notificationCard, fileSubmissionsCard, assumedReportingCard)
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }

    "when platform operators are enabled" - {

      "and file submissions and assumed reporting are disabled" - {

        "must display the platform operator and reporting notification cards" in {

          when(mockPlatformOperatorConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Nil))

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(bind[PlatformOperatorConnector].toInstance(mockPlatformOperatorConnector))
              .configure("features.platform-operators" -> true)
              .configure("features.file-submissions" -> false)
              .configure("features.assumed-reporting" -> false)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]
            val appConfig = application.injector.instanceOf[FrontendAppConfig]

            status(result) mustEqual OK

            val operatorCard = PlatformOperatorCardViewModel(Nil, appConfig)(messages(application))
            val notificationCard = ReportingNotificationCardViewModel(Nil, appConfig)(messages(application))
            val fileSubmissionsCard = FileSubmissionsCardViewModel(CardState.Hidden, Nil)
            val assumedReportingCard = AssumedReportingCardViewModel(CardState.Hidden, Nil)
            val viewModel = IndexViewModel(operatorCard, notificationCard, fileSubmissionsCard, assumedReportingCard)

            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
          }
        }
      }

      "and file submissions is enabled, assumed reporting is disabled" - {

        "must display the platform operator, reporting notification and file submission cards" in {

          when(mockPlatformOperatorConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Nil))
          when(mockSubmissionsConnector.submissionsExist(any())(any())) thenReturn Future.successful(false)

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(
                bind[PlatformOperatorConnector].toInstance(mockPlatformOperatorConnector),
                bind[SubmissionsConnector].toInstance(mockSubmissionsConnector)
              )
              .configure("features.platform-operators" -> true)
              .configure("features.file-submissions" -> true)
              .configure("features.assumed-reporting" -> false)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]
            val appConfig = application.injector.instanceOf[FrontendAppConfig]

            status(result) mustEqual OK

            val operatorCard = PlatformOperatorCardViewModel(Nil, appConfig)(messages(application))
            val notificationCard = ReportingNotificationCardViewModel(Nil, appConfig)(messages(application))
            val fileSubmissionsCard = FileSubmissionsCardViewModel(false, Nil, appConfig)(messages(application))
            val assumedReportingCard = AssumedReportingCardViewModel(CardState.Hidden, Nil)
            val viewModel = IndexViewModel(operatorCard, notificationCard, fileSubmissionsCard, assumedReportingCard)

            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString

            verify(mockSubmissionsConnector, times(1)).submissionsExist(eqTo(false))(any())
            verify(mockSubmissionsConnector, never()).submissionsExist(eqTo(true))(any())
          }
        }
      }

      "and file submissions is disabled, assumed reporting is enabled" - {

        "must display the platform operator, reporting notification and assumed reporting cards" in {

          when(mockPlatformOperatorConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Nil))
          when(mockSubmissionsConnector.submissionsExist(any())(any())) thenReturn Future.successful(false)

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(
                bind[PlatformOperatorConnector].toInstance(mockPlatformOperatorConnector),
                bind[SubmissionsConnector].toInstance(mockSubmissionsConnector)
              )
              .configure("features.platform-operators" -> true)
              .configure("features.file-submissions" -> false)
              .configure("features.assumed-reporting" -> true)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]
            val appConfig = application.injector.instanceOf[FrontendAppConfig]

            status(result) mustEqual OK

            val operatorCard = PlatformOperatorCardViewModel(Nil, appConfig)(messages(application))
            val notificationCard = ReportingNotificationCardViewModel(Nil, appConfig)(messages(application))
            val fileSubmissionsCard = FileSubmissionsCardViewModel(CardState.Hidden, Nil)
            val assumedReportingCard = AssumedReportingCardViewModel(false, Nil, appConfig)(messages(application))
            val viewModel = IndexViewModel(operatorCard, notificationCard, fileSubmissionsCard, assumedReportingCard)

            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString

            verify(mockSubmissionsConnector, times(1)).submissionsExist(eqTo(true))(any())
            verify(mockSubmissionsConnector, never()).submissionsExist(eqTo(false))(any())
          }
        }
      }

      "and file submissions and assumed reporting are enabled" - {

        "must display the platform operator, reporting notification, file submission and assumed reporting cards" in {

          when(mockPlatformOperatorConnector.viewPlatformOperators(any())) thenReturn Future.successful(ViewPlatformOperatorsResponse(Nil))
          when(mockSubmissionsConnector.submissionsExist(any())(any())) thenReturn Future.successful(false)

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(
                bind[PlatformOperatorConnector].toInstance(mockPlatformOperatorConnector),
                bind[SubmissionsConnector].toInstance(mockSubmissionsConnector)
              )
              .configure("features.platform-operators" -> true)
              .configure("features.file-submissions" -> true)
              .configure("features.assumed-reporting" -> true)
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[IndexView]
            val appConfig = application.injector.instanceOf[FrontendAppConfig]

            status(result) mustEqual OK

            val operatorCard = PlatformOperatorCardViewModel(Nil, appConfig)(messages(application))
            val notificationCard = ReportingNotificationCardViewModel(Nil, appConfig)(messages(application))
            val fileSubmissionsCard = FileSubmissionsCardViewModel(false, Nil, appConfig)(messages(application))
            val assumedReportingCard = AssumedReportingCardViewModel(false, Nil, appConfig)(messages(application))
            val viewModel = IndexViewModel(operatorCard, notificationCard, fileSubmissionsCard, assumedReportingCard)

            contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString

            verify(mockSubmissionsConnector, times(1)).submissionsExist(eqTo(true))(any())
            verify(mockSubmissionsConnector, times(1)).submissionsExist(eqTo(false))(any())
          }
        }
      }
    }
  }
}
