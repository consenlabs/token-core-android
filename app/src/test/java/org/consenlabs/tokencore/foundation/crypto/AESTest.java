package org.consenlabs.tokencore.foundation.crypto;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by jesushula on 15/12/2016.
 */

public class AESTest {

  // expected, input text, key, iv
  private String[][] CTRExample = new String[][]{new String[]{"5318b4d5bcd28de64ee5559e671353e16f075ecae9f99c7a79a38af5f869aa46", "7a28b5ba57c53603b0b07b56bba752f7784bf506fa95edc395f5cf6c7514fe9d", "f06d69cdc7da0faffb1008270bca38f5", "6087dab2f9fdbbfaddc31a909735c1e6"},
      new String[]{"f1a47dd870488797347517eb4641c6b1927268ae1cc04dde003d4b2877f7781a", "c951ac12154816ea03176fbba327f64fc3adb741df8e81a4fa1e80612050b80a", "868c3e73b6b2222c52492a2295f9666e", "3c700055452aa9f5d0e9da0304988f6f"},
      new String[]{"2d743dda0caabdfb9fca0034d33cd0da7fb1ffe78cb80d643d67bf3f2aa12819", "366438120cd47b27fa77d8d80b56e02759ecdb9b8702bf3cdcbbc858ee78340e", "6cd5c42099904171bfd370723bd38f70", "915da6af8d96b558167ff4e663e057b2"}};

  private String[][] CBCExample = new String[][]{new String[]{"946ddddd56d77b12afb9c352d879ee10a2e2f7d576623b0495ce44b0141c4584bdf525f0ab95763300bef6028a88265c", "c951ac12154816ea03176fbba327f64fc3adb741df8e81a4fa1e80612050b80a", "868c3e73b6b2222c52492a2295f9666e", "3c700055452aa9f5d0e9da0304988f6f"},
      new String[]{"07533e172414bfa50e99dba4a0ce603f654ebfa1ff46277c3e0c577fdc87f6bb4e4fe16c5a94ce6ce14cfa069821ef9bbadb2863671be96fc4f96a879395f77d", "cb19dce82bdb902efb5b5b75d0fe4c4c09dee0e99ef222af35dd8da136bde8995f7fd84acfb1679fe2e91de783a5e006", "9fc409900f835bb38302e976e16c49e7", "16d67ba0ce5a339ff2f07951253e6ba8"},
      new String[]{"4ee50259867f9994fc1d711332d72679bb38ca4e0e1b8b07468a0ae901726dabe379535d8aa12206bf076237877d0cda", "366438120cd47b27fa77d8d80b56e02759ecdb9b8702bf3cdcbbc858ee78340e", "6cd5c42099904171bfd370723bd38f70", "915da6af8d96b558167ff4e663e057b2"}};

  @Test
  public void doCTREncrypt() {
    for (String[] example : CTRExample) {
      byte[] encrypt = AES.encryptByCTR(NumericUtil.hexToBytes(example[1]),
          NumericUtil.hexToBytes(example[2]),
          NumericUtil.hexToBytes(example[3]));
      assertEquals(example[0], NumericUtil.bytesToHex(encrypt));
    }
  }

  @Test
  public void doCTRDecrypt() {
    for (String[] example : CTRExample) {
      byte[] decrypt = AES.decryptByCTR(NumericUtil.hexToBytes(example[0]),
          NumericUtil.hexToBytes(example[2]),
          NumericUtil.hexToBytes(example[3]));
      assertEquals(example[1], NumericUtil.bytesToHex(decrypt));
    }
  }

  @Test
  public void doCBCEncrypt() {
    for (String[] example : CBCExample) {
      byte[] decrypt = AES.encryptByCBC(NumericUtil.hexToBytes(example[1]),
          NumericUtil.hexToBytes(example[2]),
          NumericUtil.hexToBytes(example[3]));
      assertEquals(example[0], NumericUtil.bytesToHex(decrypt));
    }
  }

  @Test
  public void doCBCDecrypt() {
    for (String[] example : CBCExample) {
      byte[] decrypt = AES.decryptByCBC(NumericUtil.hexToBytes(example[0]),
          NumericUtil.hexToBytes(example[2]),
          NumericUtil.hexToBytes(example[3]));
      assertEquals(example[1], NumericUtil.bytesToHex(decrypt));
    }
  }
}
