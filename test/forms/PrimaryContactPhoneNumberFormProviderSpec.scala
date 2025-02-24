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

class PrimaryContactPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "primaryContactPhoneNumber.error.required"
  private val lengthKey = "primaryContactPhoneNumber.error.length"
  private val formatKey = "primaryContactPhoneNumber.error.format"
  private val contactName = "name"
  private val form = new PrimaryContactPhoneNumberFormProvider()(contactName)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf("07777777777", "+1 (555) 000 0000", "07777 777777   ")
    )


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(contactName))
    )

    "must fail to bind an invalid phone number" in {
      form.bind(Map(fieldName -> "invalid")).error("value").value.message mustEqual formatKey
    }

    "must fail to bind phone numbers longer than 24 characters" in {
      val result = form.bind(Map(fieldName -> "+44 7777 777777 EXT 12345"))
      result.error("value").value.message mustEqual lengthKey
    }
  }
}
