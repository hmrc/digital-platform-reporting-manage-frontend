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

package forms

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class SecondaryContactPhoneNumberFormProvider @Inject() extends Mappings {
  private val maxLength = 24

  def apply(contactName: String): Form[String] = {
    Form(
      "value" -> text("secondaryContactPhoneNumber.error.required", args = Seq(contactName))
        .verifying(firstError(
          maxLength(maxLength, "secondaryContactPhoneNumber.error.length"),
          regexp(Validation.phoneNumberPattern.toString, "secondaryContactPhoneNumber.error.format"),
          validPhoneNumber("secondaryContactPhoneNumber.error.format")
        ))
    )
  }
}
