package com.eesoo.EESOO.auth.infrastructure.security;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eesoo.EESOO.auth.application.exception.AuthSessionNotUsableException;
import com.eesoo.EESOO.auth.domain.model.entity.AuthSession;
import com.eesoo.EESOO.auth.domain.model.token.AccessTokenClaims;
import com.eesoo.EESOO.auth.domain.port.TokenValidationPort;
import com.eesoo.EESOO.auth.domain.repository.AuthSessionRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PasetoAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String BEARER_PREFIX =
            "Bearer ";

    private static final Set<String> PUBLIC_POST_PATHS =
            Set.of(
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/users/register",
                    "/api/v1/devices/store",
                    "/api/v1/devices/check"
            );

    private final TokenValidationPort tokenValidationPort;
    private final AuthSessionRepository authSessionRepository;
    private final Clock clock;
    private final PasetoAuthenticationEntryPoint authenticationEntryPoint;

    public PasetoAuthenticationFilter(
            TokenValidationPort tokenValidationPort,
            AuthSessionRepository authSessionRepository,
            Clock clock,
            PasetoAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.tokenValidationPort = tokenValidationPort;
        this.authSessionRepository = authSessionRepository;
        this.clock = clock;
        this.authenticationEntryPoint =
                authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        if (HttpMethod.OPTIONS.matches(
                request.getMethod()
        )) {
            return true;
        }

        return HttpMethod.POST.matches(
                request.getMethod()
        ) && PUBLIC_POST_PATHS.contains(
                request.getServletPath()
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader =
                request.getHeader(
                        HttpHeaders.AUTHORIZATION
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                        BEARER_PREFIX
                )) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        if (SecurityContextHolder
                .getContext()
                .getAuthentication() != null) {
            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {
            String rawAccessToken =
                    authorizationHeader
                            .substring(
                                    BEARER_PREFIX.length()
                            )
                            .trim();

            AccessTokenClaims claims =
                    tokenValidationPort
                            .validateAccessToken(
                                    rawAccessToken
                            );

            AuthSession authSession =
                    authSessionRepository
                            .findById(
                                    claims.getSessionId()
                            )
                            .orElseThrow(
                                    AuthSessionNotUsableException::new
                            );

            validateSession(
                    authSession,
                    claims
            );

            AuthenticatedUserPrincipal principal =
                    new AuthenticatedUserPrincipal(
                            claims.getUserId(),
                            claims.getSessionId()
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContext securityContext =
                    SecurityContextHolder
                            .createEmptyContext();

            securityContext.setAuthentication(
                    authentication
            );

            SecurityContextHolder.setContext(
                    securityContext
            );

            filterChain.doFilter(
                    request,
                    response
            );

        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "Access-token authentication failed",
                            exception
                    )
            );
        }
    }

    private void validateSession(
            AuthSession authSession,
            AccessTokenClaims claims
    ) {
        if (!authSession
                .getUserId()
                .equals(claims.getUserId())) {
            throw new AuthSessionNotUsableException();
        }

        if (!authSession.isUsable(
                clock.instant()
        )) {
            throw new AuthSessionNotUsableException();
        }
    }
}
