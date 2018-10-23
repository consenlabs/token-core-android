package org.consenlabs.tokencore.foundation.utils;

import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xyz on 2018/2/1.
 */

public class MnemonicUtilTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testValidateMnemonic() {
    try {
      List<String> mnemonic = Arrays.asList(SampleKey.MNEMONIC.split(" "));
      MnemonicUtil.validateMnemonics(mnemonic);
      assertTrue(true);
    } catch (TokenException ex) {
      fail("Should not throw any exception");
    }

  }

  @Test
  public void testMnemonicShortLengthException() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.MNEMONIC_INVALID_LENGTH);

    String test = "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo wrong";
    List<String> mnemonic = Arrays.asList(test.split(" "));
    MnemonicUtil.validateMnemonics(mnemonic);
    fail("Should throw a exception");
  }

  @Test
  public void testMnemonicLongLengthException() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.MNEMONIC_INVALID_LENGTH);

    String test = "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo wrong";
    List<String> mnemonic = Arrays.asList(test.split(" "));
    MnemonicUtil.validateMnemonics(mnemonic);
    fail("Should throw a exception");
  }

  @Test
  public void testMnemonicBadWordException() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.MNEMONIC_BAD_WORD);
    // "hot" not in english mnemonic word list
    String test = "hot zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo wrong";
    List<String> mnemonic = Arrays.asList(test.split(" "));
    MnemonicUtil.validateMnemonics(mnemonic);
    fail("Should throw a exception");
  }

  @Test
  public void testMnemonicBadChecksumException() {
    thrown.expect(TokenException.class);
    thrown.expectMessage(Messages.MNEMONIC_CHECKSUM);
    // "hot" not in english mnemonic word list
    String test = "zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo zoo";
    List<String> mnemonic = Arrays.asList(test.split(" "));
    MnemonicUtil.validateMnemonics(mnemonic);
    fail("Should throw a exception");
  }

  @Test
  public void testRandomMnemonicCodes() {
    List<String> mnemonic = MnemonicUtil.randomMnemonicCodes();
    assertEquals("Should be 12 words", 12, mnemonic.size());
    MnemonicUtil.validateMnemonics(mnemonic);
    assertTrue("Should valid", true);
  }

}
