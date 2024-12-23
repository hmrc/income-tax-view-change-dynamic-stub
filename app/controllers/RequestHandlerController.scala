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

import io.circe._
import models.HttpMethod
import models.HttpMethod._
import org.mongodb.scala.model.Filters._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SchemaValidation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RequestHandlerController @Inject()(schemaValidation: SchemaValidation,
                                         dataRepository: DataRepository,
                                         cc: MessagesControllerComponents,
                                         defaultValues: DefaultValues)
                                        (implicit val ec: ExecutionContext)
  extends FrontendController(cc) with Logging {

  def getRequestHandler(url: String): Action[AnyContent] = Action.async {
    implicit request => {

      // Detect 1553 calls
      if (url.contains("financial-data")) {
        val fromDate = request.getQueryString("dateFrom").get
        val toDate = request.getQueryString("dateTo").get
        val from = LocalDate.parse(fromDate)
        val to = LocalDate.parse(toDate)
        logger.error(s"RequestHandlerController-URI: ${request.uri} - ${fromDate} - ${toDate} - ${to.getYear - from.getYear}")

        // Detect that its a request for a range of TaxYears
        if (to.getYear - from.getYear > 1) {

          // Call mongoDb for the give range of taxYears
          val baseUrl = request.uri.replace(fromDate, "TaxYearFrom").replace(toDate, "TaxYearTo")

          val mongoResponses: Future[IndexedSeq[Option[JsValue]]] = Future.sequence( {
            (0 to (to.getYear - from.getYear) - 1)
            .map { delta =>
              val f = from.plusYears(delta)
              val t = from.plusYears(delta + 1).plusDays(-1)
              //logger.error(s"RequestHandlerController-Range: $f - $t")
              baseUrl
                .replace("TaxYearFrom", f.format(DateTimeFormatter.ISO_DATE))
                .replace("TaxYearTo", t.format(DateTimeFormatter.ISO_DATE))
            }.map(mongoUrl => {
              //logger.error(s"RequestHandlerController-MongoUrl: $mongoUrl")
              dataRepository.find(equal("_id", mongoUrl), equal("method", GET)).map {
                stubData =>
                  if (stubData.nonEmpty) {
                    if (stubData.head.response.isEmpty) {
                      None
                    } else {
                      stubData.head.response
                    }
                  } else {
                    None
                  }
              }
            })
          })

          val jsonListOfStrins: Future[List[String]] = mongoResponses.flatMap{ x =>
           Future.successful( x.toList.map { y =>
              y.map(_.toString()).getOrElse("")
            })
          }

          // Merging logic
          {
            for {
              ls <- jsonListOfStrins
            } yield {
              // Circe Json processing logic
              //val doc = io. parse(json).getOrElse(Json.Null)
              //logger.error(s"RequestHandlerController-33/ ->")

              // Get list of all documentDetails
              val dds = ls.map{
                json => {
                  val doc = io.circe.parser.parse(json).getOrElse(Json.Null)
                  val cursor: HCursor = doc.hcursor
                  val documentDetails = cursor.downField("documentDetails").values.getOrElse(List.empty).toList
                  //logger.error(s"RequestHandlerController-DocDetails: ${documentDetails.values.get.toList}")
                  documentDetails
                }
              }.flatten

              // Get list of all financialDetails
              val fds = ls.map{
                json => {
                  val doc = io.circe.parser.parse(json).getOrElse(Json.Null)
                  val cursor: HCursor = doc.hcursor
                  val financialDetails = cursor.downField("financialDetails").values.getOrElse(List.empty).toList
                  //logger.error(s"RequestHandlerController-DocDetails: ${documentDetails.values.get.toList}")
                  financialDetails
                }
              }.flatten

              //logger.error(s"RequestHandlerController-22/ -> ${dds}")
              // Get any 1553 response from the list ? lets take last one
              // balanceDetails must be the same across all these responses???
              val doc = io.circe.parser.parse(ls.last).getOrElse(Json.Null)

              // Replace DocumentDetails
              val documentDetails = doc.hcursor.downField("documentDetails")
                .withFocus(_ => Json.fromValues(dds))

              // Replace FinancialDetails
              val financialDetails = documentDetails.top.getOrElse(Json.Null)
                  .hcursor.downField("financialDetails")
                .withFocus(_ => Json.fromValues(fds))

              // Get top document
              val finalJsonDocumentAsString: String = financialDetails.top.getOrElse(Json.Null).toString()

              logger.error(s"RequestHandlerController-FinalJson: ${finalJsonDocumentAsString}")
              val js =  play.api.libs.json.Json.parse(finalJsonDocumentAsString)

// For debug only
//              import java.nio.charset.StandardCharsets
//              import java.nio.file.{Files, Paths}
//              Files.write( Paths.get("1553_final_response.json"), finalJsonDocumentAsString.getBytes(StandardCharsets.UTF_8) )

              Future.successful(Status(200)(js))
            }
          }.flatten


        } else {
          dataRepository.find(equal("_id", request.uri), equal("method", GET)).map {
            stubData =>
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
      else {
        dataRepository.find(equal("_id", request.uri), equal("method", GET)).map {
          stubData =>
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


  def postRequestHandler(url: String): Action[AnyContent] = Action.async {
    implicit request => {
      dataRepository.find(equal("_id", s"""${request.uri}"""), equal("method", POST)).flatMap {
        stubData =>
          if (stubData.nonEmpty) {
            schemaValidation.validateRequestJson(stubData.head.schemaId, request.body.asJson) map {
              case true => if (stubData.head.response.isEmpty) {
                Status(stubData.head.status)
              } else {
                Status(stubData.head.status)(stubData.head.response.get)
              }
              case false =>
                BadRequest(s"The Json Body:\n\n${
                  request.body.asJson
                } did not validate against the Schema Definition")
            }
          } else {
            Future(NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: ${
              request.uri
            }"))
          }
      }
    }
  }

  def putRequestHandler(url: String): Action[AnyContent] = Action.async {
    implicit request => {
      dataRepository.find(equal("_id", s"""${request.uri}"""), equal("method", HttpMethod.PUT)).flatMap {
        stubData =>
          if (stubData.nonEmpty) {
            schemaValidation.validateRequestJson(stubData.head.schemaId, request.body.asJson) map {
              case true => if (stubData.head.response.isEmpty) {
                Status(stubData.head.status)
              } else {
                Status(stubData.head.status)(stubData.head.response.get)
              }
              case false =>
                BadRequest(s"The Json Body:\n\n${
                  request.body.asJson
                } did not validate against the Schema Definition")
            }
          } else {
            Future(NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: ${
              request.uri
            }"))
          }
      }
    }
  }

}
