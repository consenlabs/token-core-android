package org.consenlabs.tokencore.wallet.model;

public class ChainId {
  public static final int BITCOIN_MAINNET = 0;
  public static final int BITCOIN_TESTNET = 1;

  // ref: https://github.com/ethereum/EIPs/blob/master/EIPS/eip-155.md
  public static final int ETHEREUM_MAINNET = 1;
  public static final int ETHEREUM_ROPSTEN = 3;
  public static final int ETHEREUM_KOVAN = 42;
  public static final int ETHEREUM_CLASSIC_MAINNET = 61;
  public static final int ETHEREUM_CLASSIC_TESTNET = 62;

  public static final int ANY = 999;

  public static final String EOS_MAINNET = "aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906";
}
