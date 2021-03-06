/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import models.DataModel
import models.HttpMethod._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.SchemaValidation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SetupDataController @Inject()(
                                     schemaValidation: SchemaValidation,
                                     dataRepository: DataRepository,
                                     cc: ControllerComponents,
                                     applicationConfig: Config
                                   ) extends BackendController(cc) {

  val ignoreJsonValidation: Boolean = applicationConfig.getBoolean("schemaValidation.ignoreJsonValidation")

  val addData: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      withJsonBody[DataModel](
        json => json.method.toUpperCase match {
          case GET | POST =>
            schemaValidation.validateUrlMatch(json.schemaId, json._id) flatMap {
              case true =>
                schemaValidation.validateResponseJson(json.schemaId, json.response) flatMap {
                  case true if json.schemaId == "getDesObligations" => addStubDataToDB(updateObligationsWithDateParameters(json))
                  case true | `ignoreJsonValidation` => addStubDataToDB(json)
                  case false => Future.successful(BadRequest(s"The Json Body:\n\n${json.response.get} did not validate against the Schema Definition"))
                }
              case false =>
                schemaValidation.loadUrlRegex(json.schemaId) map {
                  regex => BadRequest(s"URL ${json._id} did not match the Schema Definition Regex $regex")
                }
            }
          case x => Future.successful(BadRequest(s"The method: $x is currently unsupported"))
        }
      ).recover {
        case _ => InternalServerError("Error Parsing Json DataModel")
      }
  }

  private def updateObligationsWithDateParameters(data: DataModel) = {
    val fulfilledObligations: Boolean = data._id.split("[?]").last.split("[&]").contains("status=F")
    val toDate: LocalDate = LocalDate.now()
    val fromDate: LocalDate = toDate.minusDays(365)

    if (fulfilledObligations) data.copy(_id = data._id + s"&from=$fromDate&to=$toDate")
    else data
  }

  private def addStubDataToDB(json: DataModel): Future[Result] = {
    dataRepository().addEntry(json).map(_.ok match {
      case true => Ok(s"The following JSON was added to the stub: \n\n${Json.toJson(json)}")
      case _ => InternalServerError(s"Failed to add data to Stub.")
    })
  }

  val removeData: String => Action[AnyContent] = url => Action.async {
    implicit request =>
      dataRepository().removeById(url).map(_.ok match {
        case true => Ok("Success")
        case _ => InternalServerError("Could not delete data")
      })
  }

  val removeAll: Action[AnyContent] = Action.async {
    implicit request =>
      dataRepository().removeAll().map(_.ok match {
        case true => Ok("Removed All Stubbed Data")
        case _ => InternalServerError("Unexpected Error Clearing MongoDB.")
      })
  }
}
