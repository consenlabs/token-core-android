package org.consenlabs.tokencore.wallet.model;

public class TokenException extends RuntimeException {
  private static final long serialVersionUID = 4300404932829403534L;

  public TokenException(String message) {
    super(message);
  }

  public TokenException(String message, Exception e) {
    super(message, e);
  }

}
