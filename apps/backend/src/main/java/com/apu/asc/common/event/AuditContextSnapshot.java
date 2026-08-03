package com.apu.asc.common.event;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public record AuditContextSnapshot(
    String actorId,
    String actorUsername,
    String actorRole,
    String correlationId,
    String requestMethod,
    String requestPath,
    String clientIp,
    String userAgent) {

  private static final int MAX_USER_AGENT_LENGTH = 512;

  public static AuditContextSnapshot capture() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String actorId = "SYSTEM";
    String actorUsername = "SYSTEM";
    String actorRole = "SYSTEM";
    if (authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal())) {
      actorId = resolveActorId(authentication);
      actorUsername = authentication.getName();
      actorRole =
          authentication.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .filter(authority -> authority.startsWith("ROLE_"))
              .map(authority -> authority.substring("ROLE_".length()))
              .sorted(Comparator.naturalOrder())
              .findFirst()
              .orElse("AUTHENTICATED");
    }

    HttpServletRequest request = currentRequest();
    return new AuditContextSnapshot(
        actorId,
        actorUsername,
        actorRole,
        MDC.get("traceId"),
        request != null ? request.getMethod() : null,
        request != null ? request.getRequestURI() : null,
        request != null ? request.getRemoteAddr() : null,
        request != null ? truncate(request.getHeader("User-Agent")) : null);
  }

  private static String resolveActorId(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt) return jwt.getSubject();
    if (principal instanceof OidcUser oidcUser) return oidcUser.getSubject();
    return authentication.getName();
  }

  private static HttpServletRequest currentRequest() {
    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    return attributes instanceof ServletRequestAttributes servletAttributes
        ? servletAttributes.getRequest()
        : null;
  }

  private static String truncate(String value) {
    if (value == null || value.length() <= MAX_USER_AGENT_LENGTH) return value;
    return value.substring(0, MAX_USER_AGENT_LENGTH);
  }
}
