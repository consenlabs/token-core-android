package org.consenlabs.tokencore.wallet.model;

import java.util.Objects;

public class KeyPair {

  String privateKey;
  String publicKey;

  public KeyPair() {
  }

  public KeyPair(String privateKey, String publicKey) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KeyPair keyPair = (KeyPair) o;
    return Objects.equals(privateKey, keyPair.privateKey) &&
        Objects.equals(publicKey, keyPair.publicKey);
  }

  @Override
  public int hashCode() {

    return Objects.hash(privateKey, publicKey);
  }
}
