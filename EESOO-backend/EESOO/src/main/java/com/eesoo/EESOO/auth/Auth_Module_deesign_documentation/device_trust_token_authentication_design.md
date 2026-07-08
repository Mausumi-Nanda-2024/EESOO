# 🔐 Device Trust + Token-Based Authentication (Complete Design Guide)

## 🔹 Summary

This document explains a complete authentication architecture combining:

- Token-based authentication (Access + Refresh tokens)
- Device binding (deviceId + installId)
- Device trust system (User ↔ Device relationship)
- Session management (refresh token persistence)

---

# 🔹 1. Core Architecture Overview

Your system is built on three pillars:

```
TOKEN  → proves identity (who you are)
DEVICE → proves context (where you are logging from)
DB     → proves trust/history (is this allowed?)
```

---

# 🔹 2. Existing System (User Module)

## ✅ DeviceInstall
Represents a physical device or app installation.

Contains:
- deviceId
- installId
- OS info
- firstSeenAt / lastSeenAt

👉 Purpose: Device identity

---

## ✅ UserDeviceLink
Represents relationship between user and device.

Contains:
- userId
- deviceInstallId
- status (ACTIVE, etc.)

👉 Purpose: Permission layer

---

## 🔹 Important Concept

```
DeviceInstall = "What device is this?"
UserDeviceLink = "Is this device allowed for this user?"
```

---

# 🔹 3. Missing Piece → Session Layer

You need a new concept:

## ❗ AuthSession (NEW)

Represents:

```
"Is this login session currently valid?"
```

---

# 🔹 4. New Table Design (AuthSession)

Recommended structure:

```
id (UUID)
user_id
device_install_id (FK)
refresh_token_hash
status (ACTIVE / REVOKED / EXPIRED)
created_at
expires_at
last_used_at
```

---

## 🔹 Why this table is needed

Without it:
- ❌ Cannot logout properly
- ❌ Cannot revoke tokens
- ❌ No session tracking

With it:
- ✔ Full control over sessions
- ✔ Device-level security
- ✔ Token lifecycle management

---

# 🔹 5. Token Design

## Access Token contains:

```
userId
username
deviceId
installId
exp
```

👉 Meaning:

```
Token is bound to a specific device
```

---

# 🔹 6. Login Flow (Step-by-Step)

```
User enters phone + PIN
        ↓
Backend verifies credentials
        ↓
Receives deviceId + installId
        ↓
Generate tokens (with device info)
        ↓
Store refresh_token_hash in AuthSession
        ↓
Return tokens
```

---

# 🔹 7. Request Validation Flow

Every request:

```
1. Extract token
2. Validate signature + expiry
3. Extract deviceId from token
4. Get deviceId from request header
```

### Decision Logic

```
IF deviceId missing → force re-auth
IF mismatch → reject request
IF match → allow request
```

---

# 🔹 8. Fallback Strategy (Option B - Recommended)

## Case 1: Device matches
✔ Allow

## Case 2: Device missing
❌ Force login

## Case 3: Device mismatch
❌ Reject + login again

---

# 🔹 9. Reinstall Scenario

```
Old token → deviceId = A
New install → deviceId = B
```

Result:

```
Mismatch → reject → force login
```

---

# 🔹 10. Refresh Token Flow

```
Client sends refresh token
        ↓
Hash it
        ↓
Find in AuthSession
        ↓
Check:
   status == ACTIVE
   not expired
        ↓
Generate new tokens
        ↓
(Optional) rotate refresh token
```

---

# 🔹 11. Logout Flow

```
User logs out
        ↓
Find AuthSession
        ↓
Mark status = REVOKED
```

---

# 🔹 12. Why Field Duplication is OK

You may see repeated fields like:
- user_id
- device_install_id

👉 This is NOT duplication.

### Reason:

Each table answers a different question:

| Table | Question |
|------|--------|
| DeviceInstall | What device is this? |
| UserDeviceLink | Is device allowed? |
| AuthSession | Is session active? |

---

# 🔹 13. Security Risks (Important)

## Token Theft

Possible causes:
- insecure storage
- XSS
- HTTP (no HTTPS)
- compromised device

### Impact

```
Token stolen → can be reused
```

---

## Solution in your design

```
Token + Device match required
```

👉 Stolen token alone is useless

---

# 🔹 14. Device Binding Benefit

```
Token valid + wrong device → BLOCKED
```

---

# 🔹 15. Device Trust Model

You already have foundation.

Now enhance it with:

## Trust Levels (Optional Advanced)

```
IMMEDIATE_TRUST  (<1 hour)
RECENT_TRUST     (<24 hours)
EXPIRED_TRUST    (>7 days)
NO_TRUST         (new device)
```

---

# 🔹 16. Key Design Principles

### ✔ Separation of concerns

- Device → identity
- Link → permission
- Session → login state

---

### ✔ Security layers

```
Token validation
+ Device validation
+ DB session validation
```

---

### ✔ Hybrid system

```
Stateless (token) + Stateful (DB)
```

---

# 🔹 17. Best Practices

- Use HTTPS always
- Store refresh tokens as hash
- Keep access token short-lived (10–15 min)
- Rotate refresh tokens (recommended)
- Use device_install_id instead of raw deviceId

---

# 🔹 Final Understanding

```
User → Device → Session → Token
```

---

# 🔹 One-Line Conclusion

👉 "Token proves identity, device proves context, session proves validity"

