package org.consenlabs.tokencore.wallet.keystore;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.crypto.EncPair;
import org.consenlabs.tokencore.foundation.utils.DateUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.address.AddressCreatorManager;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by xyz on 2018/2/5.
 */

public class V3MnemonicKeystore extends IMTKeystore implements EncMnemonicKeystore, ExportableKeystore {
  private static final int VERSION = 3;
  private EncPair encMnemonic;
  private String mnemonicPath;

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
    return this.mnemonicPath;
  }

  public void setMnemonicPath(String mnemonicPath) {
    this.mnemonicPath = mnemonicPath;
  }

  public V3MnemonicKeystore() {
    super();
  }

  public static V3MnemonicKeystore create(Metadata metadata, String password, List<String> mnemonicCodes, String path) {
    return new V3MnemonicKeystore(metadata, password, mnemonicCodes, path, "");
  }


  private V3MnemonicKeystore(Metadata metadata, String password, List<String> mnemonicCodes, String path, String id) {
    MnemonicUtil.validateMnemonics(mnemonicCodes);
    DeterministicSeed seed = new DeterministicSeed(mnemonicCodes, null, "", 0L);
    DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();

    this.mnemonicPath = path;
    List<ChildNumber> zeroPath = BIP44Util.generatePath(path);

    byte[] prvKeyBytes = keyChain.getKeyByPath(zeroPath, true).getPrivKeyBytes();
    this.crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, prvKeyBytes);
    this.encMnemonic = crypto.deriveEncPair(password, Joiner.on(" ").join(mnemonicCodes).getBytes());
    this.crypto.clearCachedDerivedKey();

    this.address = AddressCreatorManager.getInstance(metadata.getChainType(), metadata.isMainNet(), metadata.getSegWit()).fromPrivateKey(prvKeyBytes);
    metadata.setTimestamp(DateUtil.getUTCTime());
    metadata.setWalletType(Metadata.V3);
    this.metadata = metadata;
    this.version = VERSION;
    this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
  }



  @Override
  public Keystore changePassword(String oldPassword, String newPassword) {
    String mnemonic = new String(getCrypto().decryptEncPair(oldPassword, this.getEncMnemonic()));
    List<String> mnemonicCodes = Arrays.asList(mnemonic.split(" "));
    return new V3MnemonicKeystore(this.metadata, newPassword, mnemonicCodes, this.mnemonicPath, this.id);
  }
}
