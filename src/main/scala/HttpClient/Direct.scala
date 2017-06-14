package HttpClient

import fr.hmil.roshttp.HttpRequest
import HttpClient.Config.sourceConfig.URLs
import HttpClient.Models.TastyPieResponse
import io.circe.{Decoder, Json}
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import monix.eval.Task
import scala.util.{Failure, Success, Try}
import monix.execution.Scheduler.Implicits.global


object Direct {

  // basic operation

  def fetch(path: String, params: Map[String, Any]): Task[String] = {

    val request = HttpRequest(s"${Config.sourceConfig.host}$path")
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

object DirectApp extends App {

  import Direct.getRecentResources

  def main(): Unit = {

    val program = getRecentResources[Json]("practices", "2017-06-11")

    program runOnComplete {
      case Success(v) => println(v)
      case Failure(e) =>
        println("error:")
        println(e)
    }
  }
}
