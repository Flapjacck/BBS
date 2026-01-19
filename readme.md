# Bulletin Board - Java Assignment

A simple bulletin board system with a Java TCP server and Swing GUI client.

## Run Server

```bash
cd server
javac *.java
java Server
```

## Run Client

```bash
cd client
javac *.java
java Client
```

## Architecture

- **Server**: Multi-threaded TCP server
- **Client**: Swing GUI
  - POST - Post a message
  - GET - Fetch all posts
  - PIN - Pin a post
  - UNPIN - Unpin a post
  - SHAKE - Shuffle posts
  - CLEAR - Clear all posts
  - DISCONNECT - Close connection
