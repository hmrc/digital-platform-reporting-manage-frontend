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
import models.operator.responses.PlatformOperator
import play.api.i18n.Messages

case class ReportingNotificationCardViewModel(cardState: CardState,
                                              links: Seq[Link])

object ReportingNotificationCardViewModel {

  def apply(operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
           (implicit messages: Messages): ReportingNotificationCardViewModel = {

    val viewLink = operators.size match {
      case 0 => None
      case 1 => Some(Link(messages("reportingNotificationCard.view"), appConfig.viewNotificationsSingleUrl(operators.head.operatorId)))
      case _ => Some(Link(messages("reportingNotificationCard.view"), appConfig.viewNotificationsUrl))
    }

    val addLink = operators.size match {
      case 0 => None
      case _ => Some(Link(messages("reportingNotificationCard.add"), appConfig.addNotificationUrl))
    }

    val links = Seq(viewLink, addLink).flatten

    ReportingNotificationCardViewModel(
      cardState = if (links.isEmpty) CardState.Inactive else CardState.Active,
      links     = links
    )
  }
}
