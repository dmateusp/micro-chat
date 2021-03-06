# Micro-chat
This is a micro-service providing chat as a service for any kind of application: social networks, games, etc..

## Authentication
The service uses JWT tokens for authentication, it was created to support [AuthenticServer](https://github.com/dmateusp/authentic-server) by default but any service can be plugged in.

## API

### Conversations

* Creating a new conversation

`POST    /api/conversations`

**Headers:**
```
Authentication: USERTOKEN
Content-Type: application/json
```

**Body:**
```
{"participants": ["user@gmail.com", "user2@mail.com"], "admins": ["user@gmail.com"], "createdBy": "user@gmail.com", "conversationName": "User and User2"}
```
participants: Array of strings, each one being a unique identifier for your user (participating to the conversation). At least one participant must be specified.

admins: Array of strings, each one being a unique identifier for your user (admin of the conversation). At least one admin must be specified.

createdBy: String giving the identifier of the creator of the conversation.

conversationName: String giving the name of the conversation.

UNIQUENESS: The pair (createdBy, conversationName) is used to ensure uniqueness, new conversations should be unique.

**Response:**
* Ok: "Conversation created!"
* Ok with error when a field is missing, example:
```
{
  "error": "'conversationName' is undefined on object: {\"participants\":[\"user@gmail.com\", \"user2@mail.com\"],\"admins\":[\"user@gmail.com\"],\"createdBy\":\"user@gmail.com\"}"
}
```
* Bad request: if body/headers invalid/missing or if conversation already exists
* Unauthorized/Forbidden: if authentication header token invalid or XSS filter not passing

---

* Getting conversation information for a given user

`GET    /api/users/:USER`

**Parameters:**

:USER = the unique identifier of your user

**Headers:**
```
Authentication: USERTOKEN
```

** Response example: **
* Ok: with information about your user (example for `/api/users/rafael@gmail.com`)
```
[
  {
    "name": "user@gmail.com",
    "participating": true,
    "admin": true,
    "conversation": {
      "createdBy": "user@gmail.com",
      "conversationName": "User and User1"
    }
  },
  {
    "name": "user@gmail.com",
    "participating": true,
    "admin": false,
    "conversation": {
      "createdBy": "user1@gmail.com",
      "conversationName": "Squad"
    }
  }
]
```
* Ok : with response "User does not exist"
* Unauthorized/Forbidden: if authentication header token invalid or XSS filter not passing

---

### Messages

* Getting messages in conversation

`POST    /api/messages`


**Headers:**
```
Authentication: USERTOKEN
Content-Type: application/json
```

**Body:**
```
{
	"conversation" : {"createdBy": "user@gmail.com", "conversationName": "User and User1"},
  "page" : 1,
  "perPage" : 15
}
```
conversation: the object containing the unique identifier of the creator of the conversation and the name of the conversation.

page: any number > 0, optional (defaults to 1), page 1 will have the most recent messages

perPage: number of messages per page, any number < 51, optional (defaults to 10)

**Response:**
* Ok: Array of messages
```
[
  ...
  {
    "sender": "user@gmail.com",
    "body": "Hello world!"
  },
  ...
  {
    "sender": "user1@gmail.com",
    "body": "Hello world! :)"
  }
  ...
]
```
* Ok: with error when object passed is wrong `Conversation not found OR page/perPage wrong`
* Bad request: if body/headers invalid/missing
* Unauthorized/Forbidden: if authentication header token invalid or XSS filter not passing

---

* Posting a new message

`POST    /api/messages/new`


**Headers:**
```
Authentication: USERTOKEN
Content-Type: application/json
```

**Body:**
```
{
  "conversation" : {"createdBy": "dmateusp@gmail.com", "conversationName": "Daniel and Rafael"},
	"sender" : "dmateusp@gmail.com",
	"body": "Hello world!"
}
```
conversation: the object containing the unique identifier of the creator of the conversation and the name of the conversation.

sender: the sender of the message

body: the content of the message

**Response:**
* Ok: "success"
* Ok with error when a field is missing, example
```
{
  "error": "'conversationName' is undefined on object: {\"participants\":[\"user@gmail.com\", \"user2@mail.com\"],\"admins\":[\"user@gmail.com\"],\"createdBy\":\"user@gmail.com\"}"
}```
* Bad request: if body/headers invalid/missing
* Unauthorized/Forbidden: if authentication header token invalid or XSS filter not passing



## Tech
The application is written in Scala and using Play Framework, the DB is Redis

## Starting the DB
Do not forget to rename `redis.example.conf` to `redis.conf` and to specify the configuration file when launching the DB as follows: `redis-server ./redis.conf`
