package org.consenlabs.tokencore.wallet.keystore;

import com.google.common.io.Files;

import junit.framework.Assert;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.WalletManager;
import org.consenlabs.tokencore.wallet.WalletSupport;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.consenlabs.tokencore.wallet.Wallet;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by xyz on 2018/2/9.
 */

public class KeystoreTest extends WalletSupport {

  @Test
  public void testV3Keystore() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_PRIVATE);
    metadata.setName("V3Keystore Test");
    metadata.setChainType(ChainType.ETHEREUM);

    V3Keystore keystore = V3Keystore.create(metadata, SampleKey.PASSWORD, SampleKey.PRIVATE_KEY_STRING);
    assertEquals(SampleKey.ADDRESS, keystore.getAddress());
    assertEquals(3, keystore.getVersion());
    assertNotNull(keystore.getCrypto());
    assertNotNull(keystore.getMetadata());
    assertEquals(ChainType.ETHEREUM, keystore.getMetadata().getChainType());
    assertEquals("V3Keystore Test", keystore.getMetadata().getName());
    assertEquals(SampleKey.PRIVATE_KEY_STRING, NumericUtil.bytesToHex(keystore.decryptCiphertext(SampleKey.PASSWORD)));

    keystore = (V3Keystore) keystore.changePassword(SampleKey.PASSWORD, SampleKey.NEW_PASSWORD);
    assertEquals(SampleKey.PRIVATE_KEY_STRING, NumericUtil.bytesToHex(keystore.decryptCiphertext(SampleKey.NEW_PASSWORD)));

    metadata = new Metadata();
    metadata.setSource(Metadata.FROM_WIF);
    metadata.setName("V3Keystore Bitcoin Test");
    metadata.setNetwork(Network.MAINNET);
    metadata.setChainType(ChainType.BITCOIN);

    String[][] testcases = new String[][]{
        new String[]{"1N3RC53vbaDNrziTdWmctBEeQ4fo4quNpq", Metadata.NONE},
        new String[]{"3Js9bGaZSQCNLudeGRHL4NExVinc25RbuG", Metadata.P2WPKH}
    };
    for (String[] aCase : testcases) {
      metadata.setSegWit(aCase[1]);
      keystore = V3Keystore.create(metadata, SampleKey.PASSWORD, SampleKey.PRIVATE_KEY_WIF);
      assertEquals(aCase[0], keystore.getAddress());
      assertEquals(3, keystore.getVersion());
      assertNotNull(keystore.getCrypto());
      assertNotNull(keystore.getMetadata());
      assertEquals(ChainType.BITCOIN, keystore.getMetadata().getChainType());
      assertTrue(keystore.getMetadata().isMainNet());
      assertEquals("V3Keystore Bitcoin Test", keystore.getMetadata().getName());
      assertEquals(SampleKey.PRIVATE_KEY_WIF, new Wallet(keystore).exportPrivateKey(SampleKey.PASSWORD));

      keystore = (V3Keystore) keystore.changePassword(SampleKey.PASSWORD, SampleKey.NEW_PASSWORD);
      assertEquals(SampleKey.PRIVATE_KEY_WIF, new Wallet(keystore).exportPrivateKey(SampleKey.NEW_PASSWORD));
    }

  }

  @Test
  public void testV3MnemonicKeystore() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setName("V3MnemonicKeystore");
    metadata.setChainType(ChainType.ETHEREUM);

    V3MnemonicKeystore keystore = V3MnemonicKeystore.create(metadata, SampleKey.PASSWORD, Arrays.asList(SampleKey.MNEMONIC.split(" ")), SampleKey.ETHEREUM_HD_PATH);
    assertEquals("6031564e7b2f5cc33737807b2e58daff870b590b", keystore.getAddress());
    assertEquals(3, keystore.getVersion());
    assertNotNull(keystore.getCrypto());
    assertNotNull(keystore.getMetadata());
    assertNotNull(keystore.getEncMnemonic());
    assertEquals(SampleKey.ETHEREUM_HD_PATH, keystore.getMnemonicPath());
    assertEquals(ChainType.ETHEREUM, keystore.getMetadata().getChainType());
    assertEquals("V3MnemonicKeystore", keystore.getMetadata().getName());
    assertEquals("cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(keystore.decryptCiphertext(SampleKey.PASSWORD)));
  }

  @Test
  public void testHDMnemonicKeystore() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setName("HDMnemonicKeystore");
    metadata.setNetwork(Network.MAINNET);
    metadata.setChainType(ChainType.BITCOIN);

    String[][] testcases = new String[][]{
        new String[]{"12z6UzsA3tjpaeuvA2Zr9jwx19Azz74D6g", BIP44Util.BITCOIN_MAINNET_PATH, Metadata.NONE, "xprv9yrdwPSRnvomqFK4u1y5uW2SaXS2Vnr3pAYTjJjbyRZR8p9BwoadRsCxtgUFdAKeRPbwvGRcCSYMV69nNK4N2kadevJ6L5iQVy1SwGKDTHQ"},
        new String[]{"3JmreiUEKn8P3SyLYmZ7C1YCd4r2nFy3Dp", BIP44Util.BITCOIN_SEGWIT_MAIN_PATH, Metadata.P2WPKH, "xprv9xpNJWnYLHgctkd85tGpABEHiqsDjHdy63bNVX8XcxQZHLWF7MoJzqUJfvpCtkgHdcTMa6U8zFLALjFoxBv62keiH2uRgVf7tDPhkZJkc27"}
    };

    for (String[] aCase : testcases) {
      metadata.setSegWit(aCase[2]);
      HDMnemonicKeystore keystore = HDMnemonicKeystore.create(metadata, SampleKey.PASSWORD,
          Arrays.asList(SampleKey.MNEMONIC.split(" ")), aCase[1]);
      assertEquals(aCase[0], keystore.getAddress());
      assertEquals(44, keystore.getVersion());
      assertNotNull(keystore.getCrypto());
      assertNotNull(keystore.getMetadata());
      assertNotNull(keystore.getEncMnemonic());
      assertEquals(aCase[1], keystore.getMnemonicPath());
      assertEquals(ChainType.BITCOIN, keystore.getMetadata().getChainType());
      assertTrue(keystore.getMetadata().isMainNet());
      assertEquals("HDMnemonicKeystore", keystore.getMetadata().getName());
      assertEquals(aCase[3], new String(keystore.decryptCiphertext(SampleKey.PASSWORD)));
    }
  }

  @Test
  public void testKeystoreValid() {
    URL url = getClass().getClassLoader().getResource("invalid_keystores");
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_KEYSTORE);
    metadata.setChainType(ChainType.ETHEREUM);

    try {
      File keystore = new File(url.getFile());

      for (File source : keystore.listFiles()) {

        String v3Keystore = null;
        try (Scanner scanner = new Scanner(source)) {
          v3Keystore = scanner.useDelimiter("\\Z").next();
        }
        JSONObject jsonObject = new JSONObject(v3Keystore);
        String err = jsonObject.getString("err");

        try {
          WalletManager.importWalletFromKeystore(metadata, v3Keystore, "imToken2018", true);
          Assert.fail("Should failed!");
        } catch (TokenException ex) {
          Assert.assertEquals(String.format("filename: %s, keystore: %s", source.getName(), v3Keystore), ex.getMessage(), err);
        }
      }

    } catch (Exception ex) {
      Assert.fail(ex.getMessage());
    }
  }

  @Test
  public void testImportKeystoreWhichContainsInvalidPK() {
    String invalidKeystore = "{\n" +
        "    \"address\": \"dcc703c0e500b653ca82273b7bfad8045d85a470\",\n" +
        "    \"crypto\": {\n" +
        "        \"cipher\": \"aes-128-ctr\",\n" +
        "        \"cipherparams\": {\n" +
        "            \"iv\": \"4fd56a178ee2ad36c470fa6e8d972030\"\n" +
        "        },\n" +
        "        \"ciphertext\": \"a46adae8498e926eab52ce3cbd2bde64074dcf4927f05c85c528670e3c3b91f8\",\n" +
        "        \"kdf\": \"scrypt\",\n" +
        "        \"kdfparams\": {\n" +
        "            \"dklen\": 32,\n" +
        "            \"n\": 262144,\n" +
        "            \"p\": 1,\n" +
        "            \"r\": 8,\n" +
        "            \"salt\": \"38d1c31c43ef5806733ef2d5a3212810d8f51ff504e41f6ce9c6717e97d16145\"\n" +
        "        },\n" +
        "        \"mac\": \"b8724cf79dbecd837ed626620591f4485662692b3555e67967c358a4b7b437d6\"\n" +
        "    },\n" +
        "    \"id\": \"df242dcd-f3ee-4b8c-81fc-8cf6c2dd1779\",\n" +
        "    \"version\": 3\n" +
        "}";
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_KEYSTORE);
    metadata.setChainType(ChainType.ETHEREUM);

    try {
      WalletManager.importWalletFromKeystore(metadata, invalidKeystore, "22222222", true);
      Assert.fail("Should failed");
    } catch (TokenException ex) {
      Assert.assertEquals(ex.getMessage(), Messages.MAC_UNMATCH);
    }

    try {
      WalletManager.importWalletFromKeystore(metadata, invalidKeystore, "11111111", true);
      Assert.fail("Should failed");
    } catch (TokenException ex) {
      Assert.assertEquals(ex.getMessage(), Messages.KEYSTORE_CONTAINS_INVALID_PRIVATE_KEY);
    }
  }

}
