package com.apu.asc.util;

import com.apu.asc.util.validation.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ValidationUtil {

  private ValidationUtil() {}

  public static <T> void validate(T object) {
    List<String> violations = getViolationsList(object);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(String.join(", ", violations));
    }
  }

  public static <T> String getViolations(T object) {
    List<String> violations = getViolationsList(object);
    if (violations.isEmpty()) return null;
    return String.join(", ", violations);
  }

  private static <T> List<String> getViolationsList(T object) {
    List<String> violations = new ArrayList<>();
    if (object == null) return violations;

    Class<?> clazz = object.getClass();
    while (clazz != null && clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        field.setAccessible(true);
        try {
          Object value = field.get(object);

          if (field.isAnnotationPresent(NotNull.class)) {
            if (value == null) {
              violations.add(I18n.resolveError(field.getAnnotation(NotNull.class).message()));
            }
          }

          if (field.isAnnotationPresent(NotBlank.class)) {
            if (value == null || value.toString().trim().isEmpty()) {
              violations.add(I18n.resolveError(field.getAnnotation(NotBlank.class).message()));
            }
          }

          if (field.isAnnotationPresent(Email.class) && value != null) {
            String email = value.toString();
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
              violations.add(I18n.resolveError(field.getAnnotation(Email.class).message()));
            }
          }

          if (field.isAnnotationPresent(Pattern.class) && value != null) {
            Pattern pattern = field.getAnnotation(Pattern.class);
            if (!value.toString().matches(pattern.regexp())) {
              violations.add(I18n.resolveError(pattern.message()));
            }
          }

          if (field.isAnnotationPresent(Size.class) && value != null) {
            Size size = field.getAnnotation(Size.class);
            int length = value.toString().length();
            if (length < size.min() || length > size.max()) {
              violations.add(I18n.resolveError(size.message()));
            }
          }

          if (field.isAnnotationPresent(DecimalMin.class) && value != null) {
            DecimalMin min = field.getAnnotation(DecimalMin.class);
            BigDecimal minVal = new BigDecimal(min.value());
            BigDecimal actualVal = null;
            if (value instanceof BigDecimal) actualVal = (BigDecimal) value;
            else if (value instanceof Number) actualVal = new BigDecimal(value.toString());
            
            if (actualVal != null && actualVal.compareTo(minVal) < 0) {
              violations.add(I18n.resolveError(min.message()));
            }
          }

          if (field.isAnnotationPresent(FutureOrPresent.class) && value != null) {
            if (value instanceof LocalDateTime) {
              if (((LocalDateTime) value).isBefore(LocalDateTime.now())) {
                violations.add(I18n.resolveError(field.getAnnotation(FutureOrPresent.class).message()));
              }
            } else if (value instanceof LocalDate) {
              if (((LocalDate) value).isBefore(LocalDate.now())) {
                violations.add(I18n.resolveError(field.getAnnotation(FutureOrPresent.class).message()));
              }
            }
          }

          if (field.isAnnotationPresent(Min.class) && value != null) {
            long min = field.getAnnotation(Min.class).value();
            if (value instanceof Number && ((Number) value).longValue() < min) {
              violations.add(I18n.resolveError(field.getAnnotation(Min.class).message()));
            }
          }

          if (field.isAnnotationPresent(Max.class) && value != null) {
            long max = field.getAnnotation(Max.class).value();
            if (value instanceof Number && ((Number) value).longValue() > max) {
              violations.add(I18n.resolveError(field.getAnnotation(Max.class).message()));
            }
          }

        } catch (IllegalAccessException e) {
          // Ignore
        }
      }
      clazz = clazz.getSuperclass();
    }
    return violations;
  }
}
