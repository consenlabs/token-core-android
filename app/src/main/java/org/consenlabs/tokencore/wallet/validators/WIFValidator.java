package org.consenlabs.tokencore.wallet.validators;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.WrongNetworkException;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

/**
 * Created by xyz on 2018/2/27.
 */

public final class WIFValidator implements Validator<String> {

  String wif;
  NetworkParameters network;
  boolean requireCompressed = false;
  public WIFValidator(String wif, NetworkParameters network) {
    this.wif = wif;
    this.network = network;
  }

  public WIFValidator(String wif, NetworkParameters network, boolean requireCompressed) {
    this.wif = wif;
    this.network = network;
    this.requireCompressed = requireCompressed;
  }

  @Override
  public String validate() {
    try {
      DumpedPrivateKey.fromBase58(network, wif);
    } catch (WrongNetworkException addressException) {
      throw new TokenException(Messages.WIF_WRONG_NETWORK);
    } catch (AddressFormatException addressFormatException) {
      throw new TokenException(Messages.WIF_INVALID);
    }
    if (requireCompressed && !DumpedPrivateKey.fromBase58(network, wif).getKey().isCompressed()) {
      throw new TokenException(Messages.SEGWIT_NEEDS_COMPRESS_PUBLIC_KEY);
    }
    return this.wif;
  }
}
