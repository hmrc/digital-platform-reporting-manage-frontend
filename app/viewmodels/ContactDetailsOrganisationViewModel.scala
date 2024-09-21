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

import controllers.routes
import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Card
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, CardTitle, SummaryList}
import viewmodels.checkAnswers._

case class ContactDetailsOrganisationViewModel(primaryContactList: SummaryList,
                                               secondaryContactList: SummaryList)

object ContactDetailsOrganisationViewModel {

  def apply(answers: UserAnswers)(implicit messages: Messages): ContactDetailsOrganisationViewModel = {

    val primaryContactList = SummaryList(
      rows = Seq(
        PrimaryContactNameSummary.row(answers),
        PrimaryContactEmailAddressSummary.row(answers),
        CanPhonePrimaryContactSummary.row(answers),
        PrimaryContactPhoneNumberSummary.row(answers)
      ).flatten,
      card = Some(Card(
        title = Some(CardTitle(content = Text(messages("contactDetails.organisation.primaryContact")))),
        actions = Some(Actions(
          items = Seq(ActionItem(
            content = Text(messages("site.change")),
            href = routes.PrimaryContactNameController.onPageLoad.url,
            visuallyHiddenText = Some(messages("contactDetails.organisation.primaryContact.change.hidden"))
          ))
        ))
      ))
    )

    val secondaryContactList = SummaryList(
      rows = Seq(
        HasSecondaryContactSummary.row(answers),
        SecondaryContactNameSummary.row(answers),
        SecondaryContactEmailAddressSummary.row(answers),
        CanPhoneSecondaryContactSummary.row(answers),
        SecondaryContactPhoneNumberSummary.row(answers)
      ).flatten,
      card = Some(Card(
        title = Some(CardTitle(content = Text(messages("contactDetails.organisation.secondaryContact")))),
        actions = Some(Actions(
          items = Seq(ActionItem(
            content = Text(messages("site.change")),
            href = routes.HasSecondaryContactController.onPageLoad.url,
            visuallyHiddenText = Some(messages("contactDetails.organisation.secondaryContact.change.hidden"))
          ))
        ))
      ))
    )

    ContactDetailsOrganisationViewModel(primaryContactList, secondaryContactList)
  }
}
