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

import io.circe.ParsingFailure
import io.circe.yaml.syntax.AsYaml
import testUtils.TestSupport

import scala.io.Source

class JsonYamlConverterSpec extends TestSupport {

  val converter = new JsonYamlConverter()

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

  val yamlStringFromFile = formatYaml(readFile("test/resources/sample.yaml"))
  val jsonStringFromFile = formatJson(readFile("test/resources/sample.json"))

  ".yamlToJson()" should {

    "convert valid yaml from file to json" in {
      val result = converter.yamlToJson(yamlStringFromFile)
      result.map(_.spaces2) shouldBe Right(jsonStringFromFile)
    }

    "return a Left parsing failure when given an empty string" in {
      val result = converter.yamlToJson("")
      result.isLeft shouldBe true
      result.left.get shouldBe a[ParsingFailure]
    }

    "return a Left parsing failure when given an invalid yaml string" in {
      val result = converter.yamlToJson("???")
      result.isLeft shouldBe true
      result.left.get shouldBe a[ParsingFailure]
    }

    "return a Left parsing failure when given malformed yaml" in {
      val malformedYaml =
        """
      name: John
      age: thirty  # Invalid type
      hobbies: [reading, coding
      """
      val result = converter.yamlToJson(malformedYaml)
      result.isLeft shouldBe true
      result.left.get shouldBe a[ParsingFailure]
    }
  }

  ".jsonToYaml()" should {

    "convert valid json file to yaml" in {
      val result = converter.jsonToYaml(jsonStringFromFile)
      result shouldBe Right(yamlStringFromFile)
    }

    "return a Left parsing failure when empty string" in {
      val result: Either[ParsingFailure, String] = converter.jsonToYaml("")
      result.isLeft shouldBe true
    }

    "return a Left failure when input string is invalid" in {
      val result: Either[ParsingFailure, String] = converter.jsonToYaml("{?")
      result.isLeft shouldBe true
    }
  }
}
