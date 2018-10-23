package org.consenlabs.tokencore.foundation.crypto;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.spongycastle.crypto.params.KeyParameter;

/**
 * Created by jesushula on 15/12/2016.
 */

public class PBKDFTest {

  // salt, iterations, dklen, password, expected
  String[][] PBKDFExample = new String[][]{new String[]{"salt", "4096", "32", "password", "c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a"},
      new String[]{"saltSALTsaltSALTsaltSALTsaltSALTsalt", "4096", "40", "passwordPASSWORDpassword", "348c89dbcbd32b2f32d814b8116e84cf2b17347ebc1800181c4e2a1fb8dd53e1c635518c7dac47e9"}};

  @Test
  public void derive() {
    for (String[] example : PBKDFExample) {
      PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
      gen.init(example[3].getBytes(), example[0].getBytes(), Integer.parseInt(example[1]));
      byte[] derivedKey = ((KeyParameter) gen.generateDerivedParameters(Integer.parseInt(example[2]) * 8)).getKey();
      Assert.assertEquals(NumericUtil.bytesToHex(derivedKey), example[4]);
    }
  }
}
