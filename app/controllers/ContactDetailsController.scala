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

import com.google.inject.Inject
import connectors.SubscriptionConnector
import controllers.actions.IdentifierAction
import models.subscription._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.OriginalSubscriptionInfoQuery
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{ContactDetailsIndividualViewModel, ContactDetailsOrganisationViewModel}
import views.html.{ContactDetailsIndividualView, ContactDetailsOrganisationView}

import scala.concurrent.{ExecutionContext, Future}

class ContactDetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            individualView: ContactDetailsIndividualView,
                                            organisationView: ContactDetailsOrganisationView,
                                            connector: SubscriptionConnector,
                                            sessionRepository: SessionRepository,
                                            userAnswersService: UserAnswersService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>

      for {
        subscriptionInfo <- connector.getSubscription
        userAnswers      <- Future.fromTry(userAnswersService.fromSubscription(request.userId, subscriptionInfo))
        updatedAnswers   <- Future.fromTry(userAnswers.set(OriginalSubscriptionInfoQuery, subscriptionInfo))
        _                <- sessionRepository.set(updatedAnswers)
      } yield {

        subscriptionInfo.primaryContact match {
          case _: IndividualContact =>
            Ok(individualView(ContactDetailsIndividualViewModel(updatedAnswers)))

          case _: OrganisationContact =>
            Ok(organisationView(ContactDetailsOrganisationViewModel(updatedAnswers)))
        }
      }
  }
}
