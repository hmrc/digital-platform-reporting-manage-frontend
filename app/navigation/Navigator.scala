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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._

@Singleton
class Navigator @Inject() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case PrimaryContactNamePage           => _ => routes.PrimaryContactEmailAddressController.onPageLoad(NormalMode)
    case PrimaryContactEmailAddressPage   => _ => routes.CanPhonePrimaryContactController.onPageLoad(NormalMode)
    case CanPhonePrimaryContactPage       => canPhonePrimaryContactRoute
    case PrimaryContactPhoneNumberPage    => _ => routes.PrimaryContactUpdatedController.onPageLoad()
    case HasSecondaryContactPage          => hasSecondaryContactRoute
    case SecondaryContactNamePage         => _ => routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode)
    case SecondaryContactEmailAddressPage => _ => routes.CanPhoneSecondaryContactController.onPageLoad(NormalMode)
    case CanPhoneSecondaryContactPage     => canPhoneSecondaryContactRoute
    case SecondaryContactPhoneNumberPage  => _ => routes.SecondaryContactUpdatedController.onPageLoad()
    case IndividualEmailAddressPage       => _ => routes.IndividualEmailAddressUpdatedController.onPageLoad()
    case CanPhoneIndividualPage           => canPhoneIndividualRoute
    case IndividualPhoneNumberPage        => _ => routes.IndividualPhoneNumberUpdatedController.onPageLoad()
    case _                                => _ => routes.IndexController.onPageLoad()
  }

  private def canPhonePrimaryContactRoute(answers: UserAnswers): Call =
    answers.get(CanPhonePrimaryContactPage).map {
      case true => routes.PrimaryContactPhoneNumberController.onPageLoad(NormalMode)
      case false => routes.PrimaryContactUpdatedController.onPageLoad()
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def hasSecondaryContactRoute(answers: UserAnswers): Call =
    answers.get(HasSecondaryContactPage).map {
      case true => routes.SecondaryContactNameController.onPageLoad(NormalMode)
      case false => routes.SecondaryContactUpdatedController.onPageLoad()
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def canPhoneSecondaryContactRoute(answers: UserAnswers): Call =
    answers.get(CanPhoneSecondaryContactPage).map {
      case true => routes.SecondaryContactPhoneNumberController.onPageLoad(NormalMode)
      case false => routes.SecondaryContactUpdatedController.onPageLoad()
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def canPhoneIndividualRoute(answers: UserAnswers): Call =
    answers.get(CanPhoneIndividualPage).map {
      case true => routes.IndividualPhoneNumberController.onPageLoad(NormalMode)
      case false => routes.IndividualPhoneNumberRemovedController.onPageLoad()
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.ContactDetailsController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
