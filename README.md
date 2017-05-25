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


## Tech
The application is written in Scala and using Play Framework, the DB is Redis
