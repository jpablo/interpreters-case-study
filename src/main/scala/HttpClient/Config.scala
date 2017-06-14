package HttpClient

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

@js.native @JSName("Buffer")
class Buffer(s: String) extends js.Object { @JSName("toString") def toString_(f: String): String = js.native }

object Config {

  object sourceConfig {
    val host = "https://myapi.com"
    val user = "username"
    val password = "pwd"

    val API_CHUNK_SIZE = 100

    val API_AUTHORIZATION_ENCODED_CREDS = new Buffer(s"$user:$password").toString_("base64")
    val API_AUTHORIZATION_HEADER = s"Basic $API_AUTHORIZATION_ENCODED_CREDS"


    object URLs {
      def recentIds(path: String) = s"/bpa/recent/$path/"
      def resources(path: String) = s"/bpa/$path/"
    }
  }
}
