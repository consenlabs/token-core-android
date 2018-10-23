package org.consenlabs.tokencore.wallet.keystore;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.consenlabs.tokencore.foundation.crypto.Crypto;

import java.util.UUID;

/**
 * Created by xyz on 2018/2/5.
 */

public abstract class Keystore {
  String id;
  int version;

  Crypto crypto;

  public Keystore() {
    this.id = UUID.randomUUID().toString();
  }
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public Crypto getCrypto() {
    return crypto;
  }

  public void setCrypto(Crypto crypto) {
    this.crypto = crypto;
  }

  public boolean verifyPassword(String password) {
    return getCrypto().verifyPassword(password);
  }

  public byte[] decryptCiphertext(String password) {
    return getCrypto().decrypt(password);
  }
}






