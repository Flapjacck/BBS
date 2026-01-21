# RFC: Bulletin Board Client–Server Protocol

**Document Status:**  Informational

**Intended Audience:**  CP372-C Assignment 01

**Authors:**  Spencer Kelly & Nick Shahbaz

**Version:**  1.0

**Date:**  20-01-2026

[GitHub Repository](https://github.com/Flapjacck/bbs)

---

## 1. Introduction

### 1.1 Purpose of This Document

This Document defines the client–server protocol for a simple bulletin board system implemented in Java. It specifies the message formats, commands, and responses used for communication between the Swing GUI client and the multi-threaded TCP server.

### 1.2 Scope

### 1.3 Design Goals

- Simplicity: The protocol should be easy to implement and understand.
- Maintainability: The protocol should allow for future extensions without breaking existing functionality.
- Efficiency: The protocol should minimize overhead in message exchanges.
- Robustness: The protocol should handle errors gracefully and ensure data integrity.

---

## 2. System Overview

### 2.1 Architectural Model

Centralized server, multiple concurrent clients, TCP stream sockets, request–response.

### 2.2 Connection Lifecycle

1. Client initiates a TCP connection to the server.
2. Client sends commands to the server as per the defined protocol.
3. Server processes commands and sends appropriate responses.
4. Client may disconnect by sending the DISCONNECT command.

---

## 3. Terminology and Definitions

- **Client**: The Swing GUI application that allows users to interact with the bulletin board.
- **Server**: The multi-threaded TCP server that manages the bulletin board data and handles client requests.
- **Post**: A message created by a client and stored on the server.
- **Pin**: A marker that indicates a post should remain at the top of the bulletin board.
- **Board**: The virtual space where posts are displayed.
- **Complete Overlap**: Two notes occupying identical rectangular regions (disallowed).
- **Atomic Operation**: An operation whose effects are observed as occurring instantaneously with no intermediate state visible to other clients.

---

## 4. Data Model

### 4.1 Coordinate System

- Origin (0,0) located at the upper-left corner.
- x increases rightward, y increases downward.
- All coordinates are non-negative integers.
- A coordinate is valid if it lies within board boundaries.

### 4.2 Board Properties

- Width and height fixed at server startup.
- Notes must lie fully inside the board.
- Board state exists only during server execution.
- Posts are not persisted across server restarts.
- Board supports concurrent access by multiple clients.

### 4.3 Note Properties

Each note consists of:

- Upper-left coordinate (x, y)
- Fixed width and height
- Color selected from a predefined palette
- Free-form text content
- Zero or more associated pins

Partial overlaps are permitted; exact overlaps are rejected.

### 4.4 Pin Properties

- Defined by a single coordinate.
- May exist only within the area of at least one note.
- A pin may affect multiple notes if placed in an overlapping region.
- Notes are considered pinned if at least one pin is present.

---

## 5. Protocol Overview

### 5.1 Transport Protocol

- TCP (Transmission Control Protocol) stream sockets.
- Reliable, ordered, and error-checked delivery of messages.
- Line-oriented ASCII messages
- Each message terminated by \n.

### 5.2 Message Exchange Pattern

- Client sends a command message to the server.
- Server processes it atomically.
- Server sends a single response.
- No unsolicited server messages except during initial handshake.

---

## 6. Message Format Specification

### 6.1 General Message Syntax

All protocol messages are transmitted as plain-text lines over a TCP stream. Each message is terminated by a single newline character (`\n`). Commands are case-sensitive and use uppercase keywords. Parameters are separated by one or more spaces.

Textual fields such as note content may contain spaces and are interpreted as the remainder of the line following the final fixed parameter.

Rules:

- Leading and trailing whitespace is ignored.
- Empty lines are ignored.
- Each client command results in exactly one server response.
- Server responses begin with either `OK` or `ERROR`.

### 6.2 Formal Grammar (BNF or Equivalent)

#### Lexical Elements

```bnf
<command>        ::= POST | GET | PIN | UNPIN | SHAKE | CLEAR | DISCONNECT
<integer>        ::= [0-9]+
<coordinate>     ::= <integer>
<color>          ::= <color-token>
<message>        ::= any sequence of characters excluding newline
<color-token>    ::= server-declared color identifier
```

#### Client Commands

```bnf
COMMAND        ::= POST-CMD
                 | GET-CMD
                 | PIN-CMD
                 | UNPIN-CMD
                 | SHAKE-CMD
                 | CLEAR-CMD
                 | DISCONNECT-CMD

POST-CMD       ::= "POST" <coordinate> <coordinate> <color> <message>

GET-CMD        ::= "GET" "PINS"
                 | "GET" [ FILTERS ]

FILTERS        ::= FILTER [ FILTERS ]

FILTER         ::= "color=" <color-token>
                 | "contains=" <coordinate> <coordinate>
                 | "refersTo=" <message>

PIN-CMD        ::= "PIN" <coordinate> <coordinate>

UNPIN-CMD      ::= "UNPIN" <coordinate> <coordinate>

SHAKE-CMD      ::= "SHAKE"

CLEAR-CMD      ::= "CLEAR"

DISCONNECT-CMD ::= "DISCONNECT"
```

#### Server Responses

```bnf
SERVER-RESPONSE ::= SUCCESS-RESPONSE
                  | ERROR-RESPONSE

SUCCESS-RESPONSE ::= "OK" [ <response-data> ]

ERROR-RESPONSE ::= "ERROR" <error-code> <error-message>

<response-data>  ::= <note-list> | <pin-list> | <color-list>

<note-list>      ::= <note> [ <note-list> ]

<note>           ::= <coordinate> <coordinate> <color> <message>

<pin-list>       ::= <coordinate> <coordinate> [ <pin-list> ]

<color-list>     ::= <color-token> [ <color-list> ]

<error-code>     ::= [0-9]+

<error-message>  ::= any sequence of characters excluding newline
```

---

## 7. Client-to-Server Commands

### 7.1 POST

**Purpose:**  
Create a new note on the board.

**Syntax:**  
`POST <x> <y> <color> <message>`

**Parameters:**  

- `<x> <y>`: Upper-left coordinate of the note  
- `<color>`: One of the valid colors announced by the server  
- `<message>`: Arbitrary text content

**Validation Rules:**  

- Note must lie fully within board boundaries  
- Color must be supported  
- Note must not completely overlap an existing note  

**Responses:**  

- `OK` on success  
- `ERROR` with appropriate code otherwise

### 7.2 GET

**Purpose:**  
Retrieve notes or pins matching given criteria.

**Syntax:**  

- `GET PINS`  
- `GET [color=<color>] [contains=<x> <y>] [refersTo=<substring>]`

**Semantics:**  

- Omitted filters imply no restriction  
- All provided filters must be satisfied  
- Result is a list of matching notes or pin coordinates

### 7.3 PIN

**Purpose:**  
Place a pin at a specific coordinate.

**Syntax:**  
`PIN <x> <y>`

**Semantics:**

- Applies to all notes containing the coordinate  
- Fails if no note contains the point

### 7.4 UNPIN

**Purpose:**  
Remove a pin from a coordinate.

**Syntax:**  
`UNPIN <x> <y>`

**Semantics:**

- Removes one pin at the coordinate  
- Fails if no pin exists there  

### 7.5 SHAKE

**Purpose:**  
Remove all unpinned notes atomically.

**Syntax:**  
`SHAKE`

**Semantics:**

- All unpinned notes are deleted  
- Operation is atomic with respect to concurrent clients  

### 7.6 CLEAR

**Purpose:**  
Reset the board to an empty state.

**Syntax:**  
`CLEAR`

**Semantics:**

- Removes all notes and all pins  
- Operation is atomic  

### 7.7 DISCONNECT

**Purpose:**  
Terminate the client session.

**Syntax:**  
`DISCONNECT`

**Semantics:**  

- Server closes the connection gracefully after responding

---

## 8. Server-to-Client Responses

### 8.1 Success Responses

### 8.2 Error Responses

---

## 9. Error Handling

### 9.1 Error Classification

### 9.2 Client-Side Responsibilities

### 9.3 Server-Side Responsibilities

---

## 10. Concurrency and Synchronization

### 10.1 Multithreading Model

### 10.2 Shared Data Protection

### 10.3 Consistency Guarantees

---

## 11. Border and Failure Cases

---

## 12. Security Considerations

---

## 13. Implementation Notes (Non-Normative)

---

## 14. Division of Responsibilities

**Client:** Spencer & Nick

**Server:** Spencer & Nick

**RFC Document:** Spencer & Nick

---

## Appendix A: Example Message Exchanges

---

## Appendix B: Revision History
