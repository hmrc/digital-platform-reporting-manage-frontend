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
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class PlatformOperatorCardViewModelSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val mockAppConfig = mock[FrontendAppConfig]
  private implicit val msgs: Messages = stubMessages()

  override def beforeEach(): Unit = {
    Mockito.reset(mockAppConfig)
    super.beforeEach()
  }

  ".apply" - {

    "when there are no platform operators" - {

      "must contain an add link" in {

        when(mockAppConfig.addPlatformOperatorUrl) thenReturn "add-link"

        val card = PlatformOperatorCardViewModel(Nil, mockAppConfig)

        card.cardState mustEqual CardState.Active
        card.links must contain only Link(msgs("platformOperatorCard.add"), "add-link")
      }
    }

    "when there is one or more platform operator" - {

      "must contain an `add another` link and a view link" in {

        when(mockAppConfig.addPlatformOperatorUrl) thenReturn "add-link"
        when(mockAppConfig.viewPlatformOperatorsUrl) thenReturn "view-link"

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

        val card = PlatformOperatorCardViewModel(Seq(operator), mockAppConfig)

        card.cardState mustEqual CardState.Active
        card.links must contain theSameElementsInOrderAs Seq(
          Link(msgs("platformOperatorCard.view"), "view-link"),
          Link(msgs("platformOperatorCard.addAnother"), "add-link")
        )
      }
    }
  }
}
