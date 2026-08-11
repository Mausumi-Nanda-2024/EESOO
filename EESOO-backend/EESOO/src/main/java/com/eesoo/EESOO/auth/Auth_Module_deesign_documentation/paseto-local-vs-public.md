# PASETO `local` vs `public`

Both are used to create authentication tokens, but they protect the token differently.

```text
local  → encrypts the token using one secret key
public → signs the token using a private/public key pair
```

The easiest way to understand them is to first understand what happens after login.

---

# 1. Why does the backend generate a token?

Suppose a user logs in:

```text
Username: neha1234
Password: ****
```

The backend checks the credentials:

```text
Are the username and password correct?
        ↓
       Yes
        ↓
Generate an access token
```

The token may represent information such as:

```json
{
  "userId": "f734...",
  "role": "USER",
  "issuedAt": "2026-07-15T10:00:00Z",
  "expiresAt": "2026-07-15T10:15:00Z"
}
```

The backend sends that token to the frontend.

For future requests, the frontend sends it back:

```http
Authorization: Bearer <token>
```

For example:

```text
Frontend requests user profile
        ↓
Sends access token
        ↓
Backend validates the token
        ↓
Backend understands which user made the request
```

PASETO gives you two main ways to protect this token:

```text
local
public
```

---

# 2. What is PASETO `local`?

`local` uses **symmetric encryption**.

Symmetric means:

> The same secret key is used both to create and validate the token.

```text
One secret key
     │
     ├── Encrypts and creates the token
     │
     └── Decrypts and validates the token
```

A local token starts with something like:

```text
v2.local....
```

Here:

```text
v2    → PASETO protocol version
local → token purpose
```

---

## Simple locker example

Imagine a locker that has one key.

The same key is used to:

```text
Lock the locker
Unlock the locker
```

Anyone who possesses that key can do both.

That is how `local` works:

```text
Secret key creates token
Secret key validates token
```

---

# 3. How `local` token creation works

Suppose your authentication module wants to create this token information:

```json
{
  "userId": "123",
  "role": "USER"
}
```

The authentication module has a secret key:

```text
EESOO_SECRET_KEY
```

The process is:

```text
User information
        ↓
Authentication module
        ↓
Encrypt using secret key
        ↓
Generate v2.local token
```

The frontend receives something like:

```text
v2.local.j8FhKQ9s7...
```

The frontend cannot normally see the original information because it is encrypted.

It cannot directly see:

```json
{
  "userId": "123",
  "role": "USER"
}
```

---

# 4. How `local` token validation works

The frontend later sends the token:

```http
Authorization: Bearer v2.local.j8FhKQ9s7...
```

The backend performs these steps:

```text
Receive token
    ↓
Use the same secret key
    ↓
Check whether the token was modified
    ↓
Decrypt the token
    ↓
Read userId and other claims
    ↓
Check expiration
    ↓
Accept or reject request
```

The important point is:

```text
Token creation key = Token validation key
```

Both use the same secret.

---

# 5. What protection does `local` provide?

A local token provides two important protections.

## A. Confidentiality

Confidentiality means the contents are hidden.

Suppose the token contains:

```json
{
  "userId": "123",
  "role": "USER"
}
```

Because the token is encrypted, someone holding the token cannot easily read those values without the secret key.

```text
Token contents → hidden
```

## B. Integrity

Integrity means nobody can secretly change the token.

Suppose an attacker tries to change:

```json
{
  "role": "USER"
}
```

into:

```json
{
  "role": "ADMIN"
}
```

The validation will fail because the token has been modified.

```text
Modified token
      ↓
Validation fails
      ↓
Request rejected
```

So `local` provides:

```text
Encryption + protection against modification
```

---

# 6. Important risk with `local`

Anyone who has the secret key can do everything:

```text
Create tokens
Validate tokens
Decrypt tokens
```

Suppose three separate services have the same key:

```text
Authentication service → secret key
User service           → secret key
Payment service        → secret key
```

Each service can create a valid token because each has the same secret.

Therefore, the secret must be protected carefully.

In your modular monolith, this is manageable because one Spring Boot application handles authentication.

---

# 7. When should `local` be used?

Use `local` when:

* One backend creates and validates tokens.
* Token contents should remain hidden.
* You have a monolith or modular monolith.
* Only trusted backend components need the secret.
* You want simpler key management.

Your current EESOO architecture is:

```text
React Native frontend
        ↓
Spring Boot modular monolith
        ↓
Authentication module
```

The authentication module:

```text
Creates token
Validates token
```

Therefore, `local` is a suitable choice.

---

# 8. What is PASETO `public`?

`public` uses **asymmetric digital signatures**.

Asymmetric means there are two different keys:

```text
Private key
Public key
```

They have different jobs:

```text
Private key → creates and signs tokens
Public key  → verifies tokens
```

A public token starts with something like:

```text
v2.public....
```

---

# 9. Simple signature example

Imagine a university issues a certificate.

The university has a special signature stamp.

```text
University's private stamp → signs certificate
Public verification method → checks signature
```

Other people can verify that the certificate was issued by the university.

But they cannot create a new valid university certificate because they do not possess the private stamp.

That is how `public` works:

```text
Private key signs token
Public key verifies signature
```

---

# 10. How a `public` token is created

Suppose the token contains:

```json
{
  "userId": "123",
  "role": "USER"
}
```

The authentication service holds a private key.

The flow is:

```text
User information
        ↓
Authentication service
        ↓
Sign using private key
        ↓
Generate v2.public token
```

Only the system that possesses the private key can create a valid token.

---

# 11. How a `public` token is validated

Other services can hold the public key.

```text
Request contains token
        ↓
Service receives token
        ↓
Verify signature using public key
        ↓
Accept or reject token
```

For example:

```text
Authentication service
    └── Private key: creates tokens

User service
    └── Public key: validates tokens

Order service
    └── Public key: validates tokens

Payment service
    └── Public key: validates tokens
```

The user, order and payment services can validate tokens, but they cannot create valid tokens.

---

# 12. Is a `public` token encrypted?

No.

A public token is **signed**, not encrypted.

That means:

```text
Token contents can be read
Token contents cannot be safely modified
```

Suppose it contains:

```json
{
  "userId": "123",
  "role": "USER"
}
```

Someone holding the token may be able to read these values.

But if they change:

```text
role: USER
```

to:

```text
role: ADMIN
```

the signature will no longer match.

```text
Modified information
        ↓
Signature verification fails
        ↓
Token rejected
```

Therefore, `public` provides:

```text
Integrity and authenticity
```

But it does not provide:

```text
Payload confidentiality
```

---

# 13. Why is it called `public`?

It does not mean:

```text
The token should be publicly shared
```

It means:

```text
The token is validated using a public key
```

The public key can be distributed safely to token-verifying services.

The private key must remain secret.

---

# 14. Main difference between the keys

## `local`

```text
One secret key
```

The same key:

```text
Creates token
Validates token
Decrypts token
```

## `public`

```text
Two keys
```

The private key:

```text
Creates and signs token
```

The public key:

```text
Verifies token
Cannot create token
```

---

# 15. Complete comparison

| Question                                    | `local`                    | `public`                               |
| -------------------------------------------- | --------------------------- | --------------------------------------- |
| What cryptography is used?                  | Symmetric encryption       | Asymmetric signing                     |
| How many keys?                              | One secret key             | Private and public key                 |
| What creates the token?                     | Secret key                 | Private key                            |
| What validates the token?                   | Same secret key            | Public key                             |
| Is the payload encrypted?                   | Yes                        | No                                     |
| Can someone read the payload?               | Not without the secret key | Yes, but they cannot trust or alter it |
| Can the validating component create tokens? | Yes                        | No, if it only has the public key      |
| Does it detect modification?                | Yes                        | Yes                                    |
| Is key management simpler?                  | Usually yes                | More complex                           |
| Good for one backend?                       | Yes                        | Also possible                          |
| Good for many independent services?         | Less ideal                 | Usually better                         |

---

# 16. Example with your EESOO application

Your current architecture is approximately:

```text
React Native frontend
        ↓
Spring Boot application
        ├── Authentication module
        ├── User module
        └── Device module
```

The authentication module performs:

```text
Login
Password verification
Token generation
Token validation
Spring Security authentication
```

Using `local`, the flow is:

```text
1. User sends username and PIN.

2. Authentication module checks the credentials.

3. Authentication module creates a local token using the secret key.

4. Backend returns the token to the frontend.

5. Frontend stores the token securely.

6. Frontend sends the token with future requests.

7. Authentication filter receives the token.

8. Authentication module validates and decrypts it using the same secret key.

9. Authentication module obtains the user ID.

10. Spring Security stores the authenticated user in SecurityContext.

11. User and device modules receive the authenticated user ID.
```

Your user and device modules do not need to parse the token.

```text
Raw PASETO token
        ↓
Authentication module only
        ↓
Verified authenticated user
        ↓
Other modules
```

---

# 17. Should other modules know the secret key?

Preferably, no.

Even though all modules run in one Spring Boot application, your design should keep token-related code inside the authentication infrastructure.

For example:

```text
auth
├── application
├── domain
└── infrastructure
    └── token
        ├── PasetoTokenGenerator
        ├── PasetoTokenVerifier
        └── PasetoKeyProvider
```

The other modules should receive something simple:

```java
UUID authenticatedUserId
```

They should not receive:

```java
String rawToken
SecretKey pasetoSecret
```

This maintains your module boundary.

---

# 18. What information should be placed in the token?

Keep the token small.

A suitable token may contain:

```json
{
  "sub": "user-uuid",
  "iss": "eesoo-auth",
  "aud": "eesoo-api",
  "iat": "token-created-time",
  "exp": "token-expiration-time",
  "jti": "unique-token-id"
}
```

## Meaning of these claims

### `sub`

`sub` means subject.

It identifies the user:

```text
sub = userId
```

### `iss`

`iss` means issuer.

It tells who generated the token:

```text
iss = eesoo-auth
```

### `aud`

`aud` means audience.

It tells which application should accept the token:

```text
aud = eesoo-api
```

### `iat`

`iat` means issued at.

It tells when the token was created.

### `exp`

`exp` means expiration.

It tells when the token stops being valid.

### `jti`

`jti` is a unique ID for the token.

It can help with tracking or revoking particular tokens.

---

# 19. What should not be placed in the token?

Do not place unnecessary sensitive information such as:

```text
Password
PIN
Password hash
Complete phone number
Secret device identifier
Private personal information
Database credentials
Encryption keys
```

Even with a local encrypted token, only include information required for authentication.

Encryption should not be treated as a reason to put everything inside the token.

---

# 20. Does encryption mean the token cannot be stolen?

No.

This is extremely important.

Both `local` and `public` access tokens are normally used as **bearer tokens**.

Bearer means:

> Whoever possesses the token can send it.

Suppose an attacker steals a valid token.

They do not need to decrypt it. They can simply send the exact token to your backend:

```http
Authorization: Bearer <stolen-token>
```

If the token is still valid, the backend may accept it.

Therefore:

```text
Encryption protects the token contents
Encryption does not stop token theft or replay
```

You must also use:

* HTTPS;
* short token expiration;
* secure frontend storage;
* refresh-token controls, when added;
* logout/revocation strategy;
* no token logging.

---

# 21. Why is HTTPS still required?

You may think:

```text
A local token is already encrypted, so why use HTTPS?
```

Because HTTPS protects the entire request:

```text
Username
Password or PIN
Token
Request body
Response body
Headers
```

PASETO only protects the token itself.

It does not encrypt the whole network connection.

Therefore:

```text
PASETO + HTTPS
```

Both are required.

---

# 22. Where should the local secret key be stored?

Do not write it directly in Java:

```java
private static final String SECRET = "my-secret";
```

Do not commit it to GitHub.

Prefer an environment variable:

```text
PASETO_SECRET_KEY=<secure-random-key>
```

Your Spring configuration can read it:

```properties
security.paseto.secret=${PASETO_SECRET_KEY}
```

Then your production environment supplies the actual value.

```text
Source code
    └── contains variable name only

Environment
    └── contains actual secret
```

---

# 23. What happens if the local secret is leaked?

If someone gets your local secret, they may be able to:

```text
Decrypt tokens
Create fake tokens
Generate admin-like tokens
Impersonate users
```

You would need to:

```text
1. Generate a new secret.
2. Replace the old secret.
3. Invalidate existing tokens.
4. Investigate how the secret leaked.
```

This is why the secret must never be:

```text
Sent to frontend
Placed in GitHub
Written in logs
Returned through an API
Hard-coded in Java
```

The frontend should only receive the generated token, never the key.

---

# 24. What happens if a public key is leaked?

A public key is designed to be distributed.

If someone gets the public key, they can:

```text
Verify token signatures
```

But they cannot normally:

```text
Create valid tokens
```

The dangerous key in the `public` approach is the private key.

```text
Public key  → can be shared with verifiers
Private key → must remain secret
```

This is the biggest architectural benefit of `public`.

---

# 25. When would you move from `local` to `public`?

Suppose your modular monolith later becomes separate applications:

```text
Authentication service
User service
Device service
Order service
Payment service
```

You want every service to validate the token.

## With `local`

Every service would need the shared secret:

```text
Authentication service → secret
User service           → secret
Device service         → secret
Payment service        → secret
```

Now all those services could potentially create valid tokens.

A leaked secret from any service affects the complete authentication system.

## With `public`

Only the authentication service receives the private key:

```text
Authentication service → private key
User service           → public key
Device service         → public key
Payment service        → public key
```

Only authentication can issue tokens.

Other services can only verify them.

That is usually safer for distributed systems.

---

# 26. Does using `public` automatically make authentication more secure?

Not always.

`public` solves a particular problem:

```text
Many systems need to verify tokens,
but only one system should create them.
```

For a single backend:

```text
Authentication module creates token
Authentication module validates token
```

A local token is simpler and completely reasonable.

Security depends more on:

* proper key storage;
* token expiration;
* HTTPS;
* correct claim validation;
* secure token storage;
* protection against token theft;
* correct Spring Security configuration.

Using asymmetric cryptography does not automatically fix poor authentication design.

---

# 27. One simple decision rule

Use `local` when:

```text
The same trusted backend creates and validates tokens
and token contents should be encrypted.
```

Use `public` when:

```text
One system creates tokens,
but many separate systems need to verify them.
```

---

# 28. Your current decision

For EESOO, your current setup is:

```text
One Spring Boot modular monolith
One authentication module
Same application creates and validates tokens
```

Therefore:

```text
PASETO local is appropriate
```

Your authentication module should:

```text
Generate token using secret key
Validate token using the same secret key
Check expiration and required claims
Create Spring Security Authentication
Store it in SecurityContext
```

Other modules should:

```text
Use the authenticated user ID
Avoid parsing the token themselves
```

The simplest final memory rule is:

```text
LOCAL
One secret key
Encrypts and validates
Payload is hidden
Good for one trusted backend
```

```text
PUBLIC
Private key signs
Public key verifies
Payload is readable
Good when many systems must verify
```
