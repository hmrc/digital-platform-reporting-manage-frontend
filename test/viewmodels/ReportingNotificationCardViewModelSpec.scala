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

package viewmodels

import config.FrontendAppConfig
import models.operator.{AddressDetails, ContactDetails}
import models.operator.responses.PlatformOperator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class ReportingNotificationCardViewModelSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockAppConfig = mock[FrontendAppConfig]
  private implicit val msgs: Messages = stubMessages()

  override def beforeEach(): Unit = {
    Mockito.reset(mockAppConfig)
    super.beforeEach()
  }

  ".apply" - {

    "must be inactive and have no links when there are no platform operators" in {

      val card = ReportingNotificationCardViewModel(Nil, mockAppConfig)

      card.cardState mustEqual CardState.Inactive
      card.links mustBe empty
    }

    "must be active and have a `view single` link and an add link when there is one platform operator" in {

      when(mockAppConfig.addNotificationUrl) thenReturn "add-link"
      when(mockAppConfig.viewNotificationsSingleUrl(any())) thenReturn "view-link"

      val operator = PlatformOperator(
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

      val card = ReportingNotificationCardViewModel(Seq(operator), mockAppConfig)

      card.cardState mustEqual CardState.Active
      card.links must contain theSameElementsInOrderAs Seq(
        Link(msgs("reportingNotificationCard.view"), "view-link"),
        Link(msgs("reportingNotificationCard.add"), "add-link")
      )
    }

    "must be active have a view link and an add link when there is more than one platform operator" in {

      when(mockAppConfig.addNotificationUrl) thenReturn "add-link"
      when(mockAppConfig.viewNotificationsUrl) thenReturn "view-link"

      val operator = PlatformOperator(
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

      val card = ReportingNotificationCardViewModel(Seq(operator, operator), mockAppConfig)

      card.cardState mustEqual CardState.Active
      card.links must contain theSameElementsInOrderAs Seq(
        Link(msgs("reportingNotificationCard.view"), "view-link"),
        Link(msgs("reportingNotificationCard.add"), "add-link")
      )
    }
  }
}
