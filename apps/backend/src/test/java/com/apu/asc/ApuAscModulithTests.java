package com.apu.asc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ApuAscModulithTests {

  @Test
  void verifyModulithArchitecture() {
    ApplicationModules modules = ApplicationModules.of(ApuAscApplication.class);
    modules.forEach(System.out::println);
    modules.verify();
  }
}
