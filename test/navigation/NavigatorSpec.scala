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

import base.SpecBase
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "must go from a page that doesn't exist in the route map to Index" in {

      case object UnknownPage extends Page
      navigator.nextPage(UnknownPage, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
    }

    "must go from Primary Contact Name to Primary Contact Email Address" in {

      navigator.nextPage(PrimaryContactNamePage, emptyUserAnswers) mustBe routes.PrimaryContactEmailAddressController.onPageLoad
    }

    "must go from Primary Contact Email Address to Can Phone Primary Contact" in {

      navigator.nextPage(PrimaryContactEmailAddressPage, emptyUserAnswers) mustBe routes.CanPhonePrimaryContactController.onPageLoad
    }

    "must go from Can Phone Primary Contact" - {

      "to Primary Contact Phone Number when the answer is yes" in {

        val answers = emptyUserAnswers.set(CanPhonePrimaryContactPage, true).success.value
        navigator.nextPage(CanPhonePrimaryContactPage, answers) mustBe routes.PrimaryContactPhoneNumberController.onPageLoad
      }

      "to Primary Contact Updated when the answer is no" in {

        val answers = emptyUserAnswers.set(CanPhonePrimaryContactPage, false).success.value
        navigator.nextPage(CanPhonePrimaryContactPage, answers) mustBe routes.PrimaryContactUpdatedController.onPageLoad()
      }
    }

    "must go from Has Secondary Contact" - {

      "to Secondary Contact Name when the answer is yes" in {

        val answers = emptyUserAnswers.set(HasSecondaryContactPage, true).success.value
        navigator.nextPage(HasSecondaryContactPage, answers) mustBe routes.SecondaryContactNameController.onPageLoad
      }

      "to Secondary Contact Updated when the answer is no" in {

        val answers = emptyUserAnswers.set(HasSecondaryContactPage, false).success.value
        navigator.nextPage(HasSecondaryContactPage, answers) mustBe routes.SecondaryContactUpdatedController.onPageLoad()
      }
    }

    "must go from Secondary Contact Name to Secondary Contact Email Address" in {

      navigator.nextPage(SecondaryContactNamePage, emptyUserAnswers) mustBe routes.SecondaryContactEmailAddressController.onPageLoad
    }

    "must go from Secondary Contact Email Address to Can Phone Secondary Contact" in {

      navigator.nextPage(SecondaryContactEmailAddressPage, emptyUserAnswers) mustBe routes.CanPhoneSecondaryContactController.onPageLoad
    }

    "must go from Can Phone Secondary Contact" - {

      "to Secondary Contact Phone Number when the answer is yes" in {

        val answers = emptyUserAnswers.set(CanPhoneSecondaryContactPage, true).success.value
        navigator.nextPage(CanPhoneSecondaryContactPage, answers) mustBe routes.SecondaryContactPhoneNumberController.onPageLoad
      }

      "to Secondary Contact Updated when the answer is no" in {

        val answers = emptyUserAnswers.set(CanPhoneSecondaryContactPage, false).success.value
        navigator.nextPage(CanPhoneSecondaryContactPage, answers) mustBe routes.SecondaryContactUpdatedController.onPageLoad()
        }
    }

    "must go from Individual Email Address to Individual Email Address Updated" in {

      navigator.nextPage(IndividualEmailAddressPage, emptyUserAnswers) mustBe routes.IndividualEmailAddressUpdatedController.onPageLoad()
    }

    "must go from Can Phone Individual" -{

      "to Individual Phone Number when the answer is yes" in {

        val answers = emptyUserAnswers.set(CanPhoneIndividualPage, true).success.value
        navigator.nextPage(CanPhoneIndividualPage, answers) mustBe routes.IndividualPhoneNumberController.onPageLoad
      }

      "to Individual Phone Number Removed when the answer is no" in {

        val answers = emptyUserAnswers.set(CanPhoneIndividualPage, false).success.value
        navigator.nextPage(CanPhoneIndividualPage, answers) mustBe routes.IndividualPhoneNumberRemovedController.onPageLoad()
      }
    }

    "must go from Individual Phone Number to Individual Phone Number Updated" in {

      navigator.nextPage(IndividualPhoneNumberPage, emptyUserAnswers) mustBe routes.IndividualPhoneNumberUpdatedController.onPageLoad()
    }
  }
}
