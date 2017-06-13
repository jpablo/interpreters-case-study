package gateway

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

object SourceService {

  case class Meta(
    limit          : Int,
    next           : Option[String],
    offset         : Int,
    previous       : Option[String],
    total_count    : Int
  )

  case class TastyPieResponse[A](meta: Meta, objects: List[A])


}
