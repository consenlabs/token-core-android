package org.consenlabs.tokencore.wallet.transaction;

import org.bitcoinj.params.TestNet3Params;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.SampleKey;
import org.consenlabs.tokencore.wallet.Wallet;
import org.consenlabs.tokencore.wallet.WalletManager;
import org.consenlabs.tokencore.wallet.WalletSupport;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainId;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class BitcoinTransactionTest extends WalletSupport {
  private static final Metadata walletMetadata = new Metadata(ChainType.BITCOIN, Network.TESTNET, "name", "passwordHint");

  @Test
  public void testSignTxByWIFWalletOnTestnet() {

    walletMetadata.setSource(Metadata.FROM_WIF);
    walletMetadata.setNetwork(Network.TESTNET);
    walletMetadata.setSegWit("none");
    Wallet wallet = WalletManager.importWalletFromPrivateKey(walletMetadata, SampleKey.TESTNET_WIF, SampleKey.PASSWORD, true);


    ArrayList<BitcoinTransaction.UTXO> utxo = new ArrayList<>();

    utxo.add(new BitcoinTransaction.UTXO(
        "e112b1215813c8888b31a80d215169809f7901359c0f4bf7e7374174ab2a64f4", 0,
        65000000, "n2ZNV88uQbede7C5M5jzi6SyG4GVuPpng6",
        "76a914899305a3f569188193fb75843b53c2b56b37988288ac",
        null));

    try {
      new BitcoinTransaction("mxCVgJtD2jSMv2diQVJQAwwq7Cg2wbwpmG",
          0,
          2000,
          10000, utxo);
      Assert.fail("Should throw amount_less_than_minimum");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.AMOUNT_LESS_THAN_MINIMUM, ex.getMessage());
    }

    BitcoinTransaction tran = new BitcoinTransaction("mxCVgJtD2jSMv2diQVJQAwwq7Cg2wbwpmG", 1,
        63999000, 1000000, utxo);

    TxSignResult signedResult = tran.signTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), SampleKey.PASSWORD, wallet);
    // expectedSignedHex doesn't has change output, you can check this on : https://live.blockcypher.com/btc/decodetx/
    String expected = "0100000001f4642aab744137e7f74b0f9c3501799f806951210da8318b88c8135821b112e1000000006b4830450221009b4a952af51fa057b8e5fb2eb114d0396a0fcbd2912d49c557bb11fc4caa87440220664202cffe79927aaef08aef9a07a60d9cf3f07599b5333d2b31ff2a701974e6012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aabaffffffff01188cd003000000001976a914b6fc6ecf55a41b240fd26aaed696624009818d9988ac00000000";
    assertEquals(expected, signedResult.getSignedTx());



    tran = new BitcoinTransaction("mxCVgJtD2jSMv2diQVJQAwwq7Cg2wbwpmG", 1,
        60000000, 1000000, utxo);

    signedResult = tran.signTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), SampleKey.PASSWORD, wallet);
    expected = "0100000001f4642aab744137e7f74b0f9c3501799f806951210da8318b88c8135821b112e1000000006b483045022100a2d7e684e61df275c35055952a37d0411964caf653172d947475153444ee1c20022038e1219322562864af183c9d4a9a376968b00efb8943ea84475799a23f412a3c012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aabaffffffff0200879303000000001976a914b6fc6ecf55a41b240fd26aaed696624009818d9988ac00093d00000000001976a914e6cfaab9a59ba187f0a45db0b169c21bb48f09b388ac00000000";
    assertEquals(expected, signedResult.getSignedTx());
  }

  @Test
  public void testSignTxByHDWalletOnTestnet() {
    walletMetadata.setSource(Metadata.FROM_MNEMONIC);
    walletMetadata.setNetwork(Network.TESTNET);
    Wallet wallet = WalletManager.importWalletFromMnemonic(walletMetadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_TESTNET_PATH,
        SampleKey.PASSWORD, true);

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
    try {
       new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3",
          0,
          2000,
          10000, utxo);
      Assert.fail("Should throw amount_less_than_minimum");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.AMOUNT_LESS_THAN_MINIMUM, ex.getMessage());
    }

    BitcoinTransaction tran = new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3", 53,
        799988000, 10000, utxo);

    TxSignResult signedResult = tran.signTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), SampleKey.PASSWORD, wallet);
    // the expected doesn't has change output, you can check on: https://live.blockcypher.com/btc/decodetx/
    String expected = "01000000047a222fb053b6e5339a9b6f9649f88a9481606cf3c64c4557802b3a819ddf3a98000000006b483045022100c610f77f71cc8afcfbd46df8e3d564fb8fb0f2c041bdf0869512c461901a8ad802206b92460cccbcb2a525877db1b4b7530d9b85e135ce88424d1f5f345dc65b881401210312a0cb31ff52c480c049da26d0aaa600f47e9deee53d02fc2b0e9acf3c20fbdfffffffff31b5a9794dcaf82af1738745afe1ecf402ea4a93e71ae75c7d3d8bf7c78aef45010000006b483045022100dce4a4c3d79bf9392832f68da3cd2daf85ac7fa851402ecc9aaac69b8761941d02201e1fd6601812ea9e39c6df0030cb754d4c578ff48bc9db6072ba5207a4ebc2b60121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffffa92c40dfd195a188d87110557fb7f46dbbfb68c4bb8718f33dc31d61927ec614000000006b483045022100e1802d80d72f5f3be624df3ab668692777188a9255c58067840e4b73a5a61a99022025b23942deb21f5d1959aae85421299ecc9efefb250dbacb46a4130abd538d730121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffffb99a3e8884b14f330d2a444a4bc2a03af16804fb99b5e37ee892ed5db8b67f11010000006a47304402207b82a62ed0d35c9878e6a7946d04679c8a17a8dd0a856b5cc14928fe1e9b554a0220411dd1a61f8ac2a8d7564de84e2c8a2c2583986bd71ac316ade480b8d0b4fffd0121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffff0120d9ae2f000000001976a91455bdc1b42e3bed851959846ddf600e96125423e088ac00000000";
    assertEquals(expected, signedResult.getSignedTx());


    tran = new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3", 53,
        750000000, 502130, utxo);

    signedResult = tran.signTransaction(Integer.toString(ChainId.BITCOIN_TESTNET), SampleKey.PASSWORD, wallet);
    expected = "01000000047a222fb053b6e5339a9b6f9649f88a9481606cf3c64c4557802b3a819ddf3a98000000006b483045022100c4f39ce7f2448ab8e7154a7b7ce82edd034e3f33e1f917ca43e4aff822ba804c02206dd146d1772a45bb5e51abb081d066114e78bcb504671f61c5a301a647a494ac01210312a0cb31ff52c480c049da26d0aaa600f47e9deee53d02fc2b0e9acf3c20fbdfffffffff31b5a9794dcaf82af1738745afe1ecf402ea4a93e71ae75c7d3d8bf7c78aef45010000006b483045022100d235afda9a56aaa4cbe05df712202e6b1a45aab7a0c83540d3053133f15acc5602201b0e144bec3a02a5c556596040b0be81b0202c19b163bb537b8d965afd61403a0121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffffa92c40dfd195a188d87110557fb7f46dbbfb68c4bb8718f33dc31d61927ec614000000006b483045022100dd8f1e20116f96a3400f55e0c637a0ad21ae47ff92d83ffb0c3d324c684a54be0220064b0a6d316154ef07a69bd82de3a052e43c3c6bb0e55e4de4de939b093e1a3a0121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffffb99a3e8884b14f330d2a444a4bc2a03af16804fb99b5e37ee892ed5db8b67f11010000006a473044022048d8cb0f1480174b3b9186cc6fe410db765f1f9d3ce036b0d4dee0eb19aa3641022073de4bb2b00a0533e9c8f3e074c655e0695c8b223233ddecf3c99a84351d50a60121033d710ab45bb54ac99618ad23b3c1da661631aa25f23bfe9d22b41876f1d46e4effffffff028017b42c000000001976a91455bdc1b42e3bed851959846ddf600e96125423e088ac0e47f302000000001976a91412967cdd9ceb72bbdbb7e5db85e2dbc6d6c3ab1a88ac00000000";
    assertEquals(expected, signedResult.getSignedTx());
  }

  @Test
  @Ignore
  /*
    this case not work Because BitcoinJ doesn't allow the prvKey be `BigInteger.ONE`
   */
  public void testSignTxByWIFWalletOnMainnet() {

    /*

    { "exec": "./bitcoin-tx",
        "args":
    ["-create", "nversion=1",
        "in=4d49a71ec9da436f71ec4ee231d04f292a29cd316f598bb7068feccabdc59485:0",
        "set=privatekeys:[\"5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf\"]",
        "set=prevtxs:[{\"txid\":\"4d49a71ec9da436f71ec4ee231d04f292a29cd316f598bb7068feccabdc59485\",\"vout\":0,\"scriptPubKey\":\"76a91491b24bf9f5288532960ac687abb035127b1d28a588ac\"}]",
        "sign=ALL",
        "outaddr=0.001:193P6LtvS4nCnkDvM9uXn1gsSRqh4aDAz7"],
      "output_cmp": "txcreatesignv1.hex",
        "description": "Creates a new v1 transaction with a single input and a single output, and then signs the transaction"
    }
    */


    String mainnetWIF = "5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAnchuDf";
    walletMetadata.setSource(Metadata.FROM_WIF);
    walletMetadata.setNetwork(Network.MAINNET);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(walletMetadata, mainnetWIF, SampleKey.PASSWORD, true);


    TxSignResult signedResult = createSingleUXTOOnMainnet().signTransaction(Integer.toString(ChainId.BITCOIN_MAINNET), SampleKey.PASSWORD, wallet);
    String expected = "01000000018594c5bdcaec8f06b78b596f31cd292a294fd031e24eec716f43dac91ea7494d000000008b48304502210096a75056c9e2cc62b7214777b3d2a592cfda7092520126d4ebfcd6d590c99bd8022051bb746359cf98c0603f3004477eac68701132380db8facba19c89dc5ab5c5e201410479be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8ffffffff01a0860100000000001976a9145834479edbbe0539b31ffd3a8f8ebadc2165ed0188ac00000000";
    Assert.assertEquals(expected, signedResult.getSignedTx());
  }

  private static BitcoinTransaction createSingleUXTOOnMainnet() {
    ArrayList<BitcoinTransaction.UTXO> utxo = new ArrayList<>();

    utxo.add(new BitcoinTransaction.UTXO(
        "4d49a71ec9da436f71ec4ee231d04f292a29cd316f598bb7068feccabdc59485", 0,
        100000, "193P6LtvS4nCnkDvM9uXn1gsSRqh4aDAz7",
        "76a91491b24bf9f5288532960ac687abb035127b1d28a588ac",
        null));

    BitcoinTransaction tran = new BitcoinTransaction("193P6LtvS4nCnkDvM9uXn1gsSRqh4aDAz7", 1,
        100000, 100, utxo);

    return tran;
  }


  @Test
  public void bitcoinSegwitSign() {
    ArrayList<BitcoinTransaction.UTXO> utxos = new ArrayList<>();
    utxos.add(new BitcoinTransaction.UTXO("c2ceb5088cf39b677705526065667a3992c68cc18593a9af12607e057672717f",
        0, 50000, "2MwN441dq8qudMvtM5eLVwC3u4zfKuGSQAB",
        "a9142d2b1ef5ee4cf6c3ebc8cf66a602783798f7875987",
        "0/0"));
    utxos.add(new BitcoinTransaction.UTXO("9ad628d450952a575af59f7d416c9bc337d184024608f1d2e13383c44bd5cd74",
        0, 50000, "2N54wJxopnWTvBfqgAPVWqXVEdaqoH7Suvf",
        "a91481af6d803fdc6dca1f3a1d03f5ffe8124cd1b44787",
        "0/1"));

    walletMetadata.setSource(Metadata.FROM_MNEMONIC);
    walletMetadata.setNetwork(Network.TESTNET);
    walletMetadata.setSegWit(Metadata.P2WPKH);
    Wallet wallet = WalletManager.importWalletFromMnemonic(walletMetadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_SEGWIT_TESTNET_PATH, SampleKey.PASSWORD, true);
//    Wallet wallet = WalletManager.importWalletFromPrivateKey(walletMetadata, mainnetWIF, SampleKey.PASSWORD, true);

//<<<<<<< HEAD
//    TxSignResult result = transaction.signSegWitTransaction("0", SampleKey.PASSWORD, wallet);
//    String expectedSignedHex = "020000000001027f717276057e6012afa99385c18cc692397a666560520577679bf38c08b5cec20000000017160014654fbb08267f3d50d715a8f1abb55979b160dd5bffffffff74cdd54bc48333e1d2f108460284d137c39b6c417d9ff55a572a9550d428d69a00000000171600149d66aa6399de69d5c5ae19f9098047760251a854ffffffff02803801000000000017a914b710f6e5049eaf0404c2f02f091dd5bb79fa135e87102700000000000017a914755fba51b5c443b9f16b1f86665dec10dd7a25c58702483045022100f0c66cd322e50f992ad34448fb3bf823066e5ffaa8e840a901058a863a4d950c02206cdafb1ad1ef4d938122b106069d8b435387e4d55711f50a46a8d91d9f674c550121031aee5e20399d68cf0035d1a21564868f22bc448ab205292b4279136b15ecaebc02483045022100cfe92e4ad4fbfc13be20afc6f37429e26426257d015b409d28c260544e581b2c022028412816d1fef11093b474c2c662a25a4062f4e37d06ce66207863de98814a07012103a241c8d13dd5c92475652c43bf56580fbf9f1e8bc0aa0132ddc8443c03062bb900000000";
//    Assert.assertEquals(expectedSignedHex, result.getSignedTx());
//=======

    BitcoinTransaction transaction;
    TxSignResult signResult;
    try {
      transaction = new BitcoinTransaction("2N9wBy6f1KTUF5h2UUeqRdKnBT6oSMh4Whp",
          0,
          2000,
          10000, utxos);
      Assert.fail("Should throw amount_less_than_minimum");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.AMOUNT_LESS_THAN_MINIMUM, ex.getMessage());
    }

    transaction = new BitcoinTransaction("2N9wBy6f1KTUF5h2UUeqRdKnBT6oSMh4Whp",
        0,
        88000,
        10000, utxos);
    signResult = transaction.signSegWitTransaction("0", SampleKey.PASSWORD, wallet);
    // expectedSignedHex doesn't has change output, you can check this on : https://live.blockcypher.com/btc/decodetx/
    String expectedSignedHex = "020000000001027f717276057e6012afa99385c18cc692397a666560520577679bf38c08b5cec20000000017160014654fbb08267f3d50d715a8f1abb55979b160dd5bffffffff74cdd54bc48333e1d2f108460284d137c39b6c417d9ff55a572a9550d428d69a00000000171600149d66aa6399de69d5c5ae19f9098047760251a854ffffffff01c05701000000000017a914b710f6e5049eaf0404c2f02f091dd5bb79fa135e870247304402205fd9dea5df0db5cc7b1d4b969f63b4526fb00fd5563ab91012cb511744a53d570220784abfe099a2b063b1cfc1f145fef2ffcb100b0891514fa164d357f0ef7ca6bb0121031aee5e20399d68cf0035d1a21564868f22bc448ab205292b4279136b15ecaebc02483045022100b0246c12428dbf863fcc9060ab6fc46dc2135adaa6cf8117de49f9acecaccf6c022059377d05c9cab24b7dec14242ea3206cc1f464d5ff9904dca515fc71766507cd012103a241c8d13dd5c92475652c43bf56580fbf9f1e8bc0aa0132ddc8443c03062bb900000000";
    Assert.assertEquals(expectedSignedHex, signResult.getSignedTx());


    transaction = new BitcoinTransaction("2N9wBy6f1KTUF5h2UUeqRdKnBT6oSMh4Whp",
        0,
        80000,
        10000, utxos);
    signResult = transaction.signSegWitTransaction("0", SampleKey.PASSWORD, wallet);
    expectedSignedHex = "020000000001027f717276057e6012afa99385c18cc692397a666560520577679bf38c08b5cec20000000017160014654fbb08267f3d50d715a8f1abb55979b160dd5bffffffff74cdd54bc48333e1d2f108460284d137c39b6c417d9ff55a572a9550d428d69a00000000171600149d66aa6399de69d5c5ae19f9098047760251a854ffffffff02803801000000000017a914b710f6e5049eaf0404c2f02f091dd5bb79fa135e87102700000000000017a914755fba51b5c443b9f16b1f86665dec10dd7a25c58702483045022100f0c66cd322e50f992ad34448fb3bf823066e5ffaa8e840a901058a863a4d950c02206cdafb1ad1ef4d938122b106069d8b435387e4d55711f50a46a8d91d9f674c550121031aee5e20399d68cf0035d1a21564868f22bc448ab205292b4279136b15ecaebc02483045022100cfe92e4ad4fbfc13be20afc6f37429e26426257d015b409d28c260544e581b2c022028412816d1fef11093b474c2c662a25a4062f4e37d06ce66207863de98814a07012103a241c8d13dd5c92475652c43bf56580fbf9f1e8bc0aa0132ddc8443c03062bb900000000";
    Assert.assertEquals(expectedSignedHex, signResult.getSignedTx());


    transaction = new BitcoinTransaction("moLK3tBG86ifpDDTqAQzs4a9cUoNjVLRE3",
        0,
        80000,
        10000, utxos);
    walletMetadata.setSource(Metadata.FROM_WIF);
    walletMetadata.setNetwork(Network.TESTNET);
    walletMetadata.setSegWit(Metadata.P2WPKH);
    wallet = WalletManager.importWalletFromPrivateKey(walletMetadata, "cT4fTJyLd5RmSZFHnkGmVCzXDKuJLbyTt7cy77ghTTCagzNdPH1j", SampleKey.PASSWORD, true);
    signResult = transaction.signSegWitTransaction("0", SampleKey.PASSWORD, wallet);
    expectedSignedHex = "020000000001027f717276057e6012afa99385c18cc692397a666560520577679bf38c08b5cec20000000017160014e6cfaab9a59ba187f0a45db0b169c21bb48f09b3ffffffff74cdd54bc48333e1d2f108460284d137c39b6c417d9ff55a572a9550d428d69a0000000017160014e6cfaab9a59ba187f0a45db0b169c21bb48f09b3ffffffff0280380100000000001976a91455bdc1b42e3bed851959846ddf600e96125423e088ac102700000000000017a914bc64b2d79807cd3d72101c3298b89117d32097fb8702483045022100d738ceca17a3b313e4eba2dd8b503946458ab529080284a2b0e143e110a7fd6402201211fbc3b43e83504e763a55eac5a9c524fb2a4268e44207555e026580dd91db012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba02483045022100c4a8c5d46a9a16740a9710df7ffd2822b5bfb0404b71ca4ea86fc71913d726c102200330a13e5a1a352c76ca3228290471524edab10c328d2db60c025820976bd43c012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba00000000";
    Assert.assertEquals(expectedSignedHex, signResult.getSignedTx());


    System.out.println(new SegWitBitcoinAddressCreator(TestNet3Params.get()).fromPrivateKey("cT4fTJyLd5RmSZFHnkGmVCzXDKuJLbyTt7cy77ghTTCagzNdPH1j"));
  }

  @Test
  public void testSignMultiUTXOSegWitTransaction() {
    ArrayList<BitcoinTransaction.UTXO> utxos = new ArrayList<>();
    utxos.add(new BitcoinTransaction.UTXO("ea2cdabdb11f2afdbe9e9d51744d5924bb3917ae4b383b3ef7c9c3dbb691653a",
        1, 100000000, "2NARMf1Wb3rhiYhGBwYuCgKEDi4zmojTsvk",
        "a914bc64b2d79807cd3d72101c3298b89117d32097fb87",
        ""));
    utxos.add(new BitcoinTransaction.UTXO("ad3b68e534f6deb12e1b8c1e1098b76e4b29c0e60416daae90487a91a982e366",
        0, 100000000, "2NARMf1Wb3rhiYhGBwYuCgKEDi4zmojTsvk",
        "a914bc64b2d79807cd3d72101c3298b89117d32097fb87",
        ""));
    BitcoinTransaction transaction = new BitcoinTransaction("mvqN876ymCo7HbRbmYoaoMfwigBdEKx4J1",
        0,
        195000000,
        210090, utxos);
    walletMetadata.setSource(Metadata.FROM_WIF);
    walletMetadata.setNetwork(Network.TESTNET);
    walletMetadata.setSegWit(Metadata.P2WPKH);
    Wallet wallet = WalletManager.importWalletFromPrivateKey(walletMetadata, "cT4fTJyLd5RmSZFHnkGmVCzXDKuJLbyTt7cy77ghTTCagzNdPH1j", SampleKey.PASSWORD, true);
    TxSignResult signResult = transaction.signSegWitTransaction("0", SampleKey.PASSWORD, wallet);
    String expectedSignedHex = "020000000001023a6591b6dbc3c9f73e3b384bae1739bb24594d74519d9ebefd2a1fb1bdda2cea0100000017160014e6cfaab9a59ba187f0a45db0b169c21bb48f09b3ffffffff66e382a9917a4890aeda1604e6c0294b6eb798101e8c1b2eb1def634e5683bad0000000017160014e6cfaab9a59ba187f0a45db0b169c21bb48f09b3ffffffff02c0769f0b000000001976a914a80543dc9a417df6cccd36d1c1d85b04a8a4f49f88ac961649000000000017a914bc64b2d79807cd3d72101c3298b89117d32097fb870247304402204dfe8a3b8d22d7ebf762067ea4696b660c6550c92121ee11d582887b4c66e84302200d2945733954ff9f5edc259181f25206fcda79b04f5d453f7f536755dd6bb39d012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba02483045022100d6d1d9fa05f40d215554a0ca15642aca73e4f3edf47f7fc8edc52f80289d9dd40220162a53822d0a6913c22b27ffe60543d7b8ec2ff7943ce6f15a56f874daa33c89012102506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aaba00000000";
    Assert.assertEquals(expectedSignedHex, signResult.getSignedTx());
    Assert.assertEquals("d25fa6a70404e9ad051a2ef12128e02736668736dbab9d427d132e61c551f5a9", signResult.getTxHash());
    Assert.assertEquals("cb875cbaabe98e37f179b813a567350dae47cbd4770c5f499cf32869ca6d070d", signResult.getWtxID());

  }

}
