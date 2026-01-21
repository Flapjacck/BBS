# RFC: Bulletin Board Client–Server Protocol

**Document Status:**  Informational
**Intended Audience:**  CP372-C Assignment 01
**Authors:**  Spencer Kelly & Nick Shabazz
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

---

## 4. Data Model

### 4.1 Coordinate System

### 4.2 Board Properties

### 4.3 Note Properties

### 4.4 Pin Properties

---

## 5. Protocol Overview

### 5.1 Transport Protocol

### 5.2 Message Exchange Pattern

---

## 6. Message Format Specification

### 6.1 General Message Syntax

### 6.2 Formal Grammar (BNF or Equivalent)

---

## 7. Client-to-Server Commands

### 7.1 POST

### 7.2 GET

### 7.3 PIN

### 7.4 UNPIN

### 7.5 SHAKE

### 7.6 CLEAR

### 7.7 DISCONNECT

---

## 8. Server-to-Client Responses

### 8.1 Success Responses

All successful server responses begin with the keyword "OK" followed by the status code:
Each client request results in exactly one server response.

ex.
OK NOTE_POSTED
OK PIN_ADDED
OK PIN_REMOVED
OK SHAKE_COMPLETE
OK CLEAR_COMPLETE

Data-bearing responses are used for GET commands and consist of:

- An initial OK [n] line, where n is the number of result entries.
- Exactly n subsequent lines, each describing a returned object.

ex.
OK 2
PIN 15 12
PIN 18 15

### 8.2 Error Responses

All error responses begin with the keyword "ERROR" and indicate that the requested operation was not performed, also followed by an error code.

ex.
ERROR OUT_OF_BOUNDS Note exceeds board boundaries
ERROR COLOR_NOT_SUPPORTED blue is not a valid color

## 9. Error Handling

### 9.1 Error Classification

The server recognizes and reports the following error types:

INVALID_FORMAT: 
The request does not conform to the required command syntax or is missing required fields.

OUT_OF_BOUNDS: 
A note or coordinate lies partially or entirely outside the board boundaries.

COLOR_NOT_SUPPORTED: 
The specified note color is not in the server’s startup color list.

COMPLETE_OVERLAP: 
A posted note would exactly overlap an existing note.

NO_NOTE_AT_COORDINATE: 
A PIN command targets a coordinate that is not contained within any note.

PIN_NOT_FOUND: 
An UNPIN command targets a coordinate with no existing pin.

These error codes are exhaustive for all protocol-level validation failures.

### 9.2 Client-Side Responsibilities

Clients are expected to perform basic validation before sending requests, including:

- Ensuring required parameters are present
- Preventing malformed commands
- Restricting color selection to server-provided values
- Ensuring numeric fields are integers

Client-side validation improves usability but is not relied upon for correctness.

### 9.3 Server-Side Responsibilities

The server is the final authority on protocol correctness and must:

- Validate all incoming requests regardless of client behavior
- Detect and report all invalid conditions using structured ERROR responses
- Never crash or terminate due to malformed or malicious client input
- Preserve board consistency by rejecting invalid operations

The server must guarantee that invalid requests have no side effects.

## 10. Concurrency and Synchronization

### 10.1 Multithreading Model

The server uses a thread-per-client model:

- Each client connection is handled by a dedicated worker thread
- All threads share access to the centralized board state
- Requests are processed sequentially per client, but concurrently across clients

This model enables multiple clients to interact with the board simultaneously.

### 10.2 Shared Data Protection

All shared server data structures, including:

- The list of notes
- The set of pins

are protected using synchronization mechanisms to prevent race conditions.

Operations that modify shared state (POST, PIN, UNPIN, SHAKE, CLEAR) are executed within critical sections to ensure thread safety.

Atomic operations are enforced using mutual exclusion.

### 10.3 Consistency Guarantees

The server provides strong consistency guarantees:

- Each command is processed atomically
- Clients never observe partially applied operations
- For atomic commands such as SHAKE and CLEAR, clients observe either:
    - the complete state before the operation, or
    - the complete state after the operation

Interleaving effects from concurrent clients do not result in inconsistent or undefined board states.

## 11. Border and Failure Cases

---

## 12. Security Considerations

---

## 13. Implementation Notes (Non-Normative)

---

## 14. Division of Responsibilities

**Client:**
**Server:**
**RFC Document:** Spencer & Nick

---

## Appendix A: Example Message Exchanges

---

## Appendix B: Revision History
