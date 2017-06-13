package gateway

import fr.hmil.roshttp.HttpRequest
import gateway.Config.sourceConfig.URLs
import gateway.SourceService.TastyPieResponse
import io.circe.Decoder
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import monix.eval.Task
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success, Try}
import monix.execution.Scheduler.Implicits.global


object Direct {

  // basic operation

  def fetch(resource: String, params: Map[String, Any]): Task[String] = {
    val url = s"${Config.sourceConfig.host}$resource"
    val request = HttpRequest(url)
      .withQueryParameters(params.mapValues(_.toString).toList: _*)
      .withHeader("Authorization", Config.sourceConfig.API_AUTHORIZATION_HEADER)

    Task.deferFuture(request.get).map(_.body)
  }

  // --- Derived ops

  def getRecentIds(path: String, modifiedSince: String = ""): Task[Try[List[Long]]] =
    for {
      body <- fetch(URLs.recentIds(path), if (modifiedSince.isEmpty) Map() else Map("modified_since" -> modifiedSince))
    } yield decode[List[Long]](body).toTry

  def getResourcesById[B: Decoder](path: String, ids: List[Long]): Task[Try[TastyPieResponse[B]]] =
    for {
      body <- fetch(URLs.resources(path), Map("id__in" -> ids.mkString(","), "limit" -> ids.length))
    } yield decode[TastyPieResponse[B]](body).toTry


  def getRecentResources[B: Decoder](resource: String, modifiedSince: String): Task[Try[TastyPieResponse[B]]] =
    getRecentIds(resource, modifiedSince) flatMap {
      case Success(ids) => getResourcesById[B](resource, ids)
      case Failure(f) => Task.now(Failure(f))
    }
}

object DirectApp extends JSApp {
  import Direct.getRecentResources
  import gateway.extraDecoders.dynamicDecoder
  import scala.scalajs.js.Dynamic

  def main(): Unit = {

    trustCerts()

    val program = getRecentResources[Dynamic]("practices", "2017-06-11")

    program runOnComplete {
      case Success(v) => println(v)
      case Failure(e) =>
        println("error:")
        println(e)
    }
  }
}
