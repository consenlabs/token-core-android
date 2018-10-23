package org.consenlabs.tokencore.wallet;

import com.google.common.io.Files;

import junit.framework.Assert;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.keystore.LegacyEOSKeystore;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;


/**
 * Created by xyz on 2018/1/30.
 */

public class StorageTest extends WalletSupport {

  @Test
  public void testGenerateIdentityKeystore() {
    Identity.createIdentity("xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);

    try {
      String fileContent = readFileContent("identity.json");
      JSONObject jsonObject = new JSONObject(fileContent);
      Assert.assertNotNull(jsonObject);
      Assert.assertNotNull(jsonObject.getString("identifier"));
      Assert.assertNotNull(jsonObject.getString("ipfsId"));
      Assert.assertNotNull(jsonObject.getString("encKey"));
      Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
      Assert.assertEquals(10240, jsonObject.getJSONObject("crypto").getJSONObject("kdfparams").getInt("c"));
      Assert.assertNotNull(jsonObject.getJSONObject("encMnemonic"));
      Assert.assertNotNull(jsonObject.getJSONObject("encAuthKey"));
    } catch (Exception e) {
      Assert.fail("some error happened, exception: " + e.getMessage());
    }

    File walletDir = new File(KEYSTORE_DIR);
    String[] filePaths = walletDir.list();
    Assert.assertEquals("Should generate 3 file", 3, filePaths.length);
    for (String filename : filePaths) {
      if (filename.endsWith("identity.json")) continue;
      try {
        String fileContent = readFileContent(filename);
        JSONObject jsonObject = new JSONObject(fileContent);
        Assert.assertNotNull(jsonObject);
        Assert.assertNotNull(jsonObject.getString("address"));
        Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
        Assert.assertEquals(10240, jsonObject.getJSONObject("crypto").getJSONObject("kdfparams").getInt("c"));
        Assert.assertNotNull(jsonObject.getJSONObject("encMnemonic"));
        Assert.assertNotNull(jsonObject.getJSONObject("imTokenMeta"));
      } catch (Exception e) {
        Assert.fail("some error happened, exception: " + e.getMessage());
      }
    }
  }

  @Test
  public void testImportETHWalletFromPrivate() {
    Metadata metadata = new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint");
    metadata.setSource(Metadata.FROM_PRIVATE);
    WalletManager.importWalletFromPrivateKey(metadata, SampleKey.PRIVATE_KEY_STRING, SampleKey.PASSWORD, true);

    File keystoreDir = new File(KEYSTORE_DIR);
    Assert.assertNotNull(keystoreDir.list());
    String walletFilePath = keystoreDir.list()[0];
    Assert.assertNotNull(walletFilePath);
    String fileContent = readFileContent(walletFilePath);
    try {
      JSONObject jsonObject = new JSONObject(fileContent);
      Assert.assertNotNull(jsonObject);
      Assert.assertNotNull(jsonObject.getString("address"));
      Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
      JSONObject metadataObj = jsonObject.getJSONObject("imTokenMeta");
      assertNotNull(metadataObj);
      assertEquals(ChainType.ETHEREUM, metadataObj.getString("chainType"));
      assertNotEquals(0L, metadataObj.getLong("timestamp"));
      assertEquals(Metadata.V3, metadataObj.getString("walletType"));
      assertEquals(Metadata.NORMAL, metadataObj.getString("mode"));
      assertEquals(Metadata.FROM_PRIVATE, metadataObj.getString("source"));

    } catch (JSONException e) {
      Assert.fail("Some error happened, exception: " + e.getMessage());
    }
  }

  @Test
  public void testImportBTCWalletFromPrivate() {
    Metadata metadata = new Metadata(ChainType.BITCOIN, Network.MAINNET, "name", "passwordHint");

    metadata.setSource(Metadata.FROM_WIF);
    String[] segWits = new String[]{Metadata.NONE, Metadata.P2WPKH};
    for (int i = 0; i < segWits.length; i++) {
      metadata.setSegWit(segWits[i]);
      Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, SampleKey.PRIVATE_KEY_WIF, SampleKey.PASSWORD, true);

      String fileContent = readFileContent(wallet.getId() + ".json");
      try {
        JSONObject jsonObject = new JSONObject(fileContent);
        Assert.assertNotNull(jsonObject);
        Assert.assertNotNull(jsonObject.getString("address"));
        Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
        JSONObject metadataObj = jsonObject.getJSONObject("imTokenMeta");
        assertNotNull(metadataObj);
        assertTrue(metadataObj.getString("network").equalsIgnoreCase(Network.MAINNET));
        assertEquals(ChainType.BITCOIN, metadataObj.getString("chainType"));
        assertNotEquals(0L, metadataObj.getLong("timestamp"));
        assertEquals(Metadata.V3, metadataObj.getString("walletType"));
        assertEquals(Metadata.NORMAL, metadataObj.getString("mode"));
        assertEquals(Metadata.FROM_WIF, metadataObj.getString("source"));
        if (i == 0) {
          assertEquals(Metadata.NONE, metadataObj.getString("segWit"));
        } else {
          assertEquals(Metadata.P2WPKH, metadataObj.getString("segWit"));
        }

      } catch (JSONException e) {
        Assert.fail("Some error happened, exception: " + e.getMessage());
      }
    }
  }

  @Test
  public void testImportETHWalletFromMnemonic() {
    Metadata metadata = new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint");
    WalletManager.importWalletFromMnemonic(metadata, SampleKey.MNEMONIC, SampleKey.ETHEREUM_HD_PATH, SampleKey.PASSWORD, false);

    File keystoreDir = new File(KEYSTORE_DIR);
    Assert.assertNotNull(keystoreDir.list());
    String walletFilePath = keystoreDir.list()[0];
    Assert.assertNotNull(walletFilePath);
    String fileContent = readFileContent(walletFilePath);
    try {
      JSONObject jsonObject = new JSONObject(fileContent);
      Assert.assertNotNull(jsonObject);
      Assert.assertNotNull(jsonObject.getString("address"));
      Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
      Assert.assertNotNull(jsonObject.getJSONObject("encMnemonic"));
      JSONObject metadataObj = jsonObject.getJSONObject("imTokenMeta");
      assertNotNull(metadataObj);
      assertEquals(ChainType.ETHEREUM, metadataObj.getString("chainType"));
      assertNotEquals(0L, metadataObj.getLong("timestamp"));
      assertEquals(Metadata.V3, metadataObj.getString("walletType"));
      assertEquals(Metadata.NORMAL, metadataObj.getString("mode"));
      assertEquals(Metadata.FROM_MNEMONIC, metadataObj.getString("source"));

    } catch (JSONException e) {
      Assert.fail("Some error happened, exception: " + e.getMessage());
    }
  }

  @Test
  public void testImportBTCWalletFromMnemonic() {
    Metadata metadata = new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint");

    String[] segWits = new String[]{Metadata.NONE, Metadata.P2WPKH};
    for (int i = 0; i < segWits.length; i++) {
      metadata.setSegWit(segWits[i]);
      Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, SampleKey.MNEMONIC, SampleKey.BITCOIN_MAINNET_HD_PATH, SampleKey.PASSWORD, true);
      String walletFileName = wallet.getId() + ".json";
      String fileContent = readFileContent(walletFileName);
      try {
        JSONObject jsonObject = new JSONObject(fileContent);
        Assert.assertNotNull(jsonObject);
        Assert.assertNotNull(jsonObject.getString("address"));
        Assert.assertNotNull(jsonObject.getJSONObject("crypto"));

        Assert.assertNotNull(jsonObject.getJSONObject("encMnemonic"));
        JSONObject metadataObj = jsonObject.getJSONObject("imTokenMeta");
        assertNotNull(metadataObj);
//        assertEquals(Network.MAINNET, metadataObj.getString("network"));
        assertEquals(ChainType.BITCOIN, metadataObj.getString("chainType"));
        assertNotEquals(0L, metadataObj.getLong("timestamp"));
        assertEquals(Metadata.HD, metadataObj.getString("walletType"));
        assertEquals(Metadata.NORMAL, metadataObj.getString("mode"));
        assertEquals(Metadata.FROM_MNEMONIC, metadataObj.getString("source"));

        if (i == 0) {
          assertEquals(Metadata.NONE, metadataObj.getString("segWit"));
        } else {
          assertEquals(Metadata.P2WPKH, metadataObj.getString("segWit"));
        }

      } catch (JSONException e) {
        Assert.fail("Some error happened, exception: " + e.getMessage());
      }
    }
  }

  @Test
  public void testImportETHWalletFromKeystore() {
    Metadata metadata = new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint");

    WalletManager.importWalletFromKeystore(metadata, SampleKey.V3JSON, SampleKey.PASSWORD, false);

    File keystoreDir = new File(KEYSTORE_DIR);
    Assert.assertNotNull(keystoreDir.list());
    String walletFilePath = keystoreDir.list()[0];
    Assert.assertNotNull(walletFilePath);
    String fileContent = readFileContent(walletFilePath);
    try {
      JSONObject jsonObject = new JSONObject(fileContent);
      Assert.assertNotNull(jsonObject);
      Assert.assertNotNull(jsonObject.getString("address"));
      Assert.assertNotNull(jsonObject.getJSONObject("crypto"));
      Assert.assertTrue(!fileContent.contains("encMnemonic"));
      JSONObject metadataObj = jsonObject.getJSONObject("imTokenMeta");
      assertNotNull(metadataObj);
      assertEquals(ChainType.ETHEREUM, metadataObj.getString("chainType"));
      assertNotEquals(0L, metadataObj.getLong("timestamp"));
      assertEquals(Metadata.V3, metadataObj.getString("walletType"));
      assertEquals(Metadata.NORMAL, metadataObj.getString("mode"));
      assertEquals(Metadata.FROM_KEYSTORE, metadataObj.getString("source"));
    } catch (JSONException e) {
      Assert.fail("Some error happened, exception: " + e.getMessage());
    }
  }

  @Test
  public void testRestoreFromFiles() {
    URL url = getClass().getClassLoader().getResource("keystore");
    try {
      File keystore = new File(url.getFile());

      for (File source : keystore.listFiles()) {
        String dest = WalletSupport.KEYSTORE_DIR + File.separator + source.getName();
        Files.copy(source, new File(dest));
      }
      WalletManager.scanWallets();

      // identity test
      Identity.currentIdentity = null;
      Identity identity = Identity.getCurrentIdentity();
      Assert.assertEquals("Compare identifier:", "im18MDKM8hcTykvMmhLnov9m2BaFqsdjoA7cwNg", identity.getIdentifier());
      Assert.assertEquals("Compare ipfs id", "QmVoPZQnQjppqcD4gUh1KrfmjbWQCtXQpm2YrFG6Voquh2", identity.getIpfsId());
      Assert.assertEquals("wallets size should be three", 3, identity.getWallets().size());
      Assert.assertEquals(SampleKey.MNEMONIC, identity.exportIdentity("imtoken1"));
      String expectedSignResult = "0340b2495a111111111111111111111111111111111084e741e2b83ec644e844985088fd58d838b995d430a36b19db889a9d2c40ad6645ef47040256e7d901a4b91b8f406dd00d6228fc8faabee349f11140cecd642cc47228cf1c96efc8d02035ddf203ae1f1c";
      long unixTimestamp = 1514779200;
      Assert.assertEquals(expectedSignResult, identity.encryptDataToIPFS("a", unixTimestamp, NumericUtil.hexToBytes("11111111111111111111111111111111")));
      String authMsg = identity.signAuthenticationMessage(1514736000, "12345ABCDE", "imtoken1");
      String expectedAuthMsg = "663ace6d60225f6d1a71d25735c66646f71977a9f25f709fca162db3c664a1e161881a51a8034c240dd8f0093285fd6245f65246708546e8eadd592f995daeb11c";
      Assert.assertEquals(expectedAuthMsg, authMsg);

      // 894d341c-8e29-46b8-a3f4-9ccb21fe08de is Bitcoin keystore
      String btcWalletID = "02a55ab6-554a-4e78-bc26-6a7acced7e5e";
      Wallet wallet = WalletManager.findWalletById(btcWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(btcWalletID, wallet.getId());
      assertEquals("mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN", wallet.getAddress());
      assertEquals(1519611221L, wallet.getCreatedAt());
      String expectedEncXPub = "GekyMLycBJlFAmob0yEGM8zrEKrBHozAKr66PrMts7k6vSBJ/8DJQW7HViVqWftKhRbPAxZ3MO0281AKvWp4qa+/Q5nqoCi5/THxRLA1wDn8gWqDJjUjaZ7kJaNnreWfUyNGUeDxnN7tHDGdW4nbtA==";
      assertEquals(expectedEncXPub, wallet.getEncXPub());
      assertFalse(wallet.getMetadata().isMainNet());
      assertEquals(ChainType.BITCOIN, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_RECOVERED_IDENTITY, wallet.getMetadata().getSource());
      assertEquals(Metadata.HD, wallet.getMetadata().getWalletType());

      // 3831346d-0b81-405b-89cf-cdb1d010430e is Bitcoin HD SegWit P2WPKH Wallet
      String btcSegWitWalletID = "3831346d-0b81-405b-89cf-cdb1d010430e";
      wallet = WalletManager.findWalletById(btcSegWitWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(btcSegWitWalletID, wallet.getId());
      assertEquals("2NCTX2isUH3bwkrSab6kJT1Eu9pWPqAStRp", wallet.getAddress());
      assertEquals(1526462133L, wallet.getCreatedAt());
      expectedEncXPub = "KN9qVdfibQ6+qM/gpglypnGYL0A5Wsu/hm7q5QHoAzUNRQUmKOmyQquyka2FNzSEIfBp/3PZemS/uhEEbbpJfSh7mhbKDQfNQHRalWLEXrfZvOk3Aaej7cxtMnm0UdzNQlYlbeCo/E43kcfCnlsKBw==";
      assertEquals(expectedEncXPub, wallet.getEncXPub());
      assertFalse(wallet.getMetadata().isMainNet());
      assertEquals(ChainType.BITCOIN, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_MNEMONIC, wallet.getMetadata().getSource());
      assertEquals(Metadata.HD, wallet.getMetadata().getWalletType());


      // 045861fe-0e9b-4069-92aa-0ac03cad55e0 is imported eth wallet
      String importedWalletID = "045861fe-0e9b-4069-92aa-0ac03cad55e0";
      wallet = WalletManager.findWalletById(importedWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(importedWalletID, wallet.getId());
      assertEquals("41983f2e3af196c1df429a3ff5cdecc45c82c600", wallet.getAddress());
      assertEquals(1519611469L, wallet.getCreatedAt());
      assertEquals(ChainType.ETHEREUM, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_KEYSTORE, wallet.getMetadata().getSource());
      assertEquals(Metadata.V3, wallet.getMetadata().getWalletType());

      // 175169f7-5a35-4df7-93c1-1ff612168e71 is imported eth wallet
      String ethWalletID = "175169f7-5a35-4df7-93c1-1ff612168e71";
      wallet = WalletManager.findWalletById(ethWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(ethWalletID, wallet.getId());
      assertEquals("6031564e7b2f5cc33737807b2e58daff870b590b", wallet.getAddress());
      assertEquals(1519611221L, wallet.getCreatedAt());
      assertEquals(ChainType.ETHEREUM, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_RECOVERED_IDENTITY, wallet.getMetadata().getSource());
      assertEquals(Metadata.V3, wallet.getMetadata().getWalletType());

      // 7f5406be-b5ee-4497-948c-877deab8c994 is recover eos wallet
      String eosWalletID = "7f5406be-b5ee-4497-948c-877deab8c994";
      wallet = WalletManager.findWalletById(eosWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(eosWalletID, wallet.getId());
      assertEquals("longhairzlh2", wallet.getAddress());
      assertEquals(1530537386L, wallet.getCreatedAt());
      assertEquals(ChainType.EOS, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_RECOVERED_IDENTITY, wallet.getMetadata().getSource());
      assertEquals(Metadata.HD_SHA256, wallet.getMetadata().getWalletType());
      List<KeyPair> prvKeyPairs = wallet.exportPrivateKeys("password");
      assertEquals("5JQzQhgpGauT7GxvraAL6BBQ5cwLR6MuYvzc4ZX49Xrk4JZ78cX", prvKeyPairs.get(0).getPrivateKey());
      assertEquals("5JXLg1zCnCSnJBQYD7XfKfwzkrvLoMyLvR3DuE9UTzuFqtvTyp4", prvKeyPairs.get(1).getPrivateKey());

      // 2b5fd593-cedf-4765-9ed0-c52b7d5cf45c is a legacy eos wallet
      eosWalletID = "42c275c6-957a-49e8-9eb3-43c21cbf583f";
      wallet = WalletManager.findWalletById(eosWalletID);
      Assert.assertNotNull(wallet);
      Assert.assertNotNull(wallet.getMetadata());
      assertEquals(eosWalletID, wallet.getId());
      assertEquals("longhairzlh2", wallet.getAddress());
      assertEquals(1535426384L, wallet.getCreatedAt());
      assertEquals(ChainType.EOS, wallet.getMetadata().getChainType());
      assertEquals(Metadata.FROM_WIF, wallet.getMetadata().getSource());
      assertEquals(Metadata.V3, wallet.getMetadata().getWalletType());
      assertTrue(wallet.getKeystore() instanceof LegacyEOSKeystore);
      prvKeyPairs = wallet.exportPrivateKeys("password");
      assertEquals("5JQzQhgpGauT7GxvraAL6BBQ5cwLR6MuYvzc4ZX49Xrk4JZ78cX", prvKeyPairs.get(0).getPrivateKey());

    } catch (IOException e) {
      Assert.fail("Some error happen, exception: " + e.getMessage());
      e.printStackTrace();
    }
  }


  private String readFileContent(String filename) {
    BufferedReader bufferedReader = null;
    StringBuilder fileContent = new StringBuilder();
    try {
      bufferedReader = new BufferedReader(new FileReader(KEYSTORE_DIR + "/" + filename));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        fileContent.append(line);
      }
      return fileContent.toString();
    } catch (Exception e) {
      return null;
    } finally {
      try {
        if (bufferedReader != null)
          bufferedReader.close();
      } catch (IOException ignored) {
      }
    }
  }
}
