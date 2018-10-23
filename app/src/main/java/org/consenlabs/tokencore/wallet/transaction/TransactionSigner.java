package org.consenlabs.tokencore.wallet.transaction;

import org.consenlabs.tokencore.wallet.Wallet;

public interface TransactionSigner {
  TxSignResult signTransaction(String chainId, String password, Wallet wallet);
}
