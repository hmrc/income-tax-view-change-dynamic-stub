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

import play.api.mvc.QueryStringBindable

import scala.util.{Failure, Success, Try}

case class ContinueUrl(url: String)

object ContinueUrl {
  private def errorFor(invalidUrl: String) = s"'$invalidUrl' is not a valid continue URL"

  implicit def queryBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[ContinueUrl] =
    new QueryStringBindable[ContinueUrl] {
      def bind(key: String, params: Map[String, scala.Seq[String]]): Option[Either[String, ContinueUrl]] =
        stringBinder.bind(key, params).map {
          case Right(s) =>
            Try(ContinueUrl(s)) match {
              case Success(url) =>
                Right(url)
              case Failure(_) =>
                Left(errorFor(s)) // TODO - this can never be reached as there is no validation
            }
          case Left(message) => Left(message)
        }

      def unbind(key: String, value: ContinueUrl): String = stringBinder.unbind(key, value.url)
    }
}
