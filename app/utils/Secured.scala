package utils

import models.AuthenticatedUser
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
import play.api.libs.json._
import pdi.jwt.{Jwt, JwtAlgorithm}

class AuthenticatedRequest[A](val user: AuthenticatedUser, request: Request[A]) extends WrappedRequest[A](request)

class Authentication @Inject()(ws: WSClient, cache: CacheApi){
  object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] {
    val verifier = new Verifier(ws, cache)
    def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]) = {
      request.headers.get("Authentication") match {
        case Some(token) => {
          verifier.verifyToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbCI6ImRtYXRldXNwQGdtYWlsLmNvbSIsImlhdCI6MTQ5NTE4Nzk2MSwiZXhwIjoxNDk3Nzc5OTYxfQ.dN8RIvyS3X5_Cxr0R8w6PG2h_vMUmjJf4d9755OL1EV6ZMjaDDvj2NX3fDByZuigsFssyExNNvQbDPfAX9jBLrVjH3C3-pgbbvBlP0E1-m921d70vNxQFPfpACGA5IRJ5IHIuV-dvp6nEWDoMJaso5cIXZaPLUpinNkipDNRBHqSH4MCVLY0Lwvsuf59I73YNjaKSyIywGPpZUd1IPty2OWj9-56vR2cKKrg4m4OWU4REarXTbTix5YKjo5nAdFF9QSXoxnjZ50VDGzZTnWsO6UkAdl5gUTOIXn6hB8KSG42DoOUwvfwmqcBPRbWOaEmtKqhvBlwyH-7hQlHwMTfKVNn44QZSn-R9-rusZtbMX7d4viZm2_8HaO4AyT7-FlQlkT0VyIijJPNea_latMmL3c8BWRBQ-SgUE3DiSW-cdDroL1lmAx6HRgu1zs8zie5b9UAKtOCDTih9lC5-Y6Ef38Cwp4CDSf6Ji5ni2i5pb3YOOdWXlEDMAemVgg957c8P9CJJwfzwHJGeMNN3LWSyndhXY-vZ82CUefF6rQaA_bTuo6EXlAClB3CgnHMPYGOGYd5kCDheQYT0R5yMM6BIkf6weH62fRCZaAUFhA48tr3afmnfkN12X4b7IgBiUbX5tNMqJeqd4Wl1p-vqA5EbKk_x7aoIspephX68izdP2E").map(isValidToken => if(isValidToken) println("Valid!") else println("Not valid!"))
          block(new AuthenticatedRequest(AuthenticatedUser(token), request))
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
        case _ => ws.url(url).withHeaders("Accept" -> "application/json").get().map(
          wsResponse => {
            val jsonResponse: JsValue = Json.parse(wsResponse.body)
            val success: Boolean = (jsonResponse \ "success").as[Boolean]
            if(success) {
              val key = PublicKeyUtils.removeBeginAndEnd((jsonResponse \ "data" \ "publicKey").as[String])
              cache.set("publicKey", key)
              key
            } else {
              "error"
            }
          }

        )
      }
    }
  }
  val verifyToken: (String) => Future[Boolean] = (token) => {
    getPublicKey().map(key => {
      if(key != "error"){
        val decrypted = Jwt.decode(token, PublicKeyUtils.publicKeyFromString(key), Seq(JwtAlgorithm.RS256))
        println(decrypted)
        true
      } else {
        false
      }

    })
  }
}
