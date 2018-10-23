package org.consenlabs.tokencore.foundation.crypto;

/**
 * Created by jesushula on 15/12/2016.
 */

import com.lambdaworks.crypto.SCrypt;

import static org.junit.Assert.assertEquals;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Test;

import java.security.GeneralSecurityException;


public class ScryptTest {
    // salt, r, n, p, password, expected, dklen
    String[][] ScryptExample = new String[][]{
        new String[]{"ab0c7876052600dd703518d6fc3fe8984592145b591fc8fb5c6d43190334ba19", "1", "262144", "8", "testpassword", "fac192ceb5fd772906bea3e118a69e8bbb5cc24229e20d8766fd298291bba6bd", "32"}};

    @Test
    public void derive() throws GeneralSecurityException {
        for(String[] example : ScryptExample) {
            byte[] derivedKey = SCrypt.scrypt(example[4].getBytes(), NumericUtil.hexToBytes(example[0]),
                Integer.parseInt(example[2]), Integer.parseInt(example[1]), Integer.parseInt(example[3]), Integer.parseInt(example[6]));
            assertEquals(example[5], NumericUtil.bytesToHex(derivedKey));
        }
    }
}
