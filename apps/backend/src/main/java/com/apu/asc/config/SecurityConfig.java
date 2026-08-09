package com.apu.asc.config;

import com.apu.asc.user.CustomOidcUserService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class SecurityConfig {

  private final CustomOidcUserService customOidcUserService;

  private final ClientRegistrationRepository clientRegistrationRepository;

  @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
  private String allowedOrigins;

  @Value(
      "${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:http://localhost/auth/realms/apu-asc/protocol/openid-connect/certs}")
  private String jwkSetUri;

  @Value(
      "${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost/auth/realms/apu-asc}")
  private String jwtIssuerUri;

  @Value("${app.security.additional-jwt-issuer-uris:}")
  private String additionalJwtIssuerUris;

  @Value("${observability.prometheus.allowed-cidr:127.0.0.1/32}")
  private String prometheusAllowedCidrs;

  @Value("${app.frontend-base-url:http://localhost:3000}")
  private String frontendBaseUrl;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers(
                        "/api/v1/auth/**", "/api/v1/stripe/webhook", "/login/oauth2/**", "/logout"))
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .oauth2Login(
            oauth2 ->
                oauth2
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                    .defaultSuccessUrl(frontendBaseUrl, true)
                    .failureUrl(frontendBaseUrl + "/login?error=true"))
        .logout(
            logout ->
                logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(oidcLogoutSuccessHandler())
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID"))
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/",
                        "/favicon.ico",
                        "/favicon.svg",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/scalar",
                        "/docs",
                        "/docs/**",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/api/v1/auth/**",
                        "/api/v1/stripe/webhook",
                        "/api/v1/users/avatar/file/**",
                        "/api/v1/audit-logs/sentry-test",
                        "/login/**",
                        "/oauth2/**")
                    .permitAll()
                    .requestMatchers("/actuator/prometheus")
                    .access(
                        (authentication, context) ->
                            prometheusScrapeAccessDecision(context.getRequest()))
                    .requestMatchers("/actuator/**")
                    .hasAnyRole("MANAGER", "SYSTEM_ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  AuthorizationDecision prometheusScrapeAccessDecision(HttpServletRequest request) {
    boolean isAllowed =
        List.of(prometheusAllowedCidrs.split(",")).stream()
            .map(String::trim)
            .filter(cidr -> !cidr.isEmpty())
            .anyMatch(cidr -> new IpAddressMatcher(cidr).matches(request));
    return new AuthorizationDecision(isAllowed);
  }

  private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
    OidcClientInitiatedLogoutSuccessHandler handler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
    String signedOutPage = frontendBaseUrl + "/login?loggedOut=true";
    handler.setPostLogoutRedirectUri(signedOutPage);
    handler.setDefaultTargetUrl(signedOutPage);
    return handler;
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    jwtDecoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), trustedIssuerValidator()));
    return jwtDecoder;
  }

  OAuth2TokenValidator<Jwt> trustedIssuerValidator() {
    Set<String> trustedIssuers = new HashSet<>();
    trustedIssuers.add(jwtIssuerUri);
    for (String issuer : additionalJwtIssuerUris.split(",")) {
      if (!issuer.isBlank()) {
        trustedIssuers.add(issuer.trim());
      }
    }

    return jwt ->
        jwt.getIssuer() != null && trustedIssuers.contains(jwt.getIssuer().toString())
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(
                new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, "Untrusted token issuer", null));
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
    return converter;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    List<String> origins = List.of(allowedOrigins.split(","));
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  private static final class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (csrfToken != null) {
        csrfToken.getToken();
      }
      filterChain.doFilter(request, response);
    }
  }

  static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final Set<String> APP_ROLES =
        Set.of("CUSTOMER", "STAFF", "TECHNICIAN", "MANAGER", "SYSTEM_ADMIN");

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
      Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
      if (realmAccess == null || realmAccess.isEmpty()) {
        return List.of();
      }

      List<String> roles = (List<String>) realmAccess.get("roles");
      if (roles == null) {
        return List.of();
      }

      Set<GrantedAuthority> authorities = new HashSet<>();
      for (String roleName : roles) {
        String roleStr = roleName.toUpperCase();
        if (!APP_ROLES.contains(roleStr)) {
          continue; // Filter out default/internal Keycloak roles (e.g., default-roles-apu-asc,
          // offline_access)
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
        if ("SYSTEM_ADMIN".equals(roleStr) || "MANAGER".equals(roleStr)) {
          authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
          authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
          authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        }
      }
      return authorities;
    }
  }
}
