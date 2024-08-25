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

package controllers

import cats.data.NonEmptyChain
import connector.SubscriptionConnector
import controllers.SubscriptionUpdater.BuildSubscriptionRequestFailure
import models.UserAnswers
import models.requests.DataRequest
import org.apache.pekko.Done
import queries.Query
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

trait SubscriptionUpdater { self: FrontendBaseController =>

  val userAnswersService: UserAnswersService
  val connector: SubscriptionConnector

  protected def updateSubscription(answers: UserAnswers)(implicit request: DataRequest[_]): Future[Done] =
    userAnswersService.toSubscriptionRequest(answers, request.dprsId)
      .fold(
        errors => Future.failed(BuildSubscriptionRequestFailure(errors)),
        subscriptionRequest => connector.updateSubscription(subscriptionRequest)
      )
}

object SubscriptionUpdater {

  final case class BuildSubscriptionRequestFailure(errors: NonEmptyChain[Query]) extends Throwable {
    override def getMessage: String = s"Unable to build subscription request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
  }
}
