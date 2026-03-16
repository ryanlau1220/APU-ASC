package com.apu.asc.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationUtil {

  private static final Validator VALIDATOR;

  static {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    VALIDATOR = factory.getValidator();
  }

  private ValidationUtil() {}

  public static <T> void validate(T object) {
    Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
    if (!violations.isEmpty()) {
      String messages =
          violations.stream()
              .map(v -> I18n.resolveError(v.getMessage()))
              .collect(Collectors.joining(", "));
      throw new IllegalArgumentException(messages);
    }
  }

  public static <T> String getViolations(T object) {
    Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
    if (violations.isEmpty()) return null;
    return violations.stream()
        .map(v -> I18n.resolveError(v.getMessage()))
        .collect(Collectors.joining(", "));
  }
}
