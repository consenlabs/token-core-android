package org.consenlabs.tokencore.wallet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.consenlabs.tokencore.foundation.crypto.AES;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.crypto.Multihash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.keystore.EOSKeystore;
import org.consenlabs.tokencore.wallet.keystore.HDMnemonicKeystore;
import org.consenlabs.tokencore.wallet.keystore.IMTKeystore;
import org.consenlabs.tokencore.wallet.keystore.IdentityKeystore;
import org.consenlabs.tokencore.wallet.keystore.V3MnemonicKeystore;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.transaction.EthereumSign;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by xyz on 2017/12/11.
 */

public class Identity {

  public static Identity currentIdentity;
  public static final String TAG = "Identity";
  private static final String FILE_NAME = "identity.json";

  private IdentityKeystore keystore;
  private List<Wallet> wallets = new ArrayList<>();

  public String getIdentifier() {
    return this.keystore.getIdentifier();
  }

  public String getIpfsId() {
    return this.keystore.getIpfsId();
  }

  public List<Wallet> getWallets() {
    return this.wallets;
  }

  public Metadata getMetadata() {
    return keystore.getMetadata();
  }

  public static Identity getCurrentIdentity() {
    synchronized (Identity.class) {
      if (currentIdentity == null) {
        currentIdentity = tryLoadFromFile();
      }
      return currentIdentity;
    }
  }

  private Identity(IdentityKeystore keystore) {
    this.keystore = keystore;
    for (String walletId : this.keystore.getWalletIDs()) {
      wallets.add(WalletManager.findWalletById(walletId));
    }
  }

  private Identity(Metadata metadata, List<String> mnemonicCodes, String password) {
    String segWit = metadata.getSegWit();
    this.keystore = new IdentityKeystore(metadata, mnemonicCodes, password);
    addWallet(deriveEthereumWallet(mnemonicCodes, password));
    addWallet(deriveBitcoinWallet(mnemonicCodes, password, segWit));
    currentIdentity = this;
  }


  public static Identity createIdentity(String name, String password, String passwordHit, String network, String segWit) {
    List<String> mnemonicCodes = MnemonicUtil.randomMnemonicCodes();
    Metadata metadata = new Metadata();
    metadata.setName(name);
    metadata.setPasswordHint(passwordHit);
    metadata.setSource(Metadata.FROM_NEW_IDENTITY);
    metadata.setNetwork(network);
    metadata.setSegWit(segWit);
    Identity identity = new Identity(metadata, mnemonicCodes, password);
    currentIdentity = identity;
    return identity;
  }

  public static Identity recoverIdentity(String mnemonic, String name, String password,
                                         String passwordHit, String network, String segWit) {
    List<String> mnemonicCodes = Arrays.asList(mnemonic.split(" "));
    Metadata metadata = new Metadata();
    metadata.setName(name);
    metadata.setPasswordHint(passwordHit);
    metadata.setSource(Metadata.FROM_RECOVERED_IDENTITY);
    metadata.setNetwork(network);
    metadata.setSegWit(segWit);
    Identity identity = new Identity(metadata, mnemonicCodes, password);
    currentIdentity = identity;
    return identity;
  }

  public void deleteIdentity(String password) {
    if (!this.keystore.verifyPassword(password)) {
      throw new TokenException(Messages.WALLET_INVALID_PASSWORD);
    }

    if (WalletManager.cleanKeystoreDirectory()) {
      WalletManager.clearKeystoreMap();
      currentIdentity = null;
    }
  }

  public String exportIdentity(String password) {
    return this.keystore.decryptMnemonic(password);
  }

  public void addWallet(Wallet wallet) {
    this.keystore.getWalletIDs().add(wallet.getId());
    this.wallets.add(wallet);
    flush();
  }

  void removeWallet(String walletId) {
    this.keystore.getWalletIDs().remove(walletId);

    int idx = 0;
    for (; idx < wallets.size(); idx++) {
      if (wallets.get(idx).getId().equals(walletId)) {
        break;
      }
    }
    this.wallets.remove(idx);
    flush();
  }

  public List<Wallet> deriveWallets(List<String> chainTypes, String password) {
    List<Wallet> wallets = new ArrayList<>();
    String mnemonic = exportIdentity(password);
    List<String> mnemonics = Arrays.asList(mnemonic.split(" "));
    for (String chainType : chainTypes) {
      Wallet wallet;
      switch (chainType) {
        case ChainType.BITCOIN:
          wallet = deriveBitcoinWallet(mnemonics, password, this.getMetadata().getSegWit());
          break;
        case ChainType.ETHEREUM:
          wallet = deriveEthereumWallet(mnemonics, password);
          break;
        case ChainType.EOS:
          wallet = deriveEOSWallet(mnemonics, password);
          break;
          default:
            throw new TokenException(String.format("Doesn't support deriving %s wallet", chainType));
      }
      addWallet(wallet);
      wallets.add(wallet);
    }

    return wallets;
  }


  private static Identity tryLoadFromFile() {
    try {
      File file = new File(WalletManager.getDefaultKeyDirectory(), FILE_NAME);
      ObjectMapper mapper = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
      IdentityKeystore keystore = mapper.readValue(file, IdentityKeystore.class);
      return new Identity(keystore);
    } catch (IOException ignored) {
      return null;
    }
  }

  private void flush() {
    try {
      File file = new File(WalletManager.getDefaultKeyDirectory(), FILE_NAME);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.writeValue(file, this.keystore);
    } catch (IOException ex) {
      throw new TokenException(Messages.WALLET_STORE_FAIL, ex);
    }
  }

  private Wallet deriveBitcoinWallet(List<String> mnemonicCodes, String password, String segWit) {
    Metadata walletMetadata = new Metadata();
    walletMetadata.setChainType(ChainType.BITCOIN);
    walletMetadata.setPasswordHint(this.getMetadata().getPasswordHint());
    walletMetadata.setSource(this.getMetadata().getSource());
    walletMetadata.setNetwork(this.getMetadata().getNetwork());
    walletMetadata.setName("BTC");
    walletMetadata.setSegWit(segWit);
    String path;
    if (Metadata.P2WPKH.equals(segWit)) {
      path =  this.getMetadata().isMainNet() ? BIP44Util.BITCOIN_SEGWIT_MAIN_PATH : BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH;
    } else {
      path = this.getMetadata().isMainNet() ? BIP44Util.BITCOIN_MAINNET_PATH : BIP44Util.BITCOIN_TESTNET_PATH;
    }

    IMTKeystore keystore = HDMnemonicKeystore.create(walletMetadata, password, mnemonicCodes, path);
    return WalletManager.createWallet(keystore);
  }

  private Wallet deriveEthereumWallet(List<String> mnemonics, String password) {
    Metadata walletMetadata = new Metadata();
    walletMetadata.setChainType(ChainType.ETHEREUM);
    walletMetadata.setPasswordHint(this.getMetadata().getPasswordHint());
    walletMetadata.setSource(this.getMetadata().getSource());
    walletMetadata.setName("ETH");
    IMTKeystore keystore = V3MnemonicKeystore.create(walletMetadata, password, mnemonics, BIP44Util.ETHEREUM_PATH);
    return WalletManager.createWallet(keystore);
  }

  private Wallet deriveEOSWallet(List<String> mnemonics, String password) {
    Metadata metadata = new Metadata();
    metadata.setChainType(ChainType.EOS);
    metadata.setPasswordHint(this.getMetadata().getPasswordHint());
    metadata.setSource(this.getMetadata().getSource());
    metadata.setName("EOS");
    IMTKeystore keystore = EOSKeystore.create(metadata, password, "", mnemonics, BIP44Util.EOS_LEDGER, null);
    return WalletManager.createWallet(keystore);
  }

  public String encryptDataToIPFS(String originData) {
    long unixTimestamp = Utils.currentTimeSeconds();
    byte[] iv = NumericUtil.generateRandomBytes(16);
    return encryptDataToIPFS(originData, unixTimestamp, iv);
  }

  String encryptDataToIPFS(String originData, long unixtime, byte[] iv) {
    int headerLength = 21;
    byte[] toSign = new byte[headerLength + 32];
    byte version = 0x03;
    toSign[0] = version;
    byte[] timestamp = new byte[4];

    Utils.uint32ToByteArrayLE(unixtime, timestamp, 0);
    System.arraycopy(timestamp, 0, toSign, 1, 4);
    byte[] encryptionKey = NumericUtil.hexToBytes(this.keystore.getEncKey());

    System.arraycopy(iv, 0, toSign, 5, 16);

    byte[] encKey = Arrays.copyOf(encryptionKey, 16);
    byte[] ciphertext = AES.encryptByCBC(originData.getBytes(Charset.forName("UTF-8")), encKey, iv);
    VarInt ciphertextLength = new VarInt(ciphertext.length);

    System.arraycopy(Hash.merkleHash(ciphertext), 0, toSign, headerLength, 32);
    String signature = EthereumSign.sign(NumericUtil.bytesToHex(toSign), encryptionKey);
    byte[] signatureBytes = NumericUtil.hexToBytes(signature);
    int totalLen = (int) (headerLength + ciphertextLength.getSizeInBytes() + ciphertextLength.value + 65);
    byte[] payload = new byte[totalLen];
    int destPos = 0;
    System.arraycopy(toSign, 0, payload, destPos, headerLength);
    destPos += headerLength;
    System.arraycopy(ciphertextLength.encode(), 0, payload, destPos, ciphertextLength.getSizeInBytes());
    destPos += ciphertextLength.getSizeInBytes();
    System.arraycopy(ciphertext, 0, payload, destPos, (int) ciphertextLength.value);
    destPos += (int) ciphertextLength.value;

    System.arraycopy(signatureBytes, 0, payload, destPos, 65);
    return NumericUtil.bytesToHex(payload);

  }

  public String decryptDataFromIPFS(String encryptedData) {
    int headerLength = 21;

    byte[] payload = NumericUtil.hexToBytes(encryptedData);

    byte version = payload[0];
    if (version != 0x03) {
      throw new TokenException(Messages.UNSUPPORT_ENCRYPTION_DATA_VERSION);
    }
    int srcPos = 1;
    byte[] toSign = new byte[headerLength + 32];
    System.arraycopy(payload, 0, toSign, 0, headerLength);

    byte[] timestamp = new byte[4];
    System.arraycopy(payload, srcPos, timestamp, 0, 4);
    srcPos += 4;

    byte[] encryptionKey = NumericUtil.hexToBytes(this.keystore.getEncKey());
    byte[] iv = new byte[16];
    System.arraycopy(payload, srcPos, iv, 0, 16);
    srcPos += 16;
    VarInt ciphertextLength = new VarInt(payload, srcPos);
    srcPos += ciphertextLength.getSizeInBytes();
    byte[] ciphertext = new byte[(int) ciphertextLength.value];
    System.arraycopy(payload, srcPos, ciphertext, 0, (int) ciphertextLength.value);
    System.arraycopy(Hash.merkleHash(ciphertext), 0, toSign, headerLength, 32);
    srcPos += ciphertextLength.value;
    byte[] encKey = Arrays.copyOf(encryptionKey, 16);
    String content = new String(AES.decryptByCBC(ciphertext, encKey, iv), Charset.forName("UTF-8"));

    byte[] signature = new byte[65];
    System.arraycopy(payload, srcPos, signature, 0, 65);
    try {
      BigInteger pubKey = EthereumSign.ecRecover(NumericUtil.bytesToHex(toSign), NumericUtil.bytesToHex(signature));
      ECKey ecKey = ECKey.fromPublicOnly(ByteUtil.concat(new byte[]{0x04}, NumericUtil.bigIntegerToBytesWithZeroPadded(pubKey, 64)));
      String recoverIpfsID = new Multihash(Multihash.Type.sha2_256, Hash.sha256(ecKey.getPubKey())).toBase58();

      if (!this.keystore.getIpfsId().equals(recoverIpfsID)) {
        throw new TokenException(Messages.INVALID_ENCRYPTION_DATA_SIGNATURE);
      }

    } catch (SignatureException e) {
      throw new TokenException(Messages.INVALID_ENCRYPTION_DATA_SIGNATURE);
    }
    return content;
  }


  public String signAuthenticationMessage(int accessTime, String deviceToken, String password) {
    Crypto crypto = this.keystore.getCrypto();
    byte[] decrypted = crypto.decryptEncPair(password, this.keystore.getEncAuthKey());
    String data = String.format(Locale.ENGLISH, "%d.%s.%s", accessTime, getIdentifier(), deviceToken);
    return EthereumSign.sign(data, decrypted);
  }
}
