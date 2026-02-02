# Bulletin Board - Java Assignment

A simple bulletin board system with a Java TCP server and Swing GUI client.

## Run Server

```bash
# Compile
javac -d . client/*.java server/*.java

# Start Server
java Server

# Start Client
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
