package com.apu.asc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ApuAscModulithTests {

  ApplicationModules modules = ApplicationModules.of(ApuAscApplication.class);

  @Test
  void verifyModulithArchitecture() {
    modules.forEach(System.out::println);
    modules.verify();
  }

  @Test
  void generateDocumentation() {
    new Documenter(modules)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml()
        .writeModuleCanvases();
  }
}
