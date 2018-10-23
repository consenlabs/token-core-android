package org.consenlabs.tokencore.wallet.transaction;


import org.consenlabs.tokencore.foundation.rlp.RlpString;
import org.consenlabs.tokencore.foundation.rlp.RlpType;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.bitcoinj.core.ECKey;
import org.hamcrest.core.IsEqual;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EthereumTransactionTest {


  @Test
  public void testEtherTransactionAsRlpValues() {
    SignatureData signatureData = new SignatureData((byte) 0, new byte[32], new byte[32]);
    List<RlpType> rlpStrings = createEtherTransaction().asRlpValues(signatureData);
    assertThat(rlpStrings.size(), is(6));
    assertThat(rlpStrings.get(3), IsEqual.<RlpType>equalTo(RlpString.create(new BigInteger("add5355", 16))));
  }

  @Test
  public void testEip155Encode() {
    SignatureData signatureData = new SignatureData((byte) 1, new byte[]{}, new byte[]{});
    assertThat(createEip155RawTransaction().encodeToRLP(signatureData),
      is(NumericUtil.hexToBytes(
        "0xec098504a817c800825208943535353535353535353535353535353535353535880de0"
          + "b6b3a764000080018080")));
  }

  @Test
  public void testEip155Transaction() {
    // https://github.com/ethereum/EIPs/issues/155
    ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes("0x4646464646464646464646464646464646464646464646464646464646464646"));

    assertThat(createEip155RawTransaction().signTransaction(1, ecKey.getPrivKeyBytes()),
      is( "f86c098504a817c800825208943535353535353535353535353535353535353535880"
          + "de0b6b3a76400008025a028ef61340bd939bc2195fe537567866003e1a15d"
          + "3c71ff63e1590620aa636276a067cbe9d8997f761aecb703304b3800ccf55"
          + "5c9f3dc64214b297fb1966a3b6d83"));
  }

  @Test
  public void testETCTransaction() {
    // https://github.com/ethereum/EIPs/issues/155
    ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes("0x4646464646464646464646464646464646464646464646464646464646464646"));

    assertThat(createEip155RawTransaction().signTransaction((byte) 61, ecKey.getPrivKeyBytes()),
        is( "f86d098504a817c800825208943535353535353535353535353535353535353535880de0b6b3a764000080819da09e59aa73a10ec8fe5a97fe7560806315624c1a67aeeb59310fdc0001ba2b38a0a0719b723ff1b40c21c4235cbbbdaac0bf775be8f479c31caea806710f70f98927"));
  }

  private static EthereumTransaction createEtherTransaction() {
    return new EthereumTransaction(
      BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN, "0xadd5355",
      BigInteger.valueOf(Long.MAX_VALUE), "");
  }

  private static EthereumTransaction createEip155RawTransaction() {
    return new EthereumTransaction(
      BigInteger.valueOf(9), BigInteger.valueOf(20000000000L),
      BigInteger.valueOf(21000), "0x3535353535353535353535353535353535353535",
      BigInteger.valueOf(1000000000000000000L),"");
  }
}
