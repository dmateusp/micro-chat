package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import utils._
import play.api.libs.json._
import services._
import models._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ConversationsController @Inject()(auth: Authentication) extends Controller {

  def create = auth.AuthenticatedAction { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody match {
      case Some(json) => {
        try{
          val newConversation: Conversation = Conversation((json \ "participants").as[Vector[String]], (json \ "admins").as[Vector[String]])
          val result = newConversation.save(ConversationKey((json \ "createdBy").as[String], (json \ "conversationName").as[String]))
          if(result) {
            Ok("Conversation created!")
          } else {
            Ok("Conversation already exists")
          }
        } catch {
          case e: JsResultException => Ok(JsResultExceptionJson.toJson(e))
          case x: Throwable => Ok(x.toString())
        }
      }
      case None => Ok("hee")
    }
  }

  def getConversations(user: String) = auth.AuthenticatedAction { implicit request =>
    val result = new UserService(new RedisUserRepository()).getParticipations(user)
    result match {
      case Some(r) => Ok(Json.toJson(r))
      case None => Ok("User does not exist")
    }
  }
}
