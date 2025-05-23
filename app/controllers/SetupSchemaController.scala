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

import models.SchemaModel
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SchemaRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SetupSchemaController @Inject() (
    schemaRepository: SchemaRepository,
    cc:               MessagesControllerComponents
  )(
    implicit val ec: ExecutionContext)
    extends FrontendController(cc)
    with Logging {

  val addSchema: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[SchemaModel](json => {
      schemaRepository
        .addEntry(json)
        .map(_.wasAcknowledged() match {
          case true => Ok(s"Successfully added Schema: ${request.body}")
          case _ =>
            logger.error("[SetupSchemaController][addSchema] could not store data" + json)
            InternalServerError("Could not store data")
        })
    }).recover {
      case err =>
        logger.error("[SetupSchemaController][addSchema] error parsing schemamodel: " + err)
        BadRequest("Error Parsing Json SchemaModel")
    }
  }

  val removeSchema: String => Action[AnyContent] = id =>
    Action.async {
      schemaRepository
        .removeById(id)
        .map(_.wasAcknowledged() match {
          case true => Ok("Success")
          case _    => InternalServerError("Could not delete data")
        })
    }

  val removeAll: Action[AnyContent] = Action.async {
    schemaRepository
      .removeAll()
      .map(_.wasAcknowledged() match {
        case true => Ok("Removed All Schemas")
        case _    => InternalServerError("Unexpected Error Clearing MongoDB.")
      })
  }
}
