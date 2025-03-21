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

import io.circe.yaml.syntax._
import io.circe.{Json, ParsingFailure}

class JsonYamlConverter {

  def yamlToJson(yamlString: String): Either[ParsingFailure, Json] = {
    val parsed = io.circe.yaml.parser.parse(yamlString)

    parsed match {
      case Right(Json.Null) => Left(ParsingFailure("Parsed as null", new Exception("Invalid YAML or empty input")))
      case Right(json) if json.isObject || json.isArray => Right(json)
      case Right(_) => Left(ParsingFailure("Invalid YAML structure", new Exception("Unexpected YAML structure")))
      case Left(error) => Left(error)
    }
  }


  def jsonToYaml(jsonString: String): Either[ParsingFailure, String] = {
    io.circe.parser.parse(jsonString).map { json =>
      json.asYaml.spaces2
    }
  }

}
