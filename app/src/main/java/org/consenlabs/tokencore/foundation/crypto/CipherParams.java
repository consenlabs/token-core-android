package org.consenlabs.tokencore.foundation.crypto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;

import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

/**
 * Created by xyz on 2018/2/3.
 */
public class CipherParams {
  private String iv;

  CipherParams() {
  }

  public String getIv() {
    return iv;
  }

  public void setIv(String iv) {
    this.iv = iv;
  }

  @JsonIgnore
  public void validate() {
    if (Strings.isNullOrEmpty(iv)) {
      throw new TokenException(Messages.CIPHER_FAIL);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof CipherParams)) {
      return false;
    }

    CipherParams that = (CipherParams) o;

    return getIv() != null
        ? getIv().equals(that.getIv()) : that.getIv() == null;
  }

  @Override
  public int hashCode() {
    return getIv() != null ? getIv().hashCode() : 0;
  }
}
