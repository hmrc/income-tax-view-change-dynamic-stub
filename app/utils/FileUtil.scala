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

import models.{Nino, UserCredentials, UserRecord}

import scala.io.Source
import scala.util.Try

object FileUtil {

  def getUsersFromFile(path: String): Either[Throwable, List[UserRecord]] = {
    Try {
      val lines = Source.fromFile(path).getLines().toList
      lines.map(line => {
        Try {
          val recs = line.split('$')
          UserRecord(recs(0), recs(1), recs(2), recs(3))
        }.toOption
      }).flatten
    }.toEither
  }

  def getUserCredentials(nino: Nino): Either[Throwable, UserCredentials] = {
    FileUtil.getUsersFromFile("conf/data/users.txt") match {
      case Left(ex) => Left(ex)
      case Right(records) =>
        records.find(record => record.nino == nino.nino) match {
          case None =>
            Left(new RuntimeException("Can not fine user by nino"))
          case Some(record) =>
            Right(
              UserCredentials(credId = UserCredentials.credId,//"6528180096307862",
                affinityGroup = "Individual",
                confidenceLevel = 250,
                credentialStrength = "strong",
                Role = "User",
                enrolmentData = EnrolmentValues(record.mtditid, record.utr)
              )
            )
        }
    }
  }

}
