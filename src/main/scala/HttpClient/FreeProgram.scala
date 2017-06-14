package HttpClient

import cats.free.Free
import fr.hmil.roshttp.HttpRequest
import HttpClient.Config.sourceConfig.URLs
import HttpClient.Models.TastyPieResponse
import io.circe.{Decoder, Json}
import io.circe.generic.auto._
import io.circe.parser._
import monix.eval.Task
import scala.util.{Failure, Success, Try}

object FreeProgram {



  //  ---- original signature ----

  // def fetch(path: String, params: Map[String, Any]): Task[String]

  // --- DLS primitives

  sealed trait MyServiceA[A]

  case class Fetch(path: String, params: Map[String, Any]) extends MyServiceA[String]

  // --- Smart constructors

  type MyService[A] = Free[MyServiceA, A]

  def fetch(path: String, params: Map[String, Any]): MyService[String] =
    Free.liftF(Fetch(path, params))

  // --- Derived ops

  def getRecentIds(path: String, modifiedSince: String = ""): MyService[Try[List[Long]]] =
    for {
      body <- fetch(URLs.recentIds(path), if (modifiedSince.isEmpty) Map() else Map("modified_since" -> modifiedSince))
    } yield decode[List[Long]](body).toTry

  def getResourcesById[B: Decoder](path: String, ids: List[Long]): MyService[Try[TastyPieResponse[B]]] =
    for {
      body <- fetch(URLs.resources(path), Map("id__in" -> ids.mkString(","), "limit" -> ids.length))
    } yield decode[TastyPieResponse[B]](body).toTry


  def getRecentResources[B: Decoder](resource: String, modifiedSince: String): MyService[Try[TastyPieResponse[B]]] =
    getRecentIds(resource, modifiedSince) flatMap {
      case Success(ids) => getResourcesById[B](resource, ids)
      case Failure(f)   => Free.pure(Failure(f))
    }

}

object FakeInterpreter extends App {
  import FreeProgram._
  import cats.{Id, ~>}

  // -- Fake interpreter

  def fakeInterpreter = new (MyServiceA ~> Id) {

    def apply[A](fa: MyServiceA[A]): Id[A] = fa match {

      case Fetch(path: String, params: Map[String, Any]) =>
        path match {
          case "/bpa/practices/" => """[{"id": 1}, {"id": 2}]"""
          case _ if path.startsWith("/bpa/recent/") => "[1,2,3]"
          case _ => "{}"
        }
    }
  }

  def main(): Unit = {
    println(getRecentIds("asdfasdf") foldMap fakeInterpreter)

  }

}


object TaskInterpreter extends App {
  import FreeProgram._
  import cats._
  import monix.cats._
  import monix.execution.Scheduler.Implicits.global

  def main(): Unit = {

    val program = getRecentResources[Json]("practices", "2017-06-11")

    program.foldMap(taskInterpreter) runOnComplete {
      case Success(v) => println(v)
      case Failure(e) =>
        println("error:")
        println(e)
    }
  }



  def taskInterpreter = new (MyServiceA ~> Task) {

    def apply[A](fa: MyServiceA[A]): Task[A] = fa match {

      case Fetch(path, params) =>

        val request = HttpRequest(s"${Config.sourceConfig.host}$path")
          .withQueryParameters(params.mapValues(_.toString).toList: _*)
          .withHeader("Authorization", Config.sourceConfig.API_AUTHORIZATION_HEADER)

        Task.deferFuture(request.get).map(_.body)
    }
  }
}