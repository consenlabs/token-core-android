package org.consenlabs.tokencore.foundation.crypto;

import junit.framework.Assert;

import static org.junit.Assert.*;

import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by xyz on 2018/2/3.
 */

public class CryptoTest {
  byte[] prvKeyBytes = NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING);
  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Test
  public void testCreatePBKDF2Crypto() {

    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    assertTrue(crypto instanceof PBKDF2Crypto);
    assertEquals(Crypto.CTR, crypto.getCipher());
    assertNotNull(crypto.getCipherparams());
    assertNotNull(crypto.getCipherparams().getIv());
    assertEquals(32, crypto.getCipherparams().getIv().length());

    assertEquals(PBKDF2Crypto.PBKDF2, crypto.getKdf());
    assertTrue(crypto.getKdfparams() instanceof PBKDF2Params);
    PBKDF2Params params = (PBKDF2Params)crypto.getKdfparams();
    assertEquals(PBKDF2Params.C_LIGHT, params.getC());
    assertEquals(PBKDF2Params.PRF, params.getPrf());
    assertEquals(KDFParams.DK_LEN, params.getDklen());
    assertNotNull(params.getSalt());
    assertEquals(64, params.getSalt().length());
    assertNotNull(crypto.getCiphertext());

    assertArrayEquals(prvKeyBytes, crypto.decrypt(SampleKey.PASSWORD));
  }

  @Test
  public void testDerivedKey() {
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    assertFalse(crypto.verifyPassword(SampleKey.WRONG_PASSWORD));
    assertTrue(crypto.verifyPassword(SampleKey.PASSWORD));

    byte[] derivedKey = crypto.generateDerivedKey(SampleKey.PASSWORD.getBytes());
    assertNotNull(derivedKey);
    assertEquals(32, derivedKey.length);
  }

  @Test
  public void testValidate() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.WALLET_INVALID);
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    crypto.validate();
    assertTrue("Should not throw any exception", true);
    crypto.cipher = "not-valid-ctr";
    crypto.validate();
    assertTrue("Shoud return in early statements", false);
  }

  @Test
  public void testDecrypt() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.WALLET_INVALID_PASSWORD);
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    byte[] result = crypto.decrypt(SampleKey.PASSWORD);
    assertArrayEquals(prvKeyBytes, result);

    crypto.decrypt(SampleKey.WRONG_PASSWORD);
    assertTrue("Should return in early statements", false);
  }

  @Test
  public void testDeriveAndDecryptEncPair() {
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    EncPair pair = crypto.deriveEncPair(SampleKey.PASSWORD, SampleKey.MNEMONIC.getBytes());
    assertNotNull(pair);
    assertArrayEquals(SampleKey.MNEMONIC.getBytes(), crypto.decryptEncPair(SampleKey.PASSWORD, pair));

    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.WALLET_INVALID_PASSWORD);
    crypto.decryptEncPair(SampleKey.WRONG_PASSWORD, pair);
    assertTrue("Should return in early statements", false);

  }

  @Test
  public void testDecryptSCryptCrypto() {
    Crypto crypto = Crypto.createSCryptCrypto(SampleKey.PASSWORD, prvKeyBytes);
    assertTrue(crypto instanceof SCryptCrypto);
    assertEquals(Crypto.CTR, crypto.getCipher());
    assertNotNull(crypto.getCipherparams());
    assertNotNull(crypto.getCipherparams().getIv());
    assertEquals(32, crypto.getCipherparams().getIv().length());

    assertEquals(SCryptCrypto.SCRYPT, crypto.getKdf());
    assertTrue(crypto.getKdfparams() instanceof SCryptParams);

    SCryptParams params = (SCryptParams)crypto.getKdfparams();
    assertEquals(SCryptParams.COST_FACTOR, params.getN());
    assertEquals(SCryptParams.BLOCK_SIZE_FACTOR, params.getR());
    assertEquals(SCryptParams.PARALLELIZATION_FACTOR, params.getP());
    assertEquals(KDFParams.DK_LEN, params.getDklen());
    assertNotNull(params.getSalt());
    assertEquals(64, params.getSalt().length());
    assertNotNull(crypto.getCiphertext());

    assertArrayEquals(prvKeyBytes, crypto.decrypt(SampleKey.PASSWORD));


  }

  @Test
  public void testCreateAndDestroyCacheDerivedKey() {
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    Assert.assertNull(crypto.getCachedDerivedKey());
    crypto = Crypto.createPBKDF2CryptoWithKDFCached(SampleKey.PASSWORD, prvKeyBytes);
    Assert.assertNotNull(crypto.getCachedDerivedKey());
    crypto.clearCachedDerivedKey();
    Assert.assertNull(crypto.getCachedDerivedKey());

    crypto.cacheDerivedKey(SampleKey.PASSWORD);
    // This function can reenter
    crypto.cacheDerivedKey(SampleKey.PASSWORD);
    Assert.assertNotNull(crypto.getCachedDerivedKey());
    crypto.clearCachedDerivedKey();
    // This function can reenter
    crypto.clearCachedDerivedKey();
    Assert.assertNull(crypto.getCachedDerivedKey());
  }

  @Test
  public void testVerifyPasswordCachedDerivedKey() {

    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    try {
      crypto.cacheDerivedKey(SampleKey.WRONG_PASSWORD);
      Assert.fail("Should throw invalid password");
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains(Messages.WALLET_INVALID_PASSWORD));
    }

    try {
      crypto.cacheDerivedKey(SampleKey.PASSWORD);
      crypto.cacheDerivedKey(SampleKey.WRONG_PASSWORD);
      Assert.fail("Although it caches the derivedKey, it should throw 'wrong password' when enter the wrong password");
    } catch (Exception ex) {
      Assert.assertTrue(ex.getMessage().contains(Messages.WALLET_INVALID_PASSWORD));
    }

  }

  @Test(timeout = 1000)
  public void testCacheIsInUsedWhenCreateWithCache() {
    Crypto crypto = Crypto.createPBKDF2CryptoWithKDFCached(SampleKey.PASSWORD, prvKeyBytes);
    long begin = System.currentTimeMillis();
    for (int i=0; i< 1000; i++) {
      crypto.verifyPassword(SampleKey.PASSWORD);
      crypto.decrypt(SampleKey.PASSWORD);
      EncPair encPair = crypto.deriveEncPair(SampleKey.PASSWORD, "imToken".getBytes());
      crypto.decryptEncPair(SampleKey.PASSWORD, encPair);
    }
    System.out.println(String.format("It takes %d ms to test 1000 times", System.currentTimeMillis() - begin));
  }

  @Test(timeout = 1000)
  public void testCacheIsInUsed() {
    Crypto crypto = Crypto.createPBKDF2Crypto(SampleKey.PASSWORD, prvKeyBytes);
    crypto.cacheDerivedKey(SampleKey.PASSWORD);
    long begin = System.currentTimeMillis();
    for (int i=0; i< 1000; i++) {
      crypto.verifyPassword(SampleKey.PASSWORD);
      crypto.decrypt(SampleKey.PASSWORD);
      EncPair encPair = crypto.deriveEncPair(SampleKey.PASSWORD, "imToken".getBytes());
      crypto.decryptEncPair(SampleKey.PASSWORD, encPair);
    }

    System.out.println(String.format("It takes %d ms to test 1000 times", System.currentTimeMillis() - begin));
  }


}
