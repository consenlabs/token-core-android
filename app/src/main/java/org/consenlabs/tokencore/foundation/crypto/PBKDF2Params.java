package org.consenlabs.tokencore.foundation.crypto;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

/**
 * Created by xyz on 2018/2/2.
 */
public class PBKDF2Params implements KDFParams {
  static final String PRF = "hmac-sha256";
  static final int C_LIGHT = 10240;

  private int dklen = 0;
  private int c = 0;
  private String prf = "";
  private String salt;

  public PBKDF2Params() {
  }

  public static PBKDF2Params createPBKDF2Params() {
    PBKDF2Params params = new PBKDF2Params();
    params.dklen = DK_LEN;
    params.c = C_LIGHT;
    params.prf = PRF;
    return params;
  }

  public int getDklen() {
    return dklen;
  }

  public void setDklen(int dklen) {
    this.dklen = dklen;
  }

  public int getC() {
    return c;
  }

  public void setC(int c) {
    this.c = c;
  }

  public String getPrf() {
    return prf;
  }

  public void setPrf(String prf) {
    this.prf = prf;
  }

  public String getSalt() {
    return salt;
  }

  @Override
  public void validate() {
    if (dklen == 0 || c == 0 || Strings.isNullOrEmpty(salt) || Strings.isNullOrEmpty(prf)) {
      throw new TokenException(Messages.KDF_PARAMS_INVALID);
    }
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PBKDF2Params)) {
      return false;
    }

    PBKDF2Params that = (PBKDF2Params) o;

    if (dklen != that.dklen) {
      return false;
    }
    if (c != that.c) {
      return false;
    }
    if (getPrf() != null
        ? !getPrf().equals(that.getPrf())
        : that.getPrf() != null) {
      return false;
    }
    return getSalt() != null
        ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
  }

  @Override
  public int hashCode() {
    int result = dklen;
    result = 31 * result + c;
    result = 31 * result + (getPrf() != null ? getPrf().hashCode() : 0);
    result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
    return result;
  }
}
