package org.consenlabs.tokencore.foundation.utils;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.foundation.crypto.Hash;

public class CachedDerivedKey {
  private String hashedPassword;
  private byte[] derivedKey;


  public CachedDerivedKey(String password, byte[] derivedKey) {
    this.hashedPassword = hash(password);
    this.derivedKey = derivedKey;
  }

  private String hash(String password) {
    return NumericUtil.bytesToHex(Hash.sha256(Hash.sha256(password.getBytes())));
  }

  public byte[] getDerivedKey(String password) {
    if (hashedPassword != null && hashedPassword.equals(hash(password))) {
      return derivedKey;
    }
    return null;
  }
}
