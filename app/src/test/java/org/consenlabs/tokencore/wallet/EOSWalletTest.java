package org.consenlabs.tokencore.wallet;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.protobuf.CodedOutputStream;

import junit.framework.Assert;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.keystore.EOSKeystore;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainId;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.transaction.EOSSign;
import org.consenlabs.tokencore.wallet.transaction.EOSTransaction;
import org.consenlabs.tokencore.wallet.transaction.TxMultiSignResult;
import org.consenlabs.tokencore.wallet.transaction.TxSignResult;
import org.junit.Test;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by xyz on 2018/4/18.
 */

public class EOSWalletTest extends WalletSupport {

  public static final String WIF = "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3";
  static final String ACCOUNT_NAME = "imtoken1";
  static final String PUBLIC_KEY = "EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV";

  @Test
  public void generatePrvPubKey() {

    byte[] prvWIF = Base58.decode(WIF);
    // have omitted the checksum verification
    prvWIF = Arrays.copyOfRange(prvWIF, 1, prvWIF.length - 4);

    // use the privateKey to calculate the compressed public key directly
    ECKey ecKey = ECKey.fromPrivate(new BigInteger(1, prvWIF));
    byte[] pubKeyData = ecKey.getPubKey();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(pubKeyData, 0, pubKeyData.length);
    byte[] out = new byte[20];
    digest.doFinal(out, 0);
    byte[] checksumBytes = Arrays.copyOfRange(out, 0, 4);

    pubKeyData = ByteUtil.concat(pubKeyData, checksumBytes);
    String eosPK = "EOS" + Base58.encode(pubKeyData);
    Assert.assertEquals(PUBLIC_KEY, eosPK);
  }

  @Test
  public void serializeToBinary() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
    try {

      codedOutputStream.writeStringNoTag("jc");
      codedOutputStream.writeStringNoTag("dan");
      codedOutputStream.writeInt32NoTag(1);
      codedOutputStream.writeStringNoTag("abc");
      codedOutputStream.writeStringNoTag("");
      codedOutputStream.writeByteArrayNoTag(NumericUtil.hexToBytes("0f0f0f"));
      codedOutputStream.flush();
      ByteBuffer byteBuffer = ByteBuffer.allocate(100);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void signTransaction() {
    String wif = "5HxQKWDznancXZXm7Gr2guadK7BhK9Zs8ejDhfA9oEBM89ZaAru";
    Metadata metadata = new Metadata();
    metadata.setChainType("EOS");
    metadata.setSource(Metadata.FROM_WIF);
    metadata.setWalletType(Metadata.V3);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, "account_name", wif, SampleKey.PASSWORD, false);
    EOSTransaction transaction = new EOSTransaction(NumericUtil.hexToBytes("c578065b93aec6a7c811000000000100a6823403ea3055000000572d3ccdcd01000000602a48b37400000000a8ed323225000000602a48b374208410425c95b1ca80969800000000000453595300000000046d656d6f00"));
    TxSignResult ret = transaction.signTransaction(ChainId.EOS_MAINNET, SampleKey.PASSWORD, wallet);
    Assert.assertEquals("SIG_K1_KUzLctwEZJnbZBPbZiTiwzxSuMVp5ik8CbJTsusbBaDk9yKHuuw9D9jUj4fMaWKdnbcmqxj8BJCvJkoR4GtkVhD8msihFj", ret.getSignedTx());
  }

  @Test
  public void sighHashTest() {
    byte[] dataSha256 = NumericUtil.hexToBytes("6cb75bc5a46a7fdb64b92efefca01ed7b060ab5e0d625226e8efbc0980c3ddc1");
    String result = EOSSign.sign(dataSha256, "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3");

  }

  private Metadata eosMetadata() {
    Metadata metadata = new Metadata();
    metadata.setChainType(ChainType.EOS);
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setWalletType(Metadata.HD_SHA256);
    return metadata;
  }

  @Test
  public void eosSignTransactions() {

    // import eos wallet
    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), SampleKey.MNEMONIC, BIP44Util.EOS_LEDGER, SampleKey.PASSWORD, true);

    // construct  to sign objects
    List<EOSTransaction.ToSignObj> toSignObjs = new ArrayList<>();
    EOSTransaction.ToSignObj toSignObj = new EOSTransaction.ToSignObj();
    toSignObj.setPublicKeys(Collections.singletonList("EOS88XhiiP7Cu5TmAUJqHbyuhyYgd6sei68AU266PyetDDAtjmYWF"));
    toSignObj.setTxHex("c578065b93aec6a7c811000000000100a6823403ea3055000000572d3ccdcd01000000602a48b37400000000a8ed323225000000602a48b374208410425c95b1ca80969800000000000453595300000000046d656d6f00");
    toSignObjs.add(toSignObj);

    EOSTransaction eosTransaction = new EOSTransaction(toSignObjs);
    List<TxMultiSignResult> signResults = eosTransaction.signTransactions(ChainId.EOS_MAINNET, SampleKey.PASSWORD, wallet);
    Assert.assertEquals(1, signResults.size());

    TxMultiSignResult actualResult = signResults.get(0);
    Assert.assertEquals(1, actualResult.getSigned().size());
    Assert.assertEquals("SIG_K1_KjZXm86HMVyUd59E15pCkrpn5uUPAAsjTxjEVRRueEvGciinxRS3sATmEEWdkb8hRNHhf6SXofsz4qzPdD6mfZ67FoqLxh", actualResult.getSigned().get(0));
    Assert.assertEquals("6af5b3ae9871c25e2de195168ed7423f455a68330955701e327f02276bb34088", actualResult.getTxHash());

  }

  @Test
  public void importEOSWalletByMnemonic() {

    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), SampleKey.MNEMONIC, BIP44Util.EOS_LEDGER, SampleKey.PASSWORD, true);
    Assert.assertEquals(ChainType.EOS, wallet.getMetadata().getChainType());
    Assert.assertEquals(Metadata.FROM_MNEMONIC, wallet.getMetadata().getSource());
    Assert.assertTrue(Strings.isNullOrEmpty(wallet.getAddress()));
    MnemonicAndPath mnemonicAndPath = wallet.exportMnemonic(SampleKey.PASSWORD);
    Assert.assertEquals(SampleKey.MNEMONIC, mnemonicAndPath.getMnemonic());
    Assert.assertEquals(BIP44Util.EOS_LEDGER, mnemonicAndPath.getPath());
    Assert.assertNotSame(0L, wallet.getCreatedAt());
    Assert.assertTrue(Strings.isNullOrEmpty(wallet.getAddress()));
    List<String> expectedPubKeys = new ArrayList<>();

    expectedPubKeys.add("EOS88XhiiP7Cu5TmAUJqHbyuhyYgd6sei68AU266PyetDDAtjmYWF");
    List<String> publicKeys = new ArrayList<>(2);
    publicKeys.add(wallet.getKeyPathPrivates().get(0).getPublicKey());

    Assert.assertTrue(Arrays.equals(expectedPubKeys.toArray(), publicKeys.toArray()));

  }

  @Test
  public void importEOSWalletByMnemonicMultiPermissions() {
    List<EOSKeystore.PermissionObject> permissionObjects = new ArrayList<>();
    EOSKeystore.PermissionObject permObj = new EOSKeystore.PermissionObject();
    permObj.setPublicKey("EOS88XhiiP7Cu5TmAUJqHbyuhyYgd6sei68AU266PyetDDAtjmYWF");
    permObj.setPermission("active");
    permissionObjects.add(permObj);

//
//    permObj = new EOSKeystore.PermissionObject();
//    permObj.setPublicKey("EOS7uUZkJKheG9Ag5C1TA78LX74fWY28sBEfFjP49Cae8Ski7cvVR");
//    permObj.setPermission("sns");
//    permissionObjects.add(permObj);

    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), ACCOUNT_NAME, SampleKey.MNEMONIC, BIP44Util.EOS_LEDGER, permissionObjects, SampleKey.PASSWORD, true);
    Assert.assertEquals(1, wallet.getKeyPathPrivates().size());
    List<String> expectedPubKeys = new ArrayList<>();
    expectedPubKeys.add("EOS88XhiiP7Cu5TmAUJqHbyuhyYgd6sei68AU266PyetDDAtjmYWF");
    List<String> publicKeys = new ArrayList<>(3);
    publicKeys.add(wallet.getKeyPathPrivates().get(0).getPublicKey());
    Assert.assertTrue(Arrays.equals(expectedPubKeys.toArray(), publicKeys.toArray()));
  }

  @Test
  public void importEOSWalletFailedWhenDerivedPubKeyNotSame() {
    try {
      List<EOSKeystore.PermissionObject> permissionObjects = new ArrayList<>();
      EOSKeystore.PermissionObject permObj = new EOSKeystore.PermissionObject();
      // this pubkey is wrong, the last letter should be w not W
      permObj.setPublicKey("EOS7tpXQ1thFJ69ZXDqqEan7GMmuWdcptKmwgbs7n1cnx3hWPw3jW");
      permObj.setPermission("owner");
      permissionObjects.add(permObj);

      permObj = new EOSKeystore.PermissionObject();
      permObj.setPublicKey("EOS5SxZMjhKiXsmjxac8HBx56wWdZV1sCLZESh3ys1rzbMn4FUumU");
      permObj.setPermission("active");
      permissionObjects.add(permObj);

      WalletManager.importWalletFromMnemonic(eosMetadata(), ACCOUNT_NAME, SampleKey.MNEMONIC, BIP44Util.EOS_LEDGER, permissionObjects, SampleKey.PASSWORD, true);
      Assert.fail("Should throw exception");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.EOS_PRIVATE_PUBLIC_NOT_MATCH, ex.getMessage());
    }

  }

  @Test
  public void importEOSWalletBySinglePrvKey() {
//    owner key: 5Jnx4Tv6iu5fyq9g3aKmKsEQrhe7rJZkJ4g3LTK5i7tBDitakvP
//    active key: 5JK2n2ujYXsooaqbfMQqxxd8P7xwVNDaajTuqRagJNGPi88yPGw
//    active key: 5J25CphXSMh2SUdjspX7M4sLT5QATkTXJhiGSMn4nwg1HbhHLRe

    List<String> prvKeys = new ArrayList<>(3);
    prvKeys.add("5Jnx4Tv6iu5fyq9g3aKmKsEQrhe7rJZkJ4g3LTK5i7tBDitakvP");
    prvKeys.add("5JK2n2ujYXsooaqbfMQqxxd8P7xwVNDaajTuqRagJNGPi88yPGw");
    prvKeys.add("5J25CphXSMh2SUdjspX7M4sLT5QATkTXJhiGSMn4nwg1HbhHLRe");
    Metadata meta = eosMetadata();
    meta.setSource(Metadata.FROM_WIF);

    List<EOSKeystore.PermissionObject> permissionObjects = new ArrayList<>();
    EOSKeystore.PermissionObject permObj = new EOSKeystore.PermissionObject();
    permObj.setPublicKey("EOS621QecaYWvdKdCvHJRo76fvJwTo1Y4qegPnKxsf3FJ5zm2pPru");
    permObj.setPermission("owner");
    permissionObjects.add(permObj);

    permObj = new EOSKeystore.PermissionObject();
    permObj.setPublicKey("EOS6qTGVvgoT39AAJp1ykty8XVDFv1GfW4QoS4VyjfQQPv5ziMNzF");
    permObj.setPermission("active");
    permissionObjects.add(permObj);

    permObj = new EOSKeystore.PermissionObject();
    permObj.setPublicKey("EOS877B3gaJytVzFizhWPD26SefS9QV1qYTZT2QCcXueQfV4PAN8h");
    permObj.setPermission("sns");
    permissionObjects.add(permObj);

    Wallet wallet = WalletManager.importWalletFromPrivateKeys(meta, ACCOUNT_NAME, prvKeys, permissionObjects, SampleKey.PASSWORD, true);
    Assert.assertEquals(ChainType.EOS, wallet.getMetadata().getChainType());
    Assert.assertEquals(Metadata.FROM_WIF, wallet.getMetadata().getSource());
    Assert.assertNotSame(0L, wallet.getCreatedAt());
    Assert.assertEquals(ACCOUNT_NAME, wallet.getAddress());
    List<String> expectedPubKeys = new ArrayList<>();
    expectedPubKeys.add("EOS621QecaYWvdKdCvHJRo76fvJwTo1Y4qegPnKxsf3FJ5zm2pPru");
    expectedPubKeys.add("EOS6qTGVvgoT39AAJp1ykty8XVDFv1GfW4QoS4VyjfQQPv5ziMNzF");
    expectedPubKeys.add("EOS877B3gaJytVzFizhWPD26SefS9QV1qYTZT2QCcXueQfV4PAN8h");
    List<String> publicKeys = new ArrayList<>(3);
    for (EOSKeystore.KeyPathPrivate keyPathPrivate : wallet.getKeyPathPrivates()) {
      publicKeys.add(keyPathPrivate.getPublicKey());
    }
    Assert.assertTrue(Arrays.equals(expectedPubKeys.toArray(), publicKeys.toArray()));
  }

  @Test
  public void importEOSWalletByPrvKeysShouldFailedWhenDerivedPubKeyNotSame() {
    try {
      List<EOSKeystore.PermissionObject> permissionObjects = new ArrayList<>();
      EOSKeystore.PermissionObject permObj = new EOSKeystore.PermissionObject();
      // this pubkey is wrong, the last letter should be u not U
      permObj.setPublicKey("EOS621QecaYWvdKdCvHJRo76fvJwTo1Y4qegPnKxsf3FJ5zm2pPrU");
      permObj.setPermission("owner");
      permissionObjects.add(permObj);

      permObj = new EOSKeystore.PermissionObject();
      permObj.setPublicKey("EOS6qTGVvgoT39AAJp1ykty8XVDFv1GfW4QoS4VyjfQQPv5ziMNzF");
      permObj.setPermission("active");
      permissionObjects.add(permObj);

      List<String> prvKeys = new ArrayList<>(3);
      prvKeys.add("5Jnx4Tv6iu5fyq9g3aKmKsEQrhe7rJZkJ4g3LTK5i7tBDitakvP");
      prvKeys.add("5JK2n2ujYXsooaqbfMQqxxd8P7xwVNDaajTuqRagJNGPi88yPGw");
      Metadata meta = eosMetadata();
      meta.setSource(Metadata.FROM_WIF);
      WalletManager.importWalletFromPrivateKeys(meta, ACCOUNT_NAME, prvKeys, permissionObjects, SampleKey.PASSWORD, true);
      Assert.fail("Should throw exception");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.EOS_PRIVATE_PUBLIC_NOT_MATCH, ex.getMessage());
    }

  }


  @Test
  public void exportWalletPrvKeys() {
    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), SampleKey.MNEMONIC, BIP44Util.EOS_LEDGER, SampleKey.PASSWORD, true);
    List<KeyPair> prvKeys = WalletManager.exportPrivateKeys(wallet.getId(), SampleKey.PASSWORD);
    List<KeyPair> expectedPrvKeys = new ArrayList<>();

    expectedPrvKeys.add(new KeyPair("5KAigHMamRhN7uwHFnk3yz7vUTyQT1nmXoAA899XpZKJpkqsPFp", "EOS88XhiiP7Cu5TmAUJqHbyuhyYgd6sei68AU266PyetDDAtjmYWF"));

    Assert.assertTrue(Arrays.equals(prvKeys.toArray(), expectedPrvKeys.toArray()));
  }

  @Test
  public void accountName() {
    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), SampleKey.MNEMONIC, BIP44Util.EOS_PATH, SampleKey.PASSWORD, true);
    Assert.assertTrue(Strings.isNullOrEmpty(wallet.getAddress()));
    try {
      String accountName = "AccountName";
      wallet = WalletManager.setAccountName(wallet.getId(), accountName);
      Assert.fail("Account name can't contains uppercase char");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.EOS_ACCOUNT_NAME_INVALID, ex.getMessage());
    }

    try {
      String accountName = "accountnameaccountname";
      wallet = WalletManager.setAccountName(wallet.getId(), accountName);
      Assert.fail("Account name's length can't greater than 12");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.EOS_ACCOUNT_NAME_INVALID, ex.getMessage());
    }

    try {
      String accountName = "accountname6";
      wallet = WalletManager.setAccountName(wallet.getId(), accountName);
      Assert.fail("Account name can't contain number 6~9 and 0");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.EOS_ACCOUNT_NAME_INVALID, ex.getMessage());
    }

    String accountName = "imtoken.1111";
    wallet = WalletManager.setAccountName(wallet.getId(), accountName);
    Assert.assertEquals(accountName, wallet.getAddress());
    Wallet foundedWallet = WalletManager.findWalletByAddress(ChainType.EOS, accountName);
    Assert.assertEquals(wallet.getId(), foundedWallet.getId());

    try {
      WalletManager.setAccountName(wallet.getId(), "NewAccountName");
      Assert.fail("EOS wallet only can change the accountName once");
    } catch (TokenException ex) {
      Assert.assertTrue(true);
    }
  }

  @Test
  public void compatibilityV3Sign() {
    String wif = "5HxQKWDznancXZXm7Gr2guadK7BhK9Zs8ejDhfA9oEBM89ZaAru";
    Metadata metadata = new Metadata();
    metadata.setChainType("EOS");
    metadata.setSource(Metadata.FROM_WIF);
    metadata.setWalletType(Metadata.V3);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, "account.name", wif, SampleKey.PASSWORD, false);
    List<EOSTransaction.ToSignObj> toSignObjs = new ArrayList<>();
    EOSTransaction.ToSignObj toSignObj = new EOSTransaction.ToSignObj();
    toSignObj.setPublicKeys(Collections.singletonList("EOS5SxZMjhKiXsmjxac8HBx56wWdZV1sCLZESh3ys1rzbMn4FUumU"));
    toSignObj.setTxHex("c578065b93aec6a7c811000000000100a6823403ea3055000000572d3ccdcd01000000602a48b37400000000a8ed323225000000602a48b374208410425c95b1ca80969800000000000453595300000000046d656d6f00");
    toSignObjs.add(toSignObj);

    EOSTransaction eosTransaction = new EOSTransaction(toSignObjs);
    List<TxMultiSignResult> signResults = eosTransaction.signTransactions(ChainId.EOS_MAINNET, SampleKey.PASSWORD, wallet);
    Assert.assertEquals(1, signResults.size());
    Assert.assertEquals("SIG_K1_KUzLctwEZJnbZBPbZiTiwzxSuMVp5ik8CbJTsusbBaDk9yKHuuw9D9jUj4fMaWKdnbcmqxj8BJCvJkoR4GtkVhD8msihFj", signResults.get(0).getSigned().get(0));
  }

  @Test
  public void importEOSWalletWhenHasEmptyAddressWallet() {
    // regression testing
    // if keystoreMap has a empty address keystore, then import the second keystore will produce a 'null object reference' exception
    Wallet wallet = WalletManager.importWalletFromMnemonic(eosMetadata(), SampleKey.MNEMONIC, BIP44Util.EOS_PATH, SampleKey.PASSWORD, true);
    WalletManager.importWalletFromMnemonic(eosMetadata(), ACCOUNT_NAME, SampleKey.MNEMONIC, BIP44Util.EOS_PATH, null, SampleKey.PASSWORD, true);
  }

  @Test
  public void testEOSSign() {
    long start = System.currentTimeMillis();
    URL url = getClass().getClassLoader().getResource("EOSSignTestcase.txt");
    try {
      BufferedReader reader = new BufferedReader(new FileReader(url.getFile()));
      String line;
      while ((line = reader.readLine()) != null) {
        String[] strs = line.split(",");
        Assert.assertEquals(strs[0], strs[1], EOSSign.sign(Hash.sha256(strs[0].getBytes()), "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3"));
      }
    } catch (Exception ex) {
      Assert.fail(ex.getMessage());
    }
    System.out.println(String.format("run 'EOSSignTestcase' test take %d ms", (System.currentTimeMillis() - start)));
  }

  @Test
  public void testLegacyEOSExport() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_WIF);
    metadata.setChainType(ChainType.EOS);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, ACCOUNT_NAME, WIF, SampleKey.PASSWORD, true);
    List<KeyPair> keyPairs = wallet.exportPrivateKeys(SampleKey.PASSWORD);
    Assert.assertEquals(1, keyPairs.size());
    KeyPair keyPair = keyPairs.get(0);
    Assert.assertEquals(WIF, keyPair.getPrivateKey());
    Assert.assertEquals(PUBLIC_KEY, keyPair.getPublicKey());
  }


}
