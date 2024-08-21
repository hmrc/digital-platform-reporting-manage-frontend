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

package pages

import models.UserAnswers
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class HasSecondaryContactPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".cleanup" - {

    val emptyAnswers = UserAnswers("id")

    "must not remove Secondary Contact details when the answer is yes" in {

      val answers =
        emptyAnswers
          .set(SecondaryContactNamePage, "name").success.value
          .set(SecondaryContactEmailAddressPage, "email").success.value
          .set(CanPhoneSecondaryContactPage, true).success.value
          .set(SecondaryContactPhoneNumberPage, "phone").success.value

      val result = answers.set(HasSecondaryContactPage, true).success.value

      result.get(SecondaryContactNamePage) mustBe defined
      result.get(SecondaryContactEmailAddressPage) mustBe defined
      result.get(CanPhoneSecondaryContactPage) mustBe defined
      result.get(SecondaryContactPhoneNumberPage) mustBe defined
    }

    "must remove Secondary Contact details when the answer is no" in {

      val answers =
        emptyAnswers
          .set(SecondaryContactNamePage, "name").success.value
          .set(SecondaryContactEmailAddressPage, "email").success.value
          .set(CanPhoneSecondaryContactPage, true).success.value
          .set(SecondaryContactPhoneNumberPage, "phone").success.value

      val result = answers.set(HasSecondaryContactPage, false).success.value

      result.get(SecondaryContactNamePage) must not be defined
      result.get(SecondaryContactEmailAddressPage) must not be defined
      result.get(CanPhoneSecondaryContactPage) must not be defined
      result.get(SecondaryContactPhoneNumberPage) must not be defined
    }
  }
}
