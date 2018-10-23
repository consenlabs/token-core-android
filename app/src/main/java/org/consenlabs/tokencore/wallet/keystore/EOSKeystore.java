package org.consenlabs.tokencore.wallet.keystore;

import android.text.TextUtils;
import android.util.Pair;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.crypto.EncPair;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.DateUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainId;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.transaction.EOSKey;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class EOSKeystore extends IMTKeystore implements EncMnemonicKeystore {

  // the version = IdentityVersion + 1;
  static int VERSION = 10001;
  private static final String PERM_OWNER = "owner";
  private static final String PERM_ACTIVE = "active";


  private EncPair encMnemonic;
  private String mnemonicPath;
  private List<KeyPathPrivate> mKeyPathPrivates = new ArrayList<>();

  public List<KeyPathPrivate> getKeyPathPrivates() {
    return mKeyPathPrivates;
  }

  public void setKeyPathPrivates(List<KeyPathPrivate> keyPathPrivates) {
    this.mKeyPathPrivates = keyPathPrivates;
  }

  @Override
  public EncPair getEncMnemonic() {
    return encMnemonic;
  }

  @Override
  public void setEncMnemonic(EncPair encMnemonic) {
    this.encMnemonic = encMnemonic;
  }

  @Override
  public String getMnemonicPath() {
    return mnemonicPath;
  }

  @Override
  public Keystore changePassword(String oldPassword, String newPassword) {
    return null;
  }

  public EOSKeystore() {
    super();
  }

  public static EOSKeystore create(Metadata metadata, String password, String accountName, List<String> mnemonics, String path, List<PermissionObject> permissions) {
    return new EOSKeystore(metadata, password, accountName, mnemonics, path, permissions, "");
  }

  public static EOSKeystore create(Metadata metadata, String password, String accountName, List<String> mnemonics, String path, List<PermissionObject> permissions, String id) {
    return new EOSKeystore(metadata, password, accountName, mnemonics, path, permissions, id);
  }

  public static EOSKeystore create(Metadata metadata, String password, String accountName, List<String> prvKeys, List<PermissionObject> permissions) {
    return new EOSKeystore(metadata, password, accountName, prvKeys, permissions, "");
  }

  public List<KeyPair> exportPrivateKeys(String password) {
    List<KeyPair> keyPairs = new ArrayList<>(mKeyPathPrivates.size());
    crypto.cacheDerivedKey(password);
    for (KeyPathPrivate keyPathPrivate : mKeyPathPrivates) {
      KeyPair keyPair = new KeyPair();
      byte[] prvKeyBytes = crypto.decryptEncPair(password, keyPathPrivate.encPrivate);
      String wif = EOSKey.fromPrivate(prvKeyBytes).toBase58();
      keyPair.setPrivateKey(wif);
      keyPair.setPublicKey(keyPathPrivate.publicKey);
      keyPairs.add(keyPair);
    }
    crypto.clearCachedDerivedKey();
    return keyPairs;
  }

  private EOSKeystore(Metadata metadata, String password, String accountName, List<String> prvKeys, List<PermissionObject> permissions, String id) {

    this.crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, NumericUtil.generateRandomBytes(128));

    this.encMnemonic = null;
    this.mnemonicPath = null;
    if (metadata.getTimestamp() == 0) {
      metadata.setTimestamp(DateUtil.getUTCTime());
    }

    Set<String> permPubKeys = new HashSet<>(permissions.size());
    for (PermissionObject permissionObject : permissions) {
      permPubKeys.add(permissionObject.publicKey);
    }

    for (String prvKey : prvKeys) {

      EOSKey eosKey = EOSKey.fromWIF(prvKey);
      String pubKey = eosKey.getPublicKeyAsHex();

      if (!permPubKeys.contains(pubKey)) {
        throw new TokenException(Messages.EOS_PRIVATE_PUBLIC_NOT_MATCH);
      }
      KeyPathPrivate keyPath = new KeyPathPrivate();
      keyPath.publicKey = pubKey;
      keyPath.encPrivate = crypto.deriveEncPair(password, eosKey.getPrivateKey());
      keyPath.derivedMode = KeyPathPrivate.IMPORTED;
      this.mKeyPathPrivates.add(keyPath);

    }
    this.crypto.clearCachedDerivedKey();

    metadata.setWalletType(Metadata.RANDOM);
    this.metadata = metadata;
    this.version = VERSION;
    if (!Strings.isNullOrEmpty(accountName)) {
      setAccountName(accountName);
    }

    this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
  }


  private EOSKeystore(Metadata metadata, String password, String accountName, List<String> mnemonics, String path, List<PermissionObject> permissions, String id) {
    MnemonicUtil.validateMnemonics(mnemonics);
    this.mnemonicPath = path;

    List<KeyPath> allDefaultKeys = calcAllDefaultKeys(mnemonics);

    byte[] masterPrivateKeyBytes = NumericUtil.generateRandomBytes(16);

    if (metadata.getTimestamp() == 0) {
      metadata.setTimestamp(DateUtil.getUTCTime());
    }
    metadata.setWalletType(Metadata.HD);

    this.crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, masterPrivateKeyBytes);
    this.metadata = metadata;
    this.encMnemonic = crypto.deriveEncPair(password, Joiner.on(" ").join(mnemonics).getBytes());
    this.derivedKeyPath(allDefaultKeys, permissions, password);
    this.crypto.clearCachedDerivedKey();

    if (!Strings.isNullOrEmpty(accountName)) {
      setAccountName(accountName);
    }
    this.version = VERSION;
    this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
  }

  private void derivedKeyPath(List<KeyPath> keyPaths, List<PermissionObject> permissions, String password) {

    for (KeyPath keyPath : keyPaths) {
      EncPair encPair = crypto.deriveEncPair(password, keyPath.getPrivateKey());
      String publicKey = EOSKey.fromPrivate(keyPath.getPrivateKey()).getPublicKeyAsHex();
      String derivedMode = Strings.isNullOrEmpty(keyPath.getPath()) ? KeyPathPrivate.HD_SHA256 : KeyPathPrivate.PATH_DIRECTLY;
      KeyPathPrivate keyPathPrivate = new KeyPathPrivate(encPair, publicKey, keyPath.getPath(), derivedMode);
      mKeyPathPrivates.add(keyPathPrivate);
    }
    if (permissions != null) {
      for (PermissionObject perm : permissions) {
        if (PERM_OWNER.equalsIgnoreCase(perm.permission) || PERM_ACTIVE.equalsIgnoreCase(perm.permission)) {
          boolean isContainsPermission = false;
          for (KeyPathPrivate keyPathPrivate : mKeyPathPrivates) {
            if (keyPathPrivate.getPublicKey().equals(perm.publicKey)) {
              isContainsPermission = true;
            }
          }
          if (!isContainsPermission) {
            throw new TokenException(Messages.EOS_PRIVATE_PUBLIC_NOT_MATCH);
          }
        }
      }
    }
  }

  public byte[] decryptPrivateKeyFor(String pubKey, String password) {
    EncPair targetPair = null;
    for (KeyPathPrivate privatePath : mKeyPathPrivates) {
      if (privatePath.publicKey.equals(pubKey)) {
        targetPair = privatePath.encPrivate;
        break;
      }
    }
    if (targetPair == null) {
      throw new TokenException(Messages.EOS_PUBLIC_KEY_NOT_FOUND);
    }
    return this.crypto.decryptEncPair(password, targetPair);
  }

  public void setAccountName(String accountName) {
    if (!Strings.isNullOrEmpty(this.address) && !this.address.equals(accountName)) {
      throw new TokenException("Only can set accountName once in eos wallet");
    }
    if (!Pattern.matches("[1-5a-z.]{1,12}", accountName)) {
      throw new TokenException(Messages.EOS_ACCOUNT_NAME_INVALID);
    }
    this.address = accountName;
  }

  // calc default keys
  private List<KeyPath> calcAllDefaultKeys(List<String> mnemonics) {
    DeterministicSeed seed = new DeterministicSeed(mnemonics, null, "", 0L);
    DeterministicKeyChain rootKeyChain = DeterministicKeyChain.builder().seed(seed).build();

    List<KeyPath> defaultKeys = calcDefaultKeys(rootKeyChain, this.mnemonicPath);

    return defaultKeys;
  }


  private static List<KeyPath> calcDefaultKeys(DeterministicKeyChain rootKeyChain, String path) {

    String[] subpaths = path.split(",");
    List<KeyPath> result = new ArrayList<>(subpaths.length);
    for (String subpath : subpaths) {
      byte[] prvKeyBytes = rootKeyChain.getKeyByPath(BIP44Util.generatePath(subpath), true).getPrivKeyBytes();
      result.add(new KeyPath(prvKeyBytes, subpath));
    }
    return result;
  }

  public static class KeyPathPrivate {
    public final static String PATH_DIRECTLY = "PATH_DIRECTLY";
    public final static String IMPORTED = "IMPORTED";
    public final static String HD_SHA256 = "HD_SHA256";

    private EncPair encPrivate;
    private String publicKey;
    private String path;
    private String derivedMode;

    public KeyPathPrivate() {
    }

    public KeyPathPrivate(EncPair encPrivate, String publicKey, String path, String derivedMode) {
      this.encPrivate = encPrivate;
      this.publicKey = publicKey;
      this.path = path;
      this.derivedMode = derivedMode;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public String getDerivedMode() {
      return derivedMode;
    }

    public void setDerivedMode(String derivedMode) {
      this.derivedMode = derivedMode;
    }

    public EncPair getEncPrivate() {
      return encPrivate;
    }

    public void setEncPrivate(EncPair encPrivate) {
      this.encPrivate = encPrivate;
    }

    public String getPublicKey() {
      return publicKey;
    }

    public void setPublicKey(String publicKey) {
      this.publicKey = publicKey;
    }
  }

  public static class KeyPath {
    byte[] privateKey;
    String path;

    public KeyPath(byte[] privateKey, String path) {
      this.privateKey = privateKey;
      this.path = path;
    }

    public byte[] getPrivateKey() {
      return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
      this.privateKey = privateKey;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }
  }

  public static class PermissionObject {
    String permission;
    String publicKey;
    String parent;

    public String getParent() {
      return parent;
    }

    public void setParent(String parent) {
      this.parent = parent;
    }

    public String getPermission() {
      return permission;
    }

    public void setPermission(String permission) {
      this.permission = permission;
    }

    public String getPublicKey() {
      return publicKey;
    }

    public void setPublicKey(String publicKey) {
      this.publicKey = publicKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PermissionObject that = (PermissionObject) o;
      return Objects.equals(permission, that.permission) &&
          Objects.equals(publicKey, that.publicKey) &&
          Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {

      return Objects.hash(permission, publicKey, parent);
    }
  }


}
