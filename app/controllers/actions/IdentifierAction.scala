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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.PendingEnrolmentConnector
import controllers.routes
import models.eacd.EnrolmentDetails
import models.requests.IdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc._
import services.{EnrolmentService, UserAllowListService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              userAllowListService: UserAllowListService,
                                              config: FrontendAppConfig,
                                              val parser: BodyParsers.Default,
                                              pendingEnrolmentConnector: PendingEnrolmentConnector,
                                              enrolmentService: EnrolmentService)
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(internalId) ~ allEnrolments =>
        val enrolment = allEnrolments.enrolments.filter(enrolment => enrolment.key == "HMRC-DPRS" )
        getEnrolment(Enrolments(enrolment)).map { dprsId =>
          userAllowListService.isUserAllowed(allEnrolments).flatMap { //TODO - This needs to be removed once we go to public beta.
            case true => block(IdentifierRequest(request, internalId, dprsId))
            case false => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
          }
        }.getOrElse {
          Future.successful(Redirect(routes.UnauthorisedController.noAccessPageLoad()))
        }

      case _ => reEnrol(request, block)(hc).recover {
        case NonFatal(_) => Redirect(routes.UnauthorisedController.onPageLoad())
      }
    } recoverWith {
      case _: NoActiveSession =>
        Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: InsufficientEnrolments => reEnrol(request, block)(hc).recover {
        case NonFatal(_) => Redirect(routes.UnauthorisedController.onPageLoad())
      }
      case _: AuthorisationException =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    }
  }

  private def reEnrol[A](request: Request[A], block: IdentifierRequest[A] => Future[Result])(hc: HeaderCarrier): Future[Result] = {
    for {
      pendingEnrolment <- pendingEnrolmentConnector.getPendingEnrolment()(hc)
      _ <- enrolmentService.enrol(EnrolmentDetails(pendingEnrolment))(hc)
      _ <- pendingEnrolmentConnector.remove()(hc)
      result <- block(IdentifierRequest(request, pendingEnrolment.userId, pendingEnrolment.dprsId))
    } yield result
  }

  private def getEnrolment(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-DPRS")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(_.key == "DPRSID")
          .map(_.value)
      }
}
