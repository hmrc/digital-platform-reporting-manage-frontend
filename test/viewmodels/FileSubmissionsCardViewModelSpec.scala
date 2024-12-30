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
import models.operator.{AddressDetails, ContactDetails, NotificationType}
import models.operator.responses.{NotificationDetails, PlatformOperator}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.Instant

class FileSubmissionsCardViewModelSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues {

  private val mockAppConfig = mock[FrontendAppConfig]
  private implicit val msgs: Messages = stubMessages()

  override def beforeEach(): Unit = {
    Mockito.reset(mockAppConfig)
    super.beforeEach()
  }

  ".apply" - {

    "when there are no platform operators" - {

      "must be inactive" in {

        val card = FileSubmissionsCardViewModel(false, Nil, mockAppConfig)

        card.cardState mustEqual CardState.Inactive
        card.items mustBe empty
        card.tag.value.content mustEqual Text(msgs("card.cannotStart"))
      }
    }

    "when no platform operators have reporting notifications" - {

      "must be inactive" in {

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

        val card = FileSubmissionsCardViewModel(false, Seq(operator), mockAppConfig)

        card.cardState mustEqual CardState.Inactive
        card.items mustBe empty
        card.tag.value.content mustEqual Text(msgs("card.cannotStart"))
      }
    }

    "when at least one platform operator exists with a reporting notification" - {

      "when submissions are enabled" - {

        "must contain an add link and a `not started` tag when there are no submissions" in {

          when(mockAppConfig.addSubmissionUrl) thenReturn "add-link"
          when(mockAppConfig.submissionsAllowed) thenReturn true

          val operator = PlatformOperator(
            operatorId = "operatorId",
            operatorName = "operatorName",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Seq(NotificationDetails(NotificationType.Epo, None, None, 2024, Instant.now))
          )

          val card = FileSubmissionsCardViewModel(false, Seq(operator), mockAppConfig)

          card.cardState mustEqual CardState.Active
          card.items must contain only CardLink(msgs("fileSubmissionsCard.add"), "add-link")
          card.tag.value.content mustEqual Text(msgs("card.notStarted"))
        }

        "must contain view and add links, and no tag, when there are some submissions" in {

          when(mockAppConfig.addSubmissionUrl) thenReturn "add-link"
          when(mockAppConfig.viewSubmissionsUrl) thenReturn "view-link"
          when(mockAppConfig.submissionsAllowed) thenReturn true

          val operator = PlatformOperator(
            operatorId = "operatorId",
            operatorName = "operatorName",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Seq(NotificationDetails(NotificationType.Epo, None, None, 2024, Instant.now))
          )

          val card = FileSubmissionsCardViewModel(true, Seq(operator), mockAppConfig)

          card.cardState mustEqual CardState.Active
          card.items must contain theSameElementsInOrderAs Seq(
            CardLink(msgs("fileSubmissionsCard.view"), "view-link"),
            CardLink(msgs("fileSubmissionsCard.add"), "add-link")
          )
          card.tag must not be defined
        }
      }

      "when submissions are disabled" - {

        "must contain an add message and a `not available` tag when there are no submissions" in {

          when(mockAppConfig.submissionsAllowed) thenReturn false

          val operator = PlatformOperator(
            operatorId = "operatorId",
            operatorName = "operatorName",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Seq(NotificationDetails(NotificationType.Epo, None, None, 2024, Instant.now))
          )

          val card = FileSubmissionsCardViewModel(false, Seq(operator), mockAppConfig)

          card.cardState mustEqual CardState.Active
          card.items must contain only CardMessage(msgs("fileSubmissionsCard.add"))
          card.tag.value.content mustEqual Text(msgs("card.notAvailable"))
        }

        "must contain a view link, an add message, and a `not available` tag when there are some submissions" in {

          when(mockAppConfig.viewSubmissionsUrl) thenReturn "view-link"
          when(mockAppConfig.submissionsAllowed) thenReturn false

          val operator = PlatformOperator(
            operatorId = "operatorId",
            operatorName = "operatorName",
            tinDetails = Nil,
            businessName = None,
            tradingName = None,
            primaryContactDetails = ContactDetails(None, "name", "email"),
            secondaryContactDetails = None,
            addressDetails = AddressDetails("line 1", None, None, None, None, None),
            notifications = Seq(NotificationDetails(NotificationType.Epo, None, None, 2024, Instant.now))
          )

          val card = FileSubmissionsCardViewModel(true, Seq(operator), mockAppConfig)

          card.cardState mustEqual CardState.Active
          card.items must contain theSameElementsInOrderAs Seq(
            CardLink(msgs("fileSubmissionsCard.view"), "view-link"),
            CardMessage(msgs("fileSubmissionsCard.add"))
          )
          card.tag.value.content mustEqual Text(msgs("card.notAvailable"))
        }
      }
    }
  }
}
