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
import connectors.{PlatformOperatorConnector, SubmissionsConnector}
import controllers.actions.IdentifierAction
import models.operator.responses.PlatformOperator

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels._
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 view: IndexView,
                                 platformOperatorConnector: PlatformOperatorConnector,
                                 submissionsConnector: SubmissionsConnector,
                                 appConfig: FrontendAppConfig
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify.async { implicit request =>

    if (appConfig.platformOperatorsEnabled)  {
      platformOperatorConnector.viewPlatformOperators.flatMap { platformOperatorResponse =>

        val operators = platformOperatorResponse.platformOperators

        for {
          fileSubmissionsCard  <- getFileSubmissionsCard(operators, appConfig)
          assumedReportingCard <- getAssumedReportingCard(operators, appConfig)
        } yield {

          val viewModel = IndexViewModel(
            operatorId                = request.dprsId,
            platformOperatorCard      = PlatformOperatorCardViewModel(operators, appConfig),
            reportingNotificationCard = ReportingNotificationCardViewModel(operators, appConfig),
            fileSubmissionsCard       = fileSubmissionsCard,
            assumedReportingCard      = assumedReportingCard
          )

          Ok(view(viewModel))
        }
      }
    } else {
      val viewModel = IndexViewModel(
        operatorId                = request.dprsId,
        platformOperatorCard      = PlatformOperatorCardViewModel(CardState.Hidden, Nil, None),
        reportingNotificationCard = ReportingNotificationCardViewModel(CardState.Hidden, Nil, None),
        fileSubmissionsCard       = FileSubmissionsCardViewModel(CardState.Hidden, Nil, None),
        assumedReportingCard      = AssumedReportingCardViewModel(CardState.Hidden, Nil, None)
      )

      Future.successful(Ok(view(viewModel)))
    }
  }

  private def getFileSubmissionsCard(operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
                                    (implicit request: Request[_]): Future[FileSubmissionsCardViewModel] = {
    if (appConfig.fileSubmissionsEnabled) {
      submissionsConnector.submissionsExist(assumedReporting = false).map { response =>
        FileSubmissionsCardViewModel(response, operators, appConfig)
      }
    } else {
      Future.successful(FileSubmissionsCardViewModel(CardState.Hidden, Nil, None))
    }
  }

  private def getAssumedReportingCard(operators: Seq[PlatformOperator], appConfig: FrontendAppConfig)
                                     (implicit request: Request[_]): Future[AssumedReportingCardViewModel] = {
    if (appConfig.assumedReportingEnabled) {
      submissionsConnector.submissionsExist(assumedReporting = true).map { response =>
        AssumedReportingCardViewModel(response, operators, appConfig)
      }
    } else {
      Future.successful(AssumedReportingCardViewModel(CardState.Hidden, Nil, None))
    }
  }
}
