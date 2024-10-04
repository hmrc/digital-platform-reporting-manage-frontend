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

import audit.AuditService
import connectors.SubscriptionConnector
import controllers.actions._
import forms.SecondaryContactPhoneNumberFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.{SecondaryContactNamePage, SecondaryContactPhoneNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SecondaryContactPhoneNumberView

import scala.concurrent.{ExecutionContext, Future}

class SecondaryContactPhoneNumberController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       sessionRepository: SessionRepository,
                                                       navigator: Navigator,
                                                       identify: IdentifierAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction,
                                                       formProvider: SecondaryContactPhoneNumberFormProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: SecondaryContactPhoneNumberView,
                                                       val connector: SubscriptionConnector,
                                                       val userAnswersService: UserAnswersService,
                                                       val auditService: AuditService
                                                     )(implicit val ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with AnswerExtractor with SubscriptionUpdater{

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(SecondaryContactNamePage) { contactName =>

        val form = formProvider(contactName)

        val preparedForm = request.userAnswers.get(SecondaryContactPhoneNumberPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, contactName))
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(SecondaryContactNamePage) { contactName =>

        val form = formProvider(contactName)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, contactName))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SecondaryContactPhoneNumberPage, value))
              _              <- updateSubscription(updatedAnswers)
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(SecondaryContactPhoneNumberPage, updatedAnswers))
        )
      }
  }
}
