package org.consenlabs.tokencore.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.keystore.EOSKeystore;
import org.consenlabs.tokencore.wallet.keystore.EncMnemonicKeystore;
import org.consenlabs.tokencore.wallet.keystore.ExportableKeystore;
import org.consenlabs.tokencore.wallet.keystore.HDMnemonicKeystore;
import org.consenlabs.tokencore.wallet.keystore.IMTKeystore;
import org.consenlabs.tokencore.wallet.keystore.LegacyEOSKeystore;
import org.consenlabs.tokencore.wallet.keystore.V3Ignore;
import org.consenlabs.tokencore.wallet.keystore.V3Keystore;
import org.consenlabs.tokencore.wallet.keystore.V3MnemonicKeystore;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.MnemonicAndPath;
import org.consenlabs.tokencore.wallet.model.TokenException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Wallet {
  private IMTKeystore keystore;

  public IMTKeystore getKeystore() {
    return keystore;
  }

  public Wallet(IMTKeystore keystore) {
    this.keystore = keystore;
  }

  public String getId() {
    return this.keystore.getId();
  }

  public String getAddress() {
    return this.keystore.getAddress();
  }

  public Metadata getMetadata() {
    return keystore.getMetadata();
  }

  public String getEncXPub() {
    if (keystore instanceof HDMnemonicKeystore) {
      return ((HDMnemonicKeystore) keystore).getEncryptXPub();
    }
    return null;
  }

  public byte[] decryptMainKey(String password) {
    return keystore.decryptCiphertext(password);
  }

  MnemonicAndPath exportMnemonic(String password) {
    if (keystore instanceof EncMnemonicKeystore) {
      EncMnemonicKeystore encMnemonicKeystore = (EncMnemonicKeystore) keystore;
      String mnemonic = encMnemonicKeystore.decryptMnemonic(password);
      String path = encMnemonicKeystore.getMnemonicPath();
      return new MnemonicAndPath(mnemonic, path);
    }
    return null;
  }

  String exportKeystore(String password) {
    if (keystore instanceof ExportableKeystore) {
      if (!keystore.verifyPassword(password)) {
        throw new TokenException(Messages.WALLET_INVALID_PASSWORD);
      }

      try {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(IMTKeystore.class, V3Ignore.class);
        return mapper.writeValueAsString(keystore);
      } catch (Exception ex) {
        throw new TokenException(Messages.WALLET_INVALID, ex);
      }
    } else {
      throw new TokenException(Messages.CAN_NOT_EXPORT_MNEMONIC);
    }
  }

  public String exportPrivateKey(String password) {
    if (keystore instanceof V3Keystore || keystore instanceof V3MnemonicKeystore) {
      byte[] decrypted = keystore.decryptCiphertext(password);
      if (keystore.getMetadata().getSource().equals(Metadata.FROM_WIF)) {
        return new String(decrypted);
      } else {
        return NumericUtil.bytesToHex(decrypted);
      }
    }
    throw new TokenException(Messages.ILLEGAL_OPERATION);
  }

  boolean verifyPassword(String password) {
    return keystore.verifyPassword(password);
  }

  public byte[] decryptPrvKeyFor(String pubKey, String password) {
    if (!(keystore instanceof EOSKeystore)) {
      throw new TokenException("This method is only for EOS wallet!");
    }

    EOSKeystore eosKeystore = (EOSKeystore) keystore;
    return eosKeystore.decryptPrivateKeyFor(pubKey, password);
  }

  public String newReceiveAddress(int nextRecvIdx) {
    if (keystore instanceof HDMnemonicKeystore) {
      return ((HDMnemonicKeystore) keystore).newReceiveAddress(nextRecvIdx);
    } else {
      return keystore.getAddress();
    }
  }

  public long getCreatedAt() {
    return this.keystore.getMetadata().getTimestamp();
  }

  boolean hasMnemonic() {
    return this.keystore instanceof EncMnemonicKeystore;
  }

  public List<KeyPair> exportPrivateKeys(String password) {
    if (keystore instanceof EOSKeystore) {
      return ((EOSKeystore)keystore).exportPrivateKeys(password);
    } else if (keystore instanceof LegacyEOSKeystore) {
      return ((LegacyEOSKeystore)keystore).exportPrivateKeys(password);
    } else {
      throw new TokenException("Only eos wallet can export multi private keys");
    }
  }

  public void setAccountName(String accountName) {
    if (!(keystore instanceof EOSKeystore)) {
      throw new TokenException("Only EOS wallet can update account name!");
    }
    ((EOSKeystore) keystore).setAccountName(accountName);
  }

  public List<EOSKeystore.KeyPathPrivate> getKeyPathPrivates() {

    if (keystore instanceof EOSKeystore) {
      return ((EOSKeystore) keystore).getKeyPathPrivates();
    } else if (keystore instanceof LegacyEOSKeystore) {
      return Collections.emptyList();
    } else {
      throw new TokenException("Only EOS wallet can export all public keys!");
    }

  }

  boolean delete(String password) {
    return keystore.verifyPassword(password) && WalletManager.generateWalletFile(keystore.getId()).delete();
  }

}
