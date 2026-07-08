# Complete Login Flow Documentation

## Table of Contents
- [Overview](#overview)
- [The Big Picture](#the-big-picture)
- [Step-by-Step Flow](#step-by-step-flow)
- [Visual Flow Diagram](#visual-flow-diagram)
- [Architecture & Design Decisions](#architecture--design-decisions)
- [Real-World Analogy](#real-world-analogy)
- [Security Considerations](#security-considerations)
- [Key Takeaways](#key-takeaways)

---

## Overview

This document explains the complete authentication flow for the login system. It covers how a user logs in using their phone number and PIN, how the system validates credentials, checks permissions, and issues authentication tokens.

**Target Audience:** Beginners and developers new to the authentication system.

---

## The Big Picture

Think of the login process like going through a security checkpoint at an airport:

1. **Show your phone number** (like showing your ticket)
2. **Security checks if you're allowed to enter** (not banned, not on hold)
3. **Enter your PIN** (like showing your ID)
4. **If everything is okay, you get a boarding pass** (authentication token)

---

## Step-by-Step Flow

### Step 1: User Opens Login Screen

**What the user sees:**
```
Username: Rahul123        (read-only, for display)
Phone:    [____________]  (input field)
PIN:      [****]          (password field)
          [Login Button]
```

**User enters:**
- Phone: `+919876543210`
- PIN: `1234`

**User clicks:** Login button

> **Note:** The username field is pre-filled and read-only. It's NOT sent to the backend during login.

---

### Step 2: Frontend Sends Data to Backend

**Request Payload:**
```json
{
  "phone": "+919876543210",
  "pin": "1234"
}
```

**Important:** 
- Username is NOT sent (it's just for display)
- Only phone number and PIN are transmitted

---

### Step 3: Request Reaches Auth Controller

The **Auth Controller** acts as a receptionist:
- Receives the phone number and PIN
- Passes the request to **LoginService** (the manager)
- Returns the response back to the frontend

**Responsibility:** Route requests, handle HTTP concerns

---

### Step 4: LoginService Starts Working

**LoginService** is the orchestrator (manager) that coordinates the entire login process.

**Responsibilities:**
1. Find the user by phone number
2. Create domain entity (AuthUser)
3. Check login permissions
4. Validate PIN
5. Generate authentication tokens

---

### Step 5: Find the User (Using Phone Number)

**Process:**

1. **LoginService asks User Module:**
   ```
   "Hey User Module, do you have a user with phone +919876543210?"
   ```

2. **User Module checks database:**
   - Searches the `users` table
   - Finds user with `userId = abc-123-xyz`

3. **User Module responds with a snapshot:**
   ```json
   {
     "userId": "abc-123-xyz",
     "username": "Rahul123",
     "loginPermission": "ELIGIBLE"
   }
   ```

**What User Module does NOT send:**
- ❌ PIN (too sensitive to expose)
- ❌ Phone (already used for lookup, no longer needed)
- ❌ Other sensitive user data

---

### Step 6: Create AuthUser (Domain Entity)

**LoginService creates an AuthUser object:**

```typescript
const authUser = new AuthUser({
  userId: "abc-123-xyz",
  username: "Rahul123",
  loginPermission: LoginPermission.ELIGIBLE
});
```

Think of **AuthUser** as a temporary ID card that contains:
- `userId` - Unique identifier
- `username` - Display name
- `loginPermission` - Authorization status

**Special ability:** The `canLogin()` method checks if the user is allowed to log in.

---

### Step 7: Check if User Can Login (canLogin())

**LoginService checks the authorization:**

```typescript
if (!authUser.canLogin()) {
  throw new UnauthorizedException("Access denied");
}
```

**How `canLogin()` works:**

#### Scenario 1: ELIGIBLE ✅
```
loginPermission = ELIGIBLE
canLogin() returns: true
Result: Continue to PIN validation
```

#### Scenario 2: PENDING_VERIFICATION ❌
```
loginPermission = PENDING_VERIFICATION
canLogin() returns: false
Result: "Sorry, your account is pending verification"
🛑 STOP HERE - Don't proceed
```

#### Scenario 3: ACCOUNT_DELETED ❌
```
loginPermission = ACCOUNT_DELETED
canLogin() returns: false
Result: "Sorry, your account has been deleted"
🛑 STOP HERE - Don't proceed
```

#### Scenario 4: SUSPENDED ❌
```
loginPermission = SUSPENDED
canLogin() returns: false
Result: "Sorry, your account has been suspended"
🛑 STOP HERE - Don't proceed
```

---

### Step 8: Validate PIN (If canLogin is true)

**LoginService asks User Module again:**

```
"Hey User Module, is PIN '1234' correct for userId abc-123-xyz?"
```

**User Module verification process:**

1. Fetches user from database using `userId`
2. Retrieves stored hashed PIN: `$2a$10$xyz...`
3. Uses **BCrypt** to compare:
   ```typescript
   const isValid = await bcrypt.compare(enteredPin, storedHashedPin);
   ```

**User Module responds:**

**If correct:** ✅
```json
{
  "isValid": true
}
```

**If incorrect:** ❌
```json
{
  "isValid": false
}
```

**If PIN is wrong:**
```
LoginService throws: "Invalid credentials"
🛑 STOP HERE
```

---

### Step 9: Generate Tokens (If PIN is correct)

**LoginService says:**
```
"Everything is good! Let me create authentication tokens for you"
```

**Creates two tokens:**

#### 1. Access Token (Short-lived: 15 minutes)
```json
{
  "userId": "abc-123-xyz",
  "username": "Rahul123",
  "type": "access",
  "expiresAt": "2024-01-01T12:15:00Z"
}
```

**Purpose:** Used for API requests to access protected resources

#### 2. Refresh Token (Long-lived: 7 days)
```json
{
  "userId": "abc-123-xyz",
  "type": "refresh",
  "expiresAt": "2024-01-08T12:00:00Z"
}
```

**Purpose:** Used to generate new access tokens when they expire

**Additional security step:**
- Refresh token is also saved in the database
- Allows for token revocation if needed

---

### Step 10: Return Tokens to User

**LoginService sends back:**
```json
{
  "accessToken": "v2.local.eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "refreshToken": "v2.local.dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Frontend receives tokens and:**
1. Stores them securely (localStorage, httpOnly cookies, or secure storage)
2. Includes `accessToken` in Authorization header for future API requests
3. Uses `refreshToken` to get a new `accessToken` when it expires

---

## Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     USER ENTERS CREDENTIALS                  │
│                  Phone: +919876543210, PIN: 1234             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   FRONTEND SENDS TO BACKEND                  │
│                  POST /auth/login {phone, pin}               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    AUTH CONTROLLER RECEIVES                  │
│                 Routes to LoginService.login()               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    LOGINSERVICE STARTS                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: FIND USER                                            │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ LoginService → User Module                              │ │
│ │ "Who has phone +919876543210?"                          │ │
│ │                                                           │ │
│ │ User Module → Database                                   │ │
│ │ SELECT * FROM users WHERE phone = '+919876543210'        │ │
│ │                                                           │ │
│ │ Response:                                                │ │
│ │ - userId: abc-123-xyz                                    │ │
│ │ - username: Rahul123                                     │ │
│ │ - loginPermission: ELIGIBLE                              │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: CREATE AUTHUSER (Domain Entity)                     │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ new AuthUser({                                           │ │
│ │   userId: "abc-123-xyz",                                 │ │
│ │   username: "Rahul123",                                  │ │
│ │   loginPermission: LoginPermission.ELIGIBLE              │ │
│ │ })                                                        │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: CHECK canLogin()                                     │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ authUser.canLogin()                                      │ │
│ │ → Checks: Is loginPermission === ELIGIBLE?              │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↓
                        YES ✅ │ NO ❌
                              │
              ┌───────────────┴────────────────┐
              ↓                                ↓
┌───────────────────────────┐    ┌────────────────────────────┐
│ STEP 4: VALIDATE PIN      │    │ THROW UNAUTHORIZED ERROR   │
│ ┌───────────────────────┐ │    │ "Access denied"            │
│ │ LoginService →        │ │    │ 🛑 STOP HERE               │
│ │ User Module           │ │    └────────────────────────────┘
│ │ "Is PIN 1234 correct  │ │
│ │  for user abc-123?"   │ │
│ │                       │ │
│ │ User Module:          │ │
│ │ - Get hashed PIN      │ │
│ │ - bcrypt.compare()    │ │
│ │ - Return true/false   │ │
│ └───────────────────────┘ │
└───────────────────────────┘
              ↓
        YES ✅ │ NO ❌
              │
  ┌───────────┴────────────┐
  ↓                        ↓
┌─────────────────┐  ┌──────────────────────┐
│ STEP 5:         │  │ THROW UNAUTHORIZED   │
│ GENERATE TOKENS │  │ "Invalid credentials"│
│ ┌─────────────┐ │  │ 🛑 STOP HERE         │
│ │ Create:     │ │  └──────────────────────┘
│ │ - Access    │ │
│ │ - Refresh   │ │
│ │             │ │
│ │ Save refresh│ │
│ │ to database │ │
│ └─────────────┘ │
└─────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ RETURN TOKENS TO USER                                        │
│ {                                                            │
│   "accessToken": "v2.local.xyz...",                          │
│   "refreshToken": "v2.local.abc..."                          │
│ }                                                            │
└─────────────────────────────────────────────────────────────┘
        ↓
┌─────────────────────────────────────────────────────────────┐
│ FRONTEND STORES TOKENS                                       │
│ - localStorage or secure storage                             │
│ - Use accessToken for API requests                           │
│ - Use refreshToken when accessToken expires                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Architecture & Design Decisions

### Why Phone and PIN Are Not in AuthUser

#### Phone Number
- **Used ONLY to find the user** in the database
- Once we have `userId`, phone is no longer needed
- Analogy: Using a ticket number to find your seat, then you don't need the ticket number anymore

#### PIN
- **Used ONLY to verify identity**
- Too sensitive to keep around in memory
- Immediately discarded after verification
- Analogy: Entering a password at an ATM - it's checked and immediately forgotten

### What AuthUser Contains

```typescript
class AuthUser {
  userId: string;           // WHO you are
  username: string;         // FOR display purposes
  loginPermission: enum;    // CAN you login?
}
```

That's all the Auth domain needs!

### Separation of Concerns

| Concern | Module | Responsibility |
|---------|--------|---------------|
| **Storage** | User Module | Manages phone, PIN (hashed), user data |
| **Authentication** | Auth Module | Validates credentials, manages sessions |
| **Authorization** | AuthUser | Business rules (can this user login?) |
| **Presentation** | Frontend | Displays UI, manages user input |

---

## Real-World Analogy

Think of the login process like entering a corporate building:

### 1. Show Phone Number
👤 **You:** "Hi, I'm here. My employee ID is +919876543210"  
🏢 **Security Guard:** *Looks you up in the visitor system*

### 2. Check Status (canLogin)
🏢 **Security Guard:** *Checks your status*
- ✅ Active employee → Continue
- ❌ Pending onboarding → "Sorry, come back when onboarding is complete"
- ❌ Terminated → "Sorry, you no longer have access"

### 3. Verify Identity (PIN)
🏢 **Security Guard:** "Please enter your PIN to verify it's you"  
👤 **You:** *Enters PIN on keypad*  
🏢 **System:** *Validates PIN* ✅

### 4. Get Badge (Token)
🏢 **Security Guard:** "Here's your access badge for today"  
👤 **You:** *Receives temporary badge (access token)*

### 5. Inside the Building
- Your phone number doesn't matter anymore
- Your PIN is forgotten
- You use your badge (token) to access rooms and resources
- When your badge expires (15 min), you can get a new one using your refresh token

---

## Security Considerations

### 1. Password Hashing
```typescript
// PINs are NEVER stored in plain text
const hashedPin = await bcrypt.hash(pin, 10);
```

**Benefits:**
- Even if database is compromised, PINs remain secure
- Industry standard (BCrypt with salt rounds)

### 2. Token Expiration
- **Access Token:** 15 minutes (short-lived)
- **Refresh Token:** 7 days (long-lived)

**Why?**
- Limits damage if access token is stolen
- Refresh token stored in database for revocation capability

### 3. Minimal Data Exposure
- AuthUser only contains what's necessary
- Sensitive data (phone, PIN) never leaves User Module
- Each module has minimal knowledge of other modules

### 4. Single Responsibility
Each component has one job:
- **User Module:** User data management
- **Auth Module:** Authentication & token generation
- **AuthUser:** Authorization rules

### 5. Fail-Safe Defaults
```typescript
// Default behavior: deny access
if (!authUser.canLogin()) {
  throw new UnauthorizedException();
}
```

---

## Key Takeaways

### Data Flow Summary

| What | Where It Lives | Why |
|------|---------------|-----|
| Phone | User Module | Only for finding user |
| PIN | User Module (hashed) | Only for verification |
| userId | AuthUser | Identifies user everywhere |
| username | AuthUser | For display purposes |
| loginPermission | AuthUser | Business rule: can login? |
| Tokens | Auth Module | Session management |

### Why This Design is Smart

#### 1. Security
- Sensitive data (phone, PIN) stays in User Module
- Tokens are stateless and verifiable
- Each layer has minimal access

#### 2. Separation of Concerns
- Auth doesn't need to know about phone/PIN storage
- User Module doesn't need to know about tokens
- Each module is independently testable

#### 3. Simplicity
- AuthUser only has what it needs
- Clear boundaries between modules
- Easy to understand and maintain

#### 4. Flexibility
- Can change phone/PIN logic without affecting Auth
- Can switch token mechanism without affecting User Module
- Can add new login permissions without changing infrastructure

#### 5. Scalability
- Stateless tokens enable horizontal scaling
- No session storage needed
- Database queries are efficient (indexed by userId)

---

## Appendix: Login Permission States

| State | canLogin() | Description |
|-------|-----------|-------------|
| `ELIGIBLE` | ✅ true | User can log in normally |
| `PENDING_VERIFICATION` | ❌ false | Email/phone verification pending |
| `ACCOUNT_DELETED` | ❌ false | Account has been soft-deleted |
| `SUSPENDED` | ❌ false | Account temporarily suspended |
| `BANNED` | ❌ false | Account permanently banned |

---

## Questions & Answers

### Q: Why not send username during login?
**A:** Username is just for display on the UI. The backend doesn't need it because:
1. Phone number uniquely identifies the user
2. Username is retrieved from the database along with userId
3. Reduces payload size and attack surface

### Q: What if someone steals my refresh token?
**A:** 
- Refresh tokens are stored in the database
- They can be revoked immediately by deleting the record
- They have a limited lifespan (7 days)
- Best practice: Store in httpOnly cookies (not accessible via JavaScript)

### Q: Why check canLogin() before validating PIN?
**A:**
- Saves computational resources (BCrypt is expensive)
- Prevents timing attacks
- Provides better error messages
- Enforces business rules first, technical validation second

### Q: Can I use email instead of phone?
**A:** Yes! The design is flexible:
```typescript
// Just change the lookup field
const user = await userModule.findByEmail(email);
// Rest of the flow remains the same
```

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Author:** Development Team  
**Status:** Production Ready
