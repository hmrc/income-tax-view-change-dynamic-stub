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

import com.typesafe.config.Config
import models.DataModel
import models.HttpMethod._
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{PopulateYear, SchemaValidation}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SetupDataController @Inject() (
    schemaValidation:  SchemaValidation,
    dataRepository:    DataRepository,
    cc:                MessagesControllerComponents,
    applicationConfig: Config
  )(
    implicit val ec: ExecutionContext)
    extends FrontendController(cc)
    with Logging {

  val ignoreJsonValidation: Boolean = applicationConfig.getBoolean("schemaValidation.ignoreJsonValidation")

  val addData: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[DataModel](json =>
      json.method.toUpperCase match {
        case GET | POST | PUT =>
          schemaValidation.validateUrlMatch(json.schemaId, json._id) flatMap {
            case true =>
              schemaValidation.validateResponseJson(json.schemaId, json.response) flatMap {
                case true if json.schemaId == "getDesObligations" =>
                  addStubDataToDB(updateObligationsWithDateParameters(json))
                case true | `ignoreJsonValidation` =>
                  val modifiedJson = PopulateYear(Json.toJson(json)).as[DataModel]
                  addStubDataToDB(modifiedJson)
                case false =>
                  logger.error("[SetupDataController][addData] validateResponseJson failed" + json._id)
                  Future.successful(
                    BadRequest(s"The Json Body:\n\n${json.response.get} did not validate against the Schema Definition")
                  )
              }
            case false =>
              logger.error("[SetupDataController][addData] validateUrlMatch failed" + json._id)
              schemaValidation.loadUrlRegex(json.schemaId) map { regex =>
                BadRequest(s"URL ${json._id} did not match the Schema Definition Regex $regex")
              }
          }
        case x => Future.successful(BadRequest(s"The method: $x is currently unsupported"))
      }
    ).recover {
      case _ => InternalServerError("Error Parsing Json DataModel")
    }
  }

  private def updateObligationsWithDateParameters(data: DataModel) = {
    val fulfilledObligations: Boolean   = data._id.split("[?]").last.split("[&]").contains("status=F")
    val datesSet:             Boolean   = data._id.contains("&from=") && data._id.contains("&to=")
    val toDate:               LocalDate = LocalDate.now()
    val days365 = 365
    val fromDate: LocalDate = toDate.minusDays(days365)

    if (fulfilledObligations && !datesSet) data.copy(_id = data._id + s"&from=$fromDate&to=$toDate")
    else data
  }

  private def addStubDataToDB(json: DataModel): Future[Result] = {
    dataRepository
      .addEntry(json)
      .map(_.wasAcknowledged() match {
        case true => Ok(s"The following JSON was added to the stub: \n\n${Json.toJson(json)}")
        case _ =>
          logger.error("[SetupDataController][addStubDataToDB] Failed to add data to Stub::" + json)
          InternalServerError(s"Failed to add data to Stub.")
      })
  }

  val removeData: String => Action[AnyContent] = url =>
    Action.async {
      dataRepository
        .removeById(url)
        .map(_.wasAcknowledged() match {
          case true => Ok("Success")
          case _    => InternalServerError("Could not delete data")
        })
    }

  val removeAll: Action[AnyContent] = Action.async {
    dataRepository
      .removeAll()
      .map(_.wasAcknowledged() match {
        case true => Ok("Removed All Stubbed Data")
        case _    => InternalServerError("Unexpected Error Clearing MongoDB.")
      })
  }
}
