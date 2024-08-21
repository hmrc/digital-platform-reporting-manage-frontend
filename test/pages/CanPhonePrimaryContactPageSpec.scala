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

class CanPhonePrimaryContactPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".cleanup" - {

    val emptyAnswers = UserAnswers("id")

    "must not remove Primary Contact Phone Number when the answer is yes" in {

      val answers = emptyAnswers.set(PrimaryContactPhoneNumberPage, "phone").success.value

      val result = answers.set(CanPhonePrimaryContactPage, true).success.value

      result.get(PrimaryContactPhoneNumberPage) mustBe defined
    }

    "must remove Primary Contact Phone Number when the answer is no" in {

      val answers = emptyAnswers.set(PrimaryContactPhoneNumberPage, "phone").success.value

      val result = answers.set(CanPhonePrimaryContactPage, false).success.value

      result.get(PrimaryContactPhoneNumberPage) must not be defined
    }
  }
}
