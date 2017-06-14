package HttpClient


object Config {

  object sourceConfig {
    val host = "https://myapi.com"
    val user = "username"
    val password = "pwd"

    val API_CHUNK_SIZE = 100

    val API_AUTHORIZATION_ENCODED_CREDS = s"$user:$password"
    val API_AUTHORIZATION_HEADER = s"Basic $API_AUTHORIZATION_ENCODED_CREDS"


    object URLs {
      def recentIds(path: String) = s"/bpa/recent/$path/"
      def resources(path: String) = s"/bpa/$path/"
    }
  }
}
