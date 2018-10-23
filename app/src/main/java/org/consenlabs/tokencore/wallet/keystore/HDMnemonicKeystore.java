package org.consenlabs.tokencore.wallet.keystore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.subgraph.orchid.encoders.Hex;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.foundation.crypto.AES;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.crypto.EncPair;
import org.consenlabs.tokencore.foundation.utils.DateUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by xyz on 2018/2/5.
 */

public final class HDMnemonicKeystore extends IMTKeystore implements EncMnemonicKeystore {

  // !!! Don't use this key in production !!!
  public static String XPubCommonKey128 = "B888D25EC8C12BD5043777B1AC49F872";
  public static String XPubCommonIv = "9C0C30889CBCC5E01AB5B2BB88715799";

  static int VERSION = 44;
  private EncPair encMnemonic;
  private String mnemonicPath;
  private String xpub;

  public Info getInfo() {
    return info;
  }

  public void setInfo(Info info) {
    this.info = info;
  }

  private Info info;

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

  public void setMnemonicPath(String mnemonicPath) {
    this.mnemonicPath = mnemonicPath;
  }

  public String getXpub() {
    return this.xpub;
  }

  public void setXpub(String xpub) {
    this.xpub = xpub;
  }

  public HDMnemonicKeystore() {
    super();
  }

  public static HDMnemonicKeystore create(Metadata metadata, String password, List<String> mnemonics, String path) {
    return new HDMnemonicKeystore(metadata, password, mnemonics, path, "");
  }

  public HDMnemonicKeystore(Metadata metadata, String password, List<String> mnemonics, String path, String id) {
    MnemonicUtil.validateMnemonics(mnemonics);
    DeterministicSeed seed = new DeterministicSeed(mnemonics, null, "", 0L);
    DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
    this.mnemonicPath = path;

    DeterministicKey parent = keyChain.getKeyByPath(BIP44Util.generatePath(path), true);
    NetworkParameters networkParameters = metadata.isMainNet() ? MainNetParams.get() : TestNet3Params.get();
    this.xpub = parent.serializePubB58(networkParameters);
    String xprv = parent.serializePrivB58(networkParameters);
    DeterministicKey mainAddressKey = keyChain.getKeyByPath(BIP44Util.generatePath(path + "/0/0"), true);
    if (Metadata.P2WPKH.equals(metadata.getSegWit())) {
      this.address = new SegWitBitcoinAddressCreator(networkParameters).fromPrivateKey(mainAddressKey.getPrivateKeyAsHex());
    } else {
      this.address = mainAddressKey.toAddress(networkParameters).toBase58();
    }
    if (metadata.getTimestamp() == 0) {
      metadata.setTimestamp(DateUtil.getUTCTime());
    }
    metadata.setWalletType(Metadata.HD);

    this.crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, xprv.getBytes(Charset.forName("UTF-8")));
    this.metadata = metadata;
    this.encMnemonic = crypto.deriveEncPair(password, Joiner.on(" ").join(mnemonics).getBytes());
    this.crypto.clearCachedDerivedKey();

    this.version = VERSION;
    this.info = new Info();
    this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
  }


  @Override
  public Keystore changePassword(String oldPassword, String newPassword) {
    String mnemonic = new String(getCrypto().decryptEncPair(oldPassword, encMnemonic));
    List<String> mnemonicCodes = Arrays.asList(mnemonic.split(" "));
    return new HDMnemonicKeystore(metadata, newPassword, mnemonicCodes, this.mnemonicPath, this.id);
  }

  @JsonIgnore
  public String getEncryptXPub() {
    String plainText = this.xpub;
    try {

      byte[] commonKey128 = Hex.decode(XPubCommonKey128);
      byte[] clean = plainText.getBytes();
      byte[] commonIv = Hex.decode(XPubCommonIv);
      byte[] encrypted = AES.encryptByCBC(clean, commonKey128, commonIv);
      return BaseEncoding.base64().encode(encrypted);
    } catch (Exception ex) {
      throw new TokenException(Messages.ENCRYPT_XPUB_ERROR);
    }
  }

  public String newReceiveAddress(int nextIdx) {
    NetworkParameters networkParameters = this.metadata.isMainNet() ? MainNetParams.get() : TestNet3Params.get();
    DeterministicKey key = DeterministicKey.deserializeB58(this.xpub, networkParameters);
    DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(key, ChildNumber.ZERO);
    DeterministicKey indexKey = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(nextIdx));
    if (Metadata.P2WPKH.equals(metadata.getSegWit())) {
      return new SegWitBitcoinAddressCreator(networkParameters).fromPrivateKey(indexKey).toBase58();
    } else {
      return indexKey.toAddress(networkParameters).toBase58();
    }
  }


  public static class Info {
    private String curve = "spec256k1";
    private String purpuse = "sign";

    public Info() {

    }

    public String getCurve() {
      return curve;
    }

    public void setCurve(String curve) {
      this.curve = curve;
    }

    public String getPurpuse() {
      return purpuse;
    }

    public void setPurpuse(String purpuse) {
      this.purpuse = purpuse;
    }
  }
}
