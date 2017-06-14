package OnlineOrders

object Models {
  case class OrderRequest(
    email    : String,
    address  : String,
    item     : String,
    quantity : Int
  )


}


object DirectApproach {

}
