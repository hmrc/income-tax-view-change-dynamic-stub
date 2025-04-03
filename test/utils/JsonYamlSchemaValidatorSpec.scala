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
import testUtils.TestSupport

class JsonYamlSchemaValidatorSpec extends TestSupport {

  val jsonYamlSchemaValidator = new JsonYamlSchemaValidator()

  val yamlSample = formatYaml(readFile("test/resources/sample.yaml"))
  val jsonSample = formatJson(readFile("test/resources/sample.json"))

  val api1566SchemaYaml: String = readFile("test/resources/schemas/yaml/api_1566_schema.yaml")
  val api1566SchemaJson: String = formatJson(readFile("test/resources/schemas/json/api_1566_schema.json"))

  val api1878SchemaYaml: String = readFile("test/resources/schemas/yaml/api_1878_schema.yaml")
  val api1878SchemaJson: String = formatJson(readFile("test/resources/schemas/json/api_1878_schema.json"))

  val api1566DataJson: String = formatJson(readFile("test/resources/data/api_1566_valid_data.json"))
  val api1566DataJsonInvalidData: String = formatJson(readFile("test/resources/data/api_1566_invalid_data.json"))

  val api1878DataJson: String = formatJson(readFile("test/resources/data/api_1878_valid_data.json"))
  val api1878DataJsonInvalidDateAndStatus: String = formatJson(readFile("test/resources/data/api_1878_invalid_date_and_status.json"))
  val invalidJsonData: String = formatJson(readFile("test/resources/data/invalid_data.json"))

  ".yamlToJson()" when {

    "Custom sample yaml" should {

      "convert valid yaml from file to json" in {

        val result = jsonYamlSchemaValidator.yamlToJson(yamlSample)
        result.map(_.spaces2) shouldBe Right(jsonSample)
      }
    }

    "API-1566" should {

      "convert valid yaml api spec from file to correct json schema" in {

        val result = jsonYamlSchemaValidator.yamlToJson(api1566SchemaYaml)
        result.map(_.spaces2) shouldBe Right(api1566SchemaJson)
      }
    }

    "API-1878" should {

      "convert valid yaml api spec from file to correct json schema" in {

        val result = jsonYamlSchemaValidator.yamlToJson(api1878SchemaYaml)
        result.map(_.spaces2) shouldBe Right(api1878SchemaJson)
      }
    }

    "some input formats with errors" should {

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
  }

  ".validateJson()" when {

    "API-1878" should {

      "return true when validating a valid json payload against the correct json schema" in {

        val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, api1878DataJson)

        result shouldBe Right(ReportSuccess)
      }

      "return Left ValidationFailure when validating a invalid json payload against the desired json schema" in {

        val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, invalidJsonData)
        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }

      "return Left ValidationFailure when validating a empty json payload against the desired json schema" in {

        val emptyJson = "{}"

        val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, emptyJson)
        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }

      "return Left PayLoadParseFailure when validating a empty string against the desired json schema" in {

        val emptyString = ""

        val result = jsonYamlSchemaValidator.validateJson(api1878SchemaJson, emptyString)

        result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
      }
    }

    "API-1566" should {

      "return true when validating a valid json payload against the correct json schema" in {

        val result = jsonYamlSchemaValidator.validateJson(api1566SchemaJson, api1566DataJson)

        result shouldBe Right(ReportSuccess)
      }

      "return Left ValidationFailure when validating a invalid json payload against the desired json schema" in {

        val result = jsonYamlSchemaValidator.validateJson(api1566SchemaJson, invalidJsonData)
        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }

      "return Left ValidationFailure when validating a empty json payload against the desired json schema" in {

        val emptyJson = "{}"

        val result = jsonYamlSchemaValidator.validateJson(api1566SchemaJson, emptyJson)
        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }

      "return Left PayLoadParseFailure when validating a empty string against the desired json schema" in {

        val emptyString = ""

        val result = jsonYamlSchemaValidator.validateJson(api1566SchemaJson, emptyString)

        result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
      }
    }
  }


  ".validateJsonAgainstYamlSchema()" when {

    "API-1878" should {

      "return Right(ReportSuccess) when validating a valid json payload against the correct yaml schema" in {

        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, api1878DataJson)
        result shouldBe Right(ReportSuccess)
      }

      "return Left ValidationFailure when json contains multiple invalid data values for date and status field" in {

        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, api1878DataJsonInvalidDateAndStatus)
        result shouldBe Left(ValidationFailure("instance value (\"123456\") not found in enum (possible values: [\"No Status\",\"MTD Mandated\",\"MTD Voluntary\",\"Annual\",\"Non Digital\",\"Dormant\",\"MTD Exempt\"]), string \"ABC\" is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.SSSZ]"))
      }

      "return Left YamlParsingFailure when yaml schema is an empty string" in {

        val noYamlSchema = ""
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(noYamlSchema, api1878DataJson)

        result shouldBe Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
      }

      "return Left PayLoadParseFailure when jsonData is an empty string" in {

        val noJsonData = ""
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, noJsonData)

        result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
      }

      "return Left PayLoadParseFailure when jsonData is empty - {} (object) and not an array" in {

        val emptyJsonData = "{}"
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1878SchemaYaml, emptyJsonData)

        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }
    }

    "API-1566" should {

      "return Right(ReportSuccess) when validating a valid json payload against the correct yaml schema" in {

        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1566SchemaYaml, api1566DataJson)
        result shouldBe Right(ReportSuccess)
      }

      "return Left ValidationFailure with error message when json contains multiple invalid data values" in {

        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1566SchemaYaml, api1566DataJsonInvalidData)
        result shouldBe
          Left(ValidationFailure("instance type (integer) does not match any allowed primitive type (allowed: [\"string\"]), instance type (string) does not match any allowed primitive type (allowed: [\"object\"])"))
      }

      "return Left YamlParsingFailure when yaml schema is an empty string" in {

        val noYamlSchema = ""
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(noYamlSchema, api1566DataJson)

        result shouldBe Left(YamlParsingFailure("[JsonYamlSchemaValidator][yamlToJson] Invalid YAML structure: Unexpected YAML structure"))
      }

      "return Left PayLoadParseFailure when jsonData is an empty string" in {

        val noJsonData = ""
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1566SchemaYaml, noJsonData)

        result shouldBe Left(PayLoadParseFailure("no JSON Text to read from input\n at [Source: (StringReader); line: 1, column: 1]"))
      }

      "return Left PayLoadParseFailure when jsonData is empty - {}" in {

        val emptyJsonData = "{}"
        val result = jsonYamlSchemaValidator.validateJsonAgainstYamlSchema(api1566SchemaYaml, emptyJsonData)

        result shouldBe Left(ValidationFailure("instance type (object) does not match any allowed primitive type (allowed: [\"array\"])"))
      }
    }
  }


}
