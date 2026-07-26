package com.apu.asc.common;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

  @GetMapping("/")
  public Map<String, Object> getRootStatus() {
    return Map.of(
        "service", "APU Automotive Service Centre API",
        "status", "UP",
        "version", "1.0.0",
        "documentation", "/scalar",
        "health", "/actuator/health");
  }
}
