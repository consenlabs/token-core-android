package org.consenlabs.tokencore.wallet;

import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.MnemonicUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLOutput;

/**
 * Created by xyz on 2017/12/11.
 */

public class IdentityTest extends WalletSupport {

  @Test(timeout = 3000)
  public void testCreateIdentity() {
    Identity.createIdentity("xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);
    Identity identity = Identity.getCurrentIdentity();


    Assert.assertNotNull("Should has identifier", identity.getIdentifier());
    Assert.assertNotNull("Should has ipfs id", identity.getIpfsId());

    Assert.assertEquals("Should has two wallet", 2, identity.getWallets().size());
    Wallet ethereumWallet = identity.getWallets().get(0);
    Assert.assertEquals("First Wallet should be ethereum wallet", ChainType.ETHEREUM, ethereumWallet.getMetadata().getChainType());
    Wallet bitcoinWallet = identity.getWallets().get(1);
    Assert.assertEquals("Second Wallet should be Bitcoin wallet", ChainType.BITCOIN, bitcoinWallet.getMetadata().getChainType());
    Assert.assertFalse("Bitcoin wallet should be in testnet", bitcoinWallet.getMetadata().isMainNet());

    identity.deleteIdentity(SampleKey.PASSWORD);
  }

  @Test(timeout = 3000)
  public void testRecoverIdentityOnTestnet() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);
    Assert.assertEquals("identifier should equal", "im18MDKM8hcTykvMmhLnov9m2BaFqsdjoA7cwNg", identity.getIdentifier());
    Assert.assertEquals("ipfs id should be equal", "QmSTTidyfa4np9ak9BZP38atuzkCHy4K59oif23f4dNAGU", identity.getIpfsId());
    Wallet ethereumWallet = identity.getWallets().get(0);
    Assert.assertEquals("ethereum wallet address should equal", "6031564e7b2f5cc33737807b2e58daff870b590b", ethereumWallet.getAddress());
    Assert.assertEquals("ethereum wallet private should be equal", "cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(ethereumWallet.decryptMainKey(SampleKey.PASSWORD)));

    Wallet bitcoinWallet = identity.getWallets().get(1);
    Assert.assertEquals("bitcoin wallet address (0/0): ", "mkeNU5nVnozJiaACDELLCsVUc8Wxoh1rQN", bitcoinWallet.getAddress());
    Assert.assertEquals("bitcoin wallet address (0/1): ", "mj78AbVtQ9SWnvbU7pcrueyE1krMmZtoUU", bitcoinWallet.newReceiveAddress(1));
    String expectedXPrv = "tprv8g8UWPRHxaNWXZN3uoaiNpyYyaDr2j5Dvcj1vxLxKcEF653k7xcN9wq9eT73wBM1HzE9hmWJbAPXvDvaMXqGWm81UcVpHnmATfH2JJrfhGg";
    Assert.assertEquals("bitcoin wallet xprv: ", expectedXPrv, new String(bitcoinWallet.decryptMainKey(SampleKey.PASSWORD)));
    String expectedXPub = "GekyMLycBJlFAmob0yEGM8zrEKrBHozAKr66PrMts7k6vSBJ/8DJQW7HViVqWftKhRbPAxZ3MO0281AKvWp4qa+/Q5nqoCi5/THxRLA1wDn8gWqDJjUjaZ7kJaNnreWfUyNGUeDxnN7tHDGdW4nbtA==";
    Assert.assertEquals("bitcoin wallet encxpub: ", expectedXPub, bitcoinWallet.getEncXPub());
    identity.deleteIdentity(SampleKey.PASSWORD);
  }

  @Test(timeout = 3000)
  public void testRecoverIdentityOnMainnet() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.MAINNET, Metadata.NONE);
    Assert.assertEquals("identifier should equal", "im14x5GXsdME4JsrHYe2wvznqRz4cUhx2pA4HPf", identity.getIdentifier());
    Assert.assertEquals("ipfs id should be equal", "QmWqwovhrZBMmo32BzY83ZMEBQaP7YRMqXNmMc8mgrpzs6", identity.getIpfsId());
    Wallet ethereumWallet = identity.getWallets().get(0);
    Assert.assertEquals("ethereum wallet address should equal", "6031564e7b2f5cc33737807b2e58daff870b590b", ethereumWallet.getAddress());
    Assert.assertEquals("ethereum wallet private should be equal", "cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(ethereumWallet.decryptMainKey(SampleKey.PASSWORD)));

    Wallet bitcoinWallet = identity.getWallets().get(1);
    Assert.assertEquals("bitcoin wallet address (0/0): ", "12z6UzsA3tjpaeuvA2Zr9jwx19Azz74D6g", bitcoinWallet.getAddress());
    Assert.assertEquals("bitcoin wallet address (0/1): ", "1962gsZ8PoPUYHneFakkCTrukdFMVQ4i4T", bitcoinWallet.newReceiveAddress(1));
    String expectedXPrv = "xprv9yrdwPSRnvomqFK4u1y5uW2SaXS2Vnr3pAYTjJjbyRZR8p9BwoadRsCxtgUFdAKeRPbwvGRcCSYMV69nNK4N2kadevJ6L5iQVy1SwGKDTHQ";
    Assert.assertEquals("bitcoin wallet xprv: ", expectedXPrv, new String(bitcoinWallet.decryptMainKey(SampleKey.PASSWORD)));
    String expectedXPub = "BdgvWHN/Uh/K526q/+CdpGwEPZ41SvZHHGSgiSqhFesjErdbo6UnJMIoDOHV94qW8fd2KBW18UG3nTzDwS7a5oArqPtv+2aE9+1bNvCdtYoAx3979N3vbX4Xxn/najTABykXrJDjgpoaXxSo/xTktQ==";
    Assert.assertEquals("bitcoin wallet encxpub: ", expectedXPub, bitcoinWallet.getEncXPub());
    identity.deleteIdentity(SampleKey.PASSWORD);
  }


  @Test(timeout = 3000)
  public void testRecoverIdentitySegWitOnTestNet() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.P2WPKH);
    Assert.assertEquals("identifier should equal", "im18MDKM8hcTykvMmhLnov9m2BaFqsdjoA7cwNg", identity.getIdentifier());
    Assert.assertEquals("ipfs id should be equal", "QmSTTidyfa4np9ak9BZP38atuzkCHy4K59oif23f4dNAGU", identity.getIpfsId());
    Wallet ethereumWallet = identity.getWallets().get(0);
    Assert.assertEquals("ethereum wallet address should equal", "6031564e7b2f5cc33737807b2e58daff870b590b", ethereumWallet.getAddress());
    Assert.assertEquals("ethereum wallet private should be equal", "cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(ethereumWallet.decryptMainKey(SampleKey.PASSWORD)));

    Wallet bitcoinWallet = identity.getWallets().get(1);
    Assert.assertEquals("bitcoin wallet address (0/0): ", "2MwN441dq8qudMvtM5eLVwC3u4zfKuGSQAB", bitcoinWallet.getAddress());
    Assert.assertEquals("bitcoin wallet address (0/1): ", "2N54wJxopnWTvBfqgAPVWqXVEdaqoH7Suvf", bitcoinWallet.newReceiveAddress(1));
    String expectedXPrv = "tprv8gFL636ziA5Wy7bA9uX5JhCQqXuoPRdpJx3Ekj8Cj5F2migHxXpn1ZpJJwCbbGhBB9jWick1RmCeSZtZgJvZ93m5nssbyBVbHSTZzofg4qS";
    Assert.assertEquals("bitcoin wallet xprv: ", expectedXPrv, new String(bitcoinWallet.decryptMainKey(SampleKey.PASSWORD)));
    String expectedXPub = "6lm577UNC0reTJjrMYWZm2GBjYqUjKWI1Y8X5hZ3XdSrsvEcjhO/m6bFUu3b21KDWDCe850mbUMi7U0Dx+Bt2ahFczqmgdCA3CnJPz4XUn5aNi/1kQpJSm7x92JJCWA3twQ0BSrg6HV+zYHm7voWKg==";
    Assert.assertEquals("bitcoin wallet encxpub: ", expectedXPub, bitcoinWallet.getEncXPub());
    identity.deleteIdentity(SampleKey.PASSWORD);
  }

  @Test(timeout = 3000)
  public void testRecoverIdentitySegWitOnMainNet() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.MAINNET, Metadata.P2WPKH);
    Assert.assertEquals("identifier should equal", "im14x5GXsdME4JsrHYe2wvznqRz4cUhx2pA4HPf", identity.getIdentifier());
    Assert.assertEquals("ipfs id should be equal", "QmWqwovhrZBMmo32BzY83ZMEBQaP7YRMqXNmMc8mgrpzs6", identity.getIpfsId());
    Wallet ethereumWallet = identity.getWallets().get(0);
    Assert.assertEquals("ethereum wallet address should equal", "6031564e7b2f5cc33737807b2e58daff870b590b", ethereumWallet.getAddress());
    Assert.assertEquals("ethereum wallet private should be equal", "cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(ethereumWallet.decryptMainKey(SampleKey.PASSWORD)));

    Wallet bitcoinWallet = identity.getWallets().get(1);
    Assert.assertEquals("bitcoin wallet address (0/0): ", "3JmreiUEKn8P3SyLYmZ7C1YCd4r2nFy3Dp", bitcoinWallet.getAddress());
    Assert.assertEquals("bitcoin wallet address (0/1): ", "33xJxujVGf4qBmPTnGW9P8wrKCmT7Nwt3t", bitcoinWallet.newReceiveAddress(1));
    String expectedXPrv = "xprv9xpNJWnYLHgctkd85tGpABEHiqsDjHdy63bNVX8XcxQZHLWF7MoJzqUJfvpCtkgHdcTMa6U8zFLALjFoxBv62keiH2uRgVf7tDPhkZJkc27";
    Assert.assertEquals("bitcoin wallet xprv: ", expectedXPrv, new String(bitcoinWallet.decryptMainKey(SampleKey.PASSWORD)));
    String expectedXPub = "CPEZEgxonR02LextSVWxqQmH7zSjfNN44+0KYuTJ4ezARna34lG4YcX7nR5xvSrMhuRv4eI8BG+2h3Zz4523lNPp8Y6pEEtdJHSvTzS/APQYtdpHB3Hye+kQ+D7YuJ7Ps+LxoxFAwpic7a3CS+R+cw==";
    Assert.assertEquals("bitcoin wallet encxpub: ", expectedXPub, bitcoinWallet.getEncXPub());
    identity.deleteIdentity(SampleKey.PASSWORD);
  }

  @Test(timeout = 3000)
  public void testCreateIdentitySegWitOnMainNet() {
    Identity identity = Identity.createIdentity("xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.MAINNET, Metadata.P2WPKH);
//    Assert.assertEquals("identifier should equal", "im14x5GXsdME4JsrHYe2wvznqRz4cUhx2pA4HPf", identity.getIdentifier());
//    Assert.assertEquals("ipfs id should be equal", "QmWqwovhrZBMmo32BzY83ZMEBQaP7YRMqXNmMc8mgrpzs6", identity.getIpfsId());
    Wallet ethereumWallet = identity.getWallets().get(0);
//    Assert.assertEquals("ethereum wallet address should equal", "6031564e7b2f5cc33737807b2e58daff870b590b", ethereumWallet.getAddress());
//    Assert.assertEquals("ethereum wallet private should be equal", "cce64585e3b15a0e4ee601a467e050c9504a0db69a559d7ec416fa25ad3410c2", NumericUtil.bytesToHex(ethereumWallet.decryptMainKey(SampleKey.PASSWORD)));
//
//    Wallet bitcoinWallet = identity.getWallets().get(1);
//    Assert.assertEquals("bitcoin wallet address (0/0): ", "3JmreiUEKn8P3SyLYmZ7C1YCd4r2nFy3Dp", bitcoinWallet.getAddress());
//    Assert.assertEquals("bitcoin wallet address (0/1): ", "33xJxujVGf4qBmPTnGW9P8wrKCmT7Nwt3t", bitcoinWallet.newReceiveAddress(1));
//    String expectedXPrv = "xprv9xpNJWnYLHgctkd85tGpABEHiqsDjHdy63bNVX8XcxQZHLWF7MoJzqUJfvpCtkgHdcTMa6U8zFLALjFoxBv62keiH2uRgVf7tDPhkZJkc27";
//    Assert.assertEquals("bitcoin wallet xprv: ", expectedXPrv, new String(bitcoinWallet.decryptMainKey(SampleKey.PASSWORD)));
//    String expectedXPub = "CPEZEgxonR02LextSVWxqQmH7zSjfNN44+0KYuTJ4ezARna34lG4YcX7nR5xvSrMhuRv4eI8BG+2h3Zz4523lNPp8Y6pEEtdJHSvTzS/APQYtdpHB3Hye+kQ+D7YuJ7Ps+LxoxFAwpic7a3CS+R+cw==";
//    Assert.assertEquals("bitcoin wallet encxpub: ", expectedXPub, bitcoinWallet.getEncXPub());

    String prvKey = ethereumWallet.exportPrivateKey(SampleKey.PASSWORD);
    System.out.println(String.format("PrivateKey: %s", prvKey));
    String mnemonic = ethereumWallet.exportMnemonic(SampleKey.PASSWORD).getMnemonic();
    System.out.println(String.format("Mnemonic: %s", mnemonic));
    String json = ethereumWallet.exportKeystore(SampleKey.PASSWORD);
    System.out.println(String.format("Keystore: %s", json));



    identity.deleteIdentity(SampleKey.PASSWORD);


  }

  @Test
  public void testExportIdentity() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);
    try {
      identity.exportIdentity(SampleKey.WRONG_PASSWORD);
      Assert.fail("Should throw exception when export identity use wrong password");
    } catch (TokenException ex) {
      Assert.assertEquals("Should throw invalid password", Messages.WALLET_INVALID_PASSWORD, ex.getMessage());
    }
    Assert.assertEquals("export identity mnenmonic should be equal", SampleKey.MNEMONIC, identity.exportIdentity(SampleKey.PASSWORD));
    identity.deleteIdentity(SampleKey.PASSWORD);
  }

  @Test
  public void testDeleteIdentity() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);
    try {
      identity.exportIdentity(SampleKey.WRONG_PASSWORD);
      Assert.fail("Should throw exception when delete identity use wrong password");
    } catch (TokenException ex) {
      Assert.assertEquals("Should throw invalid password", Messages.WALLET_INVALID_PASSWORD, ex.getMessage());
    }

    identity.deleteIdentity(SampleKey.PASSWORD);
    Assert.assertNull("current identity should be null", Identity.getCurrentIdentity());
  }


  @Test
  public void testIPFSData() {
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, SampleKey.PASSWORD_HINT, Network.TESTNET, Metadata.NONE);
    // header: data, iv, encrypted data
    String[][] testCase = new String[][]{
        new String[]{"imToken", "11111111111111111111111111111111", "0340b2495a1111111111111111111111111111111110b6602c68084bdd08dae796657aa6854ad13312fedc88f5b6f16c56b3e755dde125a1c4775db536ac0442ac942f9634c777f3ae5ca39f6abcae4bd6c87e54ab29ae0062b04d917b32e8d7c88eeb6261301b"},
        new String[]{"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "11111111111111111111111111111111", "0340b2495a11111111111111111111111111111111708b7e9486a339f6c482ec9d3786dd9f99222fa64753bc2e7d246b0fed9c2153b8a5dcc59ea3e320aa153ceefdd909e8484d215121a9b8416d395de38313ef65b9e27d2ba0cc17bf29c5b26fa5aa5be1a2500b017f06cdd001e8cd908c5a48f10962880a61b4704754fd6bbe3b5a1a8332376651c28205a02574ed95a70363e0d1031d133c8d2376808b74ffd78b831ec659b44e9f3d3734d26abd44dda88fac86d1a5f0128f77d0558fb1ef6d2cc8f9541c"},
        new String[]{"a", "11111111111111111111111111111111", "0340b2495a111111111111111111111111111111111084e741e2b83ec644e844985088fd58d8449cb690cd7389d74e3be1ccdca755b0235c90431b7635a441944d880bd52c860b109b7a05a960192719eb3f294ec1b72f5dfd1b8f4c6e992b9c3add7c7c1b871b"},
        new String[]{"A", "11111111111111111111111111111111", "0340b2495a1111111111111111111111111111111110de32f176b67269ddfe24b2162eae14968d2eafcb53ec5741a07a1d65dc10189e0f6b4c199e98b02fcb9ec744b134cecc4ae8bfbf79e7703781c259eab9ee2fa31f887b24d04b37b7c5aa49a3ff2a8d5e1b"},
        new String[]{"a", "11111111111111111111111111111111", "0340b2495a111111111111111111111111111111111084e741e2b83ec644e844985088fd58d8449cb690cd7389d74e3be1ccdca755b0235c90431b7635a441944d880bd52c860b109b7a05a960192719eb3f294ec1b72f5dfd1b8f4c6e992b9c3add7c7c1b871b"},
        new String[]{"a", "22222222222222222222222222222222", "0340b2495a22222222222222222222222222222222102906146aa78fadd4abac01d9aa34dbd66463220fa0a98b9212594e7624a34bb20ba50df75cb04362f8dcfe7a8c44b2b5740a2d66de015d867e609463482686959ebba6047600562fa82e94ee905f1d291c"},
    };
    long unixTimestamp = 1514779200; // 2018/1/1 12:00:00
    for (String[] test : testCase) {
      Assert.assertEquals(test[2], identity.encryptDataToIPFS(test[0], unixTimestamp, NumericUtil.hexToBytes(test[1])));
      Assert.assertEquals(test[0], identity.decryptDataFromIPFS(test[2]));
    }

  }

}
