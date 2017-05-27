package models
import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._
import play.api.libs.json.Reads._

case class Conversation(participants: Vector[String], admins: Vector[String]){

  // Saves information about Conversation participants
  val save : (ConversationKey) => Boolean = (conversationKey) => {
    val dbKey: String = conversationKey.asKey("")

    // Checking if the conversation exists
    DB.pool.withClient { client =>
      if(!client.lrange(dbKey, 0, 0).isEmpty) false else {
        client.rpush(dbKey, Json.stringify(Json.toJson(Message("BOT", "Welcome!"))))
        participants.map(
          participant => {
              // creating conversation for user and setting all rights to true
              client.rpush(participant, Json.stringify(Json.toJson(User(participant, true, true,conversationKey))))

              // removing admin rights if user is not admin
              if (!admins.contains(participant))
                client.rpush(participant, Json.stringify(Json.toJson(User(participant, true, false,conversationKey))))
          }
        )
        true
      }
    }
  }
}
object Conversation {
  implicit val conversationWrites: Writes[Conversation] = (
  (JsPath \ "participants").write[Vector[String]] and
  (JsPath \ "admins").write[Vector[String]])(unlift(Conversation.unapply))
}
case class ConversationKey(createdBy: String, conversationName: String){
  implicit private def int2bool(i:Int): Boolean = i != 0

  val asKey : (String) => String = (prefix: String) => prefix + "createdBy:" + createdBy + ",conversationName:" + conversationName
}
object ConversationKey {

  implicit val conversationKeyReads: Reads[ConversationKey] =
    (
      (JsPath \ "createdBy").read[String] and
      (JsPath \ "conversationName").read[String]
    )(ConversationKey.apply _)

    implicit val conversationKeyWrites: Writes[ConversationKey] =
      (
        (JsPath \ "createdBy").write[String] and
        (JsPath \ "conversationName").write[String]
      )(unlift(ConversationKey.unapply))
}
