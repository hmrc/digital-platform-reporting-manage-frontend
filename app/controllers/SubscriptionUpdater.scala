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

import audit.{AuditService, ChangeDetailsAuditEvent}
import cats.data.NonEmptyChain
import connectors.SubscriptionConnector
import controllers.SubscriptionUpdater.BuildSubscriptionRequestFailure
import logging.Logging
import models.UserAnswers
import models.requests.DataRequest
import org.apache.pekko.Done
import queries.{OriginalSubscriptionInfoQuery, Query}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

trait SubscriptionUpdater extends Logging { self: FrontendBaseController =>

  val userAnswersService: UserAnswersService
  val connector: SubscriptionConnector
  val auditService: AuditService
  implicit val ec: ExecutionContext

  protected def updateSubscription(answers: UserAnswers)(implicit request: DataRequest[_]): Future[Done] =
    userAnswersService.toSubscriptionInfo(answers, request.dprsId)
      .fold(
        errors => Future.failed(BuildSubscriptionRequestFailure(errors)),
        subscriptionRequest => {
          connector.updateSubscription(subscriptionRequest).map { response =>

            answers.get(OriginalSubscriptionInfoQuery).map { originalInfo =>
              val auditEvent = ChangeDetailsAuditEvent(originalInfo, subscriptionRequest)
              auditService.sendAudit(auditEvent)
            }.getOrElse(
              logger.warn("Unable to find original subscription info")
            )

            response
          }
        }
      )
}

object SubscriptionUpdater {

  final case class BuildSubscriptionRequestFailure(errors: NonEmptyChain[Query]) extends Throwable {
    override def getMessage: String = s"Unable to build subscription request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
  }
}
