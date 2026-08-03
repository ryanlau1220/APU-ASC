package com.apu.asc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApuAscApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApuAscApplication.class, args);
  }
}
