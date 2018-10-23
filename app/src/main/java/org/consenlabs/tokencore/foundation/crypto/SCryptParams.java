package org.consenlabs.tokencore.foundation.crypto;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

/**
 * Created by xyz on 2018/2/2.
 */
public class SCryptParams implements KDFParams {
  static final int COST_FACTOR = 8192;
  static final int BLOCK_SIZE_FACTOR = 8;
  static final int PARALLELIZATION_FACTOR = 1;
  private int dklen = 0;
  private int n = 0;
  private int p = 0;
  private int r = 0;
  private String salt;

  public SCryptParams() {
  }

  public static SCryptParams createSCryptParams() {
    SCryptParams params = new SCryptParams();
    params.dklen = DK_LEN;
    params.n = COST_FACTOR;
    params.p = PARALLELIZATION_FACTOR;
    params.r = BLOCK_SIZE_FACTOR;
    return params;
  }

  public int getDklen() {
    return dklen;
  }

  public void setDklen(int dklen) {
    this.dklen = dklen;
  }

  public int getN() {
    return n;
  }

  public void setN(int n) {
    this.n = n;
  }

  public int getP() {
    return p;
  }

  public void setP(int p) {
    this.p = p;
  }

  public int getR() {
    return r;
  }

  public void setR(int r) {
    this.r = r;
  }

  public String getSalt() {
    return salt;
  }

  @Override
  public void validate() {
    if (n == 0 || dklen == 0 || p == 0 ||  r == 0 || Strings.isNullOrEmpty(salt)) {
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
    if (!(o instanceof SCryptParams)) {
      return false;
    }

    SCryptParams that = (SCryptParams) o;

    if (dklen != that.dklen) {
      return false;
    }
    if (n != that.n) {
      return false;
    }
    if (p != that.p) {
      return false;
    }
    if (r != that.r) {
      return false;
    }
    return getSalt() != null
        ? getSalt().equals(that.getSalt()) : that.getSalt() == null;
  }

  @Override
  public int hashCode() {
    int result = dklen;
    result = 31 * result + n;
    result = 31 * result + p;
    result = 31 * result + r;
    result = 31 * result + (getSalt() != null ? getSalt().hashCode() : 0);
    return result;
  }
}
