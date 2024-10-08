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

import controllers.actions._
import forms.PrimaryContactEmailAddressFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.{PrimaryContactEmailAddressPage, PrimaryContactNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PrimaryContactEmailAddressView

import scala.concurrent.{ExecutionContext, Future}

class PrimaryContactEmailAddressController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PrimaryContactEmailAddressFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PrimaryContactEmailAddressView
                                    )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with AnswerExtractor {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PrimaryContactNamePage) { contactName =>

        val form = formProvider(contactName)

        val preparedForm = request.userAnswers.get(PrimaryContactEmailAddressPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, contactName))
      }
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PrimaryContactNamePage) { contactName =>

        val form = formProvider(contactName)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, contactName))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PrimaryContactEmailAddressPage, value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PrimaryContactEmailAddressPage, updatedAnswers))
        )
      }
  }
}
