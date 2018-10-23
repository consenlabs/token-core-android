package org.consenlabs.tokencore.foundation.utils;

import java.util.Arrays;

public class ByteUtil {
  private static byte[] trimLeadingBytes(byte[] bytes, byte b) {
    int offset = 0;
    for (; offset < bytes.length - 1; offset++) {
      if (bytes[offset] != b) {
        break;
      }
    }
    return Arrays.copyOfRange(bytes, offset, bytes.length);
  }

  public static byte[] trimLeadingZeroes(byte[] bytes) {
    return trimLeadingBytes(bytes, (byte) 0);
  }

  public static byte[] concat(byte[] b1, byte[] b2) {
    byte[] result = Arrays.copyOf(b1, b1.length + b2.length);
    System.arraycopy(b2, 0, result, b1.length, b2.length);
    return result;
  }
}
