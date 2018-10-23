package org.consenlabs.tokencore.wallet.transaction;

import java.util.List;

public class TxMultiSignResult {

  public TxMultiSignResult(String txHash, List<String> signed) {
    this.txHash = txHash;
    this.signed = signed;
  }

  String txHash;
  List<String> signed;

  public String getTxHash() {
    return txHash;
  }

  public void setTxHash(String txHash) {
    this.txHash = txHash;
  }

  public List<String> getSigned() {
    return signed;
  }

  public void setSigned(List<String> signed) {
    this.signed = signed;
  }
}
