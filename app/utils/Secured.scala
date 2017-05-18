package utils

import models.User
import scala.concurrent.Future
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.cache._
import play.api.libs.ws._
import javax.inject._
import play.api.Play.current
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticatedRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class Authentication @Inject()(ws: WSClient, cache: CacheApi){
  object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    val verifier = new Verifier(ws, cache)
    def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
      request.headers.get("Authentication") match {
        case Some(token) => {
          verifier.verifyToken(token).map(isValidToken => if(isValidToken) println("Valid!") else println("Not valid!"))
          block(new AuthenticatedRequest(User(token, ""), request))
        }
        case _ => Future.successful(Unauthorized)
      }
    }
  }
}


case class Verifier(ws: WSClient, cache: CacheApi) {
  val baseUrl: String = current.configuration.getString("authenticationServer").getOrElse("")
  val getPublicKey: () => Future[String] = { () => {
      val url: String = baseUrl + "/auth/public-key"
      cache.get[String]("publicKey") match {
        case Some(key) => Future(key)
        case _ => ws.url(url).withHeaders("Accept" -> "application/json").get().map(wsResponse => wsResponse.body)
      }
    }
  }
  val verifyToken: (String) => Future[Boolean] = (token) => {
    getPublicKey().map(key => {
      println(key)
      println(token)
      true
    })
  }
}
