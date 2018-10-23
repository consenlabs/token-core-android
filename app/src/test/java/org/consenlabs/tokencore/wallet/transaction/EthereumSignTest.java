package org.consenlabs.tokencore.wallet.transaction;

import com.fasterxml.jackson.core.util.BufferRecycler;
import com.google.common.base.Joiner;

import org.bitcoinj.core.ECKey;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.testutils.ResourcesManager;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.consenlabs.tokencore.wallet.address.EthereumAddressCreator;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static org.bitcoinj.core.Utils.HEX;

/**
 * Created by xyz on 2018/3/2.
 */

public class EthereumSignTest {

  private byte[] privateKey = NumericUtil.hexToBytes("3c9229289a6125f7fdf1885a77bb12c37a8d3b4962d936f7e3084dece32a3ca1");
  private String expectedSignature = "7cf775589643e8b4f68f8aa3f5fe9b6b0d847612c1e1cd23af357a1bb8bfe930186444298e6126cc6eabd60a6e5bfb295b35556c8dfb9c1c614d198b91a299471b";

  @Test
  public void testECSign() {
    String signature = EthereumSign.sign("imToken", privateKey);
    Assert.assertEquals(signature, expectedSignature);
  }

  @Test
  public void testLongTextECSignShouldReturnNormalSignature() {
    StringBuilder longTextBuilder = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      longTextBuilder.append("imToken ");
    }
    String expectedSignature = "c72572b15a69e00ce8aa7193d74c2aab4ef926aded55ac3dda975cc43e114b645e3e9b36cc9316ca5a77854d5ee69de5ce5f978158f511d2fced57e000aba1771b";
    String signature = EthereumSign.sign(longTextBuilder.toString(), privateKey);
    Assert.assertEquals(signature, expectedSignature);
  }

  @Test
  public void testShortTextECSignShouldReturnNormalSignature() {
    String shortText = "i";
    String signature = EthereumSign.sign(shortText, privateKey);
    String expectedSignature = "b650888994cf54feafe3d8c26e235631fc4d300f0da79db822c1a6158d64393f1dd2c9ac2d62f41019204354357fd87bf68d614911c39d0d3dd6d05c0196ecb51b";
    Assert.assertEquals(signature, expectedSignature);
  }

  @Test
  public void testECRecover() {
    String addr = EthereumSign.recoverAddress("imToken", expectedSignature);
    String expectedAddr = new EthereumAddressCreator().fromPrivateKey(privateKey);
    Assert.assertEquals(addr, expectedAddr);
    String sig = "0 x1 87 ebc72 f217017 b2 a8349 abdd1 8 f1 22 d16 c8 7 bf5 e455794 d641 426 d4 f65 d9 e9001 765494362 b4 b38 a60 e571 7 e96 c411 72 d25475 a9 a43 c6217 00 dded28935 b221 c".replace(" ", "").trim();
    addr = EthereumSign.recoverAddress("z78ubpeks2", sig);
    System.out.println(addr);
  }


  @Test
  public void testPersonalSign() {
    Object[][] testcases = new Object[][] {
        new Object[] { "Hello imToken", "1be38ff0ab0e6d97cba73cf61421f0641628be8ee91dcb2f73315e7fdf4d0e2770b0cb3cc7350426798d43f0fb05602664a28bb2c9fcf46a07fa1c8c4e322ec01b"},
        new Object[] { SampleKey.ADDRESS, "b12a1c9d3a7bb722d952366b06bd48cb35bdf69065dee92351504c3716a782493c697de7b5e59579bdcc624aa277f8be5e7f42dc65fe7fcd4cc68fef29ff28c21b" }
    };
    for (Object[] aCase : testcases) {
      String actual = EthereumSign.personalSign((String)aCase[0], NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING));
      Assert.assertEquals(aCase[1], actual);
    }
  }



  @Test
  public void testSignMessage() {
    Object[][] testcases = new Object[][]{
        new Object[]{"Hello imToken".getBytes(), "648081bc111e6116769bdb4396eebe17f58d3eddc0aeb04a868990deac9dfa2f322514a380fa66e0e864faaac6ef936092cdc022f5fd7d61cb501193ede537b31b"},
        new Object[]{"a".getBytes(), "5caec23c6aa80c772fef1a52655cbb46ed5e017573b054ba8d3fa61d9d26df9848a8a544478726b4b2140b444831c2f0bbd97c819e1344482190c2ad265865ca1c"},
        new Object[]{"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(), "65e4952899a8dcadf3a65a11bdac0f0cfdf93e0bae5c67674c78a72631de524d3cafe27ea71c86aa3fd838c6a50a0b09d6ece85a6dcf3ce85c30fdc51380ebdf1b"},
        new Object[]{ByteBuffer.allocate(4).putInt(1234567).array(), "2d4a505a90d3a27a71d987fbbd377414e79e69e889e1b3d3dda15f5c1aa2c0624a907df6c5e41eba108749d9220173df42c1dff153eb25cf3a9576a2b9fede511b"},
        new Object[]{ByteBuffer.allocate(4).putInt(0).array(), "511cf92b0838ba64f844fd2fdf8c2806dfc3dcc0f6b4ec552c7a7c0171e9ada07dd5625a788d4f80c4906ec5dc9e783a4c764c1d1632ebf6b35f7015415396711c"},
        new Object[]{NumericUtil.bigIntegerToBytesWithZeroPadded(new BigInteger("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"), 64), "d2dc01447baa8bc298d5e97fbeb4a126026b58f98f15427986c1c2bcede3da170531079ce0bd491e68e7a20a0b5758449596b123dfce196d89823c44ed6f47c61c"},
    };
    for (Object[] aCase : testcases) {
      Assert.assertEquals(aCase[1], EthereumSign.signMessage((byte[]) aCase[0], privateKey).toString());
    }

  }

  @Test
  public void testTransactionTest() throws IOException {
    // ref: https://github.com/ethereum/tests/blob/862b4e3d4a9a7141af1b4aaf7dfe228a6a294614/TransactionTests/ttTransactionTest.json
    List<String> testFiles = Arrays.asList("ttTransactionTest.json", "ttTransactionTestEip155VitaliksTests.json");
    for (String filename : testFiles) {
      JSONObject testcases = ResourcesManager.loadTestJSON(filename);
      Iterator<String> keys = testcases.keys();
      try {
        while (keys.hasNext()) {
          String key = keys.next();
          JSONObject testcase = testcases.getJSONObject(key);
          if (!testcase.has("transaction")) continue;
          JSONObject tran = testcase.getJSONObject("transaction");

          BigInteger nonce = new BigInteger(NumericUtil.cleanHexPrefix(tran.getString("nonce")), 16);
          BigInteger gasPrice = new BigInteger(NumericUtil.cleanHexPrefix(tran.getString("gasPrice")), 16);
          BigInteger gasLimit = new BigInteger(NumericUtil.cleanHexPrefix(tran.getString("gasLimit")), 16);
          String to = tran.getString("to");
          BigInteger value = new BigInteger(NumericUtil.cleanHexPrefix(tran.getString("value")), 16);
          String data = tran.getString("data");

          EthereumTransaction transaction = new EthereumTransaction(nonce, gasPrice, gasLimit, to, value, data);
          byte v = NumericUtil.hexToBytes(tran.getString("v"))[0];
          byte[] r = NumericUtil.hexToBytes(tran.getString("r"));
          byte[] s = NumericUtil.hexToBytes(tran.getString("s"));
          SignatureData signatureData = new SignatureData(v, r, s);
          String actualRLP = NumericUtil.bytesToHex(transaction.encodeToRLP(signatureData));
          String expected = NumericUtil.cleanHexPrefix(testcase.getString("rlp"));
          Assert.assertEquals(String.format("%s Compare SignedTx", key), expected, actualRLP);

          if (testcase.has("hash")) {
            String actualHash = NumericUtil.cleanHexPrefix(transaction.calcTxHash(actualRLP));
            String expectedHash = testcase.getString("hash");
            Assert.assertEquals(String.format("%s Compare TxHash", key), expectedHash, actualHash);
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

  }

}
