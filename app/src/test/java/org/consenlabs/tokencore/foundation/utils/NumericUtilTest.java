package org.consenlabs.tokencore.foundation.utils;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Created by xyz on 2018/2/1.
 */

public class NumericUtilTest {
  @Test
  public void testGenerateRandomBytes() {
    int[] sizeArray = new int[]{8, 9, 10};
    for (int size : sizeArray) {
      byte[][] randomBytes = new byte[10][size];
      for (int i = 0; i < 10; i++) {
        randomBytes[i] = NumericUtil.generateRandomBytes(size);
      }
      for (int i = 0; i < 10; i++) {
        assertEquals("Size should be " + size, size, randomBytes[i].length);
        for (int j = 0; j < 10; j++) {
          if (i == j) continue;
          assertNotEquals(NumericUtil.bytesToHex(randomBytes[i]), NumericUtil.bytesToHex(randomBytes[j]));
        }
      }
    }
  }

  @Test
  public void testIsValidHex() {
    String test = null;
    assertFalse(NumericUtil.isValidHex(test));
    test = "";
    assertFalse(NumericUtil.isValidHex(test));
    test = "0xAFGH";
    assertFalse(NumericUtil.isValidHex(test));
    test = "0xAF1";
    assertFalse(NumericUtil.isValidHex(test));
    test = "0x10AF";
    assertTrue(NumericUtil.isValidHex(test));
    test = "0x10af";
    assertTrue(NumericUtil.isValidHex(test));
    test = "10af";
    assertTrue(NumericUtil.isValidHex(test));
  }

  @Test
  public void testCleanHexPrefix() {
    String[][] exampleData = new String[][]{
        new String[]{
            "0x1020AF",
            "1020AF"
        },
        new String[]{
            "1020AF",
            "1020AF"
        },
        new String[]{
            "",
            ""
        }
    };

    for (String[] test : exampleData) {
      assertEquals(test[1], NumericUtil.cleanHexPrefix(test[0]));
    }
  }

  @Test
  public void testPrependHexPrefix() {
    String[][] exampleData = new String[][]{
        new String[]{
            "0x1020AF",
            "1020AF"
        },
        new String[]{
            "0x1020AF",
            "0x1020AF"
        },
        new String[]{
            "",
            ""
        }
    };

    for (String[] test : exampleData) {
      assertEquals(test[0], NumericUtil.prependHexPrefix(test[1]));
    }
  }

  @Test
  public void testBytesToBigInteger() {
    byte[] bigNumBytes = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    String bigNum = "8376419748709451303509889043802165155304723617084912823321731795381253826590747254992008184065";
    byte[] smallNumBytes = new byte[]{1, 1, 1};
    String smallNum = "65793";
    assertTrue(new BigInteger(bigNum).compareTo(NumericUtil.bytesToBigInteger(bigNumBytes)) == 0);
    assertTrue(new BigInteger(smallNum).compareTo(NumericUtil.bytesToBigInteger(smallNumBytes)) == 0);
  }

  @Test
  public void testHexToBigInteger() {
    String bigNumHex = "0x1010101010101010101010101010101010101010101010101010101010101010101010101010101";
    String bigNum = "8376419748709451303509889043802165155304723617084912823321731795381253826590747254992008184065";
    String smallNumHex = "0x10101";
    String smallNumHexWithout0x = "10101";
    String smallNum = "65793";
    assertTrue(new BigInteger(bigNum).compareTo(NumericUtil.hexToBigInteger(bigNumHex)) == 0);
    assertTrue(new BigInteger(smallNum).compareTo(NumericUtil.hexToBigInteger(smallNumHex)) == 0);
    assertTrue(new BigInteger(smallNum).compareTo(NumericUtil.hexToBigInteger(smallNumHexWithout0x)) == 0);
  }

  @Test
  public void testBigIntToHexStringNoPrefix() {
    String bigNumHex = "1010101010101010101010101010101010101010101010101010101010101010101010101010101";
    String bigNum = "8376419748709451303509889043802165155304723617084912823321731795381253826590747254992008184065";
    String smallNumHex = "10101";
    String smallNum = "65793";
    assertTrue(new BigInteger(smallNum).compareTo(NumericUtil.hexToBigInteger(smallNumHex)) == 0);
    assertTrue(new BigInteger(bigNum).compareTo(NumericUtil.hexToBigInteger(bigNumHex)) == 0);
  }

  @Test
  public void testBytesToHexStringNoPrefix() {
    byte[] bigNumBytes = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    String bigNumHex = "01010101010101010101010101010101010101010101010101010101010101010101010101010101";
    byte[] smallNumBytes = new byte[]{1, 1, 1};
    String smallNumHex = "010101";
    assertEquals(smallNumHex, NumericUtil.bytesToHex(smallNumBytes));
    assertEquals(bigNumHex, NumericUtil.bytesToHex(bigNumBytes));
  }

  @Test
  public void testBigIntToHexStringWithZeroPadded() {
    String[][] example = new String[][]{
        new String[]{
            "00010101",
            "65793",
            "8"
        },
        new String[]{
            "010101",
            "65793",
            "6"
        }
    };
    for (String[] test : example) {
      assertEquals(test[0], NumericUtil.bigIntegerToHexWithZeroPadded(new BigInteger(test[1]), Integer.parseInt(test[2])));
    }

  }

  @Test
  public void testBigIntToBytesPadded() {
    byte[] smallNumBytes = new byte[]{0, 0, 0, 0, 0, 1, 1, 1};
    String smallNum = "65793";
    byte[] mediumNumBytes = new byte[]{0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    String mediumNum = "310698676526526814092329217";

    assertArrayEquals(smallNumBytes, NumericUtil.bigIntegerToBytesWithZeroPadded(new BigInteger(smallNum), 8));
    assertArrayEquals(mediumNumBytes, NumericUtil.bigIntegerToBytesWithZeroPadded(new BigInteger(mediumNum), 16));
  }

  @Test
  public void testHexToBytes() {
    Object[][] example = new Object[][] {
        new Object[] {
            new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            "0x1010101010101010101010101010101010101010101010101010101010101010101010101010101"
        },
        new Object[] {
            new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            "1010101010101010101010101010101010101010101010101010101010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            "0x010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1,1,1 },
            "0x10101"
        },
        new Object[] {
            new byte[0],
            ""
        },
    };
    for(Object[] test : example) {
      assertArrayEquals((byte[])test[0], NumericUtil.hexToBytes((String)test[1]));
    }

  }

  @Test
  public void testBytesToHexString() {
    Object[][] example = new Object[][] {
        new Object[] {
            new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            "01010101010101010101010101010101010101010101010101010101010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            "010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1,1,1 },
            "010101"
        },
        new Object[] {
            new byte[0],
            ""
        },
    };
    for(Object[] test : example) {
      assertEquals(test[1], NumericUtil.bytesToHex((byte[])test[0]));
    }
  }

  @Test
  public void testBytesToHexWithoutPrefix() {
    Object[][] example = new Object[][] {
        new Object[] {
            new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            "01010101010101010101010101010101010101010101010101010101010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            "010101010101010101010101"
        },
        new Object[] {
            new byte[] { 1,1,1 },
            "010101"
        },
        new Object[] {
            new byte[0],
            ""
        },
    };
    for(Object[] test : example) {
      assertEquals(test[1], NumericUtil.bytesToHex((byte[])test[0]));
    }
  }

  @Test
  public void testBeBigEndian() {
    String[][] example = new String[][] {
        new String[] {
            "1020",
            "2010"
        },
        new String[] {
            "102030",
            "302010"
        },
        new String[] {
            "10",
            "10"
        }
    };
    for (String[] test : example) {
      assertEquals(test[0], NumericUtil.beBigEndianHex(test[1]));
    }
  }


}
