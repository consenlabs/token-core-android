package org.consenlabs.tokencore.foundation.crypto;

import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.crypto.Multihash;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by xyz on 2018/1/31.
 */

public class MultihashTest {
  @Test
  public void testMultihashSha() {
    // aPub, ipfsID
    String[][] fixture = new String[][] {
        new String[] {
            "031e7e1b26a0eb92f86c2c1c0bd326d9f82fffc7c490a6eef870a7090babb8d507",
            "QmVe1YF5Ts6EQXnhUcN3mJKPMfQSVgmmaYZ2JbWvCF9pn2"
        },
        new String[] {
            "022c6eb4159440968497a0f0a25f16bbac3507915139f82aa34f3af0668c1f503b",
            "QmQdT5yuC17EKvwVMSLP5b1rGjT6TQFXJGtQTECUGa6fF6"
        }
    };

    for (String[] testData : fixture) {
      byte[] apub = NumericUtil.hexToBytes(testData[0]);
      String ipfsID = new Multihash(Multihash.Type.sha2_256, Hash.sha256(apub)).toBase58();
      assertEquals(testData[1], ipfsID);
    }
  }
}
