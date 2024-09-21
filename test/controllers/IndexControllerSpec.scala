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
import viewmodels.{CardState, IndexViewModel, PlatformOperatorCardViewModel, ReportingNotificationCardViewModel}
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

          val operatorCard = PlatformOperatorCardViewModel(CardState.Hidden, Nil)
          val notificationCard = ReportingNotificationCardViewModel(CardState.Hidden, Nil)
          val viewModel = IndexViewModel(operatorCard, notificationCard)
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }

    "when platform operators are enabled" - {

      "must display the  platform operator and reporting notification cards" in {

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
          val appConfig = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          val operatorCard = PlatformOperatorCardViewModel(Nil, appConfig)(messages(application))
          val notificationCard = ReportingNotificationCardViewModel(Nil, appConfig)(messages(application))

          val viewModel = IndexViewModel(operatorCard, notificationCard)
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }
  }
}
