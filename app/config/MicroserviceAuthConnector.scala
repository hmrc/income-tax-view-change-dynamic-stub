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

import models.{DelegatedEnrolmentData, EnrolmentData, GovernmentGatewayToken, ItmpData}
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.Mapping
import play.api.http.HeaderNames
import play.api.libs.json._
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, TooManyRequestException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MdtpInformation(deviceId: String, sessionId: String)
object MdtpInformation {

  implicit val writes: OWrites[MdtpInformation] = Json.writes[MdtpInformation]

  def mdtpMapping(): Mapping[Option[MdtpInformation]] =
    optional(mapping(
      "deviceId" -> text,
      "sessionId" -> text
    )(MdtpInformation.apply)(MdtpInformation.unapply))
}
case class OAuthTokens(accessToken: Option[String] = None, refreshToken: Option[String] = None, idToken: Option[String] = None)
object OAuthTokens {

  implicit val writes: OWrites[OAuthTokens] = Json.writes[OAuthTokens]

  def oauthTokensMapping(): Mapping[Option[OAuthTokens]] =
    optional(mapping(
      "accessToken" -> optional(text),
      "refreshToken" -> optional(text),
      "idToken" -> optional(text)
    )(OAuthTokens.apply)(OAuthTokens.unapply))

}

case class AdditionalInfo(
                           profile:       Option[String]  = None,
                           groupProfile:  Option[String]  = None,
                           emailVerified: Option[Boolean] = None
                         )
case class AuthExchange(bearerToken: String, sessionAuthorityUri: String)


@Singleton
class MicroserviceAuthConnector @Inject()(servicesConfig: ServicesConfig,
                                          val http: HttpClient) extends PlayAuthConnector {
  override val serviceUrl: String = servicesConfig.baseUrl("auth")

  def login(enrolments:          Seq[EnrolmentData] = Nil,
            delegatedEnrolments: Seq[DelegatedEnrolmentData] = Nil, gatewayToken:        Option[String] = None,
            groupIdentifier:     Option[String] = None, nino:                models.Nino,
            usersName:           Option[String] = None, email:               Option[String] = None,
            itmpData:            Option[ItmpData] = None, agentId:             Option[String] = None,
            agentCode:           Option[String] = None, agentFriendlyName:   Option[String] = None,
            unreadMessageCount:  Option[Int] = None, mdtpInformation:     Option[MdtpInformation] = None,
            oauthTokens:         Option[OAuthTokens] = None
           )(implicit hc: HeaderCarrier): Future[(AuthExchange, GovernmentGatewayToken)] = {

    val payload: JsValue = Json.obj(
      "credId" -> "6528180096307862",
      "affinityGroup" -> "Individual",
      "confidenceLevel" -> 250,
      "credentialStrength" -> "strong",
      "credentialRole" -> "User",
      "usersName" -> usersName,
      "enrolments" -> enrolments.map(toJson),
      "delegatedEnrolments" -> delegatedEnrolmentsJson(delegatedEnrolments)
    ) ++ removeEmptyValues(
      "nino" -> Some( nino.value),
      "groupIdentifier" -> groupIdentifier,
      "gatewayToken" -> gatewayToken,
      "agentId" -> agentId,
      "agentCode" -> agentCode,
      "agentFriendlyName" -> agentFriendlyName,
      "email" -> email
    ) ++
      toJson(oauthTokens, "oauthTokens") ++
      toJson(itmpData, "itmpData") ++
      toJson(mdtpInformation, "mdtpInformation") ++
      toJson(gatewayToken) ++
      unreadMessageCount.map(i => Json.obj("unreadMessageCount" -> i)).getOrElse(Json.obj())


    http.POST[JsValue, HttpResponse](s"$serviceUrl/government-gateway/session/login", payload) flatMap {
      case response @ HttpResponse(201, _, _) =>
        (
          response.header(HeaderNames.AUTHORIZATION),
          response.header(HeaderNames.LOCATION),
          (response.json \ "gatewayToken").asOpt[String]
        ) match {
          case (Some(token), Some(sessionUri), Some(receivedGatewayToken)) =>
            Future.successful((AuthExchange(token, sessionUri), GovernmentGatewayToken(receivedGatewayToken)))
          case _ => Future.failed(new RuntimeException("Internal Error, missing headers or gatewayToken in response from auth-login-api"))
        }
      case response @ HttpResponse(429, _, _) =>
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

  private def toJson(enrolment: EnrolmentData): JsObject =
    Json.obj(
      "key" -> enrolment.name,
      "identifiers" -> enrolment.taxIdentifier.map(taxId => Json.obj(
        "key" -> taxId.key,
        "value" -> taxId.value
      )),
      "state" -> enrolment.state
    )

  private def toJson(enrolment: DelegatedEnrolmentData): JsObject =
    Json.obj(
      "key" -> enrolment.key,
      "identifiers" -> enrolment.taxIdentifier.map(taxId => Json.obj(
        "key" -> taxId.key,
        "value" -> taxId.value
      )),
      "delegatedAuthRule" -> enrolment.delegatedAuthRule
    )

  private def toJson[A: Writes](optData: Option[A], key: String): JsObject =
    optData.map(data => Json.obj(key -> Json.toJson(data))).getOrElse(Json.obj())

  private def toJson(gatewayToken: Option[String]): JsObject = {
    val ggToken = gatewayToken.map(token => "gatewayToken" -> Json.toJsFieldJsValueWrapper(token))

    Json.obj("gatewayInformation" -> Json.obj(scala.Seq(ggToken).flatten: _*))
  }

  private def removeEmptyValues(fields: (String, Option[String])*): JsObject = {
    val onlyDefinedFields = fields
      .collect {
        case (key, Some(value)) => key -> Json.toJsFieldJsValueWrapper(value)
      }
    Json.obj(onlyDefinedFields: _*)
  }

}