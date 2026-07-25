package com.apu.asc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScalarDocConfig {

  @Bean
  public OpenAPI apuAscOpenAPI() {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .info(
            new Info()
                .title("APU Automotive Service Centre (APU-ASC) API")
                .description("Automotive Service Management RESTful API Specifications")
                .version("1.0.0")
                .contact(new Contact().name("APU-ASC Support").email("support@apu-asc.com")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
