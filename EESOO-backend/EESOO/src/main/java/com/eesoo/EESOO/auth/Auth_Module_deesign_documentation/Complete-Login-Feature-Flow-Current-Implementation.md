# EESOO Authentication and Login Feature

## Current Implementation Guide

This document describes the authentication feature as it is currently implemented in the EESOO backend.

It covers:

- Device installation requirements
- User login
- PIN verification
- Optional device linking and blocking device conditions
- Authentication session creation
- PASETO access and refresh tokens
- Refresh-token hashing and rotation
- Refresh-token replay detection
- Spring Security authentication
- Logout and session revocation
- Transaction boundaries
- HTTP requests, responses, and errors

This document should be treated as the current implementation reference when older design documents disagree with the code.

---

# 1. Authentication Goals

The authentication feature must answer four separate questions:

1. Is the supplied phone number and PIN valid?
2. Is the application installation registered?
3. Is this device allowed to continue the login?
4. Does the request belong to an active authentication session?

These questions are intentionally handled by separate components.

```text
Credentials
    -> identify the user

DeviceInstall
    -> identify the application installation

UserDeviceLink
    -> describe whether the installation is linked to the user

AuthSession
    -> represent the server-side login session

PASETO
    -> carry encrypted proof of the authenticated session
```

PASETO does not replace `AuthSession`. The token identifies a session, while the database remains the source of truth for whether that session is active.

---

# 2. Module Boundaries

## Auth module responsibilities

The auth module owns:

- Login orchestration
- Authentication-session lifecycle
- PASETO generation and validation
- Refresh-token hashing
- Refresh-token rotation
- Replay detection
- Logout and revocation
- Spring Security authentication

## User module responsibilities

The user module owns:

- User persistence
- PIN hashes
- `DeviceInstall`
- `UserDeviceLink`
- Device-link business rules

## Cross-module communication

The auth module does not import user-domain entities directly.

It communicates through auth-owned ports:

```text
LoadAuthUserPort
PinVerificationPort
LoadDeviceInstallPort
LoginDeviceLinkPort
```

The user module provides infrastructure adapters for these ports:

```text
LoadAuthUserAdapter
PinVerificationAdapter
LoadDeviceInstallAdapter
LoginDeviceLinkAdapter
```

The boundary uses simple values and auth-owned models such as:

```text
UUID userId
UUID deviceInstallId
AuthUserSnapshot
LoginDeviceLinkStatus
```

---

# 3. Main Domain Objects

## 3.1 AuthUser

`AuthUser` is the auth module's view of a user.

It contains:

```text
userId
username
loginPermission
```

It does not contain:

- The PIN hash
- The complete user profile
- User-module persistence objects

Its important behavior is:

```java
authUser.canLogin()
```

## 3.2 AuthSession

`AuthSession` is the server-side record of one login session.

```text
AuthSession
├── id
├── userId
├── deviceInstallId
├── refreshTokenHash
├── status
├── createdAt
├── expiresAt
├── lastRotatedAt
├── revokedAt
└── version
```

### Session statuses

```text
ACTIVE
    The session may be used if it has not passed expiresAt.

REVOKED
    Logout or a security event disabled the session.

EXPIRED
    The fixed session lifetime has ended.
```

The important runtime check is:

```java
authSession.isUsable(currentTime)
```

A session is usable only when:

```text
status == ACTIVE
and
currentTime < expiresAt
```

## 3.3 AuthSessionId

`AuthSessionId` is a value object around a UUID.

New persistent session IDs use:

```java
UuidCreator.getTimeOrderedEpoch()
```

The session ID appears in PASETO as the custom claim:

```text
sid
```

## 3.4 RefreshTokenHash

The database never stores the raw refresh token.

It stores:

```text
SHA-256(raw refresh token)
```

The domain wraps that encoded value in:

```java
RefreshTokenHash
```

---

# 4. Configuration

## 4.1 Session lifetime

```properties
auth.session.duration=30d
```

This value is bound to `AuthSessionProperties`.

`AuthSessionConfiguration` converts it into:

```java
AuthSessionLifetime
```

The application handler uses the domain object and does not import PASETO infrastructure properties.

## 4.2 PASETO settings

```properties
auth.paseto.local-key-base64=${AUTH_PASETO_LOCAL_KEY_BASE64}
auth.paseto.issuer=eesoo
auth.paseto.access-audience=eesoo-api
auth.paseto.refresh-audience=eesoo-auth-refresh
auth.paseto.access-token-duration=15m
```

The secret must be a Base64 encoding of exactly 32 random bytes.

It must be supplied through the runtime environment and must not be committed to Git.

## 4.3 Fixed expiration rule

At initial login:

```java
createdAt = clock.instant();
sessionExpiresAt =
        authSessionLifetime.calculateExpiresAt(createdAt);
```

Both the session and refresh token use `sessionExpiresAt`.

During rotation:

```java
newRefreshTokenExpiresAt =
        authSession.getExpiresAt();
```

Rotation never calculates another 30 days.

Therefore, refresh rotation does not create sliding expiration.

---

# 5. PASETO Token Design

The implementation uses:

```text
PASETO v2.local
```

`local` tokens are symmetrically encrypted and authenticated with the same 32-byte secret key.

## 5.1 Access-token claims

```text
iss         issuer
sub         userId
aud         eesoo-api
iat         issued time
exp         access-token expiry
jti         unique token ID
sid         AuthSessionId
token_type  access
```

## 5.2 Refresh-token claims

```text
iss         issuer
sub         userId
aud         eesoo-auth-refresh
iat         issued time
exp         fixed AuthSession expiry
jti         unique token ID
sid         AuthSessionId
token_type  refresh
```

Tokens intentionally do not contain:

- Username
- PIN
- PIN hash
- Device ID
- Installation ID
- Device-install database ID
- Full user or session entities

Username can change, so it is not treated as immutable authentication identity.

## 5.3 Access-token expiration

The normal access-token expiration is:

```text
issuedAt + configured access duration
```

However, an access token must never outlive its session.

The generator therefore uses:

```text
minimum(
    issuedAt + accessTokenDuration,
    AuthSession.expiresAt
)
```

---

# 6. Device Rules During Login

The user module returns an exact device-link outcome. The auth adapter maps that result to `LoginDeviceLinkStatus`.

```text
LINKED
DEVICE_ID_MISSING
STORED_DEVICE_ID_MISSING
DEVICE_ID_MISMATCH
DEVICE_NOT_REGISTERED
DEVICE_LINKED_TO_ANOTHER_USER
USER_ALREADY_HAS_ACTIVE_DEVICE
LINKING_FAILED
```

The current login policy is:

```text
LINKED
    -> login allowed

DEVICE_ID_MISSING
    -> login allowed without creating a device link

Every other status
    -> login blocked
```

This policy is expressed by:

```java
deviceLinkStatus.allowsLogin()
```

Known outcomes are returned normally. Unexpected technical exceptions are rethrown so the transaction rolls back.

---

# 7. Initial Login HTTP Contract

## Endpoint

```text
POST /api/v1/auth/login
```

This is a public endpoint.

## Request

```json
{
  "phoneNumber": "9876543210",
  "pin": "1234",
  "deviceId": "physical-device-id",
  "installId": "application-install-id"
}
```

## Request validation

```text
phoneNumber
    required
    valid 10-digit Indian mobile number
    begins with 6, 7, 8, or 9

pin
    required
    exactly 4 numeric digits

installId
    required

deviceId
    optional
```

## Successful response

```json
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "userId": "user-uuid",
    "username": "display-name",
    "accessToken": "v2.local...",
    "refreshToken": "v2.local...",
    "deviceLinked": true,
    "deviceLinkFailureReason": null
  }
}
```

If login is allowed without linking:

```json
{
  "deviceLinked": false,
  "deviceLinkFailureReason": "DEVICE_ID_MISSING"
}
```

---

# 8. Initial Login Flow

## Stage 1: Presentation

`LoginUserController` receives `LoginUserRequestDTO`.

It:

1. Deserializes JSON.
2. Runs bean validation.
3. Creates `LoginUserDTO`.
4. Calls `LoginUserService`.

The controller contains no PIN, device, session, or token business logic.

## Stage 2: Application mapping

`LoginUserService` maps:

```text
LoginUserDTO
    ->
LoginUserCommand
```

It delegates to `LoginCommandHandler`.

## Stage 3: Start transaction

`LoginCommandHandler.handle()` is annotated:

```java
@Transactional
```

This is the outer login transaction.

The default propagation is `REQUIRED`, so transactional device-link and session services join the same transaction.

## Stage 4: Load user

The handler calls:

```java
loadAuthUserPort.findByPhoneNumber(phoneNumber)
```

If no user exists:

```text
InvalidCredentialsException
    -> 401 Unauthorized
```

The same generic response is used for incorrect PIN and denied login permission. This avoids revealing whether a phone number is registered.

## Stage 5: Check login permission

The snapshot is converted into `AuthUser`.

The handler calls:

```java
authUser.canLogin()
```

If false, login stops before device linking and session creation.

## Stage 6: Verify PIN

The handler calls:

```java
pinVerificationPort.verifyPin(userId, rawPin)
```

The user-module adapter retrieves the stored PIN hash and verifies it.

The auth module never receives the stored PIN hash.

## Stage 7: Resolve installation

The handler calls:

```java
loadDeviceInstallPort
        .findDeviceInstallIdByInstallId(installId)
```

Login only reads existing installation state. It does not create a `DeviceInstall`.

If the installation does not exist:

```text
DeviceInstallationNotRegisteredException
    -> 409 Conflict
    -> DEVICE_NOT_REGISTERED
```

The client must complete device storage before login.

## Stage 8: Attempt device link

The handler calls:

```java
loginDeviceLinkPort.attemptLink(
    userId,
    deviceInstallId,
    deviceId
)
```

The user module checks:

- Whether `deviceId` was supplied
- Whether the installation still exists
- Whether a stored device ID exists
- Whether supplied and stored IDs match
- Whether the installation belongs to another user
- Whether the user already has another active device
- Whether an existing correct link can be reused
- Whether a new link should be created

The exact result is mapped to `LoginDeviceLinkStatus`.

## Stage 9: Enforce device policy

```java
if (!deviceLinkStatus.allowsLogin()) {
    throw new DeviceLoginRejectedException(
        deviceLinkStatus
    );
}
```

Blocking outcomes stop the flow before token and session creation.

## Stage 10: Create session

`AuthSessionCreationService`:

1. Gets the current UTC `Instant`.
2. Calculates the fixed session boundary.
3. Creates a time-ordered `AuthSessionId`.
4. Generates access and refresh tokens.
5. Hashes the raw refresh token.
6. Creates an active `AuthSession`.
7. Persists the session.
8. Returns the raw token pair.

Only the raw tokens returned to the client contain usable token values.

The database receives only the refresh-token hash.

## Stage 11: Commit

If every required operation succeeds:

```text
UserDeviceLink changes
and
AuthSession creation
    -> commit together
```

If an unexpected exception occurs:

```text
all login transaction writes
    -> roll back
```

## Stage 12: Return response

The handler creates `LoginUserResultDTO`.

The controller converts it into `LoginUserResponseDTO` and returns HTTP 200.

Token values are protected in DTO `toString()` methods to reduce accidental logging.

---

# 9. Login Transaction Outcomes

```text
Invalid credentials
    -> no device write
    -> no AuthSession
    -> no tokens returned

Installation missing
    -> no device write
    -> no AuthSession
    -> no tokens returned

DEVICE_ID_MISSING
    -> no UserDeviceLink created
    -> AuthSession created
    -> login succeeds

Blocking device status
    -> DeviceLoginRejectedException
    -> transaction rolls back
    -> no AuthSession

Device link saved, session creation fails
    -> entire transaction rolls back
    -> no link and no session

Everything succeeds
    -> link and session commit
    -> tokens returned
```

---

# 10. Authentication of Protected Requests

Clients send:

```http
Authorization: Bearer <access-token>
```

## Security-filter flow

```text
HTTP Request
    ->
SecurityFilterChain
    ->
PasetoAuthenticationFilter
    ->
TokenValidationPort.validateAccessToken()
    ->
Load AuthSession by sid
    ->
Verify session userId matches token subject
    ->
Verify session is ACTIVE and unexpired
    ->
Build AuthenticatedUserPrincipal
    ->
Create Spring Authentication
    ->
Store in SecurityContext
    ->
AuthorizationFilter
    ->
Controller
```

## AuthenticatedUserPrincipal

The principal contains:

```text
userId
sessionId
```

It does not contain:

- Username
- Raw access token
- Raw refresh token
- PASETO key
- Device ID
- Full domain entities

Other modules should use the authenticated user ID and should not parse PASETO themselves.

## Invalid access token

Missing authentication on a protected endpoint or an invalid token results in:

```text
401 Unauthorized
```

`PasetoAuthenticationEntryPoint` writes the JSON response because filter failures happen before controller exception advice.

## Valid authentication without permission

When the user is authenticated but lacks authorization:

```text
403 Forbidden
```

`RestAccessDeniedHandler` writes the JSON response.

---

# 11. Security Configuration

The application uses one `SecurityFilterChain`.

It is configured as:

```text
CSRF disabled
CORS enabled
Form login disabled
HTTP Basic disabled
Default Spring logout disabled
Stateless session management
PASETO filter before UsernamePasswordAuthenticationFilter
```

## Public POST endpoints

```text
/api/v1/auth/login
/api/v1/auth/refresh
/api/v1/users/register
/api/v1/devices/store
/api/v1/devices/check
```

Preflight requests are public:

```text
OPTIONS /**
```

Every other request requires authentication:

```java
.anyRequest().authenticated()
```

Logout is intentionally protected.

---

# 12. Refresh Token HTTP Contract

## Endpoint

```text
POST /api/v1/auth/refresh
```

This endpoint is public because the refresh token itself is the credential.

## Request

```json
{
  "refreshToken": "v2.local..."
}
```

## Response

```json
{
  "status": "success",
  "message": "Tokens refreshed successfully",
  "data": {
    "accessToken": "v2.local...",
    "refreshToken": "v2.local..."
  }
}
```

The client must replace both old tokens with the returned pair.

---

# 13. Refresh Rotation Flow

`RefreshTokenController` maps the request through:

```text
RefreshTokenRequestDTO
    ->
RefreshTokenDTO
    ->
RefreshTokenCommand
    ->
RefreshTokenCommandHandler
```

## Step 1: Validate PASETO

```java
tokenValidationPort.validateRefreshToken(rawToken)
```

Validation confirms:

- PASETO decryption/authentication
- Issuer
- Refresh audience
- Expiration
- `token_type=refresh`
- Valid user ID
- Valid session ID
- Valid token ID

## Step 2: Lock session row

The handler calls:

```java
findForRefreshRotation(sessionId)
```

The JPA query uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This behaves like:

```sql
SELECT ...
FOR UPDATE
```

Only the selected session row is locked until commit or rollback.

## Step 3: Validate session

The handler verifies:

```text
session exists
token userId matches session userId
session is ACTIVE
session has not expired
raw refresh token matches stored hash
```

## Step 4: Generate new tokens

The fixed boundary is reused:

```java
tokenGenerationPort.generateTokens(
    authSession.getUserId(),
    authSession.getId(),
    authSession.getExpiresAt()
)
```

## Step 5: Rotate stored hash

```text
old stored hash
    -> replaced with hash(new refresh token)

lastRotatedAt
    -> current time
```

The old refresh token becomes unusable after commit.

---

# 14. Concurrent Refresh Protection

Suppose two requests send the same refresh token at nearly the same time.

```text
Request A locks session
Request B waits

Request A validates old hash
Request A rotates hash
Request A commits

Request B obtains lock
Request B reads new hash
Request B's old token no longer matches
```

The database lock prevents both requests from successfully rotating the same token.

---

# 15. Refresh-Token Replay Detection

Replay means a cryptographically valid refresh token is used after it was already rotated.

Example:

```text
R1 stored
    ->
R1 used successfully
    ->
R2 stored
    ->
someone sends R1 again
```

Because the token is valid for the correct session but its hash is no longer current, the system treats this as possible token theft.

## Replay response

```text
Detect hash mismatch
    ->
mark AuthSession REVOKED
    ->
save revocation
    ->
commit revocation
    ->
return 401
```

Response:

```json
{
  "status": "error",
  "message": "Refresh token reuse detected; authentication session has been revoked",
  "data": {
    "code": "REFRESH_TOKEN_REUSE_DETECTED"
  }
}
```

## Special transaction rule

The refresh handler uses:

```java
@Transactional(
    noRollbackFor =
        RefreshTokenReplayDetectedException.class
)
```

Normally, a runtime exception rolls back database changes.

For replay detection, the session revocation must remain persisted even though the request returns an error.

```text
Replay exception
    -> commit REVOKED status
    -> return 401

Other unexpected runtime exception
    -> rollback normally
```

After replay detection, all access and refresh tokens belonging to that session are rejected.

---

# 16. Logout HTTP Contract

## Endpoint

```text
POST /api/v1/auth/logout
Authorization: Bearer <access-token>
```

Logout is protected and requires no request body.

The filter has already produced:

```text
AuthenticatedUserPrincipal
├── userId
└── sessionId
```

## Response

```json
{
  "status": "success",
  "message": "Logout successful",
  "data": null
}
```

---

# 17. Logout Flow

```text
Access token validated
    ->
Active AuthSession validated
    ->
LogoutController receives principal
    ->
LogoutService creates LogoutCommand
    ->
LogoutCommandHandler locks session
    ->
Verify principal userId matches session userId
    ->
AuthSession.revoke(now)
    ->
Save session
    ->
Commit
```

Revocation changes:

```text
status
    ACTIVE -> REVOKED

revokedAt
    null -> current time
```

After logout:

```text
Old access token
    -> rejected because session is REVOKED

Old refresh token
    -> rejected because session is REVOKED
```

---

# 18. Refresh and Logout Race Handling

Refresh and logout both acquire a write lock on the same session row.

## Logout locks first

```text
Logout locks session
Refresh waits
Logout marks REVOKED and commits
Refresh reads REVOKED session
Refresh fails
```

## Refresh locks first

```text
Refresh locks session
Logout waits
Refresh rotates and commits
Logout marks session REVOKED and commits
```

Even if refresh briefly returns new tokens, those tokens become unusable as soon as logout commits.

---

# 19. Persistence

## AuthSessionJpaEntity

The table is:

```text
auth_sessions
```

Important columns include:

```text
id
user_id
device_install_id
refresh_token_hash
status
created_at
expires_at
last_rotated_at
revoked_at
version
```

## Optimistic version

The JPA entity uses:

```java
@Version
```

Hibernate controls the version.

The domain preserves the version value but does not increment it manually.

## Mapping

`AuthSessionMapper` maps:

```text
AuthSession
    <->
AuthSessionJpaEntity
```

The persistence model stores UUID references instead of cross-module JPA entity relationships.

---

# 20. Exception and HTTP Mapping

| Exception | HTTP | Meaning |
|---|---:|---|
| `InvalidCredentialsException` | 401 | Phone, PIN, or login permission failed |
| `DeviceInstallationNotRegisteredException` | 409 | Installation must be registered first |
| `DeviceLoginRejectedException` | 403 | Device policy blocked login |
| `InvalidTokenException` | 401 | Token is malformed, invalid, expired, or wrongly scoped |
| `AuthSessionNotUsableException` | 401 | Session is missing, revoked, or expired |
| `RefreshTokenReplayDetectedException` | 401 | Old refresh token was reused and session was revoked |

Validation failures return:

```text
400 Bad Request
```

Missing authentication on protected routes returns:

```text
401 Unauthorized
```

Valid authentication without required authorization returns:

```text
403 Forbidden
```

---

# 21. Security Properties

The implementation guarantees:

- PIN hashes do not enter the auth module.
- Raw refresh tokens are not persisted.
- Tokens do not contain device identifiers.
- Tokens do not contain mutable username information.
- Access tokens cannot outlive their session.
- Refresh rotation does not extend the session.
- Refresh tokens are one-time rotation credentials.
- Replayed refresh tokens revoke the session.
- Logout immediately invalidates all session tokens.
- Other modules do not parse PASETO.
- PASETO keys stay inside auth infrastructure.
- The server checks database session state on protected requests.

---

# 22. Client Responsibilities

The client must:

1. Register/store the installation before login.
2. Send `phoneNumber`, `pin`, `installId`, and optional `deviceId`.
3. Store returned tokens in secure platform storage.
4. Send the access token using the Bearer header.
5. Replace both tokens after every successful refresh.
6. Never reuse an old refresh token.
7. Clear local tokens after logout.
8. Return to login when receiving:
   - `401`
   - `REFRESH_TOKEN_REUSE_DETECTED`
   - an unusable-session response

The client must not log access or refresh token values.

---

# 23. End-to-End Verification Sequence

The intended runtime verification is:

```text
1. POST /api/v1/devices/store
2. POST /api/v1/users/register
3. POST /api/v1/auth/login
4. Call a protected endpoint with the access token
5. POST /api/v1/auth/refresh using R1
6. Call a protected endpoint using the new access token
7. Attempt refresh again using old R1
8. Confirm REFRESH_TOKEN_REUSE_DETECTED
9. Confirm the session is REVOKED
10. Log in again to create a new session
11. POST /api/v1/auth/logout using the new access token
12. Confirm both access and refresh tokens are rejected
```

Additional cases:

```text
Incorrect PIN
Unknown phone number
Missing installation
Missing device ID
Mismatched device ID
Device linked to another user
Expired access token
Expired refresh token
Malformed PASETO
Concurrent refresh requests
Concurrent refresh and logout
```

---

# 24. Current Verification Status

The current main source set compiles successfully.

The complete runtime flow still needs verification against the configured PostgreSQL database.

Older tests that use previous `LoginCommandHandler` constructor and return signatures must be updated before treating the full test suite as authoritative.

Important remaining work includes:

- Focused login tests
- Refresh rotation and replay tests
- Security-filter tests
- Logout tests
- End-to-end PostgreSQL verification
- Expired-session status cleanup
- Device-sensitive authorization for selected operations
- Rate limiting and PIN brute-force protection
- Database migrations
- PASETO key-rotation strategy
- Deferred module-boundary cleanup

---

# 25. Complete Flow Summary

```text
INITIAL LOGIN

HTTP request
    ->
request validation
    ->
load auth user
    ->
check login permission
    ->
verify PIN
    ->
resolve registered installation
    ->
attempt device link
    ->
enforce device policy
    ->
create fixed-lifetime AuthSession
    ->
generate PASETO pair
    ->
hash refresh token
    ->
persist session
    ->
commit
    ->
return tokens
```

```text
PROTECTED REQUEST

Bearer access token
    ->
PASETO validation
    ->
load AuthSession
    ->
check active and unexpired
    ->
create Spring Authentication
    ->
SecurityContext
    ->
controller
```

```text
REFRESH

raw refresh token
    ->
PASETO validation
    ->
lock AuthSession
    ->
check session and stored hash
    ->
reuse fixed expiresAt
    ->
generate new token pair
    ->
store new refresh-token hash
    ->
commit
```

```text
REPLAY

old valid refresh token
    ->
stored hash mismatch
    ->
revoke session
    ->
commit revocation
    ->
return 401
```

```text
LOGOUT

authenticated principal
    ->
lock AuthSession
    ->
mark REVOKED
    ->
commit
    ->
all session tokens become unusable
```

