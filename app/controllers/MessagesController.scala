package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import utils._
import play.api.libs.json._
import models._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class MessagesController @Inject()(auth: Authentication) extends Controller {

  def get = auth.AuthenticatedAction { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody match {
      case Some(json) => {
        try {
          val conversationKey: ConversationKey = (json \ "conversation").as[ConversationKey]
          val page: Option[Int] = (json \ "page").asOpt[Int]
          val perPage: Option[Int] = (json \ "perPage").asOpt[Int]
          val result : Option[Vector[Message]] = Message.get(conversationKey, page, perPage)
          result match {
            case Some(messages) => Ok(Json.toJson(messages))
            case None => Ok("Conversation not found OR page/perPage error")
          }
        } catch {
             case e: JsResultException => Ok(JsResultExceptionJson.toJson(e))
             case x: Throwable => Ok(x.toString())
        }
      }
      case None => Ok("ohhh")
    }
  }
  def create = auth.AuthenticatedAction { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody match {
      case Some(json) => {
        try{
          val message: Message = Message((json \ "sender").as[String], (json \ "body").as[String])
          val conversationKey: ConversationKey = (json \ "conversation").as[ConversationKey]
          val result : Boolean = message.save(conversationKey)
          if(result) Ok("success") else Ok("error")

        } catch {
           case e: JsResultException => Ok(JsResultExceptionJson.toJson(e))
           case x: Throwable => Ok(x.toString())
        }
      }
      case None => Ok("ohhh")
    }
  }
}
