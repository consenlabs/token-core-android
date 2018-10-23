package org.consenlabs.tokencore.wallet;

import junit.framework.Assert;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.model.BIP44Util;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.Network;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class SegWitWalletTest extends  WalletSupport{

  @Test
  public void p2sh_p2wpkhAddress() {
    DeterministicSeed seed;
    try {
      seed = new DeterministicSeed("inject kidney empty canal shadow pact comfort wife crush horse wife sketch", null, "", 0L);
      DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
      String path = "m/49'/1'/0'";
      DeterministicKey parent = keyChain.getKeyByPath(BIP44Util.generatePath(path), true);
      System.out.println(parent.serializePubB58(MainNetParams.get()));
      for (int i=0; i<3; i++) {
        DeterministicKey mainAddressKey = keyChain.getKeyByPath(BIP44Util.generatePath(path + "/0/" + i), true);
        System.out.println(mainAddressKey.getPrivateKeyAsWiF(TestNet3Params.get()));
        byte[] pubKeyHash = mainAddressKey.getPubKeyHash();
        String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(pubKeyHash));
        System.out.println("redeemScript: " + redeemScript);
        String scriptPub = Integer.toHexString(169) + NumericUtil.bytesToHex(Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript))) + Integer.toHexString(135);
        System.out.println(Address.fromP2SHHash(MainNetParams.get(), Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript))));
      }

//      ECKey key = ECKey.fromPrivate(NumericUtil.hexToBytes("eb696a065ef48a2192da5b28b694f87544b30fae8327c4510137a922f32c6dcf"));

      System.out.println(new SegWitBitcoinAddressCreator(MainNetParams.get()).fromPrivateKey("eb696a065ef48a2192da5b28b694f87544b30fae8327c4510137a922f32c6dcf"));;
    } catch (UnreadableWalletException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void transactionSignTest() {
    String address = "1Fyxts6r24DpEieygQiNnWxUdb18ANa5p7";
    byte[] hash160 = Address.fromBase58(MainNetParams.get(), address).getHash160();
    System.out.println("Public Key: " + NumericUtil.bytesToHex(hash160));

    String privateKey = "eb696a065ef48a2192da5b28b694f87544b30fae8327c4510137a922f32c6dcf";
    ECKey ecKey = ECKey.fromPrivate(NumericUtil.hexToBytes(privateKey), true);
    assertEquals("public key", "03ad1d8e89212f0b92c74d23bb710c00662ad1470198ac48c43f7d6f93a2a26873", ecKey.getPublicKeyAsHex());
    byte[] pubKeyHash = ecKey.getPubKeyHash();
    String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(pubKeyHash));
    assertEquals("redeem script", "0x001479091972186c449eb1ded22b78e40d009bdf0089", redeemScript);
    byte[] redeemScriptBytes = Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript));
    byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(pubKeyHash)));
    String scriptPub = Integer.toHexString(169) + Integer.toHexString(redeemScriptBytes.length) + NumericUtil.bytesToHex(redeemScriptBytes) + Integer.toHexString(135);
    assertEquals("scriptPubKey", "a9144733f37cf4db86fbc2efed2500b4f4e49f31202387", scriptPub);
    byte[] hashPrevouts = Sha256Hash.hashTwice(NumericUtil.hexToBytes("db6b1b20aa0fd7b23880be2ecbd4a98130974cf4748fb66092ac4d3ceb1a547701000000"));
    assertEquals("hash Prevouts", "b0287b4a252ac05af83d2dcef00ba313af78a3e9c329afa216eb3aa2a7b4613a", NumericUtil.bytesToHex(hashPrevouts));
    byte[] hashSequence = Sha256Hash.hashTwice(NumericUtil.hexToBytes("feffffff"));
    assertEquals("hashSequence", "18606b350cd8bf565266bc352f0caddcf01e8fa789dd8a15386327cf8cabe198", NumericUtil.bytesToHex(hashSequence));
    byte[] hashOutputs = Sha256Hash.hashTwice(NumericUtil.hexToBytes("b8b4eb0b000000001976a914a457b684d7f0d539a46a45bbc043f35b59d0d96388ac0008af2f000000001976a914fd270b1ee6abcaea97fea7ad0402e8bd8ad6d77c88ac"));
    assertEquals("hashOutputs", "de984f44532e2173ca0d64314fcefe6d30da6f8cf27bafa706da61df8a226c83", NumericUtil.bytesToHex(hashOutputs));

    UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
    try {
      Utils.uint32ToByteStreamLE(1L, stream);
      stream.write(hashPrevouts);
      stream.write(hashSequence);
      stream.write(NumericUtil.hexToBytes("db6b1b20aa0fd7b23880be2ecbd4a98130974cf4748fb66092ac4d3ceb1a547701000000"));
      stream.write(scriptCode);
      stream.write(NumericUtil.hexToBytes("00ca9a3b00000000"));
      stream.write(NumericUtil.hexToBytes("feffffff"));
      stream.write(hashOutputs);
      Utils.uint32ToByteStreamLE(1170, stream);
      Utils.uint32ToByteStreamLE(1, stream);
      String hashPreimage = NumericUtil.bytesToHex(stream.toByteArray());
      String expectedHashPreimage = "01000000b0287b4a252ac05af83d2dcef00ba313af78a3e9c329afa216eb3aa2a7b4613a18606b350cd8bf565266bc352f0caddcf01e8fa789dd8a15386327cf8cabe198db6b1b20aa0fd7b23880be2ecbd4a98130974cf4748fb66092ac4d3ceb1a5477010000001976a91479091972186c449eb1ded22b78e40d009bdf008988ac00ca9a3b00000000feffffffde984f44532e2173ca0d64314fcefe6d30da6f8cf27bafa706da61df8a226c839204000001000000";
      assertEquals(hashPreimage, expectedHashPreimage);
      byte[] sigHash = Sha256Hash.hashTwice(stream.toByteArray());
      assertEquals("64f3b0f4dd2bb3aa1ce8566d220cc74dda9df97d8490cc81d89d735c92e59fb6", NumericUtil.bytesToHex(sigHash));
      ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(sigHash));
      byte hashType = 0x01;
      System.out.println(NumericUtil.bytesToHex(ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType})));

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testSameMnemonicSwitchOverrideIdentity() {
    Metadata metadata = new Metadata();
    metadata.setSource(Metadata.FROM_MNEMONIC);
    metadata.setChainType(ChainType.BITCOIN);
    metadata.setNetwork(Network.MAINNET);
    metadata.setSegWit(Metadata.P2WPKH);
    Identity identity = Identity.recoverIdentity(SampleKey.MNEMONIC, "xyz", SampleKey.PASSWORD, "", Network.MAINNET, Metadata.P2WPKH);
    metadata.setSegWit(Metadata.NONE);
    Wallet wallet = WalletManager.importWalletFromMnemonic(metadata, SampleKey.MNEMONIC, BIP44Util.BITCOIN_MAINNET_PATH, SampleKey.PASSWORD, true);
    try {
      wallet = WalletManager.switchBTCWalletMode(wallet.getId(), SampleKey.PASSWORD, Metadata.P2WPKH);
      Assert.fail("Should throw an exception");
    } catch (TokenException ex) {
      Assert.assertEquals(Messages.WALLET_EXISTS, ex.getMessage());
    }

  }
}
