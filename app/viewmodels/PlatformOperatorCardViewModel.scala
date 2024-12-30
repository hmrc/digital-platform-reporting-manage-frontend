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

final case class PlatformOperatorCardViewModel(cardState: CardState,
                                               items: Seq[CardItem],
                                               tag: Option[Tag])

object PlatformOperatorCardViewModel {

  def apply(operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
           (implicit messages: Messages): PlatformOperatorCardViewModel = {

    val viewLink = operators.size match {
      case 0 => None
      case _ => Some(CardLink(messages("platformOperatorCard.view"), appConfig.viewPlatformOperatorsUrl))
    }

    val addLink = operators.size match {
      case 0 => CardLink (messages ("platformOperatorCard.add"), appConfig.addPlatformOperatorUrl)
      case _ => CardLink (messages ("platformOperatorCard.addAnother"), appConfig.addPlatformOperatorUrl)
    }

    val tag = operators.size match {
      case 0 => Some(CardTag.notStarted)
      case _ => None
    }

    PlatformOperatorCardViewModel(
      cardState = CardState.Active,
      items     = Seq(viewLink, Some(addLink)).flatten,
      tag       = tag
    )
  }
}
