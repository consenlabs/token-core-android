package org.consenlabs.tokencore.wallet.keystore;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.common.base.Joiner;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.crypto.EncPair;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.crypto.Multihash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.DateUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.Metadata;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xyz on 2017/12/11.
 */

public class IdentityKeystore extends Keystore implements EncMnemonicKeystore {
  private static final int VERSION = 10000;

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setWalletIDs(List<String> walletIDs) {
    this.walletIDs = walletIDs;
  }

  public void setIpfsId(String ipfsId) {
    this.ipfsId = ipfsId;

  }


  public void setEncKey(String encKey) {
    this.encKey = encKey;
  }

  public void setEncAuthKey(EncPair encAuthKey) {
    this.encAuthKey = encAuthKey;
  }

  public void setMnemonicPath(String mnemonicPath) {
    this.mnemonicPath = mnemonicPath;
  }

  private String identifier;
  private String ipfsId;
  private String encKey;
  private EncPair encAuthKey;
  private EncPair encMnemonic;
  private String mnemonicPath;

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  private Metadata metadata;
  private List<String> walletIDs = new ArrayList<>();

  public IdentityKeystore() {

  }


  public IdentityKeystore(Metadata metadata, List<String> mnemonicCodes, String password) {
    MnemonicUtil.validateMnemonics(mnemonicCodes);

    DeterministicSeed seed = new DeterministicSeed(mnemonicCodes, null, "", 0L);

    DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed.getSeedBytes());
    byte[] masterKey = masterPrivateKey.getPrivKeyBytes();

    String salt = metadata.isMainNet() ? "Automatic Backup Key Mainnet" : "Automatic Backup Key Testnet";
    byte[] backupKey = Hash.hmacSHA256(masterKey, salt.getBytes(Charset.forName("ASCII")));
    byte[] authenticationKey = Hash.hmacSHA256(backupKey, "Authentication Key".getBytes(Charset.forName("UTF-8")));
    ECKey authKey = ECKey.fromPrivate(authenticationKey);

    NetworkParameters networkParameters = metadata.isMainNet() ? MainNetParams.get() : TestNet3Params.get();

    String aPubHashHex = NumericUtil.bytesToHex(authKey.getPubKeyHash());
    int networkHeader = networkParameters.getAddressHeader();
    int version = 2;
    // this magic hex will start with 'im' after base58check
    String magicHex = "0fdc0c";
    String fullIdentifier = String.format("%s%02x%02x%s", magicHex, (byte) networkHeader, (byte) version, aPubHashHex);
    byte[] fullIdentifierBytes = NumericUtil.hexToBytes(fullIdentifier);
    byte[] checksumBytes = Arrays.copyOfRange(Sha256Hash.hashTwice(fullIdentifierBytes), 0, 4);
    byte[] identifierWithChecksum = ByteUtil.concat(fullIdentifierBytes, checksumBytes);
    this.identifier = Base58.encode(identifierWithChecksum);

    byte[] encKeyFullBytes = Hash.hmacSHA256(backupKey, "Encryption Key".getBytes(Charset.forName("UTF-8")));
    this.encKey = NumericUtil.bytesToHex(encKeyFullBytes);

    ECKey ecKey = ECKey.fromPrivate(encKeyFullBytes, false);
    this.ipfsId = new Multihash(Multihash.Type.sha2_256, Hash.sha256(ecKey.getPubKey())).toBase58();
    Crypto crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, masterPrivateKey.serializePrivB58(networkParameters).getBytes(Charset.forName("UTF-8")));

    this.encAuthKey = crypto.deriveEncPair(password, authenticationKey);
    this.encMnemonic = crypto.deriveEncPair(password, Joiner.on(" ").join(mnemonicCodes).getBytes());
    crypto.clearCachedDerivedKey();
    metadata.setTimestamp(DateUtil.getUTCTime());

    metadata.setSegWit(null);
    this.metadata = metadata;
    this.crypto = crypto;

    this.version = VERSION;
    this.walletIDs = new ArrayList<>();
  }

  @Override
  public EncPair getEncMnemonic() {
    return this.encMnemonic;
  }

  @Override
  public void setEncMnemonic(EncPair encMnemonic) {
    this.encMnemonic = encMnemonic;
  }

  @Override
  public String getMnemonicPath() {
    return this.mnemonicPath;
  }

  public List<String> getWalletIDs() {
    return walletIDs;
  }

  @JsonGetter(value = "imTokenMeta")
  public Metadata getMetadata() {
    return metadata;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getIpfsId() {
    return ipfsId;
  }

  public String getEncKey() {
    return encKey;
  }

  public EncPair getEncAuthKey() {
    return encAuthKey;
  }
}
