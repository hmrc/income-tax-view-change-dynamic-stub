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

import io.circe._
import models.HttpMethod._
import org.apache.pekko.actor.ActorSystem
import org.mongodb.scala.model.Filters._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest, Result, WrappedRequest}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.AddDelays

import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class FinancialDetailsRequestController @Inject()(cc: MessagesControllerComponents,
                                                  dataRepository: DataRepository,
                                                  defaultValues: DefaultValues,
                                                  requestHandlerController: RequestHandlerController)
                                                 (implicit val ec: ExecutionContext,
                                                  val actorSystem: ActorSystem) extends FrontendController(cc) with Logging with AddDelays {

  private def addSuffixToRequest(
      key:    String,
      suffix: String
    )(
      implicit request: MessagesRequest[AnyContent]
    ): WrappedRequest[AnyContent] = {
    val testHeader     = request.headers.get("Gov-Test-Scenario")
    val computedSuffix = if (testHeader.contains(key)) s"&$suffix" else ""
    val uri            = request.uri + computedSuffix
    val newRequest     = request.withTarget(request.target.withUri(URI.create(uri)))
    newRequest
  }

  def transform(): Action[AnyContent] =
    Action.async { implicit request =>
      withDelay(700.milliseconds) {
        logger.error(s"Calling 1553 override =>")
        val nino: String = request.queryString.get("idNumber").map(_.head).getOrElse("DefaultNino")
        if (request.uri.contains("dateFrom")) {
          callIndividualYears(nino)(addSuffixToRequest("afterPoaAmountAdjusted", "afterPoaAmountAdjusted=true"))
        } else {
          requestHandlerController.getRequestHandler(request.uri).apply(request)
        }
      }
    }

  def callIndividualYears(nino: String)(implicit request: WrappedRequest[AnyContent]): Future[Result] = {
    val fromDate = request.getQueryString("dateFrom").get
    val toDate   = request.getQueryString("dateTo").get
    val from     = LocalDate.parse(fromDate)
    val to       = LocalDate.parse(toDate)

    logger.info(s"RequestHandlerController-URI: ${request.uri} - ${fromDate} - ${toDate} - ${to.getYear - from.getYear}")

    // Return error if requesting a range of more than 5 tax years
    if (to.getYear - from.getYear > 5) {
      Future.successful(Status(BAD_REQUEST))
    } else {
      // Detect that its a request for a range of TaxYears
      if (to.getYear - from.getYear > 1) {

        // Call mongoDb for the give range of taxYears
        val baseUrl = request.uri.replace(fromDate, "TaxYearFrom").replace(toDate, "TaxYearTo")
        val mongoResponses: Future[IndexedSeq[Option[JsValue]]] = getSingleResponseFromMongo(baseUrl, from, to)

        val jsonListOfStrings: Future[List[String]] = mongoResponses.flatMap { x =>
          Future.successful(
            x.toList
              .map { y =>
                y.map(_.toString()).getOrElse("")
              }
              .filter(_ != "")
          )
        }

        // Merging logic
        {
          for {
            ls <- jsonListOfStrings
          } yield {
            if (ls.isEmpty) {
              //If we found no data, return 404
              Future.successful(Status(NOT_FOUND))
            } else {
              val js: JsValue = jsonMerge(ls)

              // For debug only
              // import java.nio.charset.StandardCharsets
              // import java.nio.file.{Files, Paths}
              // Files.write( Paths.get("1553_final_response.json"), finalJsonDocumentAsString.getBytes(StandardCharsets.UTF_8) )

              Future.successful(Status(OK)(js))
            }
          }
        }.flatten
      }
      else {
        dataRepository.find(equal("_id", request.uri), equal("method", GET)).map { stubData =>
          if (stubData.nonEmpty) {
            if (stubData.head.response.isEmpty) {
              Status(stubData.head.status)
            } else {
             Status(stubData.head.status)(stubData.head.response.get)
            }
          } else {
            val url = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"
            defaultValues.getResponse(url)
          }
        }
      }
    }
  }

  private case class Accumulator(jsons: Set[Json], docIds: Set[String])

  private def filterByUniqueDocumentId(unfiltered: List[Json]): Set[Json] = {
    //the List[Json] accumulates the raw JSons for the unique entries, and the List[String] accumulates all the different documentIds so far, to check against
    unfiltered
      .foldLeft(Accumulator(Set(), Set()))((acc, next) => {
        val cursor = next.hcursor
        cursor.getOrElse("documentId")("failed") match {
          case Left(_) => acc
          case Right(docIdString) =>
            if (docIdString == "failed" || acc.docIds.contains(docIdString)) {
              logger.debug(
                s"FinancialDetailsRequestController-filterByUniqueDocumentId: Duplicate data with docId $docIdString"
              )
              acc
            } else {
              Accumulator(acc.jsons + next, acc.docIds + docIdString)
            }
        }
      })
      .jsons
  }

  private def jsonMerge(jsons: List[String]): JsValue = {

    // Get list of all documentDetails
    val dds = jsons.flatMap { json =>
      {
        val doc = io.circe.parser.parse(json).getOrElse(Json.Null)
        val cursor: HCursor = doc.hcursor
        val documentDetails = cursor.downField("documentDetails").values.getOrElse(List.empty).toList
        documentDetails
      }
    }
    val uniqueDds = filterByUniqueDocumentId(dds)

    // Get list of all financialDetails
    val fds = jsons.flatMap { json =>
      {
        val doc = io.circe.parser.parse(json).getOrElse(Json.Null)
        val cursor: HCursor = doc.hcursor
        val financialDetails = cursor.downField("financialDetails").values.getOrElse(List.empty).toList
        financialDetails
      }
    }

    // Get any 1553 response from the list ? lets take last one
    // balanceDetails must be the same across all these responses???
    val doc = io.circe.parser.parse(jsons.last).getOrElse(Json.Null)

    // Replace DocumentDetails
    val documentDetails = doc.hcursor
      .downField("documentDetails")
      .withFocus(_ => Json.fromValues(uniqueDds))

    // Replace FinancialDetails
    val financialDetails = documentDetails.top
      .getOrElse(Json.Null)
      .hcursor
      .downField("financialDetails")
      .withFocus(_ => Json.fromValues(fds))

    // Get top document
    val finalJsonDocumentAsString: String = financialDetails.top.getOrElse(Json.Null).toString()

    logger.debug(s"FinancialDetailsRequestController-FinalJson: ${finalJsonDocumentAsString}")
    val js = play.api.libs.json.Json.parse(finalJsonDocumentAsString)
    js
  }

  private def getSingleResponseFromMongo(baseUrl: String, from: LocalDate, to: LocalDate) = {
    val mongoResponses: Future[IndexedSeq[Option[JsValue]]] = Future.sequence({
      (0 to (to.getYear - from.getYear) - 1)
        .map { delta =>
          val f = from.plusYears(delta)
          val t = from.plusYears(delta + 1).plusDays(-1)
          baseUrl
            .replace("TaxYearFrom", f.format(DateTimeFormatter.ISO_DATE))
            .replace("TaxYearTo", t.format(DateTimeFormatter.ISO_DATE))
        }
        .map(mongoUrl => {
          dataRepository.find(equal("_id", mongoUrl), equal("method", GET)).map { stubData =>
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
    mongoResponses
  }
}
