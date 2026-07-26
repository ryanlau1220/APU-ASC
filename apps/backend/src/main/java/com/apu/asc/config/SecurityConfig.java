package com.apu.asc.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

  @Value(
      "${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:http://localhost/auth/realms/apu-asc/protocol/openid-connect/certs}")
  private String jwkSetUri;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint customEntryPoint =
        (request, response, authException) -> {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/problem+json");
          response
              .getWriter()
              .write(
                  """
              {
                "type": "about:blank",
                "title": "Unauthorized",
                "status": 401,
                "detail": "Authentication is required to access this resource",
                "instance": "%s"
              }
              """
                      .formatted(request.getRequestURI()));
        };

    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(customEntryPoint))
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
                        "/actuator/**",
                        "/api/v1/auth/**",
                        "/api/v1/events/stream")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .authenticationEntryPoint(customEntryPoint)
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    OAuth2TokenValidator<Jwt> validator =
        new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator());
    jwtDecoder.setJwtValidator(validator);
    return jwtDecoder;
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
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  static class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
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

      return roles.stream()
          .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
          .collect(Collectors.toList());
    }
  }
}
