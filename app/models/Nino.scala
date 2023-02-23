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

import play.api.libs.json.{Reads, Writes}
import utils.{SimpleObjectReads, SimpleObjectWrites}

case class Nino(nino: String) {
  val ninoWithoutDescription: String = nino.split(" ").head

  require(Nino.isValid(ninoWithoutDescription), s"$ninoWithoutDescription is not a valid nino.")

  override def toString = ninoWithoutDescription

  private val LengthWithoutSuffix: Int = 8

  def value = ninoWithoutDescription

  val name = "nino"

  def formatted = value.grouped(2).mkString(" ")

  def withoutSuffix = value.take(LengthWithoutSuffix)
}

object Nino extends (String => Nino) {
  implicit val ninoWrite: Writes[Nino] = new SimpleObjectWrites[Nino](_.value)
  implicit val ninoRead: Reads[Nino] = new SimpleObjectReads[Nino]("nino", Nino.apply)

  private val validNinoFormat = "[[A-Z]&&[^DFIQUV]][[A-Z]&&[^DFIQUVO]] ?\\d{2} ?\\d{2} ?\\d{2} ?[A-D]{1}"
  private val invalidPrefixes = List("BG", "GB", "NK", "KN", "TN", "NT", "ZZ")

  private def hasValidPrefix(nino: String) = !invalidPrefixes.exists(nino.startsWith)

  def isValid(nino: String): Boolean = nino != null && hasValidPrefix(nino) && nino.matches(validNinoFormat)

  val validFirstCharacters: Seq[String] = ('A' to 'Z').filterNot(List('D', 'F', 'I', 'Q', 'U', 'V').contains).map(_.toString)
  val validSecondCharacters: Seq[String] = ('A' to 'Z').filterNot(List('D', 'F', 'I', 'O', 'Q', 'U', 'V').contains).map(_.toString)
  val validPrefixes: Seq[String] = validFirstCharacters.flatMap(a => validSecondCharacters.map(a + _)).filterNot(invalidPrefixes.contains(_))
  val validSuffixes: Seq[String] = ('A' to 'D').map(_.toString)
}