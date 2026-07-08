# Auth Module — Architecture & Design Documentation

> **Project:** EESOO
> **Architecture:** Modular Monolith (Spring Modulith)
> **Pattern:** Ports & Adapters (Hexagonal Architecture)
> **Token:** PASETO

---

## Table of Contents

1. [Overview & Design Philosophy](#1-overview--design-philosophy)
2. [Architecture Pattern: Ports & Adapters](#2-architecture-pattern-ports--adapters)
3. [Three Key Concepts](#3-three-key-concepts)
4. [Full Login Flow](#4-full-login-flow)
5. [Module Structure & Folder Layout](#5-module-structure--folder-layout)
6. [How Spring Modulith Enforces This](#6-how-spring-modulith-enforces-this)
7. [Who Owns Credentials? (Option A vs B)](#7-who-owns-credentials-option-a-vs-b)
8. [Auth Infrastructure Layer](#8-auth-infrastructure-layer)
9. [Future: Migrating to Microservices](#9-future-migrating-to-microservices)
10. [Summary: The Golden Rules](#10-summary-the-golden-rules)

---

## 1. Overview & Design Philosophy

### Core Question: Who Does What?

| User Module | Auth Module |
|---|---|
| Owns user data (username, PIN, status) | Handles login & credential verification |
| Handles registration & profile | Generates & validates tokens (PASETO) |
| Manages user entity & repository | Applies security rules |
| Source of truth for identity | Issues access to the system |

> **One-Line Rule:** User = WHO you are. Auth = CAN you enter.

### Why Separate Them?

Login is NOT just data retrieval. It involves verifying credentials, applying business security rules, generating tokens, and managing sessions. These are authentication concerns — they do not belong to user management.

- ✔ User module stays focused on identity data
- ✔ Auth module stays focused on access control
- ✔ Each module can evolve independently
- ✔ Easy to extract into microservices later

---

## 2. Architecture Pattern: Ports & Adapters

The design uses the Ports & Adapters pattern (also called Hexagonal Architecture). This is the industry standard for clean modular systems.

### The Core Idea

> The auth module defines **WHAT** it needs (port = interface). The user module provides **HOW** to get it (adapter = implementation). Auth never depends on user internals directly.

### Why Not Other Options?

| Approach | Simplicity | Clean Boundaries | Scalability | Verdict |
|---|---|---|---|---|
| Direct dependency (auth → userService) | ⭐⭐⭐ | ⭐ | ⭐ | Only for prototypes |
| Port + Adapter (current design) | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ✔ Recommended |
| Event-based | ⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ✘ Not for login (async) |
| Shared User entity | ⭐⭐⭐ | ⭐ | ⭐ | ✘ Avoid — breaks boundaries |

### Why Events Don't Work for Login

Login is **synchronous** — it needs an immediate Yes/No answer before the user can proceed. Events are **asynchronous** — they fire a message and move on without waiting for a reply.

Using events for login is like sending a text message to a bouncer: your code would move on before the bouncer even read the message.

```
Synchronous (login):     You ask → You WAIT → You get answer → You continue
Asynchronous (events):   You ask → You move on → Answer arrives "sometime later"
```

---

## 3. Three Key Concepts

### 3.1 Port (Interface) — Defined in Auth Module

The port is a contract. It says: *"I need someone to give me user authentication data by username — I don't care how."*

```java
// Location: auth/application/port/AuthUserReader.java

public interface AuthUserReader {

    Optional<AuthUserSnapshot> findByUsername(String username);
}
```

> **Key Rule:** The interface lives where it is **USED** (auth module), NOT where the data lives.

---

### 3.2 Adapter (Implementation) — Lives in User Module

The adapter is the implementation. It knows how to go to the database and return the data the auth module needs.

```java
// Location: user/infrastructure/auth/AuthUserReaderImpl.java

@Component
public class AuthUserReaderImpl implements AuthUserReader {

    private final UserRepository userRepository;

    @Override
    public Optional<AuthUserSnapshot> findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(user -> new AuthUserSnapshot(
                user.getUserId().getValue(),
                user.getUsername().getValue(),
                user.getPin().getHashedValue(),
                user.getStatus()
            ));
    }
}
```

---

### 3.3 Snapshot (Boundary Object) — Owned by Auth Module

The snapshot is a minimal, read-only object that contains only what auth needs. It prevents auth from ever depending on the full `User` entity.

| Full User Entity (user module) | AuthUserSnapshot (auth module) |
|---|---|
| firstName | userId |
| lastName | username |
| email | hashedPin |
| phoneNumber | status |
| devices | `canLogin()` method |
| timestamps | |
| ...everything else | |

> **Real-World Analogy:** A full User is like a customer's complete bank profile. AuthUserSnapshot is like an ATM card — it contains only what is needed to authenticate: account number, PIN, and status.

### Why Port Alone Is Not Enough

Even with a port, if it returns the full `User` entity, auth still sees all the internal fields. The snapshot acts as a **data filter** — it limits what auth can see, not just how it communicates.

```
Port controls:      HOW modules communicate
Snapshot controls:  WHAT data they see
```

---

## 4. Full Login Flow

### Step-by-Step

1. User sends `POST /login  { username, pin }`
2. `AuthController` receives the request and delegates to `LoginService`
3. `LoginService` calls `authUserReader.findByUsername(username)`
4. Spring injects `AuthUserReaderImpl` (from user module) automatically
5. `AuthUserReaderImpl` fetches from `UserRepository` and maps to `AuthUserSnapshot`
6. `LoginService` checks: `canLogin()` → yes, then PIN matches → yes
7. `TokenGenerator` creates and returns a PASETO token

### Visual Flow

```
AuthController (auth)
      ↓
LoginService (auth)
      ↓
AuthUserReader        ← interface (port)   ← lives in auth
      ↓
AuthUserReaderImpl    ← class (adapter)    ← lives in user module
      ↓
UserRepository (user module)
      ↓
Database
```

### LoginService Code

```java
public class LoginService {

    private final AuthUserReader authUserReader;
    private final PinEncoder pinEncoder;
    private final TokenGenerator tokenGenerator;

    public String login(String username, String pin) {

        AuthUserSnapshot user = authUserReader.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.canLogin()) {
            throw new RuntimeException("User cannot login");
        }

        if (!pinEncoder.matches(pin, user.getHashedPin())) {
            throw new RuntimeException("Invalid credentials");
        }

        return tokenGenerator.generate(user);
    }
}
```

---

## 5. Module Structure & Folder Layout

### Auth Module

```
auth/
 ├── domain/
 │     ├── AuthUserSnapshot.java          ← boundary object
 │     └── model/
 │           └── (auth domain models)
 │
 ├── application/
 │     ├── port/
 │     │     └── AuthUserReader.java       ← interface (port)
 │     └── service/
 │           └── LoginService.java         ← business logic
 │
 ├── infrastructure/
 │     ├── PasetoTokenGenerator.java       ← implements TokenGenerator
 │     ├── TokenVerifier.java              ← validates incoming tokens
 │     ├── AuthFilter.java                 ← Spring Security filter
 │     └── SecurityConfig.java            ← Spring Security config
 │
 └── presentation/
       └── AuthController.java            ← REST endpoints
```

### User Module

```
user/
 ├── domain/
 │     ├── model/
 │     │     └── entity/
 │     │           └── User.java           ← full user entity
 │     └── repository/
 │           └── UserRepository.java
 │
 └── infrastructure/
       └── auth/
             └── AuthUserReaderImpl.java   ← adapter (implements auth port)
```

> **Critical Placement Rule:** `AuthUserReaderImpl` lives in the user module (because user owns the data), but it implements an interface defined in the auth module (because auth defines what it needs). This is the key insight of the entire pattern.

---

## 6. How Spring Modulith Enforces This

### What Spring Does (Wiring)

Spring scans all beans at startup. When it sees that `LoginService` requires an `AuthUserReader`, it finds `AuthUserReaderImpl` (which has `@Component`) and injects it automatically. The auth module never needs to know the implementation exists.

### What Modulith Does (Boundary Enforcement)

```java
// Run this in your test to verify all module boundaries are clean
ApplicationModules.of(Application.class).verify();
```

This checks:
- No illegal dependencies between modules
- No access to internal packages of other modules
- No circular dependencies

### What to Expose

- ✔ `AuthUserReaderImpl` — must be visible to other modules (it's an adapter)
- ✔ `User` entity — should remain internal to user module
- ✔ `UserRepository` — should remain internal to user module

---

## 7. Who Owns Credentials? (Option A vs B)

### Option A — User Owns Credentials (Your Choice ✔)

```
User Module:   username  +  hashed PIN  +  status
Auth Module:   verifies them  →  generates token
```

- ✔ Simple and clean
- ✔ Perfect for single-backend systems
- ✔ All clients (web, mobile, admin) share the same backend
- ✔ No unnecessary complexity

### Option B — Auth Owns Credentials (Advanced / Future)

In Option B, the auth module keeps the credentials and the user module only stores profile data. This is used in:

- Multiple independent backend services (microservices)
- External auth providers (Google login, OTP, SSO)
- High-scale systems where auth traffic must scale independently
- Banking / enterprise systems with MFA, device binding, etc.

> **Important Distinction:** Web app + Mobile app + Admin panel does **NOT** require Option B — they are clients (frontends), not separate backend services. You only need Option B when you have multiple **backend** services.

### Comparison

| Factor | Option A (Current) | Option B (Advanced) |
|---|---|---|
| Backend services | Single | Multiple |
| External auth providers | No | Yes |
| Multiple frontends | Supported ✔ | Supported ✔ |
| Complexity | Low | High |
| When to use | Now ✔ | When system scales |

---

## 8. Auth Infrastructure Layer

Even though the adapter for fetching user data lives in the user module, the auth module has its own important infrastructure — everything related to authentication mechanisms.

### Responsibilities

- **Token generation** — PASETO token creation after successful login
- **Token validation** — verifying incoming tokens on protected routes
- **Security filter** — reading token from header, validating, setting security context
- **Security config** — Spring Security configuration, protected/public routes

### Token Generator (Port + Infrastructure)

```java
// Domain defines the contract (port)
interface TokenGenerator {
    String generate(AuthUserSnapshot user);
}

// Infrastructure implements it
@Component
class PasetoTokenGenerator implements TokenGenerator {

    @Override
    public String generate(AuthUserSnapshot user) {
        // use PASETO library here
    }
}
```

> **Why PASETO over JWT?** PASETO (Platform-Agnostic Security Tokens) is cryptographically safer than JWT. JWT allows algorithm confusion attacks (e.g., RS256 → HS256). PASETO uses fixed, secure algorithms with no flexibility to misconfigure.

### Future Changes — Only Adapter Changes

If you later switch from PASETO to JWT, or from database tokens to Redis-backed tokens, only the infrastructure implementations change. The domain interface (`TokenGenerator`) stays the same, and `LoginService` is untouched.

---

## 9. Future: Migrating to Microservices

Your current design is already microservice-ready. When you split into separate services, you only need to replace the adapter — not rewrite any business logic.

### What Changes

```java
// TODAY: Adapter makes a direct DB call
@Component
class AuthUserReaderImpl implements AuthUserReader {
    public Optional<AuthUserSnapshot> findByUsername(String username) {
        return userRepository.findByUsername(username).map(...);
    }
}
```

```java
// FUTURE: Adapter makes an HTTP call to User Service
@Component
class AuthUserReaderHttpAdapter implements AuthUserReader {
    public Optional<AuthUserSnapshot> findByUsername(String username) {
        String url = "http://user-service/api/auth/user?username=" + username;
        AuthUserSnapshot response =
            restTemplate.getForObject(url, AuthUserSnapshot.class);
        return Optional.ofNullable(response);
    }
}
```

### What Does NOT Change

- ✔ `LoginService` — untouched
- ✔ `AuthUserSnapshot` — untouched
- ✔ `AuthUserReader` interface — untouched
- ✔ `TokenGenerator` — untouched
- ✔ All business rules — untouched

### Migration Steps

1. Keep your current design (already done ✔)
2. Create `AuthUserReaderHttpAdapter` with REST call
3. Switch Spring bean config to inject the new adapter
4. Move user module to a separate service
5. Add circuit breaker (Resilience4j) for fault tolerance

---

## 10. Summary: The Golden Rules

| Concept | What It Is | Where It Lives | Why It Matters |
|---|---|---|---|
| Port (`AuthUserReader`) | Interface / contract | Auth module | Auth defines what it needs |
| Adapter (`AuthUserReaderImpl`) | Implementation | User module | User provides how to get it |
| Snapshot (`AuthUserSnapshot`) | Minimal data object | Auth module (domain) | Limits what auth can see |
| `TokenGenerator` | Token creation interface | Auth module (domain) | Decouples token tech from logic |
| `PasetoTokenGenerator` | Token implementation | Auth module (infra) | Replaceable without changing logic |

### Quick Reference

| Question | Answer |
|---|---|
| Where does login logic live? | Auth module |
| Where do credentials live? | User module (Option A) |
| Who defines the port? | Auth (consumer) |
| Who implements the adapter? | User module (owner of data) |
| What crosses the module boundary? | Only `AuthUserSnapshot` |
| What does auth infra handle? | Tokens, filters, security config |
| Multiple frontends → need Option B? | No — Option B is for multiple backends |
| Can this become microservices? | Yes — only replace the adapter |

> **Core Principle:** You are not writing extra code — you are buying future flexibility. Each extra interface and snapshot is a guarantee that your system can change, grow, and scale without rewriting everything from scratch.
