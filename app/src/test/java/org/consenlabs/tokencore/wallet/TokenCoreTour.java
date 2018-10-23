package org.consenlabs.tokencore.wallet;

import org.bitcoinj.core.ECKey;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainId;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.transaction.BitcoinTransaction;
import org.consenlabs.tokencore.wallet.transaction.EthereumTransaction;
import org.consenlabs.tokencore.wallet.transaction.TxSignResult;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TokenCoreTour extends WalletSupport {
  @Test
  public void createEthereumWalletExample() {
    System.out.println("-------- Create ethereum wallet example: ");
    Metadata metadata = new Metadata(ChainType.ETHEREUM, Network.MAINNET, "name", "passwordHint");
    metadata.setSource(Metadata.FROM_PRIVATE);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata,
        SampleKey.PRIVATE_KEY_STRING,
        SampleKey.PASSWORD, true);

    ECKey key = ECKey.fromPrivate(wallet.decryptMainKey(SampleKey.PASSWORD));
    assertEquals(
        NumericUtil.bigIntegerToHex(key.getPrivKey()),
        SampleKey.PRIVATE_KEY_STRING);

    assertNotEquals(wallet.verifyPassword("benn"), true);
    System.out.println("Ethereum wallet privateKey: " + wallet.exportPrivateKey(SampleKey.PASSWORD));
    System.out.println("Ethereum wallet address: " + wallet.getAddress());
    wallet.delete(SampleKey.PASSWORD);
  }

  @Test
  public void signEthereumTransactionExample() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_PRIVATE);
    metadata.setWalletType(Metadata.V3);
    metadata.setChainType(ChainType.ETHEREUM);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(metadata, SampleKey.PRIVATE_KEY_STRING, SampleKey.PASSWORD, true);

    EthereumTransaction ethTx = new EthereumTransaction(BigInteger.valueOf(8L), BigInteger.valueOf(20000000008L),
        BigInteger.valueOf(189000L), "0x3535353535353535353535353535353535353535", BigInteger.valueOf(512), "");
    System.out.println();
    System.out.println("-------- Ethereum transaction sign example: ");
    String result = ethTx.signTransaction("0", SampleKey.PASSWORD, wallet).getSignedTx();
    System.out.println("Signed result: " + result);
  }

  @Test
  public void createBTCHDWalletExample() {
    System.out.println();
    System.out.println("-------- Create BTC Wallet Example: ");
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setWalletType(Metadata.HD);
    metadata.setChainType(ChainType.BITCOIN);
    Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_MAINNET_PATH, SampleKey.PASSWORD, true);
    System.out.println("m/44'/0'/0'/0/0 address: " + wallet.getAddress());
    System.out.println("m/44'/0'/0'/1/0 address: " + wallet.newReceiveAddress(0));
    System.out.println("Enc XPub(Encrypted with 'aes-cbc-128'): " + wallet.getEncXPub());
  }

  @Test
  public void bitcoinTransactionSignExample() {
    System.out.println();
    System.out.println("-------- Bitcoin transaction sign example: ");
    Metadata walletMetadata = new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint");
    walletMetadata.setSource(Metadata.FROM_MNEMONIC);
    walletMetadata.setNetwork(Network.TESTNET);
    Wallet wallet = WalletManager.importWalletFromMnemonic(walletMetadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_TESTNET_PATH,
        SampleKey.PASSWORD, true);

    TxSignResult signedResult = createMultiUXTOOnTestnet().signTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), SampleKey.PASSWORD, wallet);
    System.out.println("Sign Result: " + signedResult.getSignedTx());
  }


  private static BitcoinTransaction createMultiUXTOOnTestnet() {
    ArrayList<BitcoinTransaction.UTXO> utxo = new ArrayList<>();

    utxo.add(new BitcoinTransaction.UTXO(
        "983adf9d813a2b8057454cc6f36c6081948af849966f9b9a33e5b653b02f227a", 0,
        200000000, "mh7jj2ELSQUvRQELbn9qyA4q5nADhmJmUC",
        "76a914118c3123196e030a8a607c22bafc1577af61497d88ac",
        "0/22"));
    utxo.add(new BitcoinTransaction.UTXO(
        "45ef8ac7f78b3d7d5ce71ae7934aea02f4ece1af458773f12af8ca4d79a9b531", 1,
        200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
        "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
        "0/0"));
    utxo.add(new BitcoinTransaction.UTXO(
        "14c67e92611dc33df31887bbc468fbbb6df4b77f551071d888a195d1df402ca9", 0,
        200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
        "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
        "0/0"));
    utxo.add(new BitcoinTransaction.UTXO(
        "117fb6b85ded92e87ee3b599fb0468f13aa0c24b4a442a0d334fb184883e9ab9", 1,
        200000000, "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN",
        "76a914383fb81cb0a3fc724b5e08cf8bbd404336d711f688ac",
        "0/0"));

    BitcoinTransaction tran = new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3", 53,
        750000000, 502130, utxo);

    return tran;
  }

}
