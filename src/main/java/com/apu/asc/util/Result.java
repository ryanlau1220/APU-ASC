package com.apu.asc.util;

/**
 * A simple discriminated union that carries either a success value or a human-readable error
 * message. Service methods return {@code Result<T>} instead of {@code null} so that callers can
 * surface the exact reason for a failure in the UI.
 *
 * <pre>{@code
 * Result<User> result = authService.login(username, password);
 * if (result.isSuccess()) {
 *     User user = result.getValue();
 * } else {
 *     JOptionPane.showMessageDialog(this, result.getError());
 * }
 * }</pre>
 */
public final class Result<T> {

  private final T value;
  private final String error;

  private Result(T value, String error) {
    this.value = value;
    this.error = error;
  }

  /** Creates a successful result carrying {@code value}. */
  public static <T> Result<T> success(T value) {
    return new Result<>(value, null);
  }

  /** Creates a failure result carrying a human-readable {@code error} message. */
  public static <T> Result<T> failure(String error) {
    return new Result<>(null, error);
  }

  /** Returns {@code true} if this result represents a success. */
  public boolean isSuccess() {
    return error == null;
  }

  /**
   * Returns the success value, or {@code null} if this is a failure. Always check {@link
   * #isSuccess()} before calling this.
   */
  public T getValue() {
    return value;
  }

  /**
   * Returns the error message, or {@code null} if this is a success. Always check {@link
   * #isSuccess()} before calling this.
   */
  public String getError() {
    return error;
  }
}
