package org.consenlabs.tokencore.wallet;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.address.AddressCreatorManager;
import org.consenlabs.tokencore.wallet.address.EthereumAddressCreator;
import org.consenlabs.tokencore.wallet.keystore.EOSKeystore;
import org.consenlabs.tokencore.wallet.keystore.HDMnemonicKeystore;
import org.consenlabs.tokencore.wallet.keystore.IMTKeystore;
import org.consenlabs.tokencore.wallet.keystore.Keystore;
import org.consenlabs.tokencore.wallet.keystore.LegacyEOSKeystore;
import org.consenlabs.tokencore.wallet.keystore.V3Keystore;
import org.consenlabs.tokencore.wallet.keystore.V3MnemonicKeystore;
import org.consenlabs.tokencore.wallet.keystore.WalletKeystore;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.transaction.EOSKey;
import org.consenlabs.tokencore.wallet.validators.PrivateKeyValidator;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.Nullable;


public class WalletManager {
  private static Hashtable<String, IMTKeystore> keystoreMap = new Hashtable<>();
  private static final String LOG_TAG = WalletManager.class.getSimpleName();

  public static KeystoreStorage storage;
//
//  static {
//    try {
//      scanWallets();
//    } catch (IOException ignored) {
//    }
//  }

  static Wallet createWallet(IMTKeystore keystore) {
    File file = generateWalletFile(keystore.getId());
    writeToFile(keystore, file);
    keystoreMap.put(keystore.getId(), keystore);
    return new Wallet(keystore);
  }

  public static void changePassword(String id, String oldPassword, String newPassword) {
    IMTKeystore keystore = mustFindKeystoreById(id);
    IMTKeystore newKeystore = (IMTKeystore) keystore.changePassword(oldPassword, newPassword);
    flushWallet(newKeystore, true);
  }

  public static String exportPrivateKey(String id, String password) {
    Wallet wallet = mustFindWalletById(id);
    return wallet.exportPrivateKey(password);
  }

  public static List<KeyPair> exportPrivateKeys(String id, String password) {
    Wallet wallet = mustFindWalletById(id);
    return wallet.exportPrivateKeys(password);
  }

  public static MnemonicAndPath exportMnemonic(String id, String password) {
    Wallet wallet = mustFindWalletById(id);
    return wallet.exportMnemonic(password);
  }

  public static String exportKeystore(String id, String password) {
    Wallet wallet = mustFindWalletById(id);
    return wallet.exportKeystore(password);
  }

  public static void removeWallet(String id, String password) {
    Wallet wallet = mustFindWalletById(id);
    if (!wallet.verifyPassword(password)) {
      throw new TokenException(Messages.WALLET_INVALID_PASSWORD);
    }
    if (wallet.delete(password)) {
      Identity.getCurrentIdentity().removeWallet(id);
      keystoreMap.remove(id);
    }
  }

  public static void clearKeystoreMap() {
    keystoreMap.clear();
  }

  public static Wallet importWalletFromKeystore(Metadata metadata, String keystoreContent, String password, boolean overwrite) {
    WalletKeystore importedKeystore = validateKeystore(keystoreContent, password);

    if (metadata.getSource() == null)
      metadata.setSource(Metadata.FROM_KEYSTORE);

    String privateKey = NumericUtil.bytesToHex(importedKeystore.decryptCiphertext(password));
    try {
      new PrivateKeyValidator(privateKey).validate();
    } catch (TokenException ex) {
      if (Messages.PRIVATE_KEY_INVALID.equals(ex.getMessage())) {
        throw new TokenException(Messages.KEYSTORE_CONTAINS_INVALID_PRIVATE_KEY);
      } else {
        throw ex;
      }
    }
    return importWalletFromPrivateKey(metadata, privateKey, password, overwrite);
  }

  public static Wallet importWalletFromPrivateKey(Metadata metadata, String prvKeyHex, String password, boolean overwrite) {
    IMTKeystore keystore = V3Keystore.create(metadata, password, prvKeyHex);
    Wallet wallet = flushWallet(keystore, overwrite);
    Identity.getCurrentIdentity().addWallet(wallet);
    return wallet;
  }

  /**
   * Just for import EOS wallet
   */
  public static Wallet importWalletFromPrivateKeys(Metadata metadata, String accountName, List<String> prvKeys, List<EOSKeystore.PermissionObject> permissions, String password, boolean overwrite) {
    IMTKeystore keystore = null;
    if (!ChainType.EOS.equalsIgnoreCase(metadata.getChainType())) {
      throw new TokenException("This method is only for importing EOS wallet");
    }
    keystore = EOSKeystore.create(metadata, password, accountName, prvKeys, permissions);
    return persistWallet(keystore, overwrite);
  }

  /**
   * use the importWalletFromPrivateKeys
   */
  @Deprecated
  public static Wallet importWalletFromPrivateKey(Metadata metadata, String accountName, String prvKeyHex, String password, boolean overwrite) {
    IMTKeystore keystore = LegacyEOSKeystore.create(metadata, accountName, password, prvKeyHex);
    return persistWallet(keystore, overwrite);
  }


  /**
   * import wallet from mnemonic
   *
   * @param metadata
   * @param accountName only for EOS
   * @param mnemonic
   * @param path
   * @param permissions only for EOS
   * @param password
   * @param overwrite
   * @return
   */
  public static Wallet importWalletFromMnemonic(Metadata metadata, @Nullable String accountName, String mnemonic, String path, @Nullable List<EOSKeystore.PermissionObject> permissions, String password, boolean overwrite) {

    if (metadata.getSource() == null)
      metadata.setSource(Metadata.FROM_MNEMONIC);
    IMTKeystore keystore = null;
    List<String> mnemonicCodes = Arrays.asList(mnemonic.split(" "));
    MnemonicUtil.validateMnemonics(mnemonicCodes);
    switch (metadata.getChainType()) {
      case ChainType.ETHEREUM:
        keystore = V3MnemonicKeystore.create(metadata, password, mnemonicCodes, path);
        break;
      case ChainType.BITCOIN:
        keystore = HDMnemonicKeystore.create(metadata, password, mnemonicCodes, path);
        break;
      case ChainType.EOS:
        keystore = EOSKeystore.create(metadata, password, accountName, mnemonicCodes, path, permissions);
    }

    return persistWallet(keystore, overwrite);
  }

  public static Wallet importWalletFromMnemonic(Metadata metadata, String mnemonic, String path, String password, boolean overwrite) {
    return importWalletFromMnemonic(metadata, null, mnemonic, path, null, password, overwrite);
  }

  public static Wallet findWalletByPrivateKey(String chainType, String network, String privateKey, String segWit) {
    if (ChainType.ETHEREUM.equals(chainType)) {
      new PrivateKeyValidator(privateKey).validate();
    }
    Network net = new Network(network);
    String address = AddressCreatorManager.getInstance(chainType, net.isMainnet(), segWit).fromPrivateKey(privateKey);
    return findWalletByAddress(chainType, address);
  }

  public static Wallet findWalletByKeystore(String chainType, String keystoreContent, String password) {
    WalletKeystore walletKeystore = validateKeystore(keystoreContent, password);

    byte[] prvKeyBytes = walletKeystore.decryptCiphertext(password);
    String address = new EthereumAddressCreator().fromPrivateKey(prvKeyBytes);
    return findWalletByAddress(chainType, address);
  }

  public static Wallet findWalletByMnemonic(String chainType, String network, String mnemonic, String path, String segWit) {
    List<String> mnemonicCodes = Arrays.asList(mnemonic.split(" "));
    MnemonicUtil.validateMnemonics(mnemonicCodes);
    DeterministicSeed seed = new DeterministicSeed(mnemonicCodes, null, "", 0L);
    DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
    if (Strings.isNullOrEmpty(path)) {
      throw new TokenException(Messages.INVALID_MNEMONIC_PATH);
    }

    if (ChainType.BITCOIN.equalsIgnoreCase(chainType)) {
      path += "/0/0";
    }

    DeterministicKey key = keyChain.getKeyByPath(BIP44Util.generatePath(path), true);
    Network net = new Network(network);
    String address = AddressCreatorManager.getInstance(chainType, net.isMainnet(), segWit).fromPrivateKey(key.getPrivateKeyAsHex());
    return findWalletByAddress(chainType, address);
  }

  public static Wallet switchBTCWalletMode(String id, String password, String model) {
    Wallet wallet = mustFindWalletById(id);
    // !!! Warning !!! You must verify password before you write content to keystore
    if (!wallet.getMetadata().getChainType().equalsIgnoreCase(ChainType.BITCOIN))
      throw new TokenException("Ethereum wallet can't switch mode");
    Metadata metadata = wallet.getMetadata().clone();
    if (metadata.getSegWit().equalsIgnoreCase(model)) {
      return wallet;
    }

    metadata.setSegWit(model);
    IMTKeystore keystore;
    if (wallet.hasMnemonic()) {

      MnemonicAndPath mnemonicAndPath = wallet.exportMnemonic(password);
      String path = BIP44Util.getBTCMnemonicPath(model, metadata.isMainNet());
      List<String> mnemonicCodes = Arrays.asList(mnemonicAndPath.getMnemonic().split(" "));
      keystore = new HDMnemonicKeystore(metadata, password, mnemonicCodes, path, wallet.getId());
    } else {
      String prvKey = wallet.exportPrivateKey(password);
      keystore = new V3Keystore(metadata, password, prvKey, wallet.getId());

    }
    flushWallet(keystore, false);
    keystoreMap.put(wallet.getId(), keystore);
    return new Wallet(keystore);
  }

  public static Wallet setAccountName(String id, String accountName) {
    Wallet wallet = mustFindWalletById(id);
    wallet.setAccountName(accountName);
    return persistWallet(wallet.getKeystore(), true);
  }

  static Wallet findWalletById(String id) {
    IMTKeystore keystore = keystoreMap.get(id);
    if (keystore != null) {
      return new Wallet(keystore);
    } else {
      return null;
    }
  }

  public static Wallet mustFindWalletById(String id) {
    IMTKeystore keystore = keystoreMap.get(id);
    if (keystore == null) throw new TokenException(Messages.WALLET_NOT_FOUND);
    return new Wallet(keystore);
  }


  static File generateWalletFile(String walletID) {
    return new File(getDefaultKeyDirectory(), walletID + ".json");
  }


  static File getDefaultKeyDirectory() {
    File directory = new File(storage.getKeystoreDir(), "wallets");
    if (!directory.exists()) {
      directory.mkdirs();
    }
    return directory;
  }

  static boolean cleanKeystoreDirectory() {
    return deleteDir(getDefaultKeyDirectory());
  }

  private static Wallet persistWallet(IMTKeystore keystore, boolean overwrite) {
    Wallet wallet = flushWallet(keystore, overwrite);
    Identity.getCurrentIdentity().addWallet(wallet);
    return wallet;
  }

  private static IMTKeystore findKeystoreByAddress(String type, String address) {
    if (Strings.isNullOrEmpty(address)) return null;

    for (IMTKeystore keystore : keystoreMap.values()) {

      if (Strings.isNullOrEmpty(keystore.getAddress())) {
        continue;
      }

      if (keystore.getMetadata().getChainType().equals(type) && keystore.getAddress().equals(address)) {
        return keystore;
      }
    }

    return null;
  }

  public static Wallet findWalletByAddress(String type, String address) {
    IMTKeystore keystore = findKeystoreByAddress(type, address);
    if (keystore != null) {
      return new Wallet(keystore);
    }
    return null;
  }


  private static Wallet flushWallet(IMTKeystore keystore, boolean overwrite) {

    IMTKeystore existsKeystore = findKeystoreByAddress(keystore.getMetadata().getChainType(), keystore.getAddress());
    if (existsKeystore != null) {
      if (!overwrite) {
        throw new TokenException(Messages.WALLET_EXISTS);
      } else {
        keystore.setId(existsKeystore.getId());
      }
    }

    File file = generateWalletFile(keystore.getId());
    writeToFile(keystore, file);
    keystoreMap.put(keystore.getId(), keystore);
    return new Wallet(keystore);
  }

  private static void writeToFile(Keystore keyStore, File destination) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.writeValue(destination, keyStore);
    } catch (IOException ex) {
      throw new TokenException(Messages.WALLET_STORE_FAIL, ex);
    }
  }

  private static boolean deleteDir(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (String child : children) {
        boolean success = deleteDir(new File(dir, child));
        if (!success) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  private static V3Keystore validateKeystore(String keystoreContent, String password) {
    V3Keystore importedKeystore = unmarshalKeystore(keystoreContent, V3Keystore.class);
    if (Strings.isNullOrEmpty(importedKeystore.getAddress()) || importedKeystore.getCrypto() == null) {
      throw new TokenException(Messages.WALLET_INVALID_KEYSTORE);
    }

    importedKeystore.getCrypto().validate();

    if (!importedKeystore.verifyPassword(password))
      throw new TokenException(Messages.MAC_UNMATCH);

    byte[] prvKey = importedKeystore.decryptCiphertext(password);
    String address = new EthereumAddressCreator().fromPrivateKey(prvKey);
    if (Strings.isNullOrEmpty(address) || !address.equalsIgnoreCase(importedKeystore.getAddress())) {
      throw new TokenException(Messages.PRIVATE_KEY_ADDRESS_NOT_MATCH);
    }
    return importedKeystore;
  }

  private static IMTKeystore mustFindKeystoreById(String id) {
    IMTKeystore keystore = keystoreMap.get(id);
    if (keystore == null) {
      throw new TokenException(Messages.WALLET_NOT_FOUND);
    }

    return keystore;
  }

  private static <T extends WalletKeystore> T unmarshalKeystore(String keystoreContent, Class<T> clazz) {
    T importedKeystore;
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
      importedKeystore = mapper.readValue(keystoreContent, clazz);
    } catch (IOException ex) {
      throw new TokenException(Messages.WALLET_INVALID_KEYSTORE, ex);
    }
    return importedKeystore;
  }

  public static void scanWallets() {
    File directory = getDefaultKeyDirectory();

    keystoreMap.clear();
    for (File file : directory.listFiles()) {
      if (!file.getName().startsWith("identity")) {
        try {
          IMTKeystore keystore = null;
          CharSource charSource = Files.asCharSource(file, Charset.forName("UTF-8"));
          String jsonContent = charSource.read();
          JSONObject jsonObject = new JSONObject(jsonContent);
          int version = jsonObject.getInt("version");
          if (version == 3) {
            if (jsonContent.contains("encMnemonic")) {
              keystore = unmarshalKeystore(jsonContent, V3MnemonicKeystore.class);
            } else if (jsonObject.has("imTokenMeta") && ChainType.EOS.equals(jsonObject.getJSONObject("imTokenMeta").getString("chainType"))) {
              keystore = unmarshalKeystore(jsonContent, LegacyEOSKeystore.class);
            } else {
              keystore = unmarshalKeystore(jsonContent, V3Keystore.class);
            }
          } else if (version == 1) {
            keystore = unmarshalKeystore(jsonContent, V3Keystore.class);
          } else if (version == 44) {
            keystore = unmarshalKeystore(jsonContent, HDMnemonicKeystore.class);
          } else if (version == 10001) {
            keystore = unmarshalKeystore(jsonContent, EOSKeystore.class);
          }

          if (keystore != null) {
            keystoreMap.put(keystore.getId(), keystore);
          }
        } catch (Exception ex) {
          Log.e(LOG_TAG, "Can't loaded " + file.getName() + " file", ex);
        }
      }
    }
  }

  private WalletManager() {
  }
}
