import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport



package object HttpClient {
  // JS: `const https = require('https');`
  @js.native
  @JSImport("https", JSImport.Namespace)
  object https extends js.Object {
    val globalAgent: js.Dynamic = js.native
  }

  // JS: `const sslRootCas = require('ssl-root-cas/latest');`
  @scalajs.js.native
  @JSImport("ssl-root-cas/latest", JSImport.Namespace)
  object sslRootCas extends js.Object {
    def create(): js.Dynamic = js.native
  }


  def trustCerts() = {
    val rootCas = sslRootCas.create()
    rootCas.addFile("myOrgCA.pem")
    https.globalAgent.options.ca = rootCas
  }
}
