package org.consenlabs.tokencore.wallet;

import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.bitcoinj.core.ECKey;
import java.math.BigInteger;

/**
 * from web3j sample key
 */
public class SampleKey {
  public static final String NAME = "imToken Test";
  public static final String PRIVATE_KEY_STRING =
      "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6";
  public static final String PRIVATE_KEY_WIF = "L2hfzPyVC1jWH7n2QLTe7tVTb6btg9smp5UVzhEBxLYaSFF7sCZB";
  public static final String TESTNET_WIF = "cT4fTJyLd5RmSZFHnkGmVCzXDKuJLbyTt7cy77ghTTCagzNdPH1j";
  public static final String PRIVATE_KEY_TESTNET_WIF = "cT4fTJyLd5RmSZFHnkGmVCzXDKuJLbyTt7cy77ghTTCagzNdPH1j";
  static final String PUBLIC_KEY_STRING =
      "0x506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aab"
          + "a645c0b7b58158babbfa6c6cd5a48aa7340a8749176b120e8516216787a13dc76";
  public static final String ADDRESS = "ef678007d18427e6022059dbc264f27507cd1ffc";
  public static final String ADDRESS_NO_PREFIX = NumericUtil.cleanHexPrefix(ADDRESS);

  public static final String MNEMONIC = "inject kidney empty canal shadow pact comfort wife crush horse wife sketch";
  public static final String OTHER_MNEMONIC = "spy excess school tiger quick link olympic timber final learn rebuild dragon";
  public static final String BITCOIN_TESTNET_HD_PATH = "m/44'/1'/0";
  public static final String BITCOIN_MAINNET_HD_PATH = "m/44'/0'/0'";
  public static final String ETHEREUM_HD_PATH = "m/44'/60'/0'/0/0";

  public static final String PASSWORD = "Insecure Pa55w0rd";
  public static final String NEW_PASSWORD = "NEW_Password";
  public static final String WRONG_PASSWORD = "Wrong Password";
  public static final String PASSWORD_HINT = "Password Hint";

  public static final BigInteger PRIVATE_KEY = NumericUtil.hexToBigInteger(PRIVATE_KEY_STRING);
  public static final BigInteger PUBLIC_KEY = NumericUtil.hexToBigInteger(PUBLIC_KEY_STRING);

  public static final ECKey KEY_PAIR = ECKey.fromPrivate(PRIVATE_KEY,false);

  public static String V3JSON = "{\"version\":3,\"id\":\"83d0cf85-6230-4b93-aadc-e9220df32674\",\"address\":\"41983f2e3af196c1df429a3ff5cdecc45c82c600\",\"Crypto\":{\"ciphertext\":\"11ba4af9f87ed29b2fdcf04e581caf50d687559f9a0a667dc1f7d389db2bfa24\",\"cipherparams\":{\"iv\":\"dc65f752f9ee37f546c4b3c0aefac1dd\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"997d65727201163620a597f8e7540f67d284bb1a27dbf6f7267ea723c1b400ad\",\"n\":8192,\"r\":8,\"p\":1},\"mac\":\"4444445aecc1b4d6c823419afce45baf57e5680f7bb9b7e6a0e810859b83aec1\"}}";


  public static String WRONG_V3JSON = "{\"version\":3,\"id\":\"83d0cf85-6230-4b93-aadc-e9220df32674\",\"address\":\"41983f2e3af196c1df429a3ff5cdecc45c82c600\",\"Crypto\":{\"ciphertext\":\"11ba4af9f87ed29b2fdcf04e581caf50d687559f9a0a667dc1f7d389db2bfa24\",\"cipherparams\":{\"iv\":\"dc65f752f9ee37f546c4b3c0aefac1dd\"},\"cipher\":\"aes-128-ctr\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"salt\":\"997d65727201163620a597f8e7540f67d284bb1a27dbf6f7267ea723c1b400ad\",\"n\":8192,\"r\":8,\"p\":1}}}";

}
