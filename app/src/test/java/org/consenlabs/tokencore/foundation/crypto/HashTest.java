package org.consenlabs.tokencore.foundation.crypto;

import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Test;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.assertEquals;

/**
 * Created by jesushula on 15/12/2016.
 */

public class HashTest {
  // input, expected
  private String[][] SHA3Example = new String[][]{new String[]{HEX.encode("".getBytes()), "c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470"},
      new String[]{HEX.encode("helloworld".getBytes()), "fa26db7ca85ead399216e7c6316bc50ed24393c3122b582735e7f3b0f91b93f0"},
      new String[]{"3c9229289a6125f7fdf1885a77bb12c37a8d3b4962d936f7e3084dece32a3ca1", "82ff40c0a986c6a5cfad4ddf4c3aa6996f1a7837f9c398e17e5de5cbd5a12b28"}};

  @Test
  public void testKeccak256() {
    for (String[] example : SHA3Example) {
      assertEquals(example[1], Hash.keccak256(example[0]));
    }
  }

  @Test
  public void testSha256() {
    String msg = "0x3c9229289a6125f7fdf1885a77bb12c37a8d3b4962d936f7e3084dece32a3ca1";
    String expected = "58bbda5e10bc11a32d808e40f9da2161a64f00b5557762a161626afe19137445";
    assertEquals(expected, Hash.sha256(msg));

    byte[] bytes = NumericUtil.hexToBytes(msg);
    assertEquals(expected, NumericUtil.bytesToHex(Hash.sha256(bytes)));
  }

  @Test
  public void testGenerateMac() {

    String[][] example = new String[][]{
        new String[]{
            "5334a31e27d5a4ea3dd815ffc8a0483b1a370ef32baf4f26349985027ab01d6e1fa23e6e0aea328814d3e2aefaf2c1f47b96c86ec24dc5cd66d7050fb249ec03d985b4793a9703f7ac189ecef67fb5a29797e2952a3efd12fe5565465b1fadeaaced304d142b2d2d46f3e8986ac3e2",
            "592bbbd0596b97439ee041e7c0679aba846031e66730c5f20e3626d59bfd22bd",
            "9600a5081753d9bc0f0855d55a592925ea5784a6c45c7a5c161ffa82e140e4eb"
        },
        new String[]{
            "ebef0d152fe4c889d2a903ec5c3adfdd7c8c8e207679c86475f3cf9d48f4455766953d3a4305fd37b35ae2e87082daa649bb51e55940c566ec21772a11a6606ccde7b26b88ac5e5f6e9569787041324ab978e695656776cd961139713ebe3fdd23027a205683f108579e162a839e69",
            "539d8bea635f1b8cba4ab4802e7c557dad1e097df6c4b75a6aab0385fd4e2cb9",
            "8dabc02eb079e935ce7dc4ec6639aaad8dc2de1275479465e86b6a475f7ebe24"
        },
        new String[]{
            "30c50f3c3bba03c6186ca65675b1b447b9dd803cbd86d12708ea9581a6ad5e930c0064c2c549cb354cace5e2a860abfb11f66589858c11751eceeaa11b61c8eee3d60646a26b39d9d8744b8dadc2cb7568e63e55f04428fea52c073b94ee570fe7f13266b338094d5ee85970129c12",
            "2cb813ecf4a1cc47fd80817f63f88bb17b35dcb34f08facf186dbba67c640c08",
            "973d56c4662e64db58d097c156290f2b7efecfbc357cfa8b7e71b032dacedeec"
        }
    };
    for (String[] testData : example) {
      byte[] cipherText = NumericUtil.hexToBytes(testData[0]);
      byte[] derivedKey = NumericUtil.hexToBytes(testData[1]);

      String expected = testData[2];
      assertEquals(expected, NumericUtil.bytesToHex(Hash.generateMac(derivedKey, cipherText)));
    }

  }


  @Test
  public void testCalcMerkleRoot() {
    String[][] expected = new String[][]{
        new String[]{"1000", "3fa2b684fa9d80f04b70187e6c9ff1c8dd422ce1846beb79cf5e1546c7062d41"},
        new String[]{"2000", "4b19aa611413ba9a6b89a2be7833bb835349b9e9e9872c5eacfc82daa2e5f08f"},
        new String[]{"3000", "c9ec2ec071ed70d02802decd912a1e8d124420556789384efaab80fcb7ce7ecb"},
        new String[]{"4000", "5cfa6745c50787e3d97a1322789713036f8cab7ba534d2a996bea015d811640c"},
        new String[]{"5000", "233bc40f24c071507474a9c978f0f0099d0c457f9874326640be55a8a8b96325"},
        new String[]{"1024", "5a6c9dcbec66882a3de754eb13e61d8908e6c0b67a23c9d524224ecd93746290"},
        new String[]{"2048", "5ee830087937da00520c4ce3793c5c7b951d37771d69a098415ddf7d682a39d9"},
    };
    for (String[] testCase : expected) {
      int dataLength = Integer.valueOf(testCase[0]);
      byte[] data = new byte[dataLength];
      for (int i = 0; i < dataLength; i++) {
        data[i] = (byte) (i / 1024);
      }
      assertEquals(testCase[1], NumericUtil.bytesToHex(Hash.merkleHash(data)));
    }
  }

}
