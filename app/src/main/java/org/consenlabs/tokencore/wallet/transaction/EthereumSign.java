package org.consenlabs.tokencore.wallet.transaction;


import com.subgraph.orchid.encoders.Hex;

import org.bitcoinj.core.Sha256Hash;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.bitcoinj.core.ECKey;
import org.consenlabs.tokencore.wallet.address.EthereumAddressCreator;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by xyz on 2017/12/20.
 */

public class EthereumSign {

  public static String personalSign(String data, byte[] prvKeyBytes) {
    byte[] dataBytes = dataToBytes(data);
    int msgLen = dataBytes.length;
    String headerMsg = String.format(Locale.ENGLISH, "\u0019Ethereum Signed Message:\n%d", msgLen);
    byte[] headerMsgBytes = headerMsg.getBytes(Charset.forName("UTF-8"));
    byte[] dataToSign = ByteUtil.concat(headerMsgBytes, dataBytes);
    return signMessage(dataToSign, prvKeyBytes).toString();
  }

  public static String sign(String data, byte[] prvKeyBytes) {
    return signMessage(dataToBytes(data), prvKeyBytes).toString();
  }

  public static BigInteger ecRecover(String data, String signature) throws SignatureException {
    byte[] msgBytes = dataToBytes(data);
    signature = NumericUtil.cleanHexPrefix(signature);
    byte[] r = Hex.decode(signature.substring(0, 64));
    byte[] s = Hex.decode(signature.substring(64, 128));
    int receiveId = Integer.valueOf(signature.substring(128), 16);
    SignatureData signatureData = new SignatureData((byte) receiveId, r, s);

    return signedMessageToKey(msgBytes, signatureData);
  }

  public static String recoverAddress(String data, String signature) {
    try {
      BigInteger pubKey = ecRecover(data, signature);
      return new EthereumAddressCreator().fromPublicKey(pubKey);
    } catch (SignatureException e) {
      return "";
    }
  }

  private static byte[] dataToBytes(String data) {
    byte[] messageBytes;
    if (NumericUtil.isValidHex(data)) {
      messageBytes = NumericUtil.hexToBytes(data);
    } else {
      messageBytes = data.getBytes(Charset.forName("UTF-8"));
    }
    return messageBytes;
  }

  static SignatureData signMessage(byte[] message, byte[] prvKeyBytes) {
    ECKey ecKey = ECKey.fromPrivate(prvKeyBytes);
    byte[] messageHash = Hash.keccak256(message);
    return signAsRecoverable(messageHash, ecKey);
  }

  /**
   * Given an arbitrary piece of text and an Ethereum message signature encoded in bytes,
   * returns the public key that was used to sign it. This can then be compared to the expected
   * public key to determine if the signature was correct.
   *
   * @param message       RLP encoded message.
   * @param signatureData The message signature components
   * @return the public key used to sign the message
   * @throws SignatureException If the public key could not be recovered or if there was a
   *                            signature format error.
   */
  private static BigInteger signedMessageToKey(byte[] message, SignatureData signatureData) throws SignatureException {

    byte[] r = signatureData.getR();
    byte[] s = signatureData.getS();
    checkState(r != null && r.length == 32, "r must be 32 bytes");
    checkState(s != null && s.length == 32, "s must be 32 bytes");

    int header = signatureData.getV() & 0xFF;
    // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
    //                  0x1D = second key with even y, 0x1E = second key with odd y
    if (header < 27 || header > 34) {
      throw new SignatureException("Header byte out of range: " + header);
    }

    ECKey.ECDSASignature sig = new ECKey.ECDSASignature(
        new BigInteger(1, signatureData.getR()),
        new BigInteger(1, signatureData.getS()));

    byte[] messageHash = Hash.keccak256(message);
    int recId = header - 27;
    ECKey key = ECKey.recoverFromSignature(recId, sig, Sha256Hash.wrap(messageHash), false);
    if (key == null) {
      throw new SignatureException("Could not recover public key from signature");
    }
    byte[] pubKeyBytes = key.getPubKeyPoint().getEncoded(false);
    return NumericUtil.bytesToBigInteger(Arrays.copyOfRange(pubKeyBytes, 1, pubKeyBytes.length));
  }

  public static SignatureData signAsRecoverable(byte[] value, ECKey ecKey) {

    ECKey.ECDSASignature sig = ecKey.sign(Sha256Hash.wrap(value));

    // Now we have to work backwards to figure out the recId needed to recover the signature.
    int recId = -1;
    for (int i = 0; i < 4; i++) {
      ECKey recoverKey = ECKey.recoverFromSignature(i, sig, Sha256Hash.wrap(value), false);
      if (recoverKey != null && recoverKey.getPubKeyPoint().equals(ecKey.getPubKeyPoint())) {
        recId = i;
        break;
      }
    }
    if (recId == -1) {
      throw new RuntimeException(
          "Could not construct a recoverable key. This should never happen.");
    }

    int headerByte = recId + 27;

    // 1 header + 32 bytes for R + 32 bytes for S
    byte v = (byte) headerByte;
    byte[] r = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.r, 32);
    byte[] s = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.s, 32);

    return new SignatureData(v, r, s);
  }


}

