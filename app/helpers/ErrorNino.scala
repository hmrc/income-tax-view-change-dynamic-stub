/*
 * Copyright 2017 HM Revenue & Customs
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

package helpers

case class ErrorNino(nino: String)

object ErrorNino {
  val notFoundNino = ErrorNino("AA404404A")
  val badRequest = ErrorNino("AA400400A")
  val internalServerError = ErrorNino("AA500500A")
  val badGateway = ErrorNino("AA502502A")
  val serviceUnavailable = ErrorNino("AA503503A")
  val timeout = ErrorNino("AA408408A")
}