# AuthSession & Device Identity Design — Research Report

**Project:** EESOO Backend (Java / Spring Boot)
**Topic:** Whether `AuthSession` should carry `deviceInstallId`, and the resulting login/auth architecture
**Status:** Final decision made

---

## 1. Problem Statement

The system has two identity concerns that needed to be cleanly separated or deliberately linked:

- **Authentication** — who is the logged-in user (`AuthSession`, PASETO tokens).
- **Device identity** — what physical/app installation is making the request (`DeviceInstall`, `UserDeviceLink`).

The open question: **should `AuthSession` store `deviceInstallId`, binding a login session to the installation that created it — or should `AuthSession` stay user-only, with device linking treated as a fully separate, optional concern?**

---

## 2. Option A — AuthSession without `deviceInstallId` (initially explored)

```
AuthSession
├── sessionId
├── userId
├── refreshTokenHash
├── status
├── createdAt
├── expiresAt
└── revokedAt
```

**Behavior:**
- AuthSession answers only "which user owns this session?"
- Device info lives entirely in `DeviceInstall` (the installation) and `UserDeviceLink` (whether it's linked to the user).
- Login flow: verify credentials → attempt optional device linking → create AuthSession with `userId` only → generate PASETO with `sessionId` → return tokens.
- If device is missing/unknown/linking fails: **login still succeeds**, AuthSession is still created, just no `UserDeviceLink`.
- Sensitive operations check device separately at request time: `userId` (from token) + `installId` (from request) → look up `DeviceInstall` → check for an `ACTIVE UserDeviceLink` → allow/reject (e.g. `403 DEVICE_LINK_REQUIRED`).

**Consequence noted:** since the session isn't device-bound, a refresh token belongs to the user session, not a specific installation. Restricting refresh to the original device would require adding `deviceInstallId` later anyway.

This was treated as a reasonable, valid design and briefly locked in ("ok so for now i will not add deviceInstall to the AuthSession. that is decided").

---

## 3. Option B — AuthSession *with* `deviceInstallId` (final decision)

Reconsidered and ultimately chosen instead.

```
AuthSession
├── sessionId
├── userId
├── deviceInstallId     (required)
├── refreshTokenHash
├── status
├── createdAt
├── expiresAt
├── lastRotatedAt
└── revokedAt
```

**Key clarification:** adding `deviceInstallId` to `AuthSession` records *which installation created the session* — it does **not** imply that installation is linked/trusted for the user. That trust relationship still lives only in `UserDeviceLink`. So:

- `AuthSession` → which installation created this login session?
- `UserDeviceLink` → is that installation actively linked to this user?
- An `AuthSession` can exist with **no** active `UserDeviceLink`.

**Behavior differences vs. Option A:**

| Scenario | Without `deviceInstallId` | With `deviceInstallId` |
|---|---|---|
| `installId` unknown/not registered | Login still succeeds | Login **blocked** — cannot create AuthSession, must return e.g. `DEVICE_INSTALL_NOT_REGISTERED` |
| `deviceId` missing but `installId` known | Login succeeds, linking skipped | Login succeeds, linking skipped (AuthSession still gets `deviceInstallId`) |
| Refresh token scope | Bound only to user session | Can later be restricted to originating installation |
| Session/device auditing | Not possible directly | Straightforward |
| Device-specific logout | Not directly supported | Supported |

**Trade-off accepted:** login now *requires* a pre-registered `DeviceInstall` (via `installId`). This is acceptable because device installation is expected to happen before login (`POST /api/v1/devices/store`), so login should only ever need to *load* an existing `DeviceInstall`, never create one.

---

## 4. Supporting Investigation: Reinstall Handling

Before finalizing, confirmed that reinstall/device-update logic already existed and didn't need to be rebuilt:

- `StoreDeviceCommandHandler` (`.../device/handler/StoreDeviceCommandHandler.java`) and `DeviceInstall.java` already handle:
  - Reinstall with same `deviceId` + new `installId` → updates the existing row's `installId`, keeps the same internal `DeviceInstallId`.
  - Reinstall with missing `deviceId` → cannot correlate to old install, falls back to lookup/create by `installId`.
  - Progressive `deviceId` attachment when an existing `DeviceInstall` had `deviceId = null`.
- Confirmed this logic lives in `POST /api/v1/devices/store`, **not** in the login flow — reinforcing that login should only *read* device install data, not mutate it.

---

## 5. Final Data Model Relationships

```
DeviceInstall
├── id                  internal UUID
├── installId           required
├── deviceId            optional
├── platform
├── osVersion
└── deviceIdNullableReason

AuthSession
├── sessionId
├── userId
├── deviceInstallId     required
├── refreshTokenHash
├── status
├── createdAt
├── expiresAt
├── lastRotatedAt
└── revokedAt

UserDeviceLink
├── userId
├── deviceInstallId
├── linkedDeviceType
├── status
└── linkedAt
```

---

## 6. Final Login Flow

```
Verify phone number and PIN
    ↓
Check login permission (authUser.canLogin())
    ↓
Load existing DeviceInstall by installId
    (not found → DEVICE_INSTALL_NOT_REGISTERED, no session created)
    ↓
Attempt optional device linking (skipped if deviceId missing)
    ↓
Create AuthSession (userId + deviceInstallId)
    ↓
Generate PASETO tokens (sessionId only — no raw deviceId/installId in token)
    ↓
Hash + persist refresh token
    ↓
Return LoginUserResultDTO (tokens + deviceLinked status)
```

Linking failure (e.g. `DEVICE_LINKED_TO_ANOTHER_USER`, `USER_ALREADY_HAS_ACTIVE_DEVICE`) does **not** block login — only an unregistered `installId` does.

---

## 7. PASETO Token Design

- Local symmetric PASETO (`v2.local...` style), one version used consistently.
- Tokens carry `sessionId`, never raw `deviceId`, `installId`, PIN, password hash, or full entity payloads.
- **Access token claims:** `iss`, `sub` (userId), `aud` (eesoo-api), `iat`, `exp`, `jti`, `token_type=access`, `sid` (sessionId), `username`.
- **Refresh token claims:** same shape with `aud=eesoo-auth-refresh`, `token_type=refresh`.
- Anything device-related is resolved server-side via `sessionId → AuthSession → deviceInstallId → DeviceInstall`, never embedded in the token itself.

---

## 8. PASETO + Spring Security Integration

PASETO does not replace Spring Security — it plugs into the filter chain:

```
Request
    ↓
SecurityFilterChain
    ↓
PasetoAuthenticationFilter
    ↓
Extract Bearer token → validate/decrypt → read userId, username, sessionId
    ↓
Load active AuthSession
    ↓
Build AuthenticatedUserPrincipal → Spring Authentication → SecurityContext
    ↓
AuthorizationFilter → Controller
```

Security config target state: CSRF disabled, no form login/HTTP Basic, stateless sessions, `register`/`login`/`refresh` permitted, everything else `.anyRequest().authenticated()`, single `SecurityFilterChain` bean.

**Sensitive-operation authorization** (distinct from authentication) additionally checks: `AuthSession → deviceInstallId → DeviceInstall → ACTIVE UserDeviceLink`, returning `403 DEVICE_LINK_REQUIRED` if not linked. Clarified distinction: `401` = token missing/invalid; `403` = token valid but device/user permission insufficient.

---

## 9. Layered File Plan (Final)

**Domain layer**
- New: `AuthSession.java`, `AuthSessionStatus.java`, `AuthSessionId.java`
- New ports: `AuthSessionRepositoryPort`, `LoadDeviceInstallPort`, `TokenValidationPort`, `RefreshTokenHasherPort`
- Modify: `TokenGenerationPort` (now takes `AuthUser` + `sessionId`), `LoginDeviceLinkPort`
- `AuthSession` should hold a raw UUID reference to the device install, not import the full `DeviceInstall` entity (keep auth domain decoupled from user module internals).

**Application layer**
- Modify: `LoginUserDTO`, `LoginUserCommand`, `LoginUserMapper`, `LoginCommandHandler`, `LoginUserService`, `LoginUserResultDTO`
- Later: refresh (`RefreshTokenCommand`/`Handler`/`ResultDTO`) and logout (`LogoutCommand`/`Handler`)

**User-module adapters**
- Existing: `LoadAuthUserAdapter`, `PinVerificationAdapter`, `LoginDeviceLinkAdapter`
- New: `LoadDeviceInstallAdapter` (read-only lookup by `installId`, never creates)
- Modify: `DeviceLinkService` to accept the already-resolved internal `DeviceInstallId`

**AuthSession persistence**
- New: `AuthSessionJpaEntity`, `AuthSessionMapper`, `SpringDataAuthSessionRepository`, `JpaAuthSessionRepository`

**PASETO token layer**
- New: `PasetoKeyProvider`, `PasetoTokenGenerationAdapter`, `PasetoTokenValidationAdapter`

**Presentation layer**
- New: `LoginUserController`, `LoginUserRequestDTO`, `LoginUserResponseDTO` — `POST /api/v1/auth/login`
- Controller stays thin: receive → validate → convert → delegate → respond. No PIN checks, session creation, or token generation here.

**Security layer**
- New: `AuthenticatedUserPrincipal` (userId, username, sessionId only — not full profile), `PasetoAuthenticationFilter`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`
- Modify: `SecurityConfig`

---

## 10. Recommended Build Order (Checkpoints)

1. `AuthSession` domain model + tests (requires `userId` + `deviceInstallId`; new session starts `ACTIVE`; revoked/expired ≠ active)
2. `AuthSessionRepositoryPort` (domain contract only, no DB adapter yet)
3. `LoadDeviceInstallPort` + `LoadDeviceInstallAdapter` (read-only lookup by `installId`)
4. Update optional login-link flow to use resolved internal `DeviceInstallId`
5. Token contracts: `AccessTokenClaims`, `RefreshTokenClaims`, `TokenValidationPort`, `RefreshTokenHasherPort`; modify `TokenGenerationPort`
6. `PasetoKeyProvider` + `PasetoTokenGenerationAdapter` (with tests)
7. Wire up `LoginCommandHandler`, `LoginUserService`, `LoginUserResultDTO`, `LoginUserMapper`
8. `LoginUserController` + request/response DTOs; register missing Spring beans
9. `PasetoTokenValidationAdapter` (with tests)
10. `AuthenticatedUserPrincipal` + `PasetoAuthenticationFilter` (with tests)
11. Update `SecurityConfig` from `permitAll()` to real protected-endpoint rules
12. Refresh-token rotation, session revocation, logout
13. Device-sensitive authorization service (`AuthSession → DeviceInstall → active UserDeviceLink` check)

**First file to create:** `auth/domain/model/entity/AuthSession.java`, with `deviceInstallId` as a required field.

---

## 11. Final Conclusion

- `AuthSession` **will** store `deviceInstallId` (required), binding every login session to the installation that created it.
- `UserDeviceLink` remains the sole source of truth for whether an installation is *trusted/linked* to a user — this is unaffected by the `AuthSession` change.
- `PASETO` tokens stay minimal: only `sessionId` (plus standard claims) — no device identifiers or PII.
- Login now **requires** a previously-registered `DeviceInstall` (via `installId`); unregistered installs must complete `POST /api/v1/devices/store` before login can succeed.
- Device installation/reinstall logic already exists and is intentionally kept out of the login flow — login only *reads* device install state.
- Full layer-by-layer file plan and a 13-step checkpoint build order were produced to implement this end-to-end, from domain model through Spring Security integration.
