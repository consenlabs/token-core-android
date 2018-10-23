package org.consenlabs.tokencore.foundation.utils;

import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by xyz on 2018/2/1.
 */

public class ByteUtilTest {

  @Test
  public void testTrimLeadingZeroes() {
    byte[][][] exampleData = new byte[][][] {
        new byte[][] {
            new byte[] { 0, 0, 0, 0, 1, 2, 3, 4},
            new byte[] { 1, 2, 3, 4}
        },
        new byte[][] {
            new byte[] { 1, 2, 3, 4},
            new byte[] { 1, 2, 3, 4}
        },
        new byte[][] {
            new byte[] { 1, 2, 3, 4, 0, 0},
            new byte[] { 1, 2, 3, 4, 0, 0}
        }
    };

    for (byte[][] test : exampleData) {
      assertArrayEquals(ByteUtil.trimLeadingZeroes(test[0]), test[1]);
    }
  }

  @Test
  public void testConcat() {

    byte[][][] exampleData = new byte[][][] {

        new byte[][] {
            new byte[] { 0, 0, 0, 0, 1, 2, 3, 4},
            new byte[] { 5, 6, 7, 8},
            new byte[] { 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8},
        },
        new byte[][] {
            new byte[] { 1, 2, 3, 4},
            new byte[] { 1, 2, 3, 4},
            new byte[] { 1, 2, 3, 4, 1, 2, 3, 4},
        },
        new byte[][] {
            new byte[] { 1, 2, 3, 4, 0, 0},
            new byte[0],
            new byte[] { 1, 2, 3, 4, 0, 0},
        },
        new byte[][] {
            new byte[0],
            new byte[] { 1, 2, 3, 4, 0, 0},
            new byte[] { 1, 2, 3, 4, 0, 0},
        }
    };

    for (byte[][] test : exampleData) {
      byte[] concatResult = ByteUtil.concat(test[0], test[1]);
      assertArrayEquals(concatResult, test[2]);
    }
  }


}
