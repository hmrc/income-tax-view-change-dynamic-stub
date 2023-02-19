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

package models

import play.api.libs.json.{Json, Reads}

import scala.collection.Seq

case class ConfidenceLevel(value: Int) extends AnyVal

object ConfidenceLevel {
  val values: Seq[String] = Seq("50", "200", "250")
}

object CredStrength {
  val values: Seq[String] = Seq("strong", "weak", "none")
}

case class AffinityGroup(value: String) extends AnyVal

object AffinityGroup {
  val values: Seq[String] = Seq("Individual", "Organisation", "Agent")
}

object CredentialRole {
  val values: Seq[String] = Seq("User", "Assistant")
}

case class CredId(value: String) extends AnyVal

case class EnrolmentData(name: String, state: String, taxIdentifier: scala.Seq[TaxIdentifierData])

case class DelegatedEnrolmentData(key: String, taxIdentifier: Seq[TaxIdentifierData], delegatedAuthRule: String)

case class TaxIdentifierData(key: String, value: String)

case class GovernmentGatewayToken(gatewayToken: String)

object GovernmentGatewayToken {
  implicit val reads: Reads[GovernmentGatewayToken] = {
    Json.reads[GovernmentGatewayToken]
  }
}