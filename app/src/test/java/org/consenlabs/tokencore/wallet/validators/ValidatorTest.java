package org.consenlabs.tokencore.wallet.validators;

//import com.facebook.react.bridge.JavaOnlyMap;
//import com.facebook.react.bridge.ReadableMap;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.consenlabs.tokencore.wallet.WalletManager;
import org.consenlabs.tokencore.wallet.WalletSupport;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by xyz on 2018/3/5.
 */

public class ValidatorTest extends WalletSupport {

  @Test
  public void testValidateMetadata() {

    HashMap<String, Object> params = new LinkedHashMap<>();
    params.put("name", "");
    params.put("passwordHint", "no hint");
    params.put("chainType", "Bitcoin");
    params.put("network", Network.MAINNET);
    MetadataValidator validator = new MetadataValidator(params);
    try {
      validator.validate();
      Assert.fail("Should not run to here");
    } catch (IllegalStateException ex) {
      Assert.assertEquals(ex.getMessage(), "Can't allow empty name");
    }

    params = new LinkedHashMap<>();
    params.put("name", "xyz");
    params.put("passwordHint", "");
    params.put("chainType", "BTC");
    params.put("network", Network.MAINNET);
    validator = new MetadataValidator(params);

    try {
      validator.validate();
      Assert.fail("Should not run to here");
    } catch (TokenException ex) {
      Assert.assertEquals(ex.getMessage(), Messages.WALLET_INVALID_TYPE);
    }

    params = new HashMap<>();
    params.put("name", "xyz");
    params.put("passwordHint", "");
    params.put("chainType", ChainType.BITCOIN);
    params.put("network", Network.MAINNET);
    validator = new MetadataValidator(params);

    try {
      validator.validate();
    } catch (TokenException ex) {
      Assert.fail("Should not run to here");
    }

    params = new LinkedHashMap<>();
    params.put("name", "xyz");
    params.put("passwordHint", "");
    params.put("chainType", ChainType.BITCOIN);
    params.put("network", Network.MAINNET);
    validator = new MetadataValidator(params, Metadata.FROM_NEW_IDENTITY);

    try {
      Metadata metadata = validator.validate();
      Assert.assertTrue(Metadata.FROM_NEW_IDENTITY.equals(metadata.getSource()));
    } catch (TokenException ex) {
      Assert.fail("Should not run to here");
    }
  }

  @Test
  public void testValidateWIF() {
    WIFValidator validator = new WIFValidator(SampleKey.TESTNET_WIF, MainNetParams.get());
    try {
      validator.validate();
      Assert.fail("Should not run to here");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.WIF_WRONG_NETWORK, ex.getMessage());
    }

    validator = new WIFValidator(SampleKey.PRIVATE_KEY_STRING, MainNetParams.get());
    try {
      validator.validate();
      Assert.fail("Should not run to here");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.WIF_INVALID, ex.getMessage());
    }

    validator = new WIFValidator(SampleKey.TESTNET_WIF, TestNet3Params.get());
    try {
      validator.validate();
    } catch (TokenException ex) {
      Assert.fail("Should not run to here");
    }


  }

  @Test
  public void testValidETHAddress() {
    String[] invalidAddresses = new String[]{
        "shortstr",
        "longstrlongstrlongstrlongstrlongstrlongstrlongstrlongstrlongstr"
    };
    for (String address : invalidAddresses) {
      ETHAddressValidator validator = new ETHAddressValidator(address);
      try {
        validator.validate();
        Assert.fail("Should throw exception");
      } catch (TokenException ex) {
        Assert.assertEquals(1, 1);
      }
    }

    String[] validAddresses = new String[]{
        SampleKey.ADDRESS,
        SampleKey.ADDRESS_NO_PREFIX,
        SampleKey.ADDRESS.toLowerCase()
    };

    for (String address : validAddresses) {
      try {
        new ETHAddressValidator(address).validate();
      } catch (Exception ex) {
        Assert.fail("Should not throw any exception when validate a Valid Address");
      }
    }
  }

  @Test
  public void testValidateImportingMnemonic() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setChainType(ChainType.ETHEREUM);
    String[][] testcase = new String[][]{
        new String[]{"inject kidney empty canal shadow pact comfort wife crush horse wife", Messages.MNEMONIC_INVALID_LENGTH},
        new String[]{"BAD_WORD kidney empty canal shadow pact comfort wife crush horse wife sketch", Messages.MNEMONIC_BAD_WORD},
        new String[]{"inject kidney empty canal shadow pact comfort wife crush horse wife wife", Messages.MNEMONIC_CHECKSUM}
    };

    for (String[] aCase : testcase) {
      try {
        WalletManager.importWalletFromMnemonic(metadata, aCase[0], BIP44Util.ETHEREUM_PATH, SampleKey.PASSWORD, true);
        Assert.fail(String.format("%s should throw %s", aCase[0], aCase[1]));
      } catch (TokenException ex) {
        Assert.assertEquals(String.format("Mnemonic: %s", aCase[0]), ex.getMessage(), aCase[1]);
      }
    }

    metadata.setChainType(ChainType.EOS);
    for (String[] aCase : testcase) {
      try {
        WalletManager.importWalletFromMnemonic(metadata, aCase[0], BIP44Util.EOS_PATH, SampleKey.PASSWORD, true);
        Assert.fail(String.format("%s should throw %s", aCase[0], aCase[1]));
      } catch (TokenException ex) {
        Assert.assertEquals(String.format("Mnemonic: %s", aCase[0]), ex.getMessage(), aCase[1]);
      }
    }
  }

  // There is not a good implement of java to verifyPrivateKey
  @Test
  public void testValidateImportingPrivateKey() {

    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_PRIVATE);
    metadata.setChainType(ChainType.ETHEREUM);

    String[][] testcase = new String[][]{
        new String[]{"a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5", "invalid length"},
        new String[]{"0000000000000000000000000000000000000000000000000000000000000000", "num is too small"},
        new String[]{"a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5fG", "invalid format"}
    };
    for (String[] aCase : testcase) {
      try {
        WalletManager.importWalletFromPrivateKey(metadata, aCase[0], SampleKey.PASSWORD, true);
        Assert.fail(String.format("%s should throw privatekey_invalid", aCase[1]));
      } catch (Exception ex) {
        Assert.assertEquals(aCase[1], ex.getMessage(), Messages.PRIVATE_KEY_INVALID);
      }

    }

  }

  @Test
  public void testImportingValidationPrivateKey() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_PRIVATE);
    metadata.setChainType(ChainType.ETHEREUM);
    DeterministicSeed seed = new DeterministicSeed(Arrays.asList(SampleKey.MNEMONIC.split(" ")), null, "", 0L);
    DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
    for (int i=0; i< 100; i++) {
      String path = String.format("m/44'/60'/0'/0/%d", i);
      DeterministicKey key = keyChain.getKeyByPath(BIP44Util.generatePath(path), true);
      new PrivateKeyValidator(key.getPrivateKeyAsHex()).validate();
    }

  }

}
