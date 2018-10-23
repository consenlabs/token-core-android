package org.consenlabs.tokencore.foundation.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

  public enum AESType {
    CTR, CBC
  }

  public static byte[] encryptByCTR(byte[] data, byte[] key, byte[] iv) {
    return doAES(data, key, iv, Cipher.ENCRYPT_MODE, AESType.CTR, "PKCS5Padding");
  }

  public static byte[] decryptByCTR(byte[] ciphertext, byte[] key, byte[] iv) {
    return doAES(ciphertext, key, iv, Cipher.DECRYPT_MODE, AESType.CTR, "PKCS5Padding");
  }

  public static byte[] encryptByCBC(byte[] data, byte[] key, byte[] iv) {
    return doAES(data, key, iv, Cipher.ENCRYPT_MODE, AESType.CBC, "PKCS5Padding");
  }

  public static byte[] decryptByCBC(byte[] ciphertext, byte[] key, byte[] iv) {
    return doAES(ciphertext, key, iv, Cipher.DECRYPT_MODE, AESType.CBC, "PKCS5Padding");
  }

  public static byte[] encryptByCTRNoPadding(byte[] data, byte[] key, byte[] iv) {
    return doAES(data, key, iv, Cipher.ENCRYPT_MODE, AESType.CTR, "NoPadding");
  }

  public static byte[] decryptByCTRNoPadding(byte[] ciphertext, byte[] key, byte[] iv) {
    return doAES(ciphertext, key, iv, Cipher.DECRYPT_MODE, AESType.CTR, "NoPadding");
  }

  public static byte[] encryptByCBCNoPadding(byte[] data, byte[] key, byte[] iv) {
    return doAES(data, key, iv, Cipher.ENCRYPT_MODE, AESType.CBC, "NoPadding");
  }

  public static byte[] decryptByCBCNoPadding(byte[] ciphertext, byte[] key, byte[] iv) {
    return doAES(ciphertext, key, iv, Cipher.DECRYPT_MODE, AESType.CBC, "NoPadding");
  }

  private static byte[] doAES(byte[] data, byte[] key, byte[] iv, int cipherMode, AESType type, String paddingType) {
    String aesType;
    if (type == AESType.CBC) {
      aesType = "CBC";
    } else {
      aesType = "CTR";
    }
    try {
      IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
      SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

      String algorithmDesc = String.format("AES/%s/%s", aesType, paddingType);
      Cipher cipher = Cipher.getInstance(algorithmDesc);
      cipher.init(cipherMode, secretKeySpec, ivParameterSpec);
      return cipher.doFinal(data);
    } catch (Exception ignored) {
    }
    return new byte[0];
  }

}
