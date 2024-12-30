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

final case class FileSubmissionsCardViewModel(cardState: CardState,
                                              items: Seq[CardItem],
                                              tag: Option[Tag])

object FileSubmissionsCardViewModel {

  def apply(submissionsExist: Boolean, operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
           (implicit messages: Messages): FileSubmissionsCardViewModel = {

    if (operators.exists(_.notifications.nonEmpty)) {
      val addLink = CardLink(messages("fileSubmissionsCard.add"), appConfig.addSubmissionUrl)
      val viewLink = CardLink(messages("fileSubmissionsCard.view"), appConfig.viewSubmissionsUrl)
      val addMessage = CardMessage(messages("fileSubmissionsCard.add"))

      val items = if (appConfig.submissionsAllowed) {
        if (submissionsExist) Seq(viewLink, addLink) else Seq(addLink)
      } else {
        if (submissionsExist) Seq(viewLink, addMessage) else Seq(addMessage)
      }

      val tag = if (!appConfig.submissionsAllowed) Some(CardTag.notAvailable) else if (submissionsExist) None else Some(CardTag.notStarted)

      FileSubmissionsCardViewModel(
        cardState = CardState.Active,
        items = items,
        tag = tag
      )
    } else {
      FileSubmissionsCardViewModel(cardState = CardState.Inactive, items = Nil, tag = Some(CardTag.cannotStart))
    }
  }
}
