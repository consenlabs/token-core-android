package org.consenlabs.tokencore.wallet.keystore;

public abstract class WalletKeystore extends Keystore {
  String address;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public abstract Keystore changePassword(String oldPassword, String newPassword);

  public WalletKeystore() { super();}
}
