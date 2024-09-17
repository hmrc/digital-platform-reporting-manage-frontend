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

import config.FrontendAppConfig
import connectors.PlatformOperatorConnector
import controllers.actions.IdentifierAction

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.{CardState, IndexViewModel}
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 view: IndexView,
                                 connector: PlatformOperatorConnector,
                                 appConfig: FrontendAppConfig
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>

    if (appConfig.platformOperatorsEnabled)  {
      connector.viewPlatformOperators.map { response =>
        val viewModel = IndexViewModel(
          platformOperatorCard = response.platformOperators match {
            case Nil => CardState.AddOnly
            case _   => CardState.AddAndView
          },
          reportingNotificationCard = response.platformOperators match {
            case Nil                                                     => CardState.Inactive
            case operators if operators.exists(_.notifications.nonEmpty) => CardState.AddAndView
            case _                                                       => CardState.AddOnly
          }
        )

        Ok(view(viewModel))
      }
    } else {
      val viewModel = IndexViewModel(
        platformOperatorCard = CardState.Hidden,
        reportingNotificationCard = CardState.Hidden
      )

      Future.successful(Ok(view(viewModel)))
    }
  }
}
