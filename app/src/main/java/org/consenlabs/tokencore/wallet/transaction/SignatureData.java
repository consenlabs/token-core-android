package org.consenlabs.tokencore.wallet.transaction;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;

import java.util.Arrays;

/**
 * Created by xyz on 2018/3/2.
 */
public class SignatureData {
  private final int v;
  private final byte[] r;
  private final byte[] s;

  public SignatureData(int v, byte[] r, byte[] s) {
    this.v = v;
    this.r = r;
    this.s = s;
  }

  public int getV() {
    return v;
  }

  public byte[] getR() {
    return r;
  }

  public byte[] getS() {
    return s;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SignatureData that = (SignatureData) o;

    if (v != that.v) {
      return false;
    }
    if (!Arrays.equals(r, that.r)) {
      return false;
    }
    return Arrays.equals(s, that.s);
  }

  @Override
  public int hashCode() {
    int result = v;
    result = 31 * result + Arrays.hashCode(r);
    result = 31 * result + Arrays.hashCode(s);
    return result;
  }

  @Override
  public String toString() {
    String r = NumericUtil.bytesToHex(getR());
    String s = NumericUtil.bytesToHex(getS());
    return String.format("%s%s%02x", r, s, getV());
  }
}
