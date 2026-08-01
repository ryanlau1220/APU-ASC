package com.apu.asc.user.internal;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${keycloak.admin.server-url:http://localhost/auth}")
  private String keycloakServerUrl;

  @Value("${keycloak.admin.username:admin}")
  private String adminUsername;

  @Value("${keycloak.admin.password:admin_password}")
  private String adminPassword;

  @Value("${keycloak.admin.realm:apu-asc}")
  private String realm;

  private String getAdminAccessToken() {
    String tokenUrl = keycloakServerUrl + "/realms/master/protocol/openid-connect/token";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", "admin-cli");
    body.add("grant_type", "password");
    body.add("username", adminUsername);
    body.add("password", adminPassword);

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
    ResponseEntity<Map<String, Object>> response =
        restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {});

    Map<String, Object> responseBody = response.getBody();
    if (response.getStatusCode().is2xxSuccessful()
        && responseBody != null
        && responseBody.containsKey("access_token")) {
      return (String) responseBody.get("access_token");
    }
    throw new IllegalStateException("Failed to obtain Keycloak Admin Access Token");
  }

  public String findUserIdByEmail(String email) {
    try {
      String token = getAdminAccessToken();
      String searchUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users?email=" + email;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<List<Map<String, Object>>> response =
          restTemplate.exchange(
              searchUrl,
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {});

      List<Map<String, Object>> users = response.getBody();
      if (response.getStatusCode().is2xxSuccessful()
          && users != null
          && !users.isEmpty()
          && users.get(0) != null) {
        return (String) users.get(0).get("id");
      }
    } catch (Exception e) {
      log.warn("Failed to find Keycloak user ID by email {}: {}", email, e.getMessage());
    }
    return null;
  }

  public void resetUserPassword(String keycloakUserId, String newPassword) {
    String token = getAdminAccessToken();
    String resetUrl =
        keycloakServerUrl
            + "/admin/realms/"
            + realm
            + "/users/"
            + keycloakUserId
            + "/reset-password";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);

    Map<String, Object> body = Map.of("type", "password", "value", newPassword, "temporary", false);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    restTemplate.exchange(resetUrl, HttpMethod.PUT, entity, Void.class);
    log.info("Successfully reset password in Keycloak for user ID {}", keycloakUserId);
  }

  public String createKeycloakUser(String username, String email, String fullName, String role) {
    try {
      String token = getAdminAccessToken();
      String createUserUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(token);

      String firstName = fullName != null && !fullName.isBlank() ? fullName : username;
      String lastName = "";
      if (fullName != null && fullName.contains(" ")) {
        int lastSpace = fullName.lastIndexOf(' ');
        firstName = fullName.substring(0, lastSpace);
        lastName = fullName.substring(lastSpace + 1);
      }

      Map<String, Object> body =
          Map.of(
              "username", username,
              "email", email,
              "firstName", firstName,
              "lastName", lastName,
              "enabled", true,
              "emailVerified", true);

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
      ResponseEntity<Void> response =
          restTemplate.exchange(createUserUrl, HttpMethod.POST, entity, Void.class);

      if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().value() == 201) {
        log.info("Successfully created Keycloak user {}", username);
        String keycloakId = findUserIdByEmail(email);
        if (keycloakId != null && role != null) {
          assignRoleToUser(keycloakId, role);
        }
        return keycloakId;
      }
    } catch (Exception e) {
      log.warn(
          "Keycloak user creation skipped or failed for username {}: {}", username, e.getMessage());
    }
    return null;
  }

  public void assignRoleToUser(String keycloakUserId, String roleName) {
    try {
      String token = getAdminAccessToken();
      String getRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> entity = new HttpEntity<>(headers);

      ResponseEntity<Map<String, Object>> roleResp =
          restTemplate.exchange(
              getRoleUrl,
              HttpMethod.GET,
              entity,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      Map<String, Object> roleObj = roleResp.getBody();
      if (roleResp.getStatusCode().is2xxSuccessful() && roleObj != null) {
        String assignRoleUrl =
            keycloakServerUrl
                + "/admin/realms/"
                + realm
                + "/users/"
                + keycloakUserId
                + "/role-mappings/realm";

        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        postHeaders.setBearerAuth(token);

        List<Map<String, Object>> body = List.of(roleObj);
        HttpEntity<List<Map<String, Object>>> postEntity = new HttpEntity<>(body, postHeaders);

        restTemplate.exchange(assignRoleUrl, HttpMethod.POST, postEntity, Void.class);
        log.info("Assigned role {} to Keycloak user ID {}", roleName, keycloakUserId);
      }
    } catch (Exception e) {
      log.warn(
          "Failed to assign role {} to Keycloak user {}: {}",
          roleName,
          keycloakUserId,
          e.getMessage());
    }
  }
}
