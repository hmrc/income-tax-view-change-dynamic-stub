/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{AuthExchange, DelegatedEnrolmentData, EnrolmentData, GovernmentGatewayToken}
import play.api.http.HeaderNames
import play.api.libs.json._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, TooManyRequestException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.LoginUtil.{enrolmentData, loginConfig}

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future




@Singleton
class MicroserviceAuthConnector @Inject()(servicesConfig: ServicesConfig,
                                          val http: HttpClient) extends PlayAuthConnector {
  override val serviceUrl: String = servicesConfig.baseUrl("auth-login")


  def login(nino: models.Nino)(implicit hc: HeaderCarrier): Future[(AuthExchange, GovernmentGatewayToken)] = {

    val payload: JsValue = Json.obj(
      "credId" -> loginConfig.getString(s"$nino.credId"),
      "affinityGroup" -> loginConfig.getString(s"$nino.affinityGroup"),
      "confidenceLevel" -> loginConfig.getInt(s"$nino.confidenceLevel"),
      "credentialStrength" -> loginConfig.getString(s"$nino.credentialStrength"),
      "credentialRole" -> loginConfig.getString(s"$nino.credentialRole"),
      "usersName" -> "usersName",
      "enrolments" -> enrolmentData(nino.value),
      "delegatedEnrolments" -> delegatedEnrolmentsJson(Nil)
    ) ++ removeEmptyValues(
      "nino" -> Some(nino.value),
      "groupIdentifier" -> Some("groupIdentifier"),
      "gatewayToken" -> Some("gatewayToken"),
      "agentId" -> Some("agentId"),
      "agentCode" -> Some("agentCode"),
      "agentFriendlyName" -> Some("agentFriendlyName"),
      "email" -> Some("email")
    )

    http.POST[JsValue, HttpResponse](s"$serviceUrl/government-gateway/session/login", payload) flatMap {
      case response@HttpResponse(201, _, _) =>
        (
          response.header(HeaderNames.AUTHORIZATION),
          response.header(HeaderNames.LOCATION),
          (response.json \ "gatewayToken").asOpt[String]
        ) match {
          case (Some(token), Some(sessionUri), Some(receivedGatewayToken)) =>
            Future.successful((AuthExchange(token, sessionUri), GovernmentGatewayToken(receivedGatewayToken)))
          case _ => Future.failed(new RuntimeException("Internal Error, missing headers or gatewayToken in response from auth-login-api"))
        }
      case response@HttpResponse(429, _, _) =>
        Future.failed(new TooManyRequestException(s"response from $serviceUrl/government-gateway/session/login was ${response.status}. Body ${response.body}"))
      case response =>
        Future.failed(new RuntimeException(s"response from $serviceUrl/government-gateway/session/login was ${response.status}. Body ${response.body}"))
    }
  }

  private def delegatedEnrolmentsJson(delegatedEnrolments: Seq[DelegatedEnrolmentData]) =
    delegatedEnrolments
      .filterNot(invalidDelegatedEnrolment)
      .filter(validateDelegatedEnrolmentIdentifiers)
      .map(toJson)

  private def invalidDelegatedEnrolment(delegatedEnrolment: DelegatedEnrolmentData) =
    delegatedEnrolment.key.isEmpty || delegatedEnrolment.delegatedAuthRule.isEmpty

  private def validateDelegatedEnrolmentIdentifiers(delegatedEnrolment: DelegatedEnrolmentData) =
    delegatedEnrolment.taxIdentifier.forall(taxId => !(taxId.key.isEmpty || taxId.value.isEmpty))

  private def toJson(enrolment: DelegatedEnrolmentData): JsObject =
    Json.obj(
      "key" -> enrolment.key,
      "identifiers" -> enrolment.taxIdentifier.map(taxId => Json.obj(
        "key" -> taxId.key,
        "value" -> taxId.value
      )),
      "delegatedAuthRule" -> enrolment.delegatedAuthRule
    )

  private def toJson(enrolment: EnrolmentData): JsObject =
    Json.obj(
      "key" -> enrolment.name,
      "identifiers" -> enrolment.taxIdentifier.map(taxId => Json.obj(
        "key" -> taxId.key,
        "value" -> taxId.value
      )),
      "state" -> enrolment.state
    )

  private def toJson[A: Writes](optData: Option[A], key: String): JsObject =
    optData.map(data => Json.obj(key -> Json.toJson(data))).getOrElse(Json.obj())

  private def removeEmptyValues(fields: (String, Option[String])*): JsObject = {
    val onlyDefinedFields = fields
      .collect {
        case (key, Some(value)) => key -> Json.toJsFieldJsValueWrapper(value)
      }
    Json.obj(onlyDefinedFields: _*)
  }

  private def toJson(gatewayToken: Option[String]): JsObject = {
    val ggToken = gatewayToken.map(token => "gatewayToken" -> Json.toJsFieldJsValueWrapper(token))

    Json.obj("gatewayInformation" -> Json.obj(scala.Seq(ggToken).flatten: _*))
  }

}