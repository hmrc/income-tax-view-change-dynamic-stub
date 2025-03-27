/*
 * Copyright 2025 HM Revenue & Customs
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

import adts._
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.yaml.syntax._
import io.circe.{Json, ParsingFailure}
import play.api.Logger

import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Try

class JsonYamlSchemaValidator {

  def yamlToJson(yamlString: String): Either[ValidationError, Json] = {
    val parsed = io.circe.yaml.parser.parse(yamlString)

    parsed match {
      case Right(Json.Null) =>
        Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Parsed as null: Invalid YAML or empty input"))
      case Right(json) if json.isObject || json.isArray =>
        Right(json)
      case Right(_) =>
        Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
      case Left(error) =>
        Left(YamlParsingFailure(error.getMessage))
    }
  }

  def validateJson(jsonSchema: String, payloadJson: String): Either[ValidationError, ReportValidation] = {
    for {
      schemaNode <- Try(JsonLoader.fromString(jsonSchema))
        .toEither
        .left
        .map(e => SchemaParseFailure(e.getMessage))

      payLoadJsonNode <- Try(JsonLoader.fromString(payloadJson))
        .toEither
        .left
        .map(e => PayLoadParseFailure(e.getMessage))

      factory = JsonSchemaFactory.byDefault()
      schema = factory.getJsonSchema(schemaNode)

      report = schema.validate(payLoadJsonNode)
      result <- if (report.isSuccess) {
        Logger("application").info("[JsonYamlConverter][validate] JSON validation was successful.")
        Right(ReportSuccess)
      } else {
        val errors = report.iterator().asScala.map(_.getMessage).mkString(", ")
        Logger("application").warn(s"[JsonYamlConverter][validate] JSON validation failed: $errors")
        Left(ValidationFailure(errors))
      }
    } yield result
  }

  def validateJsonAgainstYamlSchema(yamlSchema: String, jsonPayload: String): Either[ValidationError, ReportValidation] = {
    for {
      jsonSchemaFromYaml <- yamlToJson(yamlSchema)
      validationResult <- validateJson(jsonSchemaFromYaml.spaces2, jsonPayload)
    } yield {
      validationResult
    }
  }
}
