package org.consenlabs.tokencore.wallet.address;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;

public class BitcoinAddressCreator implements AddressCreator {
  private NetworkParameters networkParameters;

  public BitcoinAddressCreator(NetworkParameters networkParameters) {
    this.networkParameters = networkParameters;
  }

  @Override
  public String fromPrivateKey(String prvKeyHex) {
    ECKey key;
    if (prvKeyHex.length() == 51 || prvKeyHex.length() == 52) {
      DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters, prvKeyHex);
      key = dumpedPrivateKey.getKey();
    } else {
      key = ECKey.fromPrivate(NumericUtil.hexToBytes(prvKeyHex));
    }
    return key.toAddress(this.networkParameters).toBase58();
  }

  @Override
  public String fromPrivateKey(byte[] prvKeyBytes) {
    ECKey key = ECKey.fromPrivate(prvKeyBytes);
    return key.toAddress(this.networkParameters).toBase58();
  }

}
