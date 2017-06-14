package HttpClient

object Models {

  case class Meta(
    limit          : Int,
    next           : Option[String],
    offset         : Int,
    previous       : Option[String],
    total_count    : Int
  )

  case class TastyPieResponse[A](meta: Meta, objects: List[A])
}
