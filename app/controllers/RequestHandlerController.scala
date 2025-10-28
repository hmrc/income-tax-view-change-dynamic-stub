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

package controllers

import models.HttpMethod
import models.HttpMethod._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import org.mongodb.scala.model.Filters._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{AddDelays, SchemaValidation}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RequestHandlerController @Inject() (
    schemaValidation: SchemaValidation,
    dataRepository:   DataRepository,
    cc:               MessagesControllerComponents,
    defaultValues:    DefaultValues
  )(implicit val ec: ExecutionContext, val actorSystem: ActorSystem)
    extends FrontendController(cc) with AddDelays {

  def getRequestHandler(url: String, delay: Option[FiniteDuration] = None): Action[AnyContent] =
    Action.async { implicit request =>
      {
        withDelay(delay.getOrElse(700.milliseconds)) {
          dataRepository.find(equal("_id", request.uri), equal("method", GET)).map { stubData =>
            if (stubData.nonEmpty) {
              if (stubData.head.response.isEmpty) {
                Status(stubData.head.status)
              } else {
                Status(stubData.head.status)(stubData.head.response.get)
              }
            } else {
              defaultValues.getResponse(url)
            }
          }
        }
      }
    }

  def postRequestHandler(url: String, delay: Option[FiniteDuration] = None): Action[AnyContent] =
    Action.async { implicit request => {
      withDelay(delay.getOrElse(700.milliseconds)) {
        dataRepository.find(equal("_id", s"""${request.uri}"""), equal("method", POST)).flatMap { stubData =>
          if (stubData.nonEmpty) {
            schemaValidation.validateRequestJson(stubData.head.schemaId, request.body.asJson) map {
              case true =>
                if (stubData.head.response.isEmpty) {
                  Status(stubData.head.status)
                } else {
                  Status(stubData.head.status)(stubData.head.response.get)
                }
              case false =>
                BadRequest(s"The Json Body:\n\n${request.body.asJson} did not validate against the Schema Definition")
            }
          } else {
            Future(NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: ${request.uri}"))
          }
        }
      }
    }
  }

  def putRequestHandler(url: String, delay: Option[FiniteDuration] = None): Action[AnyContent] =
    Action.async { implicit request => {
      withDelay(delay.getOrElse(700.milliseconds)) {
        dataRepository.find(equal("_id", s"""${request.uri}"""), equal("method", HttpMethod.PUT)).flatMap { stubData =>
          if (stubData.nonEmpty) {
            schemaValidation.validateRequestJson(stubData.head.schemaId, request.body.asJson) map {
              case true =>
                if (stubData.head.response.isEmpty) {
                  Status(stubData.head.status)
                } else {
                  Status(stubData.head.status)(stubData.head.response.get)
                }
              case false =>
                BadRequest(s"The Json Body:\n\n${request.body.asJson} did not validate against the Schema Definition")
            }
          } else {
            Future(NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: ${request.uri}"))
          }
        }
      }
    }}
}
