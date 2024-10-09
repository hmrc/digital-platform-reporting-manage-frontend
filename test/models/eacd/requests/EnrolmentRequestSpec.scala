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

package models.eacd.requests

import models.eacd.Identifier
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class EnrolmentRequestSpec extends AnyFreeSpec
  with Matchers {

  private val underTest = new EnrolmentRequest {
    override val identifier: Identifier = Identifier("some-key", "some-value")
  }

  ".enrolmentKey" - {
    "must generate correct enrolmentKey when identifier provided" in {
      underTest.enrolmentKey mustBe "HMRC-DPRS~some-key~some-value"
    }
  }
}
