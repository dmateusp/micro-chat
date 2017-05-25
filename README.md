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
{"participants": ["user@gmail.com", "user2@mail.com"], "admins": ["user@gmail.com"]}
```
participants: Array of strings, each one being a unique identifier for your user (participating to the conversation). At least one participant must be specified.

admins: Array of strings, each one being a unique identifier for your user (admin of the conversation). At least one admin must be specified.

**Response:**
* Ok: "Conversation created!"
* Bad request: if body/headers invalid/missing
* Unauthorized/Forbidden: if authentication header token invalid



## Tech
The application is written in Scala and using Play Framework, the DB is Redis
