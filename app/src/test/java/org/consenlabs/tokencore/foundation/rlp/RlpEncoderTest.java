package org.consenlabs.tokencore.foundation.rlp;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.testutils.ResourcesManager;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RlpEncoderTest {

  /**
   * For further examples see https://github.com/ethereum/tests/tree/develop/RLPTests.
   */
  @Test
  public void testEthereumRLPTests() {
    Map<String, RLPTestCase> cases = loadRLPTestCases();
    cases.forEach((key, aCase) -> {
      RlpType rlpType = buildRLPType(aCase.getIn());
      String message = "Comparing " + key + ": ";
      String actualRlp = NumericUtil.bytesToHex(RlpEncoder.encode(rlpType));
      Assert.assertEquals(message, aCase.getOut(), actualRlp);
    });
  }

  private RlpType buildRLPType(Object in) {
    if (in instanceof ArrayList) {
      List<RlpType> elementList = new ArrayList<>();
      for (Object o : ((ArrayList) in).toArray()) {
        elementList.add(buildRLPType(o));
      }
      return new RlpList(elementList);
    } else {
      if (in instanceof String) {
        String s = in.toString();
        if (s.startsWith("#")) {
          return RlpString.create(new BigInteger(s.substring(1)));
        } else {
          return RlpString.create(s);
        }
      } else if (in instanceof Integer) {
        return RlpString.create(Integer.parseInt(in.toString()));
      }

    }
    throw new IllegalArgumentException("can't parse format: " + in);
  }

  private Map<String, RLPTestCase> loadRLPTestCases() {
    ObjectMapper mapper = new ObjectMapper();
    JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, RLPTestCase.class);

    Map<String, RLPTestCase> cases = null;

    String testContent = ResourcesManager.readFileContent("rlptest.json");

    try {
      cases = mapper.readValue(testContent, type);
    } catch (IOException e) {
      e.printStackTrace();

    }
    return cases;

  }
}
