package com.sinnerschrader.friendlycaptcha.exception;

import lombok.Getter;

@Getter
public class FRCSolutionValidatorException extends Exception {
  private final int statusCode;

  public FRCSolutionValidatorException(String message, int statusCode) {
    super(String.format("%s: %s", statusCode, message));
    this.statusCode = statusCode;
  }
}
