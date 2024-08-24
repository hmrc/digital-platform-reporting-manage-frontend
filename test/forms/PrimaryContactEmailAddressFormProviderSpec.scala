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

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class PrimaryContactEmailAddressFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "primaryContactEmailAddress.error.required"
  private val lengthKey = "primaryContactEmailAddress.error.length"
  private val formatKey = "primaryContactEmailAddress.error.format"
  private val maxLength = 132
  private val contactName = "name"
  private val form = new PrimaryContactEmailAddressFormProvider()(contactName)
  private val validData = Gen.const("foo@example.com")

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(contactName))
    )

    "not allow invalid email addreses" in {

      val noAt = "fooexample.com"
      val noUserName = "@example.com"
      val noDomain = "foo@example"
      val invalidData = Gen.oneOf(noAt, noUserName, noDomain).sample.value

      val result = form.bind(Map("value" -> invalidData)).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, formatKey, Seq(Validation.emailPattern.toString)))
    }
  }
}
