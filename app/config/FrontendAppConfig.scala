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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "digital-platform-reporting-manage-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/digital-platform-reporting"

  val userResearchBannerEnabled: Boolean = configuration.get[Boolean]("features.user-research-banner")
  val userResearchBannerLink: String = configuration.get[String]("urls.user-research-banner")

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val platformOperatorsEnabled: Boolean = configuration.get[Boolean]("features.platform-operators")
  val fileSubmissionsEnabled: Boolean = configuration.get[Boolean]("features.file-submissions")
  val assumedReportingEnabled: Boolean = configuration.get[Boolean]("features.assumed-reporting")

  private val platformOperatorFrontendBaseUrl: String = configuration.get[String]("microservice.services.digital-platform-reporting-operator-frontend.baseUrl")

  val addPlatformOperatorUrl = s"$platformOperatorFrontendBaseUrl/platform-operator/add-platform-operator/start"
  val viewPlatformOperatorsUrl= s"$platformOperatorFrontendBaseUrl/platform-operator/view"

  val addNotificationUrl = s"$platformOperatorFrontendBaseUrl/reporting-notification/which-platform-operator"
  val viewNotificationsUrl = s"$platformOperatorFrontendBaseUrl/reporting-notification/which-platform-operator-to-view"
  def viewNotificationsSingleUrl(operatorId: String) = s"$platformOperatorFrontendBaseUrl/reporting-notification/$operatorId/view"

  private val submissionsFrontendBaseUrl: String = configuration.get[String]("microservice.services.digital-platform-reporting-submission-frontend.baseUrl")
  val addSubmissionUrl = s"$submissionsFrontendBaseUrl/submission/which-platform-operator"
  val viewSubmissionsUrl = s"$submissionsFrontendBaseUrl/submission/view"
  val addAssumedReportUrl = s"$submissionsFrontendBaseUrl/assumed-reporting/which-platform-operator"
  val viewAssumedReportsUrl = s"$submissionsFrontendBaseUrl/assumed-reporting/view"

  val auditSource: String = configuration.get[String]("auditing.auditSource")

  val digitalPlatformReportingUrl: String = configuration.get[Service]("microservice.services.digital-platform-reporting")
  val taxEnrolmentsBaseUrl: String = configuration.get[Service]("microservice.services.tax-enrolments").baseUrl

  val userAllowListService: Service = configuration.get[Service]("microservice.services.user-allow-list")
  val internalAuthToken: String = configuration.get[String]("internal-auth.token")

  val userAllowListEnabled: Boolean = configuration.get[Boolean]("features.user-allow-list")
  val submissionsAllowed: Boolean = configuration.get[Boolean]("features.submissions-enabled")
}
