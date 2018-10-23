package org.consenlabs.tokencore.wallet;

import junit.framework.Assert;

import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.Network;
import org.junit.Test;
import org.bitcoinj.core.ECKey;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/*
  Old Testcases
 */
public class WalletManagerTest extends WalletSupport {

  static String V3Json = "{\n" +
      "  \"version\": 3,\n" +
      "  \"id\": \"5896e547-edb6-47d1-b960-b793624198e5\",\n" +
      "  \"address\": \"ca3b0795a42b46be755ce282924d17024c70fd88\",\n" +
      "  \"Crypto\": {\n" +
      "    \"ciphertext\": \"19a9dc09327a9b4114bf359924ff39093c0f3ebf0993e1c42821e5f1813cedbe\",\n" +
      "    \"cipherparams\": {\n" +
      "      \"iv\": \"691d7876d449e9f66ed3938e4a3635d7\"\n" +
      "    },\n" +
      "    \"cipher\": \"aes-128-ctr\",\n" +
      "    \"kdf\": \"scrypt\",\n" +
      "    \"kdfparams\": {\n" +
      "      \"dklen\": 32,\n" +
      "      \"salt\": \"87832e1217a1ec18bed3563fce0866711557b3f7365f51b6b5e92ff327ddb83a\",\n" +
      "      \"n\": 8192,\n" +
      "      \"r\": 8,\n" +
      "      \"p\": 1\n" +
      "    },\n" +
      "    \"mac\": \"e743fdf489e22eaa3c3f84f02f546d37d39ecabc1793ae45bc6b08c6104ff3a8\"\n" +
      "  }\n" +
      "}";
  private final String mPassword = "imToken2018";

  String address = "ca3b0795a42b46be755ce282924d17024c70fd88";

  @Test
  public void exportPrivateKey() {
    Metadata metadata = new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint");
    metadata.setSource(Metadata.FROM_PRIVATE);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, SampleKey.PRIVATE_KEY_STRING, SampleKey.PASSWORD, true);
    assertEquals(WalletManager.exportPrivateKey(wallet.getId(), SampleKey.PASSWORD), SampleKey.PRIVATE_KEY_STRING);

    metadata = new Metadata(ChainType.BITCOIN, Network.MAINNET, "name", "passwordHint");
    metadata.setSource(Metadata.FROM_WIF);
    wallet = WalletManager.importWalletFromPrivateKey(metadata, SampleKey.PRIVATE_KEY_WIF, SampleKey.PASSWORD, true);
    assertEquals(WalletManager.exportPrivateKey(wallet.getId(), SampleKey.PASSWORD), SampleKey.PRIVATE_KEY_WIF);
    wallet.delete(SampleKey.PASSWORD);
  }

  @Test
  public void importPrivateKey() {
    Wallet wallet = WalletManager.importWalletFromPrivateKey(new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        SampleKey.PRIVATE_KEY_STRING,
        SampleKey.PASSWORD, true);

    ECKey key = ECKey.fromPrivate(wallet.decryptMainKey(SampleKey.PASSWORD));
    assertEquals(
        NumericUtil.bigIntegerToHex(key.getPrivKey()),
        SampleKey.PRIVATE_KEY_STRING);

    assertNotEquals(wallet.verifyPassword("benn"), true);
    assertEquals(wallet.getAddress(), SampleKey.ADDRESS);
    wallet.delete(SampleKey.PASSWORD);
  }


  @Test
  public void importKeyStore() {
    Wallet wallet = WalletManager.importWalletFromKeystore(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        V3Json, mPassword, true);

    assertEquals(wallet.verifyPassword(SampleKey.PASSWORD), false);
    assertEquals(wallet.verifyPassword(mPassword), true);

    assertEquals(wallet.getAddress(), address);
    assertEquals(wallet.hasMnemonic(), false);
    wallet.delete(mPassword);
  }


  @Test
  public void exportKeystore() {
    Wallet wallet = WalletManager.importWalletFromKeystore(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        V3Json, mPassword, true);

    String t = WalletManager.exportKeystore(wallet.getId(), mPassword);

    assertThat(t, not(containsString("meta")));
    assertThat(t, containsString("ciphertext"));
    wallet.delete(mPassword);
  }

  @Test
  public void findWalletByKeystore() {
    Assert.assertNull(WalletManager.findWalletByKeystore(ChainType.ETHEREUM, V3Json, mPassword));
    Wallet wallet = WalletManager.importWalletFromKeystore(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        V3Json, mPassword, true);
    Assert.assertNotNull(WalletManager.findWalletByKeystore(ChainType.ETHEREUM, V3Json, mPassword));
    wallet.delete(SampleKey.PASSWORD);
  }


  @Test
  public void importMnemonic() {
    String m = "victory truck motion urge loyal supply pipe ship assume code where hub";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        m, BIP44Util.ETHEREUM_PATH, SampleKey.PASSWORD, true);

    assertEquals(wallet.getAddress(), "30ee43ceea07f72511eb8b94eb1cacc4e21f0239");
    assertEquals(wallet.hasMnemonic(), true);

    m = "attack curtain frog balcony trash base twenty history cradle cruel illness attitude";
    wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint"),
        m, BIP44Util.BITCOIN_TESTNET_PATH, SampleKey.PASSWORD, true);

    assertEquals(wallet.getAddress(), "ms8xH9qe3rg4DPHJty5baTT8teTMQNsZFT");
    assertEquals(wallet.hasMnemonic(), true);
    wallet.delete(SampleKey.PASSWORD);

    try {
      m = "attack curtain frog balcony trash base twenty history cradle cruel illness attitud";
      wallet = WalletManager.importWalletFromMnemonic(
          new Metadata(ChainType.BITCOIN, Network.MAINNET, "name", "passwordHint"),
          m, BIP44Util.BITCOIN_MAINNET_PATH, SampleKey.PASSWORD, true);

      assertEquals("1", "2");
    } catch (Exception ex) {
      assertEquals(ex.getMessage(), Messages.MNEMONIC_BAD_WORD);
    }
  }

  @Test
  public void testMnemonicOtherAddress() {
    String m;
    m = "attack curtain frog balcony trash base twenty history cradle cruel illness attitude";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint"),
        m, BIP44Util.BITCOIN_TESTNET_PATH, SampleKey.PASSWORD, true);

    assertEquals(wallet.getAddress(), "ms8xH9qe3rg4DPHJty5baTT8teTMQNsZFT");
    assertEquals(wallet.hasMnemonic(), true);
    wallet.delete(SampleKey.PASSWORD);
  }


  @Test
  public void importDuplicate() {
    String m = "victory truck motion great urge loyal supply pipe ship code where hub";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        m, BIP44Util.ETHEREUM_PATH, SampleKey.PASSWORD, true);

    try {
      WalletManager.importWalletFromMnemonic(
          new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
          m, BIP44Util.ETHEREUM_PATH, SampleKey.PASSWORD, false);
      assertEquals("1", "2");
    } catch (Exception ex) {
      assertEquals(ex.getMessage(), Messages.WALLET_EXISTS);
    }

    assertNotNull(WalletManager.findWalletByPrivateKey(ChainType.ETHEREUM, Network.MAINNET,
        "431b050544c8b4e4be7952dbe307aa541f0ba86372aa36b145b0ac4ed2ccb53b", Metadata.NONE));

    assertNotNull(WalletManager.findWalletByMnemonic(ChainType.ETHEREUM, Network.MAINNET,
        m, BIP44Util.ETHEREUM_PATH, Metadata.NONE));

    wallet.delete(SampleKey.PASSWORD);

  }

  @Test
  public void exportMnemonic() throws Exception {
    String m = "victory truck motion great urge loyal supply pipe ship code where hub";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint"),
        m, BIP44Util.ETHEREUM_PATH, SampleKey.PASSWORD, true);

    MnemonicAndPath result = WalletManager.exportMnemonic(wallet.getId(), SampleKey.PASSWORD);
    assertEquals(result.getMnemonic(), m);
    assertEquals(result.getPath(), BIP44Util.ETHEREUM_PATH);
    wallet.delete(SampleKey.PASSWORD);
  }

  @Test
  public void changePassword() {
    String m = "victory truck motion great urge loyal supply pipe ship code where hub";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint"),
        m, BIP44Util.BITCOIN_TESTNET_PATH, SampleKey.PASSWORD, true);

    WalletManager.changePassword(wallet.getId(), SampleKey.PASSWORD, "hello1234");

    assertEquals(WalletManager.findWalletById(wallet.getId()).verifyPassword("hello1234"), true);

    MnemonicAndPath result = WalletManager.exportMnemonic(wallet.getId(), "hello1234");
    assertEquals(result.getMnemonic(), m);
    assertEquals(result.getPath(), "m/44'/1'/0'");
    wallet.delete("hello1234");
  }

  @Test
  public void removeWallet() {
    String m = "victory truck motion great urge loyal supply pipe ship code where hub";
    Wallet wallet = WalletManager.importWalletFromMnemonic(
        new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint"),
        m, BIP44Util.BITCOIN_TESTNET_PATH, SampleKey.PASSWORD, true);
    Assert.assertNotNull(WalletManager.findWalletById(wallet.getId()));
    WalletManager.removeWallet(wallet.getId(), SampleKey.PASSWORD);
    Assert.assertNull(WalletManager.findWalletById(wallet.getId()));
  }

  @Test
  public void switchBTCModel() {
    Metadata metadata = new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint");
    metadata.setSegWit(Metadata.P2WPKH);
    Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH, SampleKey.PASSWORD, true);
    String walletID = wallet.getId();
    long createdAt = wallet.getCreatedAt();
    String segWitAddress = wallet.getAddress();
    assertEquals("2MwN441dq8qudMvtM5eLVwC3u4zfKuGSQAB", segWitAddress);
    wallet = WalletManager.switchBTCWalletMode(walletID, SampleKey.PASSWORD, Metadata.NONE);
    assertEquals(walletID, wallet.getId());
    assertEquals("mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN", wallet.getAddress());
    assertEquals(createdAt, wallet.getCreatedAt());
  }


}
