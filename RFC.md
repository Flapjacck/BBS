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

**Client:**
**Server:**
**RFC Document:** Spencer & Nick

---

## Appendix A: Example Message Exchanges

---

## Appendix B: Revision History
