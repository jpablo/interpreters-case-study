package HttpClient

import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._


import scala.scalajs.js.{Dynamic, JSON}
import scala.util.{Failure, Success, Try}

object extraDecoders {

  implicit val dynamicDecoder = new Decoder[Dynamic] {
    def apply(c: HCursor) = Try(JSON.parse(c.value.asJson.noSpaces)) match {
      case Failure(exception) => Left(DecodingFailure(exception.getMessage, List.empty))
      case Success(value) => Right(value)
    }
  }


}
