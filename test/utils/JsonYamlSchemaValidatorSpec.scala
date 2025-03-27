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
import io.circe.ParsingFailure
import io.circe.yaml.syntax.AsYaml
import testUtils.TestSupport

import scala.io.Source

class JsonYamlSchemaValidatorSpec extends TestSupport {

  val jsonYamlSchemaValidator = new JsonYamlSchemaValidator()

  def readFile(path: String): String = {
    val source = Source.fromFile(path)
    try {
      source.getLines().mkString("\n")
    } finally {
      source.close()
    }
  }

  def formatJson(jsonString: String): String = {
    io.circe.parser.parse(jsonString) match {
      case Right(json) => json.spaces2
      case Left(_) => jsonString
    }
  }

  def formatYaml(yamlString: String): String = {
    io.circe.yaml.parser.parse(yamlString) match {
      case Right(json) => json.asYaml.spaces2
      case Left(_) => yamlString
    }
  }

  val api1878SchemaYaml: String = readFile("test/resources/schemas/api_1878_schema.yaml")
  val api1878SchemaJson: String = formatJson(readFile("test/resources/schemas/api_1878_schema.json"))

  val api1878DataJson: String = formatJson(readFile("test/resources/data/api_1878_data.json"))
  val api1878DataJsonInvalidDateAndStatus: String = formatJson(readFile("test/resources/data/api_1878_invalid_date_and_status.json.json"))
  val invalidJsonData: String = formatJson(readFile("test/resources/data/invalid_data.json"))

  ".yamlToJson()" should {

    "convert valid yaml from file to json" in {

      val yamlStringFromFile = formatYaml(readFile("test/resources/sample.yaml"))
      val jsonStringFromFile = formatJson(readFile("test/resources/sample.json"))

      val result = jsonYamlSchemaValidator.yamlToJson(yamlStringFromFile)
      result.map(_.spaces2) shouldBe Right(jsonStringFromFile)
    }

    "convert valid yaml api spec from file to correct json schema" in {

      val yamlStringFromFile = readFile("test/resources/schemas/api_1878_schema.yaml")
      val jsonStringFromFile = formatJson(readFile("test/resources/schemas/api_1878_schema.json"))

      val result = jsonYamlSchemaValidator.yamlToJson(yamlStringFromFile)
      result.map(_.spaces2) shouldBe Right(jsonStringFromFile)
    }

    "return a Left YamlParsingFailure when given an empty string" in {
      val result = jsonYamlSchemaValidator.yamlToJson("")
      result.isLeft shouldBe true
      result shouldBe Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
    }

    "return a Left YamlParsingFailure when given an invalid yaml string" in {
      val result = jsonYamlSchemaValidator.yamlToJson("???")
      result.isLeft shouldBe true
      result shouldBe Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
    }

    "return a Left YamlParsingFailure when given malformed yaml" in {
      val malformedYaml =
        """
      name: John
      age: thirty  # Invalid type
      hobbies: [reading, coding
      """
      val result = jsonYamlSchemaValidator.yamlToJson(malformedYaml)
      result.isLeft shouldBe true
    }
  }

  ".jsonToYaml()" should {

    "convert valid json file to yaml" in {

      val yamlStringFromFile = formatYaml(readFile("test/resources/sample.yaml"))
      val jsonStringFromFile = formatJson(readFile("test/resources/sample.json"))

      val result = jsonYamlSchemaValidator.jsonToYaml(jsonStringFromFile)
      result shouldBe Right(yamlStringFromFile)
    }

    "return a Left parsing failure when empty string" in {
      val result: Either[ParsingFailure, String] = jsonYamlSchemaValidator.jsonToYaml("")
      result.isLeft shouldBe true
    }

    "return a Left failure when input string is invalid" in {
      val result: Either[ParsingFailure, String] = jsonYamlSchemaValidator.jsonToYaml("{?")
      result.isLeft shouldBe true
    }
  }

  ".validateJson()" should {

    "return true when validating a valid json payload against the correct json schema" in {

      val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, api1878DataJson)

      result shouldBe Right(ReportSuccess)
    }

    "return Left ValidationFailure when validating a invalid json payload against the desired json schema" in {

      val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, invalidJsonData)
      result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
    }

    "return Left ValidationFailure when validating a empty json payload against the desired json schema" in {

      val invalidJsonData = "{}"

      val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, invalidJsonData)
      result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
    }

    "return Left PayLoadParseFailure when validating a empty string against the desired json schema" in {

      val invalidJsonData = ""

      val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, invalidJsonData)

      result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
    }
  }

  ".validateJsonAgainstYamlSchema()" should {

    "return Right(ReportSuccess) when validating a valid json payload against the correct yaml schema" in {

      val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, api1878DataJson)
      result shouldBe Right(ReportSuccess)
    }

    "return Left ValidationFailure when json contains multiple invalid data values for date and status field" in {

      val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, api1878DataJsonInvalidDateAndStatus)
      result shouldBe Left(ValidationFailure("instance value (\"123456\") not found in enum (possible values: [\"No Status\",\"MTD Mandated\",\"MTD Voluntary\",\"Annual\",\"Non Digital\",\"Dormant\",\"MTD Exempt\"]), string \"ABC\" is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.SSSZ]"))
    }

    "return Left YamlParsingFailure when yaml schema is an empty string" in {

      val yamlSchema = ""
      val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(yamlSchema, api1878DataJson)

      result shouldBe Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
    }

    "return Left PayLoadParseFailure when jsonData is an empty string" in {

      val jsonData = ""
      val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, jsonData)

      result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
    }

    "return Left PayLoadParseFailure when jsonData is empty - {}" in {

      val jsonData = "{}"
      val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, jsonData)

      result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
    }
  }
}
