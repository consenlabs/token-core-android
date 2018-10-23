package org.consenlabs.tokencore.wallet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Metadata implements Cloneable {
  public static final String FROM_MNEMONIC = "MNEMONIC";
  public static final String FROM_KEYSTORE = "KEYSTORE";
  public static final String FROM_PRIVATE = "PRIVATE";
  public static final String FROM_WIF = "WIF";
  public static final String FROM_NEW_IDENTITY = "NEW_IDENTITY";
  public static final String FROM_RECOVERED_IDENTITY = "RECOVERED_IDENTITY";

  public static final String P2WPKH = "P2WPKH";
  public static final String NONE = "NONE";

  public static final String NORMAL = "NORMAL";

  public static final String HD = "HD";
  public static final String RANDOM = "RANDOM";
  public static final String HD_SHA256 = "HD_SHA256";
  public static final String V3 = "V3";


  private String name;
  private String passwordHint;
  private String chainType;
  private long timestamp;
  private String network;
  private List<String> backup = new ArrayList<>();
  private String source;
  private String mode = NORMAL;
  private String walletType;
  private String segWit;

  // for jackson serial
  public Metadata() {
  }


  @Override
  public Metadata clone() {

    Metadata metadata = null;
    try {
      metadata = (Metadata) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new TokenException("Clone metadata filed");
    }
    metadata.backup = new ArrayList<>(backup);
    return metadata;
  }

  public String getSegWit() {
    return segWit;
  }

  public void setSegWit(String segWit) {
    this.segWit = segWit;
  }

  public Metadata(String type, String network, String name, String passwordHint) {
    this.chainType = type;
    this.name = name;
    this.passwordHint = passwordHint;

    this.timestamp = System.currentTimeMillis() / 1000;
    this.network = network;
  }

  public Metadata(String type, String network, String name, String passwordHint, String segWit) {
    this.chainType = type;
    this.name = name;
    this.passwordHint = passwordHint;
    this.timestamp = System.currentTimeMillis() / 1000;
    this.network = network;
    this.segWit = segWit;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPasswordHint() {
    return passwordHint;
  }

  public void setPasswordHint(String passwordHint) {
    this.passwordHint = passwordHint;
  }

  public String getChainType() {
    return chainType;
  }

  public void setChainType(String chainType) {
    this.chainType = chainType;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public List<String> getBackup() {
    return backup;
  }

  public void setBackup(List<String> backup) {
    this.backup = backup;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getWalletType() {
    return walletType;
  }

  public void setWalletType(String walletType) {
    this.walletType = walletType;
  }

  @JsonIgnore
  public Boolean isMainNet() {
    return Network.MAINNET.equalsIgnoreCase(network);
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }


}
