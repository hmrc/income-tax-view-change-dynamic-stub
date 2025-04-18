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

package utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.core.report.{LogLevel, ProcessingReport}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.Logging
import play.api.libs.json.JsValue
import repositories.SchemaRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaValidation @Inject() (repository: SchemaRepository)(implicit val ec: ExecutionContext) extends Logging {

  private final lazy val jsonMapper  = new ObjectMapper()
  private final lazy val jsonFactory = jsonMapper.getFactory

  def loadResponseSchema(schemaId: String): Future[JsonSchema] = {
    val schemaMapper = new ObjectMapper()
    val factory      = schemaMapper.getFactory
    repository.findById(schemaId).map { response =>
      val schemaParser: JsonParser = factory.createParser(response.responseSchema.toString)
      val schemaJson:   JsonNode   = schemaMapper.readTree(schemaParser)
      JsonSchemaFactory.byDefault().getJsonSchema(schemaJson)
    } recover {
      case ex => throw new Exception("Schema could not be retrieved/found in MongoDB")
    }
  }

  def validateResponseJson(schemaId: String, json: Option[JsValue]): Future[Boolean] = {
    json.fold(Future.successful(true)) { response =>
      loadResponseSchema(schemaId).map { schema =>
        val jsonParser = jsonFactory.createParser(response.toString)
        val jsonNode: JsonNode         = jsonMapper.readTree(jsonParser)
        val report:   ProcessingReport = schema.validate(jsonNode)
        report.forEach(message =>
          if (List(LogLevel.ERROR, LogLevel.FATAL).contains(message.getLogLevel))
            logger.error(s"${message.getLogLevel.toString} : $message")
        )
        schema.validate(jsonNode).isSuccess
      }
    }
  }

  def loadRequestSchema(requestSchema: JsValue): JsonSchema = {
    val schemaMapper = new ObjectMapper()
    val factory      = schemaMapper.getFactory
    val schemaParser: JsonParser = factory.createParser(requestSchema.toString)
    val schemaJson:   JsonNode   = schemaMapper.readTree(schemaParser)
    JsonSchemaFactory.byDefault().getJsonSchema(schemaJson)
  }

  def validateRequestJson(schemaId: String, json: Option[JsValue]): Future[Boolean] = {
    repository.findById(schemaId).map { schema =>
      if (schema.requestSchema.isDefined) {
        json.fold(true) { response =>
          val jsonParser = jsonFactory.createParser(response.toString)
          val jsonNode: JsonNode = jsonMapper.readTree(jsonParser)
          loadRequestSchema(schema.requestSchema.get).validate(jsonNode).isSuccess
        }
      } else {
        true
      }
    } recover {
      case ex => throw new Exception("Schema could not be retrieved/found in MongoDB")
    }
  }

  def loadUrlRegex(schemaId: String): Future[String] =
    repository.findById(schemaId).map(_.url)

  def validateUrlMatch(schemaId: String, url: String): Future[Boolean] =
    loadUrlRegex(schemaId).map(regex => url.matches(regex))
}
