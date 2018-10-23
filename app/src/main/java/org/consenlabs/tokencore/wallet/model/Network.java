package org.consenlabs.tokencore.wallet.model;

/**
 * Created by xyz on 2018/3/7.
 */

public class Network {
  public static final String MAINNET = "MAINNET";
  public static final String TESTNET = "TESTNET";
  public static final String KOVAN = "KOVAN";
  public static final String ROPSTEN = "ROPSTEN";

  private String network;

  public Network(String network) {
    this.network = network;
  }

  public boolean isMainnet() {
    return MAINNET.equalsIgnoreCase(this.network);
  }
}
