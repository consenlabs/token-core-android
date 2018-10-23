package org.consenlabs.tokencore.wallet.address;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by xyz on 2018/2/2.
 */

public class AddressCreatorTest {
  @Test
  public void testGenerateBTCAddress() {
    AddressCreator creator;

    creator = AddressCreatorManager.getInstance(ChainType.BITCOIN, true, Metadata.NONE);
    assertTrue(creator instanceof BitcoinAddressCreator);

    assertEquals("1N3RC53vbaDNrziTdWmctBEeQ4fo4quNpq", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_WIF));
    assertEquals("1N3RC53vbaDNrziTdWmctBEeQ4fo4quNpq", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));

    creator = AddressCreatorManager.getInstance(ChainType.BITCOIN, false, Metadata.NONE);
    assertTrue(creator instanceof BitcoinAddressCreator);
    assertEquals("n2ZNV88uQbede7C5M5jzi6SyG4GVuPpng6", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_TESTNET_WIF));
    assertEquals("n2ZNV88uQbede7C5M5jzi6SyG4GVuPpng6", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));
  }


  @Test
  public void testGenerateBTCP2WPKHAddress() {
    AddressCreator creator;

    creator = AddressCreatorManager.getInstance(ChainType.BITCOIN, true, Metadata.P2WPKH);
    assertTrue(creator instanceof SegWitBitcoinAddressCreator);

    assertEquals("3Js9bGaZSQCNLudeGRHL4NExVinc25RbuG", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_WIF));
    assertEquals("3Js9bGaZSQCNLudeGRHL4NExVinc25RbuG", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));

    creator = AddressCreatorManager.getInstance(ChainType.BITCOIN, false, Metadata.P2WPKH);
    assertTrue(creator instanceof SegWitBitcoinAddressCreator);
    assertEquals("2NARMf1Wb3rhiYhGBwYuCgKEDi4zmojTsvk", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_TESTNET_WIF));
    assertEquals("2NARMf1Wb3rhiYhGBwYuCgKEDi4zmojTsvk", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));
  }

  @Test
  public void testGenerateETHAddress() {
    AddressCreator creator;
    creator = AddressCreatorManager.getInstance(ChainType.ETHEREUM, false, Metadata.NONE);
    assertTrue(creator instanceof EthereumAddressCreator);
    assertEquals("ef678007d18427e6022059dbc264f27507cd1ffc", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_STRING));
    assertEquals("ef678007d18427e6022059dbc264f27507cd1ffc", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));

    creator = AddressCreatorManager.getInstance(ChainType.ETHEREUM, true, Metadata.NONE);
    assertTrue(creator instanceof EthereumAddressCreator);
    assertEquals("ef678007d18427e6022059dbc264f27507cd1ffc", creator.fromPrivateKey(SampleKey.PRIVATE_KEY_STRING));
    assertEquals("ef678007d18427e6022059dbc264f27507cd1ffc", creator.fromPrivateKey(NumericUtil.hexToBytes(SampleKey.PRIVATE_KEY_STRING)));
  }
}
