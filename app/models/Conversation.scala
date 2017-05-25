package models
import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._

case class User(name: String, participating: Boolean, admin: Boolean)
object User{
  implicit val userReads: Reads[User] = (
  (JsPath \ "name").read[String] and
  (JsPath \ "participating").read[Boolean] and
  (JsPath \ "admin").read[Boolean])(User.apply _)

  implicit val userWrites: Writes[User] = (
  (JsPath \ "name").write[String] and
  (JsPath \ "participating").write[Boolean] and
  (JsPath \ "admin").write[Boolean])(unlift(User.unapply))
}
case class Conversation(participants: Vector[String], admins: Vector[String]){

  // Saves information about Conversation participants
  val save : (ConversationKey) => Boolean = (conversationKey) => {
    val dbKey: String = conversationKey.asKey() + "-PARTICIPANTS"

    // Checking if the conversation exists
    DB.pool.withClient { client =>
      if(!client.lrange(dbKey, 0, 0).isEmpty) false else {
        participants.map(
          participant => client.rpush(dbKey, Json.stringify(Json.toJson(User(participant, true, admins.contains(participant)))))
        )
        true
      }
    }
  }
}
case class ConversationKey(createdBy: String, conversationName: String){
  val asKey : () => String = () => "createdBy:" + createdBy + ",conversationName:" + conversationName
}
