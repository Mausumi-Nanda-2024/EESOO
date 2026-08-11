package com.eesoo.EESOO.auth.application.login.dto;

public final class LoginOutcome {

    private final LoginUserResultDTO successfulLogin;
    private final PinLoginFailureResultDTO pinFailure;

    private LoginOutcome(
            LoginUserResultDTO successfulLogin,
            PinLoginFailureResultDTO pinFailure
    ) {
        this.successfulLogin =
                successfulLogin;

        this.pinFailure =
                pinFailure;
    }

    public static LoginOutcome success(
            LoginUserResultDTO successfulLogin
    ) {
        if (successfulLogin == null) {
            throw new IllegalArgumentException(
                    "LoginUserResultDTO cannot be null"
            );
        }

        return new LoginOutcome(
                successfulLogin,
                null
        );
    }

    public static LoginOutcome pinFailure(
            PinLoginFailureResultDTO pinFailure
    ) {
        if (pinFailure == null) {
            throw new IllegalArgumentException(
                    "PinLoginFailureResultDTO cannot be null"
            );
        }

        return new LoginOutcome(
                null,
                pinFailure
        );
    }

    public boolean isSuccessful() {
        return successfulLogin != null;
    }

    public LoginUserResultDTO getSuccessfulLogin() {
        if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Login outcome does not contain a successful login"
            );
        }

        return successfulLogin;
    }

    public PinLoginFailureResultDTO getPinFailure() {
        if (isSuccessful()) {
            throw new IllegalStateException(
                    "Login outcome does not contain a PIN failure"
            );
        }

        return pinFailure;
    }
}
