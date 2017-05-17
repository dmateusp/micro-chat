package models

case class Conversation(messages: Stream[Message], participants: Vector[User])
