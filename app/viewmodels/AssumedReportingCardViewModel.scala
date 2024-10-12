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
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

final case class AssumedReportingCardViewModel(cardState: CardState,
                                               links: Seq[Link],
                                               tag: Option[Tag])

object AssumedReportingCardViewModel {

  def apply(assumedReportsExist: Boolean, operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
           (implicit messages: Messages): AssumedReportingCardViewModel = {

    if (operators.exists(_.notifications.nonEmpty)) {
      val addLink = Link(messages("assumedReportingCard.add"), appConfig.addAssumedReportUrl)
      val viewLink = Link(messages("assumedReportingCard.view"), appConfig.viewAssumedReportsUrl)

      val links = if (assumedReportsExist) Seq(viewLink, addLink) else Seq(addLink)

      val tag = if (assumedReportsExist) None else Some(CardTag.notStarted)

      AssumedReportingCardViewModel(
        cardState = CardState.Active,
        links = links,
        tag = tag
      )
    } else {
      AssumedReportingCardViewModel(cardState = CardState.Inactive, links = Nil, tag = Some(CardTag.cannotStart))
    }
  }
}
