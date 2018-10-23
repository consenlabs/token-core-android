package org.consenlabs.tokencore.wallet.validators;

import org.bitcoinj.core.ECKey;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

import java.math.BigInteger;


/**
 * Created by xyz on 2018/2/27.
 */

public final class PrivateKeyValidator implements Validator<String> {

  private String privateKey;

  public PrivateKeyValidator(String prvKey) {
    this.privateKey = prvKey;
  }

  // todo: BitcoinJ provides a wrapper of NativeSecp256k1, but not provide a workable .so file
  // For stability, we do not compile the .so by ourselves, instead, we write some simple java code.
  @Override
  public String validate() {
    try {
      // validating private key
      BigInteger pkNum = NumericUtil.hexToBigInteger(privateKey);
      if (NumericUtil.hexToBytes(this.privateKey).length != 32
          || pkNum.compareTo((ECKey.CURVE.getN().subtract(BigInteger.ONE))) >= 0
          || pkNum.compareTo(BigInteger.ONE) <= 0) {
        throw new TokenException(Messages.PRIVATE_KEY_INVALID);
      }

      // validating public key
      byte[] pubKeyBytes = ECKey.fromPrivate(pkNum).getPubKey();
      BigInteger pubKeyNum = new BigInteger(1, pubKeyBytes);
      if (pubKeyNum.compareTo(BigInteger.ZERO) == 0) {
        throw new TokenException(Messages.PRIVATE_KEY_INVALID);
      }
    } catch (Exception ex) {
      throw new TokenException(Messages.PRIVATE_KEY_INVALID);
    }
    return this.privateKey;
  }
}
