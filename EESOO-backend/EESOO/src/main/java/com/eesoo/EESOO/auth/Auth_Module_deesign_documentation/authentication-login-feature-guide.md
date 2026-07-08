# Authentication & Login Feature — Theoretical Implementation Guide

> **A comprehensive conceptual reference for building custom authentication without Spring Security**
>
> Architecture: Clean Architecture + Modular Monolith
> Token Strategy: PASETO (Platform-Agnostic Security Tokens)
> Authentication Method: Phone Number + PIN

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Why Custom Authentication?](#why-custom-authentication)
3. [Architecture Philosophy](#architecture-philosophy)
4. [Core Concepts & Mental Models](#core-concepts--mental-models)
5. [Module Design & Responsibility Boundaries](#module-design--responsibility-boundaries)
6. [Domain Layer — Concepts & Design](#domain-layer--concepts--design)
7. [Application Layer — Concepts & Design](#application-layer--concepts--design)
8. [Infrastructure Layer — Concepts & Design](#infrastructure-layer--concepts--design)
9. [Presentation Layer — Concepts & Design](#presentation-layer--concepts--design)
10. [Authentication Flow — Step by Step](#authentication-flow--step-by-step)
11. [Token Validation Flow](#token-validation-flow)
12. [Security Considerations](#security-considerations)
13. [Testing Strategy](#testing-strategy)
14. [File Structure Reference](#file-structure-reference)
15. [Implementation Phases & Order](#implementation-phases--order)
16. [Production Checklist](#production-checklist)

---

## Executive Summary

### The Core Problem This Solves

When building an authentication system without Spring Security, you must explicitly design and own three critical responsibilities that the framework would otherwise handle invisibly:

1. **User Authentication** — Verifying that the phone number and PIN combination is valid and that the account is permitted to log in
2. **Token Generation** — Issuing a cryptographically secure token pair (access + refresh) after successful authentication
3. **Token Validation on Every Request** — Intercepting each incoming HTTP request, extracting the token, and establishing the caller's identity before passing the request to business logic

Spring Security abstracts all of this behind a hidden pipeline. The custom approach makes every step explicit, readable, and fully within your control.

### What Changes Conceptually

The shift is not just technical — it is a mindset shift. Instead of configuring a framework to behave the way you want, you are designing a deliberate sequence of steps that maps to your exact business requirements. Every decision is visible. Every failure point is traceable.

---

## Why Custom Authentication?

### Problems with the Spring Security + JWT Approach

Spring Security with JWT, while widely used, introduces several structural problems that compound over time:

**Tight Coupling to the Framework**
The moment your domain models implement `UserDetails`, you have permanently coupled your business logic to a framework contract. If Spring Security changes, your domain changes. If you want to migrate to a different framework, you must unpick framework interfaces from your core business objects.

**Framework-Controlled Flow**
Spring Security decides the authentication pipeline. Customizing it — such as switching from username/password to phone number/PIN — requires fighting against the framework's assumptions rather than simply designing the flow you need.

**Domain Leakage**
`UserDetails`, `GrantedAuthority`, and related Spring Security types bleed into places they do not belong. Your `User` entity ends up carrying framework-specific methods that have nothing to do with your business domain.

**Opacity**
It becomes difficult to trace exactly what happens during authentication because large portions of the flow are invisible inside Spring Security's internals.

### Benefits of the Custom Approach

**Full Visibility** — Every step of authentication is code you wrote and can read, debug, and reason about.

**Clean Domain Model** — Your domain entities have no knowledge of any framework. They express pure business concepts.

**Flexibility** — Changing authentication methods (e.g., from phone/PIN to biometrics or OAuth) is straightforward because the authentication flow is an explicit sequence, not a framework configuration.

**Testability** — Without framework magic, every component can be tested in isolation with simple unit tests.

**No Lock-in** — The domain and application layers are portable to any framework or runtime.

---

## Architecture Philosophy

### Clean Architecture in This Context

Clean Architecture dictates that business rules must not depend on frameworks, databases, or any external agency. Dependencies always point inward — from infrastructure toward the domain, never the reverse.

In the context of this authentication system, the layers are:

**Domain Layer (innermost)** — Contains business entities, business rules, and port interfaces. Has zero dependencies on Spring, databases, or any library.

**Application Layer** — Orchestrates use cases by coordinating domain objects and calling ports. Depends only on the domain.

**Infrastructure Layer** — Contains the actual implementations of ports — database access, token generation, external APIs. Depends on the application and domain layers.

**Presentation Layer (outermost)** — Handles HTTP concerns: deserializing requests, validating input, serializing responses. Depends on the application layer.

### Modular Monolith

The system is divided into two primary modules with a clear, enforced boundary:

- **Auth Module** — Owns authentication logic, token management, and login permission enforcement
- **User Module** — Owns user data storage, PIN hashing, and user lifecycle management

The Auth Module never directly accesses the User Module's database or internal classes. It communicates only through defined port interfaces. This means both modules can evolve independently.

### Dependency Rule Visualized

```
Presentation  →  Application  →  Domain  ←  Infrastructure
```

Infrastructure implements interfaces (ports) defined in the Domain. The Domain knows nothing about Infrastructure. This is the foundation of the entire design.

---

## Core Concepts & Mental Models

### Replacing Spring Security Components Conceptually

Every component that Spring Security provides has a deliberate custom equivalent:

| Spring Security Concept | Custom Equivalent | Role |
|------------------------|-------------------|------|
| `UserDetails` | `AuthUser` | Represents the authenticated user within the domain |
| `UserDetailsService` | `LoadAuthUserPort` | Retrieves user data needed for authentication |
| `AuthenticationManager` | `LoginService` | Orchestrates the full authentication sequence |
| `PasswordEncoder` | `PinVerificationPort` | Verifies the credential (PIN) |
| Token Provider | `TokenGenerationPort` | Issues tokens after successful authentication |
| Security Filter Chain | `PasetoAuthenticationFilter` | Validates tokens on every incoming request |

The key insight is that none of these custom components are framework-specific. They are pure Java interfaces and classes that happen to fulfill the same role Spring Security's components would.

### Ports and Adapters (Hexagonal Architecture)

A **Port** is an interface defined in the domain that declares what capability is needed, without specifying how it is implemented.

An **Adapter** is the concrete class in the infrastructure layer that implements a port. The domain and application layers never see the adapter — only the port interface.

This is why the Auth Module can call `LoadAuthUserPort.findByPhoneNumber()` without knowing whether that data comes from PostgreSQL, MongoDB, an in-memory cache, or a remote API. The adapter in the User Module's infrastructure layer handles that concern.

### Value Objects vs. DTOs — A Critical Distinction

**Value Objects** belong to the domain layer. They represent a concept that is defined entirely by its data, is always immutable, and carries business meaning. `TokenPair` is a value object because an access token and a refresh token are always issued together as a single atomic business concept, they should never change after creation, and they have meaning independent of any particular API shape.

**DTOs (Data Transfer Objects)** exist at layer boundaries. They carry data across a boundary (HTTP → application, application → HTTP) and are shaped by the needs of that boundary, not by business concepts. `LoginResponseDTO` is a DTO because its structure reflects what the HTTP client needs to receive, which may change independently of how the domain handles tokens.

The discipline of keeping these distinct prevents domain concepts from being shaped by API concerns and API contracts from being rigidly coupled to domain internals.

---

## Module Design & Responsibility Boundaries

### Auth Module Responsibilities

The Auth Module owns everything related to the act of authenticating and the lifecycle of tokens:

- Defining what information is needed to authenticate a user (`AuthUser`, `AuthUserSnapshot`)
- Enforcing business rules about whether authentication is permitted (`LoginPermission`, `canLogin()`)
- Orchestrating the authentication sequence (`LoginService`)
- Defining contracts for loading user data and verifying credentials (ports)
- Generating and validating tokens
- Intercepting and authenticating incoming HTTP requests

The Auth Module does **not** know how users are stored, how PINs are hashed, or what other data the User entity carries.

### User Module Responsibilities

The User Module owns everything related to the user as a business entity:

- Storing and retrieving user data
- Hashing PINs at creation time and verifying PINs at login time
- Managing user status, profile, and lifecycle

The User Module does **not** know about tokens, login flows, or authentication orchestration. It simply provides implementations for the ports that the Auth Module defines.

### The Integration Point

The User Module's infrastructure layer contains adapter classes that implement the Auth Module's port interfaces. This is the only connection between the two modules, and it flows in one direction: the User Module satisfies contracts that the Auth Module defines.

This arrangement means:
- The Auth Module defines the contracts it needs
- The User Module fulfills those contracts
- Neither module reaches into the other's internals

---

## Domain Layer — Concepts & Design

The domain layer is the heart of the system. It must be entirely self-contained, with no framework dependencies whatsoever.

### AuthUser — The Domain Entity

`AuthUser` represents a user in the context of authentication. It is not the same as the full `User` entity in the User Module — it contains only what authentication needs to know.

Its primary responsibilities are:
- Carrying the user's identity (ID and username)
- Holding the user's current login permission status
- Providing a business method (`canLogin()`) that encapsulates the rule about whether this user is allowed to proceed through authentication

The `canLogin()` method is a business rule, not a technical check. It reads: "Can this user log in?" — and the answer is determined by the `LoginPermission` value assigned to them, which may reflect account lock status, verification status, or administrative disabling.

### LoginPermission — The Permission Enum

`LoginPermission` captures all the reasons a user may or may not be permitted to log in. Each value carries two pieces of information: whether login is allowed, and if not, the business reason.

Possible states include:
- **ALLOWED** — No restrictions, authentication may proceed
- **ACCOUNT_LOCKED** — Locked due to failed attempts or administrative action
- **ACCOUNT_DISABLED** — Administratively disabled
- **PENDING_VERIFICATION** — Phone number or identity not yet verified

This enum belongs in the domain because it expresses a pure business concept: the set of states that affect login eligibility.

### AuthUserSnapshot — The Data Snapshot

`AuthUserSnapshot` is an immutable record of user data as it existed at a point in time. It is the object returned by `LoadAuthUserPort` and serves as the raw material from which `AuthUser` is constructed.

It carries: userId, username, phone number, and login permission. It is not a domain entity — it does not have behavior — but it is the bridge between the User Module's storage and the Auth Module's domain entity creation.

### TokenPair — The Value Object

`TokenPair` encapsulates the two tokens issued together at login: the access token (short-lived, used for API calls) and the refresh token (longer-lived, used only to obtain a new access token).

It is a value object because:
- The two tokens have no meaning independent of each other in the context of issuance
- It should be immutable from the moment of creation
- It is defined entirely by its values, not by an identity

### Domain Ports — The Contracts

Ports are interfaces defined in the domain that describe capabilities the domain needs. The domain uses these interfaces; the infrastructure implements them.

**LoadAuthUserPort** — Declares the ability to retrieve an `AuthUserSnapshot` by phone number or user ID. The domain calls this; the User Module's infrastructure implements it.

**PinVerificationPort** — Declares the ability to verify a raw PIN against a stored hash for a given user ID. The domain calls this; the User Module's infrastructure implements it using BCrypt or similar.

**TokenGenerationPort** — Declares the ability to generate a `TokenPair` from an `AuthUser`, and to refresh a token pair from a valid refresh token. The Auth Module's infrastructure implements this using PASETO.

### Domain Exceptions

Domain exceptions express business failure conditions, not technical ones. They are thrown by the application layer when business rules are violated.

**AuthenticationException** — The base exception for all authentication failures. Never thrown directly; always use a specific subclass.

**InvalidCredentialsException** — Thrown when the phone number is not found or the PIN does not match. Crucially, both cases throw the same exception to avoid revealing which part of the credential was wrong (a security principle called credential ambiguity).

**AccountLockedException** — Thrown when the user is found and the PIN is correct, but `canLogin()` returns false. Carries the human-readable reason from `LoginPermission`.

---

## Application Layer — Concepts & Design

The application layer contains use cases. It orchestrates domain objects and ports to fulfill business operations, but contains no business rules itself — those live in the domain.

### LoginService — The Orchestrator

`LoginService` is the central coordinator of the login use case. Its job is to sequence the steps of authentication in the correct order, using the ports and domain entities available to it.

The sequence it enforces is:

1. Attempt to load the user by phone number via `LoadAuthUserPort`
2. Construct the `AuthUser` domain entity from the snapshot
3. Check `canLogin()` — if false, throw `AccountLockedException`
4. Verify the PIN via `PinVerificationPort` — if false, throw `InvalidCredentialsException`
5. Generate a `TokenPair` via `TokenGenerationPort`
6. Map the result to a `LoginUserDTO` and return it

The order matters. Business rule checks (canLogin) come before credential verification to avoid unnecessary computation. Credential verification comes before token generation for obvious reasons.

### LoginCommandHandler — The Entry Point

The `LoginCommandHandler` is a thin wrapper around `LoginService` that follows the CQRS (Command Query Responsibility Segregation) pattern. Commands represent intentions to change state (login is a command — it results in a session being established). The handler receives the command and delegates to the service.

This separation means the presentation layer calls the handler, not the service directly. This creates a clean seam for adding cross-cutting concerns (logging, metrics, validation) at the command level without modifying business logic.

### LoginUserCommand — The Command Object

`LoginUserCommand` is an immutable object carrying the data needed to execute a login: phone number and raw PIN. It is created by the presentation layer from the HTTP request data and passed to the command handler.

Commands are distinct from DTOs in that they represent an intent, not just data transfer.

### LoginUserDTO — The Application Response

`LoginUserDTO` is the application layer's representation of a successful login result. It carries the access token, refresh token, user ID, and username — enough information for the presentation layer to build whatever HTTP response format is required.

It is shaped by what the application layer needs to communicate upward, not by what the HTTP client expects.

### LoginMapper — The Translation Layer

`LoginMapper` translates between domain concepts and application DTOs. It converts an `AuthUser` and a `TokenPair` into a `LoginUserDTO`. This mapping lives in the application layer, keeping the domain free from awareness of how its objects are represented externally.

---

## Infrastructure Layer — Concepts & Design

The infrastructure layer contains all implementations that deal with the external world: databases, tokens, network calls, and file systems. Every infrastructure class implements an interface defined in the domain.

### PasetoTokenGenerationAdapter — Token Creation

This adapter implements `TokenGenerationPort`. Its responsibility is to take an `AuthUser` and produce a `TokenPair` using the PASETO protocol.

**What goes into an access token:**
- Subject (user ID)
- Username claim
- Token type claim ("access")
- Issued-at timestamp
- Expiration timestamp (typically 15 minutes)

**What goes into a refresh token:**
- Subject (user ID)
- Token type claim ("refresh")
- Issued-at timestamp
- Expiration timestamp (typically 7 days)
- No username — refresh tokens are minimal by design

**Why keep tokens minimal:** Every claim added to a token increases its size and creates a coupling between the token's claims and the system's data model. Include only what is strictly necessary for the token's purpose.

### PasetoTokenValidator — Token Verification

Responsible for parsing and validating an incoming PASETO token string. Validation includes:

- Signature/encryption verification (proves the token was issued by this system)
- Expiration check (proves the token is still within its validity window)
- Token type check (ensures an access token is not being used as a refresh token or vice versa)

The validator returns a `TokenPayload` — a simple object carrying the validated claims — or throws a `TokenValidationException` if validation fails for any reason.

### PasetoAuthenticationFilter — Request Interception

This filter runs before every request (except public endpoints). It is the boundary between unauthenticated HTTP and authenticated application logic.

**Its sequence:**

1. Check if the request path is a public endpoint. If so, skip authentication and continue.
2. Extract the `Authorization` header. If missing or malformed, return a 401 response immediately.
3. Pass the token to `PasetoTokenValidator`.
4. If validation succeeds, attach the user ID and username as request attributes.
5. If validation fails, return a 401 response with an appropriate error message.
6. If authentication succeeds, pass the request onward to the actual endpoint handler.

**Why attach to request attributes rather than a thread-local or security context:** Request attributes are the simplest, most explicit mechanism for passing data through a request's lifecycle. They do not require any framework-specific context management.

**Public endpoint handling:** Rather than having protected endpoints check for authentication, the filter proactively rejects unauthenticated requests. Public endpoints are explicitly whitelisted. Everything else requires a valid token.

### AuthInfrastructureConfig — Bean Configuration

The configuration class wires together the infrastructure components — PASETO libraries, secret key loading, expiration time injection — and exposes them as Spring beans. This is the only place where infrastructure-specific setup lives.

---

## Presentation Layer — Concepts & Design

The presentation layer translates between the HTTP world and the application layer. It handles request format, validation, response serialization, and HTTP status codes.

### LoginRequestDTO — Inbound Data

Carries the raw data from the HTTP request body: phone number and PIN. This DTO should include validation annotations to catch malformed input before it reaches the application layer.

The presentation layer's DTO is distinct from the command object. The DTO represents raw HTTP data; the command represents a validated, typed intent.

### LoginResponseDTO — Outbound Data

Carries the data the HTTP client receives: access token, refresh token, and token expiration duration. The shape of this DTO is driven by client needs, not by how the domain represents tokens internally.

This separation means the API response format can change (e.g., adding a `tokenType` field, or renaming `accessToken` to `token`) without touching the domain or application layers.

### LoginPresentationMapper — HTTP Translation

Converts between the application layer's `LoginUserDTO` and the presentation layer's `LoginResponseDTO`. This mapping accommodates differences in field naming, data formatting, and the addition of HTTP-specific metadata (like expiration duration in seconds).

### LoginController — The HTTP Endpoint

The controller's role is narrow:

1. Receive the HTTP request
2. Deserialize and validate the request body
3. Construct a command from the validated data
4. Delegate to the command handler
5. Receive the application DTO result
6. Map it to a presentation DTO
7. Return an `ResponseEntity` with the appropriate status code

The controller contains no business logic and no conditional branching based on business rules. Those concerns belong in the application and domain layers.

---

## Authentication Flow — Step by Step

### The Complete Login Sequence

**Stage 1: Request Arrival**
The client sends a POST request to `/api/auth/login` with a JSON body containing the phone number and PIN. The request passes through the `PasetoAuthenticationFilter`, which recognizes `/api/auth/login` as a public endpoint and allows it through without token validation.

**Stage 2: Controller Processing**
The `LoginController` receives the request. It deserializes the JSON body into a `LoginRequestDTO` and triggers bean validation. If validation fails (empty phone number, missing PIN), a 400 response is returned immediately without touching the application layer. If validation passes, the controller creates a `LoginUserCommand` and passes it to `LoginCommandHandler`.

**Stage 3: Command Handling**
`LoginCommandHandler` receives the command and delegates to `LoginService.login()`.

**Stage 4: User Loading**
`LoginService` calls `LoadAuthUserPort.findByPhoneNumber()`. This port call crosses the module boundary into the User Module's infrastructure. The adapter queries the database, maps the result to an `AuthUserSnapshot`, and returns it. If no user is found, `LoginService` immediately throws `InvalidCredentialsException`.

**Stage 5: Domain Entity Construction**
`LoginService` constructs an `AuthUser` from the `AuthUserSnapshot`. The `AuthUser` is a domain entity, not a database record. It encapsulates business behavior.

**Stage 6: Login Permission Check**
`LoginService` calls `authUser.canLogin()`. This is a domain-level business rule check. If it returns false, `LoginService` retrieves the denial reason from the entity and throws `AccountLockedException`. No credential check happens — there is no point verifying a PIN for an account that cannot log in.

**Stage 7: PIN Verification**
`LoginService` calls `PinVerificationPort.verifyPin()` with the user ID and raw PIN from the command. This crosses the module boundary again. The User Module's adapter retrieves the stored PIN hash and uses BCrypt to compare. If the match fails, `InvalidCredentialsException` is thrown.

**Stage 8: Token Generation**
With identity verified and permissions confirmed, `LoginService` calls `TokenGenerationPort.generateTokenPair()`. The infrastructure adapter generates both tokens using PASETO, embedding the user ID and username in the access token.

**Stage 9: Result Assembly**
`LoginService` calls `LoginMapper.toDTO()` to convert the `AuthUser` and `TokenPair` into a `LoginUserDTO`. This DTO is returned to `LoginCommandHandler`, which returns it to the controller.

**Stage 10: Response Construction**
The controller passes the `LoginUserDTO` to `LoginPresentationMapper.toResponse()`, producing a `LoginResponseDTO`. This is wrapped in a `ResponseEntity` with HTTP 200 and returned to the client.

**Stage 11: Client Receives Tokens**
The client receives the JSON response containing the access token, refresh token, and expiration duration. It stores these and uses the access token for subsequent requests.

---

## Token Validation Flow

### Every Protected Request

Every request to a protected endpoint follows this validation path before the actual endpoint handler runs.

**Step 1: Filter Intercepts**
`PasetoAuthenticationFilter` intercepts the request before it reaches any controller.

**Step 2: Public Endpoint Check**
The filter checks whether the request path matches a whitelisted public path. If it does, the filter immediately passes the request through the chain without any token processing.

**Step 3: Token Extraction**
The filter reads the `Authorization` header. If the header is absent or does not begin with `Bearer `, the filter writes a 401 response and stops the chain. No request attributes are set.

**Step 4: Token Validation**
`PasetoTokenValidator.validate()` is called with the extracted token string. It:
- Verifies the token's cryptographic signature or encryption
- Checks the expiration timestamp against the current time
- Confirms the token type is "access" (not "refresh")

If any check fails, a `TokenValidationException` is thrown.

**Step 5: Exception Handling**
If `TokenValidationException` is caught, the filter writes a 401 response and stops. The request never reaches the controller.

**Step 6: User Attachment**
If validation succeeds, the filter extracts the user ID and username from the token payload and attaches them as request attributes (`userId`, `username`).

**Step 7: Chain Continuation**
The filter calls `filterChain.doFilter()`, passing the now-enriched request to the next filter and ultimately to the controller.

**Step 8: Controller Access**
Protected controllers read the `userId` attribute from the request. If it is null (which should not happen for protected endpoints, since the filter would have rejected the request), an `UnauthorizedException` is thrown as a safety check.

### Token Refresh Flow

When the access token expires, the client uses the refresh token to obtain a new token pair without requiring the user to re-authenticate.

1. Client sends the refresh token to a dedicated refresh endpoint
2. The filter validates that the token is cryptographically valid and not expired
3. The application layer additionally verifies it is a refresh token (not an access token)
4. The old refresh token is invalidated (token rotation)
5. A new token pair is generated and returned
6. The client updates its stored tokens

Token rotation means each refresh token can only be used once. If a stolen refresh token is used after the legitimate holder has already refreshed, the mismatch is detectable and both tokens can be revoked.

---

## Security Considerations

### Credential Ambiguity

When authentication fails, the error response should never reveal whether the phone number was not found or whether the PIN was incorrect. Both cases return the same generic "Invalid credentials" message. This prevents attackers from using the login endpoint to enumerate valid phone numbers.

### PIN Storage

PINs must never be stored in plain text. The User Module must hash PINs using a strong, adaptive hashing algorithm (BCrypt, Argon2, or scrypt) at the time of creation. Adaptive algorithms are preferable because their cost factor can be increased as hardware improves.

The PIN verification process compares the raw PIN against the stored hash using the hashing library's comparison function — never by hashing the raw PIN and comparing the strings directly (which is vulnerable to timing attacks in naive implementations).

### PASETO Over JWT

PASETO (Platform-Agnostic Security Token) addresses several known weaknesses of JWT:

**No Algorithm Confusion** — JWT allows the algorithm to be specified in the token header, which has led to attacks where `alg: none` or symmetric algorithms are used against asymmetric key systems. PASETO's version and purpose are fixed in the protocol.

**Built-in Encryption** — PASETO supports local (symmetric encryption) and public (asymmetric signing) modes. Local tokens are encrypted, meaning their claims cannot be read without the secret key.

**Versioned Protocol** — PASETO versions are explicitly defined and old versions can be deprecated cleanly.

### Rate Limiting

The login endpoint must be rate-limited. Without rate limiting, an attacker can attempt unlimited PIN guesses. Rate limiting should be applied per IP address at minimum, and ideally also per phone number.

A common approach: allow a maximum number of attempts per window (e.g., 5 attempts per minute per IP). After exceeding the limit, return 429 Too Many Requests.

### Failed Login Attempt Tracking

Beyond rate limiting at the HTTP level, the system should track failed PIN attempts per account within the application layer. After a threshold of failures (e.g., 5), the account's `LoginPermission` should be updated to `ACCOUNT_LOCKED`. This protects against distributed attacks that evade per-IP rate limiting.

The `LoginService` is the right place to increment the failure counter and trigger locking, because it is the layer that knows whether a PIN verification failed.

### HTTPS Enforcement

All production traffic must travel over HTTPS. Tokens transmitted in plain text can be intercepted. The application server should be configured to reject plain HTTP connections or redirect them to HTTPS.

### Token Revocation

PASETO tokens are stateless by default — once issued, they are valid until they expire. For logout or security events (password change, account compromise), a token blacklist or token versioning strategy is necessary.

**Blacklist approach:** Store revoked token identifiers (e.g., a unique `jti` claim) in a fast store (Redis). The validator checks each token against the blacklist.

**Token versioning approach:** Store a per-user token version in the database. Include the version in the token claims. If a user's version increments (due to logout or security event), all previously issued tokens become invalid.

### Token Expiration Design

Access tokens should have a short lifetime (10–15 minutes). This limits the window of exposure if a token is compromised. Refresh tokens should have a longer lifetime (7–30 days) but must be rotated on each use and revocable.

---

## Testing Strategy

### What to Test and Why

Testing strategy for an authentication system must cover three distinct levels, each with a different purpose.

### Domain Layer Tests

Domain tests verify business rules in isolation, with no framework, no database, and no HTTP. They are the fastest and most targeted tests.

Key scenarios to cover:

- An `AuthUser` with `LoginPermission.ALLOWED` should return true from `canLogin()`
- An `AuthUser` with any non-allowed permission should return false from `canLogin()` and provide the correct denial reason
- `TokenPair` should reject null arguments
- `LoginPermission` values should carry correct boolean and reason values

These tests run in milliseconds and should be numerous. They are the safety net for business logic changes.

### Application Layer Tests

Application layer tests verify that `LoginService` correctly orchestrates the authentication steps. All ports are mocked — no real database, no real token generation.

Key scenarios to cover:

- Successful login with valid phone number and correct PIN should produce a `LoginUserDTO` with non-null tokens
- User not found by phone number should result in `InvalidCredentialsException`
- User found but `canLogin()` returns false should result in `AccountLockedException`
- User found and `canLogin()` returns true, but PIN verification fails, should result in `InvalidCredentialsException`
- Verify that `LoadAuthUserPort`, `PinVerificationPort`, and `TokenGenerationPort` are called in the correct order

These tests verify orchestration logic without touching any real infrastructure.

### Infrastructure Layer Tests

Infrastructure tests verify that the PASETO adapter correctly generates and validates tokens.

Key scenarios:

- A generated access token should be parseable and return the correct user ID and username
- A generated access token should expire after the configured duration
- An expired token should fail validation with an appropriate exception
- A refresh token should not pass access token validation (type mismatch)
- A tampered token should fail cryptographic validation

### Presentation Layer / Integration Tests

Integration tests verify the full HTTP flow, from request to response, with mocked application layer dependencies or a full in-memory stack.

Key scenarios:

- A valid phone number and PIN should return HTTP 200 with access and refresh tokens in the body
- A missing phone number or PIN should return HTTP 400
- An invalid phone number (not found) should return HTTP 401
- An incorrect PIN should return HTTP 401
- A locked account should return HTTP 403 with the denial reason
- A protected endpoint called without a token should return HTTP 401
- A protected endpoint called with an expired token should return HTTP 401
- A protected endpoint called with a valid token should return HTTP 200

---

## File Structure Reference

### Auth Module

```
auth/
├── domain/
│   ├── model/
│   │   ├── entity/
│   │   │   └── AuthUser.java                      ← Domain entity with canLogin() business rule
│   │   ├── enums/
│   │   │   └── LoginPermission.java               ← All possible login permission states
│   │   ├── dto/
│   │   │   └── AuthUserSnapshot.java              ← Immutable snapshot from User Module
│   │   └── value/
│   │       └── TokenPair.java                     ← Immutable access + refresh token pair
│   ├── port/
│   │   ├── LoadAuthUserPort.java                  ← Contract: load user by phone or ID
│   │   ├── PinVerificationPort.java               ← Contract: verify raw PIN against hash
│   │   └── TokenGenerationPort.java               ← Contract: generate and refresh tokens
│   └── exception/
│       ├── AuthenticationException.java           ← Base authentication failure
│       ├── InvalidCredentialsException.java       ← Unknown phone or wrong PIN
│       └── AccountLockedException.java            ← User exists but cannot log in
│
├── application/
│   └── login/
│       ├── command/
│       │   └── LoginUserCommand.java              ← Immutable intent object: phone + PIN
│       ├── dto/
│       │   └── LoginUserDTO.java                  ← Application layer login result
│       ├── handler/
│       │   └── LoginCommandHandler.java           ← CQRS entry point for login command
│       ├── mapper/
│       │   └── LoginMapper.java                   ← AuthUser + TokenPair → LoginUserDTO
│       └── service/
│           └── LoginService.java                  ← Full authentication orchestration
│
├── infrastructure/
│   ├── security/
│   │   ├── PasetoTokenGenerationAdapter.java      ← Implements TokenGenerationPort
│   │   ├── PasetoTokenValidator.java              ← Validates incoming PASETO tokens
│   │   └── PasetoAuthenticationFilter.java        ← Per-request token interception
│   └── config/
│       └── AuthInfrastructureConfig.java          ← Bean wiring for infrastructure
│
└── presentation/
    └── login/
        ├── dto/
        │   ├── LoginRequestDTO.java               ← Deserializes HTTP request body
        │   └── LoginResponseDTO.java              ← Serializes HTTP response body
        ├── mapper/
        │   └── LoginPresentationMapper.java       ← LoginUserDTO → LoginResponseDTO
        └── controller/
            └── LoginController.java               ← POST /api/auth/login endpoint
```

### User Module (Integration Points)

```
user/
└── infrastructure/
    └── adapter/
        ├── LoadAuthUserAdapter.java               ← Implements LoadAuthUserPort
        └── PinVerificationAdapter.java            ← Implements PinVerificationPort
```

---

## Implementation Phases & Order

### Why Order Matters

Implementation must follow dependency direction. You cannot implement `LoginService` before `LoadAuthUserPort` exists, because the service depends on the port. Work from the inside (domain) outward (infrastructure, presentation).

### Phase 1: Domain Layer

Complete all domain components first. Nothing else can be written until the contracts and entities exist.

1. `TokenPair` value object — defines what a token pair is
2. `TokenGenerationPort` interface — defines the token generation contract
3. `AuthenticationException`, `InvalidCredentialsException`, `AccountLockedException` — defines failure language

Acceptance criteria: The domain compiles with no dependencies on any framework or library except standard Java.

### Phase 2: Application Layer

Build the orchestration logic once the domain contracts are in place.

4. `LoginUserDTO` — defines what the application layer returns from a login
5. `LoginMapper` — defines how domain objects become application DTOs
6. `LoginService` — the full authentication sequence, using mocked ports in early testing
7. `LoginCommandHandler` — thin wrapper connecting command to service

Acceptance criteria: `LoginService` can be instantiated with mocked ports and tested fully with unit tests.

### Phase 3: Infrastructure Layer

Implement the concrete adapters and filters.

8. `PasetoTokenGenerationAdapter` — implements `TokenGenerationPort` using the PASETO library
9. `PasetoTokenValidator` — parses and validates PASETO token strings
10. `PasetoAuthenticationFilter` — per-request filter extracting and validating tokens
11. `AuthInfrastructureConfig` — wires all infrastructure beans together

Acceptance criteria: Tokens can be generated and validated end-to-end in isolated infrastructure tests.

### Phase 4: Presentation Layer

Build the HTTP interface last, once everything below it is stable.

12. `LoginRequestDTO` — with validation annotations
13. `LoginResponseDTO` — shaped for client consumption
14. `LoginPresentationMapper` — converts application DTO to presentation DTO
15. `LoginController` — the HTTP endpoint with error handling

Acceptance criteria: A full HTTP integration test succeeds from POST request to 200 response with tokens.

---

## Production Checklist

### Security

- [ ] PASETO secret key is a minimum 32-byte random value stored in environment variables, never in source code
- [ ] Access token expiration is 15 minutes or less
- [ ] Refresh token expiration is configured for your use case (7–30 days)
- [ ] PINs are hashed with BCrypt (cost factor ≥ 12) or Argon2 — never stored in plain text
- [ ] All production traffic is served over HTTPS
- [ ] CORS is configured to allow only trusted origins
- [ ] Security headers are set (HSTS, X-Content-Type-Options, etc.)
- [ ] Login endpoint is rate-limited (per IP, per phone number)
- [ ] Failed login attempts are tracked and trigger account locking
- [ ] Token revocation mechanism is implemented (blacklist or versioning)
- [ ] Refresh token rotation is implemented

### Token Management

- [ ] Token blacklist or revocation strategy is in place
- [ ] Refresh token rotation is implemented and tested
- [ ] Token expiration durations are validated against business requirements
- [ ] Tokens include a unique identifier (`jti`) for revocation tracking
- [ ] Token type validation prevents access tokens from being used as refresh tokens

### Monitoring & Observability

- [ ] Successful authentication events are logged with user ID (not credentials)
- [ ] Failed authentication events are logged with phone number hash and failure reason
- [ ] Token validation failures are monitored and alerted on anomalous rates
- [ ] Login attempt rates are monitored for brute-force patterns
- [ ] Response times for the login endpoint are tracked

### Configuration

- [ ] All secrets are injected via environment variables
- [ ] Token expiration times are configurable without code changes
- [ ] Rate limit thresholds are configurable without code changes
- [ ] Account lock duration is configurable
- [ ] Public endpoint whitelist is reviewed and confirmed minimal

### Testing

- [ ] Domain layer unit tests cover all `LoginPermission` states
- [ ] Application layer unit tests cover all authentication failure paths
- [ ] Infrastructure layer tests confirm PASETO generation and validation
- [ ] Integration tests confirm the full login HTTP flow
- [ ] Integration tests confirm that expired tokens are rejected
- [ ] Integration tests confirm that tampered tokens are rejected
- [ ] Load tests confirm acceptable performance under expected concurrent login volume

### Documentation

- [ ] API endpoints are documented with request/response schemas
- [ ] All error codes and error messages are documented
- [ ] Token format and claims are documented for client teams
- [ ] Token expiration and refresh strategy is documented
- [ ] Account locking and unlock procedures are documented
- [ ] Runbook for security incidents (compromised tokens, brute-force attack) exists

---

## Conceptual Summary

### The Mental Model

The entire system can be understood through a single analogy: a security desk at a building entrance.

**Login** is like arriving at the desk, presenting your ID (phone number) and PIN. The guard (LoginService) checks that you are in the system (LoadAuthUserPort), verifies your PIN (PinVerificationPort), confirms you are not on a restricted list (canLogin()), and issues you a visitor badge (TokenPair).

**Every subsequent request** is like walking through an internal door. The badge reader (PasetoAuthenticationFilter) scans your badge, confirms it is genuine and not expired, and either unlocks the door or turns you away.

**The two modules** are like two departments: Security (Auth Module) knows how to authenticate and issue badges but does not maintain the employee database. HR (User Module) maintains the employee database but does not decide how authentication works. Security asks HR to confirm employee details — HR does not run the security desk.

### Your Custom Components as Security Equivalents

| Custom Component | Role in the Analogy |
|-----------------|---------------------|
| `LoginService` | The security guard who runs the check-in process |
| `AuthUser` | The employee record in the context of security clearance |
| `LoginPermission` | The list of reasons someone might be denied entry |
| `LoadAuthUserPort` | The phone line to HR to confirm someone works here |
| `PinVerificationPort` | The process of checking the PIN against records |
| `TokenGenerationPort` | The badge printer |
| `TokenPair` | The issued badge (access) and badge renewal card (refresh) |
| `PasetoAuthenticationFilter` | The badge readers on every internal door |

### What You Have Built

By following this design, you have an authentication system that:

- Expresses business rules in pure domain code with no framework pollution
- Can be fully unit tested without a database, HTTP server, or token library
- Can have its authentication method changed (phone/PIN → email/password → biometrics) by replacing adapters, not rewriting the domain
- Can be migrated to a different web framework by replacing only the presentation and infrastructure layers
- Is explicit at every step, making debugging, auditing, and onboarding new developers straightforward

---

*Document Version: 2.0 — Theoretical & Conceptual Reference*
*Based on implementation research: Custom Authentication without Spring Security*
