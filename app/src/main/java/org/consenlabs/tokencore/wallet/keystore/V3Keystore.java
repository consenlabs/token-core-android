package org.consenlabs.tokencore.wallet.keystore;

import com.google.common.base.Strings;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.address.BitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.address.EthereumAddressCreator;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.validators.PrivateKeyValidator;
import org.consenlabs.tokencore.wallet.validators.WIFValidator;

import java.util.UUID;

/**
 * Created by xyz on 2018/2/5.
 */

public class V3Keystore extends IMTKeystore implements ExportableKeystore {
  public static final int VERSION = 3;

  public V3Keystore() {
  }

  public static V3Keystore create(Metadata metadata, String password, String prvKeyHex) {
    return new V3Keystore(metadata, password, prvKeyHex, "");
  }

  public V3Keystore(Metadata metadata, String password, String prvKeyHex, String id) {
    byte[] prvKeyBytes;
    if (metadata.getChainType().equals(ChainType.BITCOIN)) {
      NetworkParameters network = metadata.isMainNet() ? MainNetParams.get() : TestNet3Params.get();
      prvKeyBytes = prvKeyHex.getBytes();
      new WIFValidator(prvKeyHex, network).validate();
      if (Metadata.P2WPKH.equals(metadata.getSegWit())) {
        this.address = new SegWitBitcoinAddressCreator(network).fromPrivateKey(prvKeyHex);
      } else {
        this.address = new BitcoinAddressCreator(network).fromPrivateKey(prvKeyHex);
      }
    } else if (metadata.getChainType().equals(ChainType.ETHEREUM)) {
      prvKeyBytes = NumericUtil.hexToBytes(prvKeyHex);
      new PrivateKeyValidator(prvKeyHex).validate();
      this.address = new EthereumAddressCreator().fromPrivateKey(prvKeyBytes);
    } else {
      throw new TokenException("Can't init eos keystore in this constructor");
    }
    this.crypto = Crypto.createPBKDF2Crypto(password, prvKeyBytes);
    metadata.setWalletType(Metadata.V3);
    this.metadata = metadata;
    this.version = VERSION;
    this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
  }


  @Override
  public Keystore changePassword(String oldPassword, String newPassword) {
    byte[] decrypted = this.crypto.decrypt(oldPassword);
    String prvKeyHex;
    if (Metadata.FROM_WIF.equals(getMetadata().getSource())) {
      prvKeyHex = new String(decrypted);
    } else {
      prvKeyHex = NumericUtil.bytesToHex(decrypted);
    }
    return new V3Keystore(metadata, newPassword, prvKeyHex, this.id);
  }
}
