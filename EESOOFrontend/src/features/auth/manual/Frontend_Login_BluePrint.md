## Frontend implementation blueprint

To recreate the same login page, build it as a mobile-first authentication experience with four major responsibilities:

1. Recognize the current device and registered user.
2. Verify the user’s mobile information.
3. Collect and validate a four-digit PIN.
4. Manage login, PIN reset, errors, loading states, and navigation.

This description covers only the frontend. The frontend will call authentication services, but it should not contain database, hashing, token-generation, or other backend logic.

# 1. Frontend page hierarchy

Organize the frontend into three levels.

## Application-level authentication controller

The main application controller decides which screen to display:

- Startup screen
- Login screen
- Registration screen
- Customer landing page
- Shop-owner dashboard

It also manages:

- The currently logged-in user
- Registered-user information
- Token restoration
- Device recognition
- Login success
- Logout
- Role-based navigation

## Login page container

The login page owns the complete login experience:

- Login and Reset PIN modes
- Mobile verification
- PIN entry
- Error messages
- Loading states
- Wrong-attempt counters
- Mobile replacement
- Visual status information

## Reusable UI components

Split the page into smaller visual components:

- Login logo header
- Security status bar
- Security database icon
- Recognized-user display
- Mobile verification field
- Manual identity field
- Four-digit PIN field
- Numeric keypad
- Primary action button
- Voice-help bar

This structure keeps the main login page understandable and makes smaller pieces reusable.

# 2. Startup screen

Before showing the login form, display a full-screen startup state.

The startup screen should contain:

- A white background
- Centered EESOO logo
- “Securing Access” text
- Five-second countdown
- “Initializing” message
- Logo entrance animation
- Countdown animation

During this state, the frontend should:

1. Initialize the device identifier.
2. Check whether a valid login token already exists.
3. If a token exists, request the current user.
4. If token restoration fails, check device pairing.
5. If the device is recognized, prepare the recognized-user login.
6. If local registration data exists, use it as a fallback.
7. Otherwise, navigate to registration.

The login page should not appear until this initialization is finished.

# 3. Device identification

The frontend needs a device-identification utility.

On a native mobile application, it should request the platform’s device identifier.

In a browser, it should create a stable browser identifier using browser and device information.

The identifier should be cached so the page can read it synchronously later.

The frontend should store it in:

- Browser local storage
- A browser cookie as a fallback

The login page sends this identifier with login-related requests.

For a production application, treat browser fingerprinting as a convenient identifier—not as strong proof that the person owns the device.

# 4. Exact visual layout

The page should use a mobile-first, vertically scrollable design.

Recommended dimensions matching the current layout:

- Maximum application width: approximately 430 pixels
- Maximum login-content width: approximately 384 pixels
- White full-screen background
- Horizontal page padding: approximately 16 pixels
- Logo size: approximately 72 × 72 pixels
- Form-card corner radius: approximately 20 pixels
- Input and button minimum height: approximately 48 pixels
- Button corner radius: approximately 14 pixels
- Numeric keypad: three columns

The page layout, from top to bottom, should be:

1. Logo
2. Security status bar
3. Login card
4. User identity
5. Mobile verification
6. PIN entry
7. Login or Reset PIN button
8. Voice-help bar

# 5. Visual design system

Use a consistent colour system.

## Main colours

- Page background: white
- Input background: white or very light grey
- Inactive background: light grey
- Active focus colour: bright cyan
- Main button: light-blue to darker-blue gradient
- Success colour: green
- Warning colour: amber
- Error colour: red
- Primary text: dark slate
- Secondary text: grey

## Main button

The active button should use a vertical blue gradient:

- Lighter blue at the top
- Darker blue at the bottom
- White text
- Soft blue shadow
- Slight shrinking animation when pressed

The inactive button should use:

- Light-grey background
- Grey text
- No strong shadow
- Disabled cursor appearance

## Inputs

Inputs should use:

- Approximately 1.5-pixel borders
- Rounded corners
- Soft shadows
- Cyan border and focus ring when active
- Pale red styling when invalid
- Pale blue styling when complete

# 6. Logo header

Place the EESOO logo at the top centre.

The page should enter with:

- Fade-in
- Small upward movement
- Slight scale animation

When the logo is pressed:

- Briefly pulse or shrink it
- Count repeated taps
- Reset the tap count after approximately two seconds

If hidden administrator access is required, five taps can trigger an administrator callback. That callback must be supplied by the application controller. Without the callback, only the visual tap animation should run.

# 7. Security status bar

Below the logo, create a rounded, pill-shaped status panel.

It should contain:

- Security/database icon on the left
- Dynamic message in the centre
- Matching icon on the right

The panel should have:

- White background
- Rounded full-pill shape
- Soft shadow
- Light-grey border
- Minimum height around 52 pixels

The database icon should represent three security stages:

- Blue lower section: registered user identified
- Orange middle section: mobile verification completed
- Green upper section: PIN completed or verified

# 8. Status-message priority

The status bar should show only one message at a time. Use a clear priority order.

Recommended order:

1. Missing registered identity
2. Newly reset PIN instruction
3. Login-ready or secured state
4. Wrong-PIN errors
5. Mobile-verification errors
6. Inactive-control instructions
7. General request errors
8. PIN verification in progress
9. PIN-entry guidance
10. Idle device-security state

Example messages:

- User Registration Failed
- Device Security Active
- Enter first 6 digits to verify Mobile No
- Verify Mobile to enter PIN
- Enter your 4-digit security PIN
- Verifying PIN
- You entered wrong PIN — attempt 1 of 2
- You entered wrong PIN — attempt 2 of 2
- Device Secured Successfully
- Reset PIN
- Enter Your New PIN

This priority system prevents several errors or instructions from appearing simultaneously.

# 9. Two identity-display modes

The login page must support two identity modes.

## Recognized-user mode

Use this when device pairing or locally stored registration information identifies the user.

Display:

- Read-only username
- User icon
- Green verified badge
- Masked registered mobile number

The username should not be editable.

## Manual identity mode

Use this when no registered-user information is available.

Display one editable field with:

- “Mobile Number or Username” placeholder
- Support for a ten-digit mobile number
- Support for a generated username
- Help text explaining the accepted values

When the field begins with digits, the frontend can use a numeric-friendly keyboard. Otherwise, it should allow normal text entry.

# 10. Recognized username row

The recognized-user row should contain:

- A small user-icon box on the left
- The username in a larger centre box
- Green verification badge on the right

Style it as read-only:

- Light-grey background
- Grey border
- Dark username text
- No editing cursor
- Soft inner shadow

The username should automatically come from registered-user information supplied by the application controller.

# 11. Masked mobile-number row

Display the registered mobile number using:

- Country code box containing `+91`
- Masked mobile-number section
- Verification or retry icon

The first six digits should be hidden using `X` characters.

The final four digits should remain visible.

For example, a mobile number should visually appear in this general form:

`+91 X X X X X X 1 2 3 4`

The user verifies the number by entering the first six digits.

# 12. Mobile-verification interaction

When the user presses the masked mobile field:

1. Activate mobile-verification mode.
2. Open a custom numeric keypad.
3. Focus the hidden mobile input.
4. Allow a maximum of six digits.
5. Compare each entered digit with the expected prefix.
6. Display correct digits in green.
7. Display incorrect digits in red.
8. Automatically finish when six digits are entered.

If all digits match:

- Mark the mobile as verified
- Close the keypad
- Show a green badge
- Unlock PIN entry
- Move focus to the PIN field

If the digits do not match:

- Clear the entered digits
- Increase the mobile-attempt count
- Show an error message
- Show a red retry icon

The retry icon should:

- Clear the failed value
- Clear the mobile error
- Reopen mobile verification
- Focus the mobile keypad

To reproduce the current page exactly, this comparison happens on the frontend using the mobile number already supplied to the page. For stronger security, replace it later with OTP verification.

# 13. Mobile-verification attempts

Maintain a frontend attempt counter.

After the first incorrect attempt:

- Show an incorrect-mobile message
- Explain that one attempt remains
- Allow the user to retry

After the second incorrect attempt:

- Stop normal mobile verification
- Hide the masked-number row
- Display a “Reset Mobile Number” button

The attempt counter should reset when:

- The registered mobile number changes
- The replacement mobile succeeds
- The user begins a new recognized-user session

Remember that frontend counters reset when the page reloads. Any real security limit must also be enforced by the backend.

# 14. Mobile-number replacement section

After two failed mobile checks, display a replacement-mobile flow.

The section should contain:

- “Reset Mobile Number” button
- `+91` country-code box
- Ten-digit mobile input
- “Update Mobile Number” button
- Updating state
- Error message area

Frontend validation should require:

- Exactly ten digits
- First digit between 6 and 9
- No letters or special characters

During submission:

- Disable repeated submissions
- Change the label to “Updating”
- Keep the user on the same section
- Display returned errors clearly

After success:

- Update the registered-user information
- Save the updated information locally
- Close replacement mode
- Reset mobile attempts
- Unlock PIN entry
- Clear the PIN field

The frontend service URL and request method must exactly match the backend contract. Keep this API call in the authentication service rather than directly inside the visual component.

# 15. PIN-entry component

The PIN component should display four separate visual boxes.

Each box represents one PIN digit.

When the PIN is hidden:

- Filled positions display solid dots
- Empty positions remain blank

When the PIN is visible:

- Display the actual entered digits

Also include an eye button that changes between:

- Hidden PIN
- Visible PIN

The PIN should have exactly four digits.

The frontend interface should reject:

- Letters
- Spaces
- Symbols
- More than four digits

# 16. PIN focus behaviour

When the PIN field receives focus:

- Open the custom numeric keypad
- Highlight the current PIN position
- Apply a cyan border and glow
- Keep the PIN field visible above the mobile keyboard

When four digits are completed:

- Remove visual focus
- Close the custom keypad
- Begin PIN verification if real-time verification is enabled

When a digit is deleted:

- Reopen the correct active position
- Clear previous verification success
- Allow the PIN to be checked again

# 17. Custom numeric keypad

Use a three-column keypad.

The keys should be arranged as:

- First row: 1, 2, 3
- Second row: 4, 5, 6
- Third row: 7, 8, 9
- Final row: empty space, 0, delete

Each number key should have:

- White background
- Light-grey border
- Rounded corners
- Bold number
- Soft shadow
- Cyan hover colour
- Pressed scaling animation

The delete key should have:

- Grey background
- Delete icon
- Pressed scaling animation

Use the same keypad component for:

- Mobile-prefix verification
- PIN entry

Pass different maximum lengths and update actions into it.

# 18. Login modes

The page needs two primary modes.

## Login mode

This is the normal state.

It allows:

- Identity entry or recognized identity
- Mobile verification
- PIN entry
- Login action

The main button says “Login.”

## Reset PIN mode

This mode starts after the allowed PIN failures.

It should:

- Disable normal PIN entry
- Show the reset instructions
- Request mobile confirmation when necessary
- Change the main button to “Reset PIN”

During the reset request, the button should say “Resetting PIN.”

# 19. Login-button rules

The Login button should become active only when:

- A username or mobile identity exists
- Required mobile verification is complete
- Exactly four PIN digits exist
- The page is in Login mode
- The login is not already being submitted
- The wrong-PIN limit has not been reached

If the user presses an inactive-looking button, show a helpful status instead of doing nothing.

Examples:

- Verify Mobile to enter PIN
- Verify Mobile and PIN to Login
- Enter your four-digit PIN

Use both visual disabled styling and proper disabled accessibility behaviour.

# 20. PIN verification strategy

Choose one of these frontend approaches.

## Recommended approach

Enter four PIN digits, then press Login once.

Advantages:

- Only one login request
- Easier loading-state management
- Easier error handling
- No accidental duplicate request

## Exact current-page approach

Automatically verify after the fourth digit, then require the Login button.

This creates a risk of two login requests: one during real-time PIN verification and another when Login is pressed.

If you want the same visual feeling without duplicate requests:

1. Automatically verify after the fourth digit.
2. Store the successful result temporarily.
3. Make the Login button use that result without submitting again.

# 21. Wrong-PIN behaviour

Maintain a PIN-failure counter in frontend state.

After the first wrong PIN:

- Clear the PIN
- Hide the entered digits
- Show a first-attempt error
- Focus PIN entry again
- Keep Login mode active

After the second wrong PIN:

- Lock normal PIN entry
- Switch to Reset PIN mode
- Change the status message
- Change the primary button to Reset PIN

When the user edits the PIN:

- Clear the matched state
- Clear the previous attempted PIN marker
- Remove outdated verification messages

Do not treat the frontend counter as a security control. It is only for guiding the interface.

# 22. Reset PIN frontend flow

When Reset PIN mode starts, determine whether registered mobile information is available.

## Recognized-user reset

Use:

- Stored username
- Stored registered mobile number

The user presses Reset PIN to begin the recovery request.

## Manual-user reset

Display a full ten-digit mobile field.

Require:

- Username or mobile identity
- Valid ten-digit registered mobile number

During the request:

- Disable repeated presses
- Show “Resetting PIN”
- Display returned errors in the status bar

After success:

- Return to Login mode
- Reset the wrong-PIN count
- Clear the old PIN
- Focus the PIN field
- Show a message that reset instructions were sent

If the development API returns the new PIN, the status bar can display it. In production, do not expose a new PIN directly in the interface.

# 23. Login service layer

Create a dedicated frontend authentication service.

The login screen should not know:

- The complete server URL
- Request headers
- Token-header construction
- Timeout implementation
- Response parsing details

The service should expose frontend operations for:

- Login
- Reset PIN
- Update mobile
- Get current profile
- Find user by device identifier

The shared request utility should manage:

- Base API address
- JSON headers
- Authorization header
- Request timeouts
- Network errors
- Server errors
- Error codes

# 24. Frontend data expected from services

The login page needs a user result containing:

- User ID
- Name
- Username
- Mobile number
- Roles
- Device identifier
- Optional subscription plan

A successful login result should provide:

- User information
- Authentication token

A reset-PIN result should provide:

- Success message
- Optional development-only PIN

A mobile-update result should provide:

- Updated user information
- Success message

Keep these response expectations consistent across the page, service, and application controller.

# 25. Token management

After successful login:

1. Store the token.
2. Keep token storage in one shared utility.
3. Automatically attach the token to protected API requests.
4. Remove it during logout.
5. Remove invalid or expired tokens during failed session restoration.

The application controller—not the visual login field—should own session restoration and logout.

For stronger security, consider secure platform storage on native mobile applications instead of ordinary browser local storage.

# 26. Registered-user storage

Save only the frontend information needed to recognize the user:

- Username
- Name
- Mobile number
- Registered-user flag

Use this data to reconstruct the recognized-user login page when the backend is unavailable.

Do not treat local data as proof that the account is valid. It should only improve the user experience.

# 27. Page-lock recovery

If the application supports locking a particular dashboard page, store:

- The locked page identity
- Whether automatic recovery is enabled
- The state needed to reopen that page

The existing design also temporarily stores the PIN in session storage for automatic login.

For a safer new implementation, avoid saving the raw PIN. Use a short-lived reauthentication token or a platform-secured credential instead.

# 28. Login success handling

The login screen should return the authenticated user to the main application controller.

The application controller should then:

1. Save registered-user information.
2. Mark the device as registered.
3. Set the current user.
4. Inspect the user’s roles.
5. Navigate to the correct screen.

Navigation rules matching the current page:

- Shop owner → shop-owner dashboard
- Customer → customer landing page
- User with both roles → shop-owner dashboard first

Do not perform dashboard navigation directly inside the PIN component.

# 29. Error handling

Separate errors into clear frontend categories:

- Missing identity
- Invalid identity
- Invalid PIN
- Mobile verification failed
- Mobile update failed
- Account/device restriction
- Network unavailable
- Request timed out
- Server error
- Reset PIN failed

Display the most relevant error in the main status bar.

Field-specific errors can also appear below the relevant field.

When a new attempt begins:

- Clear outdated errors
- Preserve errors that still affect the current mode
- Avoid showing multiple conflicting messages

# 30. Loading states

Create separate loading indicators for:

- Startup device check
- PIN verification
- Final login
- Mobile update
- PIN reset
- Automatic locked-page login

Do not use one loading value for every operation. Separate values prevent unrelated parts of the page from becoming disabled.

While an operation is running:

- Disable repeated submissions
- Keep the user’s current values
- Show a clear progress message
- Restore controls after failure

# 31. Voice-help bar

To reproduce the current visual design, add a rounded voice-help bar below the login card.

It should contain:

- Search icon
- “Voice help / Questions?” placeholder
- Text input
- Clear button
- Microphone button
- Black listening overlay
- “Listening” text
- Animated waveform
- Close button

If reproducing the current functionality exactly, the microphone only toggles the listening animation.

For a functional version, connect it to:

- Browser speech recognition
- A help-command system
- Search or FAQ results
- Permission and unsupported-browser handling

Do not label it as working voice assistance unless recognition is actually connected.

# 32. Animations and transitions

Implement the following visual transitions:

- Page fade and slide on entry
- Logo pulse when pressed
- Status-message colour transitions
- Mobile field expand and collapse
- Numeric keypad scale and height transition
- Error-help section expand and collapse
- Reset-mobile section slide-in
- Button press scaling
- PIN cursor animation
- Listening waveform animation

Animations should be short and should not prevent interaction.

Also respect the user’s reduced-motion preference.

# 33. Mobile keyboard handling

The frontend should keep the active input visible when the mobile keyboard opens.

Listen for:

- Input focus
- Visual viewport resize
- Visual viewport scroll

When a field becomes active:

- Scroll it into view
- Repeat the adjustment briefly while the keyboard animation completes
- Remove viewport listeners after the field loses focus

This is especially important for:

- Replacement mobile field
- Manual identity field
- Reset mobile field

# 34. Accessibility requirements

To make the page accessible:

- Give every input an accessible label.
- Give the eye button a “Show PIN” or “Hide PIN” label.
- Give the microphone a listening-state label.
- Give the delete key a clear label.
- Use real disabled button behaviour.
- Announce status and error messages through a live region.
- Support physical keyboard entry.
- Keep visible focus indicators.
- Ensure red and green are not the only indicators of success and failure.
- Maintain sufficient text contrast.
- Provide reduced-motion styling.

# 35. Frontend state groups

Keep the page state divided into understandable groups.

## Authentication state

- Current mode
- Identity
- PIN
- PIN matched
- Wrong-PIN attempts
- Login loading

## Mobile state

- Entered verification digits
- Mobile verified
- Mobile-verification attempts
- Replacement mobile
- Replacement mode
- Mobile-update loading

## Device state

- Device identifier
- Device-security status
- Recognized-user information

## Display state

- Current error
- PIN visibility
- Focused field
- Help-message visibility
- Logo-tap count
- PIN verification loading

Grouping the state prevents the page from becoming one large collection of unrelated values.

# 36. Recommended implementation order

Build the frontend in this order:

1. Create the application-level authentication controller.
2. Build the startup screen.
3. Implement device identification and local storage.
4. Build the login-page shell.
5. Add the logo and status bar.
6. Add recognized-user and manual-identity modes.
7. Create the reusable numeric keypad.
8. Build mobile-prefix verification.
9. Build the four-digit PIN input.
10. Add Login and Reset PIN modes.
11. Connect the authentication service.
12. Add token storage and login success handling.
13. Add role-based navigation.
14. Add mobile-number replacement.
15. Add page-lock recovery if required.
16. Add animations and mobile keyboard handling.
17. Add accessibility.
18. Test every state and error path.

# 37. Frontend completion checklist

The frontend can be considered complete when:

- The startup state always finishes correctly.
- Recognized devices show the correct user.
- Unknown users get the manual identity field.
- Mobile masking displays correctly.
- Mobile-prefix verification accepts only the expected digits.
- Both keypads work with touch and physical keyboards.
- PIN visibility can be toggled.
- Only one effective login request occurs.
- Wrong-PIN states transition correctly.
- Reset PIN returns the page to login mode.
- Mobile replacement uses the correct API contract.
- Successful login stores the token.
- The correct dashboard opens based on role.
- Logout clears authentication data.
- Network and timeout errors are understandable.
- The page works on narrow mobile screens.
- Focus remains visible when the keyboard opens.
- Status messages are accessible.
- Voice help is either functional or clearly presented as a visual placeholder.