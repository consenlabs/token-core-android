package org.consenlabs.tokencore.foundation.crypto;

import org.consenlabs.tokencore.foundation.utils.CachedDerivedKey;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Strings;

import java.util.Arrays;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kdf")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SCryptCrypto.class, name = "scrypt"),
    @JsonSubTypes.Type(value = PBKDF2Crypto.class, name = "pbkdf2")
})
public class Crypto<T extends KDFParams> {
  static final String CTR = "aes-128-ctr";
  static final String CBC = "aes-128-cbc";

  static final int IV_LENGTH = 16;
  static final int SALT_LENGTH = 32;

  private String ciphertext;
  private String mac;
  String cipher;
  private CipherParams cipherparams;

  /**
   * !!! This function is used for testcase, and do not call this in other places;
   *
   * @return
   */
  CachedDerivedKey getCachedDerivedKey() {
    return cachedDerivedKey;
  }

  @JsonIgnore
  private CachedDerivedKey cachedDerivedKey;

  private void setCachedDerivedKey(CachedDerivedKey cachedDerivedKey) {
    this.cachedDerivedKey = cachedDerivedKey;
  }

  String kdf;
  T kdfparams;

  public static Crypto createPBKDF2Crypto(String password, byte[] origin) {
    return createCrypto(password, origin, PBKDF2Crypto.PBKDF2, false);
  }


  public static Crypto createPBKDF2CryptoWithKDFCached(String password, byte[] origin) {
    return createCrypto(password, origin, PBKDF2Crypto.PBKDF2, true);
  }


  public static Crypto createSCryptCrypto(String password, byte[] origin) {
    return createCrypto(password, origin, SCryptCrypto.SCRYPT, false);
  }


  private static Crypto createCrypto(String password, byte[] origin, String kdfType, boolean isCached) {
    Crypto crypto = PBKDF2Crypto.PBKDF2.equals(kdfType) ? PBKDF2Crypto.createPBKDF2Crypto() : SCryptCrypto.createSCryptCrypto();

    crypto.setCipher(CTR);
    byte[] iv = NumericUtil.generateRandomBytes(IV_LENGTH);
    CipherParams cipherparams = new CipherParams();
    cipherparams.setIv(NumericUtil.bytesToHex(iv));
    crypto.setCipherparams(cipherparams);

    byte[] derivedKey = crypto.getValidDerivedKey(password);

    if (isCached) {
      crypto.setCachedDerivedKey(new CachedDerivedKey(password, derivedKey));
    }

    byte[] encrypted = crypto.encrypt(derivedKey, iv, origin);

    crypto.ciphertext = NumericUtil.bytesToHex(encrypted);

    byte[] mac = Hash.generateMac(derivedKey, encrypted);
    crypto.mac = NumericUtil.bytesToHex(mac);

    return crypto;
  }

  Crypto() {
  }

  public boolean verifyPassword(String password) {
    try {
      getCachedDerivedKey(password);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public void cacheDerivedKey(String password) {
    byte[] derivedKey = getValidDerivedKey(password);
    this.cachedDerivedKey = new CachedDerivedKey(password, derivedKey);
  }

  private byte[] getCachedDerivedKey(String password) {
    if (cachedDerivedKey != null) {
      byte[] derivedKey = cachedDerivedKey.getDerivedKey(password);
      if (derivedKey != null) {
        return derivedKey;
      }
    }

    return getValidDerivedKey(password);
  }

  public void clearCachedDerivedKey() {
    this.cachedDerivedKey = null;
  }

  private byte[] getValidDerivedKey(String password) {
    byte[] derivedKey = generateDerivedKey(password.getBytes());
    if (this.mac == null) return derivedKey;
    byte[] mac = NumericUtil.hexToBytes(this.mac);
    byte[] cipherText = NumericUtil.hexToBytes(getCiphertext());

    byte[] derivedMac = Hash.generateMac(derivedKey, cipherText);
    if (Arrays.equals(derivedMac, mac)) {
      return derivedKey;
    } else {
      throw new TokenException(Messages.WALLET_INVALID_PASSWORD);
    }
  }

  public void validate() {
    if ((!CTR.equals(cipher) && !CBC.equals(cipher)) || cipherparams == null
        || Strings.isNullOrEmpty(mac) || Strings.isNullOrEmpty(ciphertext)
        || kdfparams == null) {
      throw new TokenException(Messages.WALLET_INVALID);
    }

    cipherparams.validate();
    kdfparams.validate();

  }

  byte[] generateDerivedKey(byte[] password) {
    throw new UnsupportedOperationException("You invoke the not implement method");
  }

  private byte[] encrypt(byte[] derivedKey, byte[] iv, byte[] text) {

    byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);

    if (CTR.equals(cipher)) {
      return AES.encryptByCTRNoPadding(text, encryptKey, iv);
    } else {
      return AES.encryptByCBCNoPadding(text, encryptKey, iv);
    }
  }

  public byte[] decrypt(String password) {
    byte[] derivedKey = getCachedDerivedKey(password);
    byte[] iv = NumericUtil.hexToBytes(this.getCipherparams().getIv());
    byte[] encrypted = NumericUtil.hexToBytes(this.getCiphertext());

    return decrypt(derivedKey, iv, encrypted);
  }

  private byte[] decrypt(byte[] derivedKey, byte[] iv, byte[] text) {
    byte[] encryptKey = Arrays.copyOfRange(derivedKey, 0, 16);

    if (CTR.equals(cipher)) {
      return AES.decryptByCTRNoPadding(text, encryptKey, iv);
    } else {
      return AES.decryptByCBCNoPadding(text, encryptKey, iv);
    }
  }

  public EncPair deriveEncPair(String password, byte[] origin) {
    byte[] derivedKey = getCachedDerivedKey(password);
    EncPair encPair = new EncPair();
    byte[] iv = NumericUtil.generateRandomBytes(16);
    byte[] encrypted = this.encrypt(derivedKey, iv, origin);

    encPair.setEncStr(NumericUtil.bytesToHex(encrypted));
    encPair.setNonce(NumericUtil.bytesToHex(iv));

    return encPair;
  }

  public byte[] decryptEncPair(String password, EncPair encPair) {
    byte[] derivedKey = getCachedDerivedKey(password);
    byte[] iv = NumericUtil.hexToBytes(encPair.getNonce());
    return decrypt(derivedKey, iv, NumericUtil.hexToBytes(encPair.getEncStr()));
  }

  @JsonProperty(required = true)
  public String getCiphertext() {
    return ciphertext;
  }

  @JsonProperty(required = true)
  public CipherParams getCipherparams() {
    return cipherparams;
  }

  @JsonProperty(required = true)
  public String getMac() {
    return mac;
  }

  @JsonProperty(required = true)
  public String getCipher() {
    return cipher;
  }

  @JsonProperty(required = true)
  public String getKdf() {
    return kdf;
  }

  @JsonProperty(required = true)
  public T getKdfparams() {
    return kdfparams;
  }

  public void setCiphertext(String ciphertext) {
    this.ciphertext = ciphertext;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public void setCipher(String cipher) {
    this.cipher = cipher;
  }

  public void setCipherparams(CipherParams cipherparams) {
    this.cipherparams = cipherparams;
  }

  public void setKdf(String kdf) {
    this.kdf = kdf;
  }

  public void setKdfparams(T kdfparams) {
    this.kdfparams = kdfparams;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Crypto)) {
      return false;
    }

    Crypto that = (Crypto) o;

    if (cipher != null
        ? !cipher.equals(that.cipher)
        : that.cipher != null) {
      return false;
    }
    if (getCiphertext() != null
        ? !getCiphertext().equals(that.getCiphertext())
        : that.getCiphertext() != null) {
      return false;
    }
    if (getCipherparams() != null
        ? !getCipherparams().equals(that.getCipherparams())
        : that.getCipherparams() != null) {
      return false;
    }
    if (kdf != null
        ? !kdf.equals(that.kdf)
        : that.kdf != null) {
      return false;
    }
    if (this.kdfparams != null
        ? !this.kdfparams.equals(that.kdfparams)
        : that.kdfparams != null) {
      return false;
    }
    return mac != null
        ? mac.equals(that.mac) : that.mac == null;
  }

  @Override
  public int hashCode() {
    int result = cipher != null ? cipher.hashCode() : 0;
    result = 31 * result + (getCiphertext() != null ? getCiphertext().hashCode() : 0);
    result = 31 * result + (getCipherparams() != null ? getCipherparams().hashCode() : 0);
    result = 31 * result + (kdf != null ? kdf.hashCode() : 0);
    result = 31 * result + (this.kdfparams != null ? this.kdfparams.hashCode() : 0);
    result = 31 * result + (mac != null ? mac.hashCode() : 0);
    return result;
  }

}
