package org.consenlabs.tokencore.wallet.transaction;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;

import java.math.BigInteger;
import java.util.Arrays;

import static org.bitcoinj.core.ECKey.CURVE;

public class EOSSign {

  @Deprecated
  public static String sign(byte[] dataSha256, String wif) {
    SignatureData signatureData = signAsRecoverable(dataSha256, EOSKey.fromWIF(wif).getECKey());
    byte[] sigResult = ByteUtil.concat(NumericUtil.intToBytes(signatureData.getV()), signatureData.getR());
    sigResult = ByteUtil.concat(sigResult, signatureData.getS());
    return serialEOSSignature(sigResult);
  }

  public static String sign(byte[] dataSha256, byte[] prvKey) {
    ECKey ecKey = EOSKey.fromPrivate(prvKey).getECKey();
    SignatureData signatureData = signAsRecoverable(dataSha256, ecKey);
    byte[] sigResult = ByteUtil.concat(NumericUtil.intToBytes(signatureData.getV()), signatureData.getR());
    sigResult = ByteUtil.concat(sigResult, signatureData.getS());
    return serialEOSSignature(sigResult);
  }


  private static SignatureData signAsRecoverable(byte[] value, ECKey ecKey) {
    int recId = -1;
    ECKey.ECDSASignature sig = eosSign(value, ecKey.getPrivKey());
    for (int i = 0; i < 4; i++) {
      ECKey recoverKey = ECKey.recoverFromSignature(i, sig, Sha256Hash.wrap(value), false);
      if (recoverKey != null && recoverKey.getPubKeyPoint().equals(ecKey.getPubKeyPoint())) {
        recId = i;
        break;
      }
    }

    if (recId == -1) {
      throw new TokenException("Could not construct a recoverable key. This should never happen.");
    }
    int headerByte = recId + 27 + 4;
    // 1 header + 32 bytes for R + 32 bytes for S
    byte v = (byte) headerByte;
    byte[] r = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.r, 32);
    byte[] s = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.s, 32);

    return new SignatureData(v, r, s);

  }

  private static ECKey.ECDSASignature eosSign(byte[] input, BigInteger privateKeyForSigning) {
    EOSECDSASigner signer = new EOSECDSASigner(new MyHMacDSAKCalculator(new SHA256Digest()));
    ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
    signer.init(true, privKey);
    BigInteger[] components = signer.generateSignature(input);
    return new ECKey.ECDSASignature(components[0], components[1]).toCanonicalised();
  }

  private static String serialEOSSignature(byte[] data) {
    byte[] toHash = ByteUtil.concat(data, "K1".getBytes());
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(toHash, 0, toHash.length);
    byte[] out = new byte[20];
    digest.doFinal(out, 0);
    byte[] checksumBytes = Arrays.copyOfRange(out, 0, 4);
    data = ByteUtil.concat(data, checksumBytes);
    return "SIG_K1_" + Base58.encode(data);
  }
}
