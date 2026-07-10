# EESOO Auth Module — Login & Device Linking Design

This document consolidates the design decisions for the login flow, device linking, and second-device payment handling in the EESOO backend.

---

## 1. Core Security Rule

> **A device link must never be created from only a phone number.** The requester must first prove account ownership with the correct PIN.

```text
No new link before valid credentials
No token before valid device/link
```

Reading device state early is fine internally, but:

| Action | Allowed before PIN verification? |
|---|---|
| Reading device state internally | Yes |
| Exposing device ownership info | No |
| Creating/changing a link | Never |

This prevents an attacker from linking a victim's phone number to their own device using a guessed or wrong PIN.

---

## 2. Login Request Contract

### Minimum fields the frontend must send

```json
{
  "phoneNumber": "+919876543210",
  "pin": "1234",
  "deviceId": "device-abc-123",
  "installId": "install-xyz-456"
}
```

| Field | Purpose |
|---|---|
| `phoneNumber` | Finds the user trying to log in |
| `pin` | Proves the request comes from that user |
| `deviceId` | Identifies the physical device |
| `installId` | Identifies this installation of the app |

### Extended fields (only if login must also store/update device metadata)

```json
{
  "phoneNumber": "+919876543210",
  "pin": "1234",
  "deviceId": "device-abc-123",
  "installId": "install-xyz-456",
  "platform": "ANDROID",
  "osVersion": "15",
  "deviceIdNullableReason": null
}
```

**Recommendation:** keep device storage as a separate frontend startup step (see Section 8), and let login handle only link verification/recovery.

---

## 3. `LoginUserCommand` (Application Layer Model)

Current version only has `phoneNumber` + `pin`. It should be extended to:

```java
public final class LoginUserCommand {

    private final String phoneNumber;
    private final String pin;
    private final String deviceId;
    private final String installId;

    public LoginUserCommand(
            String phoneNumber,
            String pin,
            String deviceId,
            String installId
    ) {
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.deviceId = deviceId;
        this.installId = installId;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getPin() { return pin; }
    public String getDeviceId() { return deviceId; }
    public String getInstallId() { return installId; }
}
```

This aligns with the existing `TokenGenerationPort.generateTokens(userId, deviceId, installId)`.

---

## 4. High-Level Login Sequence

```text
1. Receive phoneNumber, pin, deviceId, installId
2. Find the user by phone number
3. Check whether the account is allowed to log in
4. Verify the PIN
5. Check device and user-device link
6. Recover/create the link if safely allowed (or treat as optional — see Section 6)
7. Generate device-bound tokens
8. Return the login result
```

---

## 5. Device Link Scenarios (Case-by-Case)

### Case A — Device already linked during registration
```text
User A ← linked to → Device 1
Login request: User A's phone + Device 1
```
→ Verify PIN → confirm link belongs to User A → generate tokens.
**Login succeeds.** PIN is still checked even though the link already exists — linking never replaces authentication.

### Case B — Device exists but link is missing (recovery case)
```text
DeviceInstall exists, User exists, no UserDeviceLink
```
→ Verify PIN → confirm device isn't linked to someone else → confirm user has no conflicting active device → **create the missing link** → generate tokens.
**Login succeeds, link repaired.**

### Case C — Device linked to another user
```text
User B ← linked to → Device 1
Login request: User A's phone + Device 1
```
→ Reject with `DEVICE_LINKED_TO_ANOTHER_USER`. Never auto-replace an existing link.
**Login rejected** — requires a separate secure device-transfer/unlink process.

### Case D — User already has another active device
```text
User A ← linked to → Device 1
Login attempt from Device 2
```
→ `DeviceLinkService` returns `USER_ALREADY_HAS_ACTIVE_DEVICE`.
**Login rejected or routed to device-change/payment flow** (see Section 7).

### Case E — Device was never stored
```text
User exists, credentials valid, no DeviceInstall for deviceId
```
→ `DeviceLinkService` returns `DEVICE_NOT_REGISTERED`.

Two design options:
- **Recommended:** frontend stores the device *before* login (Section 8) — login only recovers the link.
- **Alternative:** login stores the device itself, requiring full device metadata — more responsibility packed into `LoginService`.

---

## 6. Device Linking as a Best-Effort Operation

> Device linking is best-effort during login. Successful credential verification should generally allow login even when linking fails — **except for ownership conflicts**.

```text
Authentication result → determines whether login succeeds
Device-link result    → reports whether linking succeeded
```

### Outcome table

| Credential result | Device-link result | Login result |
|---|---|---|
| Invalid PIN | Not attempted | Login fails |
| Valid PIN | Already linked | Login succeeds |
| Valid PIN | Newly linked | Login succeeds |
| Valid PIN | Device not registered | Login succeeds, unlinked |
| Valid PIN | Device ID missing | Login succeeds, unlinked |
| Valid PIN | Linking technical error | Login succeeds, unlinked |
| Valid PIN | Device linked to another user | **Login rejected / conflict flow** |
| Valid PIN | User already has active device | **Payment or rejection flow** |

### Ordinary (recoverable) failures — safe to allow login
```text
DEVICE_ID_MISSING
DEVICE_NOT_REGISTERED
DEVICE_NOT_LINKED
DATABASE/TEMPORARY_LINK_FAILURE
```

### Security/ownership conflicts — do NOT allow silent login
```text
DEVICE_LINKED_TO_ANOTHER_USER
USER_ALREADY_HAS_ACTIVE_DEVICE
```

### `LoginUserDTO` should report both results

```java
public final class LoginUserDTO {
    private final UUID userId;
    private final String username;
    private final String accessToken;
    private final String refreshToken;
    private final boolean deviceLinked;
    private final String deviceLinkFailureReason;
}
```

Example — login succeeds, linking failed:
```json
{
  "userId": "user-123",
  "username": "Nehan",
  "accessToken": "...",
  "refreshToken": "...",
  "deviceLinked": false,
  "deviceLinkFailureReason": "DEVICE_NOT_REGISTERED"
}
```

Frontend behavior: save tokens, treat user as logged in, retry linking later, show a non-blocking notice.

### Transaction boundaries matter

Login and device linking must **not** share one all-or-nothing transaction:

- If linking throws inside the login transaction, an otherwise successful login would incorrectly roll back.
- `DeviceLinkService` already has its own `@Transactional` boundary — reuse it and just catch failures in `LoginService`.

### Token semantics when unlinked

`generateTokens(userId, deviceId, installId)` can still succeed without a DB link, but keep these concepts distinct:

```text
Device information present → token knows which device requested login
Device linked              → database confirms user-device relationship
Device trusted             → stronger security policy accepted the device
```

---

## 7. Second-Device Payment Flow

> One device is included free. A second device must be **paid for** before it can be linked and used.

The two conflict types must be handled differently — they are not the same thing.

### 7.1 `USER_ALREADY_HAS_ACTIVE_DEVICE` → genuine payment case

```text
User A already linked to Device 1
User A logs in with valid credentials on Device 2
```
→ Do **not** link Device 2 yet. Return `SECOND_DEVICE_PAYMENT_REQUIRED`.

### 7.2 `DEVICE_LINKED_TO_ANOTHER_USER` → ownership conflict, NOT a payment case

```text
Device 2 already linked to User B
User A enters valid credentials on Device 2
```
→ Payment must never let User A simply take over another user's device. Return `DEVICE_OWNERSHIP_CONFLICT` and require a recovery/transfer process:

```text
1. Verify User A's credentials
2. Detect Device 2 belongs to User B
3. Do not expose User B's info
4. Do not create a second link
5. Require existing link removal/transfer
6. Re-check second-device payment need afterward
```

### 7.3 Decision table

| Situation | Backend result | Access result |
|---|---|---|
| Same device, same user | `DEVICE_ALREADY_LINKED` | Normal login |
| No existing device, current device free | `DEVICE_LINKED` | Link + normal login |
| User already has another active device | `SECOND_DEVICE_PAYMENT_REQUIRED` | Payment flow |
| Device belongs to another user | `DEVICE_OWNERSHIP_CONFLICT` | Ownership recovery |
| Technical linking failure | `DEVICE_LINK_FAILED` | Login succeeds unlinked (optional-link rule) |

### 7.4 Second-device flow diagram

```text
User enters phone + PIN on Device 2
        ↓
Backend verifies credentials
        ↓
Device 2 not linked to another user
        ↓
User A already has Device 1
        ↓
Return SECOND_DEVICE_PAYMENT_REQUIRED
        ↓
Frontend shows payment/QR scanner
        ↓
Payment confirmed by backend (server-to-server / webhook)
        ↓
Backend records second-device entitlement
        ↓
Backend links Device 2 to User A
        ↓
Backend grants normal authenticated access
```

### 7.5 Restricted payment-session token

After valid credentials but before payment, issue a **short-lived, restricted token** — not a normal access token:

```json
{
  "loginStatus": "SECOND_DEVICE_PAYMENT_REQUIRED",
  "paymentRequired": true,
  "deviceLinked": false,
  "paymentSessionToken": "short-lived-restricted-token"
}
```

The restricted token should only allow:
- Starting second-device payment
- Checking payment status
- Displaying/scanning the payment QR
- Cancelling the flow
- Completing device activation

It must **not** grant access to normal protected features, and the frontend's claim of `"paymentSuccessful": true` must never be trusted — the backend confirms via the payment provider's verified response/webhook.

### 7.6 Repository implication

Current:
```java
Optional<UserDeviceLink> findActiveByUserId(UserId userId);
```
Once multiple paid devices are supported, this needs to become:
```java
List<UserDeviceLink> findAllActiveByUserId(UserId userId);
```
And the real question shifts from *"does the user already have a device?"* to:
```text
Number of active linked devices  vs.  Number of purchased device slots
```

---

## 8. Frontend Flow (Recommended)

```text
1. Collect device information
2. Store/update DeviceInstall via device endpoint (startup step)
3. Check whether device is currently linked
4. Open/prefill login screen
5. User enters PIN
6. Send phoneNumber + pin + deviceId + installId
7. Backend authenticates and verifies/repairs the link
8. Backend generates tokens
```

The frontend's own device check is only useful for UX/routing — it is **never** a security decision. The backend must re-verify authoritatively, since frontend data can be forged.

---

## 9. Architecture — Ports & Module Boundaries

`LoginService` belongs to the **auth** module and must not depend directly on the **user** module's `DeviceLinkService`. Use a port:

```java
public interface LoginDevicePort {
    LoginDeviceResult verifyOrLink(UUID userId, String deviceId, String installId);
}
```

Dependency direction:
```text
Auth LoginService
    ↓
LoginDevicePort
    ↑ implemented by
User-side LoginDeviceAdapter
    ↓
Existing DeviceLinkService
```

### Auth-owned result types (do not leak user-module enums)

```java
public enum LoginDeviceStatus {
    LINKED,
    LINKED_DURING_LOGIN,
    DEVICE_ID_MISSING,
    DEVICE_NOT_REGISTERED,
    DEVICE_LINKED_TO_ANOTHER_USER,
    USER_ALREADY_HAS_ACTIVE_DEVICE
}
```

or, more explicit for payment/conflict handling:

```java
public enum DeviceAccessStatus {
    ALREADY_LINKED,
    LINKED_DURING_LOGIN,
    LINK_FAILED_OPTIONAL,
    SECOND_DEVICE_PAYMENT_REQUIRED,
    DEVICE_OWNERSHIP_CONFLICT
}
```

---

## 10. `LoginService` Orchestration (Full Version)

```java
public LoginUserDTO login(LoginUserCommand command) {

    AuthUserSnapshot snapshot = loadAuthUserPort
            .findByPhoneNumber(command.getPhoneNumber())
            .orElseThrow(InvalidCredentialsException::new);

    AuthUser authUser = AuthUser.of(
            snapshot.getUserId(),
            snapshot.getUsername(),
            snapshot.getLoginPermission()
    );

    if (!authUser.canLogin()) {
        throw new LoginNotAllowedException();
    }

    boolean pinValid = pinVerificationPort.verifyPin(
            authUser.getUserId(),
            command.getPin()
    );

    if (!pinValid) {
        throw new InvalidCredentialsException();
    }

    LoginDeviceResult deviceResult = loginDevicePort.evaluate(
            authUser.getUserId(),
            command.getDeviceId(),
            command.getInstallId()
    );

    if (deviceResult.isOwnershipConflict()) {
        return loginMapper.toOwnershipConflictResult(authUser);
    }

    if (deviceResult.isSecondDevicePaymentRequired()) {
        PaymentSession paymentSession = secondDevicePaymentPort.createPaymentSession(
                authUser.getUserId(),
                command.getDeviceId(),
                command.getInstallId()
        );
        return loginMapper.toPaymentRequiredResult(authUser, paymentSession);
    }

    if (deviceResult.canAttemptFreeLink()) {
        try {
            deviceResult = loginDevicePort.tryLink(
                    authUser.getUserId(),
                    command.getDeviceId(),
                    command.getInstallId()
            );
        } catch (RuntimeException exception) {
            deviceResult = LoginDeviceResult.notLinked(
                    LoginDeviceFailureReason.LINKING_FAILED
            );
        }
    }

    TokenPair tokens = tokenGenerationPort.generateTokens(
            authUser.getUserId(),
            command.getDeviceId(),
            command.getInstallId()
    );

    return loginMapper.toAuthenticatedResult(authUser, tokens, deviceResult);
}
```

Key points embedded in this flow:
- Ownership conflicts and payment requirements are checked **before** attempting to generate normal tokens.
- Device-link exceptions are caught locally and never roll back a valid authentication.
- Token generation happens last, only after all credential and device checks are resolved.

---

## 11. Implementation Order (Application Layer)

1. Add `deviceId` and `installId` to `LoginUserCommand`.
2. Correct `LoginUserDTO` to represent full login output (tokens + device-link status).
3. Create `LoginDevicePort` (auth-owned interface).
4. Create the auth-owned device result + failure reason types.
5. Create authentication and device-login exceptions.
6. Create `LoginMapper`.
7. Create `LoginService`.
8. Create `LoginCommandHandler`.
9. Unit-test every login path using mocked ports.

---

## 12. Test Checklist

1. Unknown phone number → invalid credentials.
2. Wrong PIN → invalid credentials.
3. Account not eligible → login rejected.
4. Correct user + already-linked device → tokens returned.
5. Correct credentials + missing link → link recovered, tokens returned.
6. Device linked to another user → login rejected / ownership conflict.
7. User already linked to another device → payment required (or rejected, per policy).
8. Device not registered → login succeeds unlinked (or rejected, per Case E design choice).
9. Device ID missing → login succeeds unlinked or rejected, consistent with policy.
10. Token generation happens only after successful credential and device checks.

---

## 13. Final Consolidated Business Rules

```text
Wrong credentials
→ Login fails

Valid credentials + same linked device
→ Normal login

Valid credentials + no existing device, device free
→ Attempt free link, normal login

Valid credentials + ordinary technical linking failure
→ Login succeeds, unlinked/untrusted session

Valid credentials + another active device + paid slot available
→ Link this device, normal login

Valid credentials + another active device + no paid slot
→ SECOND_DEVICE_PAYMENT_REQUIRED

Valid credentials + current device belongs to another user
→ DEVICE_OWNERSHIP_CONFLICT
```

This keeps three concerns fully separate and independently testable:

1. **Authentication** — is the phone number + PIN correct?
2. **Device ownership** — does this device belong to this user, or does it conflict?
3. **Paid entitlement** — has the user paid for the number of active devices they're using?
