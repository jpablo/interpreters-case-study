package HttpClient

import cats.Monad
import cats.data._
import cats.implicits._
import fr.hmil.roshttp.HttpRequest
import HttpClient.Config.sourceConfig.URLs
import HttpClient.FinalTagless.RecentResources
import HttpClient.SourceService.TastyPieResponse
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser._

import scala.scalajs.js.{Dynamic, JSApp}
import scala.util.{Failure, Success, Try}

object FinalTagless {

  trait MyServiceAlg[F[_]] {
    def fetch(resource: String, params: Map[String, Any]): F[String]
  }


  class RecentResources[F[_]: Monad](ms: MyServiceAlg[F]) {
    import ms.fetch
    val F = implicitly[Monad[F]]

    def getRecentIds(path: String, modifiedSince: String = ""): F[Try[List[Long]]] =
      for {
        body <- fetch(URLs.recentIds(path), if (modifiedSince.isEmpty) Map() else Map("modified_since" -> modifiedSince))
      } yield decode[List[Long]](body).toTry

    def getResourcesById[B: Decoder](path: String, ids: List[Long]): F[Try[TastyPieResponse[B]]] =
      for {
        body <- fetch(URLs.resources(path), Map("id__in" -> ids.mkString(","), "limit" -> ids.length))
      } yield decode[TastyPieResponse[B]](body).toTry


    def getRecentResources[B: Decoder](resource: String, modifiedSince: String): F[Try[TastyPieResponse[B]]] =
      getRecentIds(resource, modifiedSince) flatMap {
        case Success(ids) => getResourcesById[B](resource, ids)
        case Failure(f)   => F.pure(Failure(f))
      }
  }
}

object TaglessInterpreter extends JSApp {
  import HttpClient.FinalTagless.MyServiceAlg
  import monix.execution.Scheduler.Implicits.global
  import monix.eval.Task
  import monix.cats._
  import HttpClient.extraDecoders.dynamicDecoder

  def main(): Unit = {

    trustCerts()

    lazy val result = new RecentResources(TaskInterpreter).getRecentResources[Dynamic]("practices", "2017-06-11")

    result runOnComplete {
      case Success(v) => println(v)
      case Failure(e) =>
        println("error:")
        println(e)
    }

    object TaskInterpreter extends MyServiceAlg[Task] {
      def fetch(resource: String, params: Map[String, Any]): Task[String] = {
        val url = s"${Config.sourceConfig.host}$resource"
        val request = HttpRequest(url)
          .withQueryParameters(params.mapValues(_.toString).toList: _*)
          .withHeader("Authorization", Config.sourceConfig.API_AUTHORIZATION_HEADER)

        Task.deferFuture(request.get).map(_.body)
      }
    }
  }
}
