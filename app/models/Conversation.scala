package models
import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._
import play.api.libs.json.Reads._

case class User(name: String, participating: Boolean, admin: Boolean){
  val toNumbers : () => UserNumbers = () => UserNumbers(name, boolToNumber(participating), boolToNumber(admin))
  private val boolToNumber : (Boolean) => Int = (bool) => if(bool) 1 else -1
}
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

case class UserNumbers(name: String, participating: Int, admin: Int){
  val merge: (UserNumbers) => UserNumbers = (user) => UserNumbers(name, participating + user.participating, admin + user.admin)
}

case class Conversation(participants: Vector[String], admins: Vector[String]){

  // Saves information about Conversation participants
  val save : (ConversationKey) => Boolean = (conversationKey) => {
    val dbKey: String = conversationKey.asKey("-PARTICIPANTS")

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
object Conversation {
  implicit val conversationWrites: Writes[Conversation] = (
  (JsPath \ "participants").write[Vector[String]] and
  (JsPath \ "admins").write[Vector[String]])(unlift(Conversation.unapply))
}
case class ConversationKey(createdBy: String, conversationName: String){
  implicit private def int2bool(i:Int): Boolean = i != 0

  val asKey : (String) => String = (suffix: String) => "createdBy:" + createdBy + ",conversationName:" + conversationName + suffix
  val getParticipants: () => Option[Conversation] = () => {
    DB.pool.withClient { client =>
      val participants: Vector[User] = client.lrange(asKey("-PARTICIPANTS"), 0, -1).map((u: String) => Json.parse(u).as[User]).toVector
      if(participants.isEmpty) None else {
        val userInfoMerged: Map[String, UserNumbers] = participants.foldLeft(Map[String,UserNumbers]())(
          (res: Map[String, UserNumbers], user) => {
            val userNumbers : UserNumbers = user.toNumbers()
            val currentValue : Option[UserNumbers] = res.get(user.name)
            currentValue match {
              case Some(u) => res + (user.name -> u.merge(userNumbers))
              case None => res + (user.name -> userNumbers)
            }
          }
        )
        val conversation: Conversation = userInfoMerged.foldLeft(Conversation(Vector[String](), Vector[String]()))(
          (res: Conversation, userInfo: (String, UserNumbers)) => {
            val temp: Conversation = if(userInfo._2.participating) Conversation(res.participants :+ userInfo._1, res.admins) else res
            if(userInfo._2.admin) Conversation(temp.participants, temp.admins :+ userInfo._1) else temp
          }
        )
        Some(conversation)

      }
    }
  }
}
object ConversationKey {

  implicit val conversationKeyReads: Reads[ConversationKey] =
    (
      (JsPath \ "createdBy").read[String] and
      (JsPath \ "conversationName").read[String]
    )(ConversationKey.apply _)
}
