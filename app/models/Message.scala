package models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._
import play.api.libs.json.Reads._

case class Message(sender: String, body: String){
  // Saves message in conversation
  val save : (ConversationKey) => Boolean = (conversationKey) => {
    val dbKey: String = conversationKey.asKey("")

    // Checking if the conversation exists
    DB.pool.withClient { client =>
      if(client.lrange(dbKey, 0, 0).isEmpty) false else {
        client.rpush(dbKey, Json.stringify(Json.toJson(this)))
        true
      }
    }
  }
}
object Message {
  implicit val messageWrites: Writes[Message] =
  (
    (JsPath \ "sender").write[String] and
    (JsPath \ "body").write[String]
  )(unlift(Message.unapply))
  
  implicit val messageReads: Reads[Message] =
  (
      (JsPath \ "sender").read[String] and
      (JsPath \ "body").read[String]
  )(Message.apply _)
}
