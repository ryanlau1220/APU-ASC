package com.apu.asc.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String resourceName, String id) {
    super(resourceName + " not found with ID: " + id);
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
