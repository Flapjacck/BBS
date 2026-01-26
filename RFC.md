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

**Overview:**  
All successful server responses begin with the keyword `OK` followed by a status code. Each client request results in exactly one server response.

**Examples (Simple Responses):**

```bnf
OK NOTE_POSTED
OK PIN_ADDED
OK PIN_REMOVED
OK SHAKE_COMPLETE
OK CLEAR_COMPLETE
```

**Data-Bearing Responses:**  
Data-bearing responses are used for GET commands and consist of:

- An initial `OK [n]` line, where n is the number of result entries
- Exactly n subsequent lines, each describing a returned object

**Example (GET with Results):**

```bnf
OK 2
PIN 15 12
PIN 18 15
```

### 8.2 Error Responses

**Overview:**  
All error responses begin with the keyword `ERROR` and indicate that the requested operation was not performed. Each error includes an error code and descriptive message.

**Format:**  
`ERROR <error-code> <error-message>`

**Examples:**

```bnf
ERROR OUT_OF_BOUNDS Note exceeds board boundaries
ERROR COLOR_NOT_SUPPORTED blue is not a valid color
```

## 9. Error Handling

### 9.1 Error Classification

**Overview:**  
The server recognizes and reports the following error types:

**INVALID_FORMAT**
The request does not conform to the required command syntax or is missing required fields.

**OUT_OF_BOUNDS**  
A note or coordinate lies partially or entirely outside the board boundaries.

**COLOR_NOT_SUPPORTED**  
The specified note color is not in the server’s startup color list.

**COMPLETE_OVERLAP**  
A posted note would exactly overlap an existing note.

**NO_NOTE_AT_COORDINATE**  
A PIN command targets a coordinate that is not contained within any note.

**PIN_NOT_FOUND**  
An UNPIN command targets a coordinate with no existing pin.

**Note:**  
These error codes are exhaustive for all protocol-level validation failures.

### 9.2 Client-Side Responsibilities

The client should perform basic validation before sending requests to improve user experience:

- Ensure required parameters are present
- Prevent malformed commands
- Restrict color selection to server-provided values
- Ensure numeric fields are valid integers

**Important:**  
Client-side validation improves usability but is not relied upon for correctness. The server is always the final authority.

### 9.3 Server-Side Responsibilities

The server is the final authority on protocol correctness and must enforce all validation rules:

- Validate all incoming requests regardless of client behavior
- Detect and report all invalid conditions using structured `ERROR` responses
- Never crash or terminate due to malformed or malicious client input
- Preserve board consistency by rejecting invalid operations
- Guarantee that invalid requests have no side effects

## 10. Concurrency and Synchronization

### 10.1 Multithreading Model

**Model Description:**  
The server uses a thread-per-client model:

- Each client connection is handled by a dedicated worker thread
- All threads share access to the centralized board state
- Requests are processed sequentially per client, but concurrently across clients

**Benefit:**  
This model enables multiple clients to interact with the board simultaneously.

### 10.2 Shared Data Protection

**Protected Resources:**  
All shared server data structures are protected using synchronization mechanisms to prevent race conditions:

- The list of notes
- The set of pins

**Synchronization Strategy:**  
Operations that modify shared state (`POST`, `PIN`, `UNPIN`, `SHAKE`, `CLEAR`) are executed within critical sections to ensure thread safety. Atomic operations are enforced using mutual exclusion.

### 10.3 Consistency Guarantees

The server provides strong consistency guarantees:

- Each command is processed atomically
- Clients never observe partially applied operations
- For atomic commands such as `SHAKE` and `CLEAR`, clients observe either:
  - The complete state before the operation, or
  - The complete state after the operation

**Invariant:**  
Interleaving effects from concurrent clients do not result in inconsistent or undefined board states.

## 11. Border and Failure Cases

This section specifies protocol behavior under boundary and failure conditions to ensure predictable and robust system operation.

### 11.1 Empty Board

- A newly started server contains an empty board with no notes and no pins.
- `GET` requests return `OK 0` with no subsequent data lines.
- `GET PINS` returns `OK 0`.
- `PIN` and `UNPIN` requests always return an error (`NO_NOTE_AT_COORDINATE` or `PIN_NOT_FOUND` respectively).

### 11.2 Empty Query Results

- If a `GET` request matches no notes, the server responds with:
  - `OK 0`
  - No additional result lines follow.

### 11.3 Server Running with No Clients

- The server maintains an empty board state until at least one client connects.
- Board dimensions, note dimensions, and valid colors are sent only upon client connection.

### 11.4 Client Disconnecting Mid-Session

- If a client disconnects unexpectedly:
  - The server releases all associated resources.
  - No partial command effects are applied.
  - Shared board state remains consistent.

### 11.5 Server Shutdown

- All client connections are closed.
- Board state is discarded (non-persistent).
- Clients must reconnect after restart and reinitialize state.

---

## 12. Security Considerations

This system is designed for an academic, trusted-network environment. Formal cryptographic security is out of scope; however, the following protections are enforced:

### 12.1 Input Validation

- All client input is validated server-side.
- Malformed commands are rejected with `INVALID_FORMAT`.
- Out-of-range coordinates, invalid colors, and illegal overlaps are rejected.

### 12.2 Denial of Service Considerations

- Each client is handled in its own thread.
- Input parsing prevents infinite loops or buffer overflow.
- Requests are processed atomically to prevent inconsistent shared state.

### 12.3 Trust Assumptions

- No authentication or encryption is implemented.
- The protocol assumes cooperative clients within a controlled lab environment.

---

## 13. Implementation Notes (Non-Normative)

This section documents design decisions and limitations that do not affect protocol compliance.

### 13.1 Synchronization Strategy

- All board-modifying commands (`POST`, `PIN`, `UNPIN`, `SHAKE`, `CLEAR`) are executed within synchronized critical sections.
- This guarantees atomic visibility and prevents race conditions.

### 13.2 Atomic Operations

- `SHAKE` and `CLEAR` are implemented as single critical transactions.
- No intermediate state is visible to concurrent clients.

### 13.3 Limitations

- Board state is memory-only and non-persistent.
- No authentication or access control.
- No message batching or streaming responses.
- GUI design is not standardized by the protocol.

---

## 14. Division of Responsibilities

**Client:** Spencer & Nick

**Server:** Spencer & Nick

**RFC Document:** Spencer & Nick

---

## Appendix A: Example Message Exchanges

### A.1 Successful POST

Client → Server:  
POST 10 5 yellow Team meeting at 3pm  

Server → Client:  
OK NOTE_POSTED  

### A.2 GET with Filters

Client → Server:  
GET color=yellow refersTo=meeting  

Server → Client:  
OK 1  
NOTE 10 5 yellow Team meeting at 3pm  

### A.3 PIN and UNPIN

Client → Server:  
PIN 12 7  

Server → Client:  
OK PIN_ADDED  

Client → Server:  
UNPIN 12 7  

Server → Client:  
OK PIN_REMOVED  

### A.4 Error Example

Client → Server:  
PIN 300 400  

Server → Client:  
ERROR NO_NOTE_AT_COORDINATE No note exists at the specified coordinate  

### A.5 Concurrent Atomic SHAKE

Client A → Server:  
SHAKE  

Client B (simultaneously) → Server:  
GET  

Client B receives either:

- Full board before SHAKE  
or  
- Fully cleared unpinned board after SHAKE  
but never a partial state.

---

## Appendix B: Revision History

Version history:

<https://github.com/Flapjacck/BBS/commits/main/>

All tracked via Git commits.
