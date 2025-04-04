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

import com.github.fge.jsonschema.main.JsonSchema
import mocks.MockSchemaRepository
import models.SchemaModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class SchemaValidationSpec extends MockSchemaRepository {

  def setupMocks(schemaModel: SchemaModel): SchemaValidation = {
    when(mockSchemaRepository.findById(ArgumentMatchers.eq(schemaModel._id)))
      .thenReturn(Future.successful(schemaModel))
    new SchemaValidation(mockSchemaRepository)
  }

  def setupFutureFailedMocks(schemaModel: SchemaModel): SchemaValidation = {
    when(mockSchemaRepository.findById(ArgumentMatchers.eq(schemaModel._id)))
      .thenThrow(new RuntimeException("Schema could not be retrieved/found in MongoDB"))
    new SchemaValidation(mockSchemaRepository)
  }

  val schema = Json.parse(
    """{
      |  "title": "Person",
      |  "type": "object",
      |  "properties": {
      |    "firstName": {"type": "string"},
      |    "lastName": {"type": "string"}
      |  },
      |  "required": ["firstName", "lastName"]
    }""".stripMargin
  )

  val postSchema = Json.parse(
    """{
      |  "title": "Person",
      |  "type": "object",
      |  "properties": {
      |    "firstName": {"type": "string"},
      |    "lastName": {"type": "string"}
      |  },
      |  "required": ["firstName", "lastName"]
    }""".stripMargin
  )

  ".loadResponseSchema()" should {
    "with a matching schema in mongo" should {
      lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))

      "return a json schema" in {
        lazy val result = validation.loadResponseSchema("testSchema")
        await(result).isInstanceOf[JsonSchema]
      }
    }

    "without a matching schema in mongo" should {
      "throw an exception" in {
        val validation = setupFutureFailedMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))

        val ex = intercept[RuntimeException] {
          await(validation.loadResponseSchema("testSchema"))
        }
        ex.getMessage shouldEqual "Schema could not be retrieved/found in MongoDB"
      }
    }
  }

  ".validateResponseJson()" should {
    "with a valid json body" should {
      "return true" in {
        val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))
        val json = Json.parse("""{ "firstName" : "Bob", "lastName" : "Bobson" }""")
        val result = validation.validateResponseJson("testSchema", Some(json))
        await(result) shouldEqual true
      }
    }

    "with an invalid json body" should {
      lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))
      val json = Json.parse("""{ "firstName" : "Bob" }""")
      lazy val result = validation.validateResponseJson("testSchema", Some(json))

      "return false" in {
        await(result) shouldEqual false
      }
    }
  }

  ".loadUrlRegex()" should {
    lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))
    "return the url of the SchemaModel" in {
      lazy val result = validation.loadUrlRegex("testSchema")
      await(result) shouldEqual "/test"
    }
  }

  ".validateUrlMatch()" should {
    lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema))
    "return 'true' if the urls match" in {
      lazy val result = validation.validateUrlMatch("testSchema", "/test")
      await(result) shouldEqual true
    }
  }

  ".loadRequestSchema()" should {
    "with a matching schema in mongo" should {
      lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema, requestSchema = Some(postSchema)))

      "return a json schema" in {
        lazy val result = validation.loadRequestSchema(postSchema)
        result.isInstanceOf[JsonSchema]
      }
    }
  }

  ".validateRequestJson()" should {
    "with a valid json body" should {
      "return true" in {
        val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema, requestSchema = Some(postSchema)))
        val json = Json.parse("""{ "firstName" : "Bob", "lastName" : "Bobson" }""")
        val result = validation.validateRequestJson("testSchema", Some(json))
        await(result) shouldEqual true
      }
    }

    "with an invalid json body" should {
      lazy val validation = setupMocks(SchemaModel("testSchema", "/test", "GET", responseSchema = schema, requestSchema = Some(postSchema)))
      val json = Json.parse("""{ "firstName" : "Bob" }""")
      lazy val result = validation.validateRequestJson("testSchema", Some(json))

      "return false" in {
        await(result) shouldEqual false
      }
    }
  }
}
