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
        client.lpush(dbKey, Json.stringify(Json.toJson(this)))
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
  
  // Gets messages
  val get: (ConversationKey, Option[Int], Option[Int]) => Option[Vector[Message]] = {
    (conversation, page, perPage) => {
      // Defaults the number of messages per page to 10
      val nPerPage : Int = perPage match {
        case Some(p) => p
        case None => 10
      }
      val nPage : Int = page match {
        case Some(p) => p - 1
        case None => 0
      }
      if(nPage < 0 || nPerPage > 50) None else {
        val dbKey: String = conversation.asKey("")

        // Checking if the conversation exists
        DB.pool.withClient { client =>
          if(client.lrange(dbKey, 0, 0).isEmpty) None else {
            val messages: Vector[Message] = client.lrange(dbKey, nPage * nPerPage, (nPage * nPerPage) + nPerPage).map((m: String) => Json.parse(m).as[Message]).toVector
            Some(messages)
          }
        }
      }
    }
  }
}
