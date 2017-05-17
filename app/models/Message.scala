package models

case class Message(from: User, in: Conversation, body: String)
