package org.consenlabs.tokencore.wallet.transaction;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.ByteUtil;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.Wallet;
import org.consenlabs.tokencore.wallet.address.SegWitBitcoinAddressCreator;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitcoinTransaction implements TransactionSigner {
  private String to;
  private long amount;
  private List<UTXO> outputs;
  private String memo;
  private long fee;
  private int changeIdx;
  private long locktime = 0;

  private Address changeAddress;
  private NetworkParameters network;
  private List<BigInteger> prvKeys;

  // 2730 sat
  private final static long DUST_THRESHOLD = 2730;


  public BitcoinTransaction(String to, int changeIdx, long amount, long fee, ArrayList<UTXO> outputs) {
    this.to = to;
    this.amount = amount;
    this.fee = fee;
    this.outputs = outputs;
    this.changeIdx = changeIdx;

    if (amount < DUST_THRESHOLD) {
      throw new TokenException(Messages.AMOUNT_LESS_THAN_MINIMUM);
    }
  }

  @Override
  public String toString() {
    return "BitcoinTransaction{" +
        "to='" + to + '\'' +
        ", amount=" + amount +
        ", outputs=" + outputs +
        ", memo='" + memo + '\'' +
        ", fee=" + fee +
        ", changeIdx=" + changeIdx +
        '}';
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public List<UTXO> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<UTXO> outputs) {
    this.outputs = outputs;
  }

  public String getMemo() {
    return memo;
  }

  public void setMemo(String memo) {
    this.memo = memo;
  }

  public long getFee() {
    return fee;
  }

  public void setFee(long fee) {
    this.fee = fee;
  }

  public int getChangeIdx() {
    return changeIdx;
  }

  public void setChangeIdx(int changeIdx) {
    this.changeIdx = changeIdx;
  }


  public static class UTXO {
    private String txHash;
    private int vout;
    private long amount;
    private String address;
    private String scriptPubKey;
    private String derivedPath;
    private long sequence = 4294967295L;

    @Override
    public String toString() {
      return "UTXO{" +
          "txHash='" + txHash + '\'' +
          ", vout=" + vout +
          ", amount=" + amount +
          ", address='" + address + '\'' +
          ", scriptPubKey='" + scriptPubKey + '\'' +
          ", derivedPath='" + derivedPath + '\'' +
          ", sequence=" + sequence +
          '}';
    }

    public UTXO(String txHash, int vout, long amount, String address, String scriptPubKey, String derivedPath) {
      this.txHash = txHash;
      this.vout = vout;
      this.amount = amount;
      this.address = address;
      this.scriptPubKey = scriptPubKey;
      this.derivedPath = derivedPath;
    }

    public UTXO(String txHash, int vout, long amount, String address, String scriptPubKey, String derivedPath, long sequence) {
      this.txHash = txHash;
      this.vout = vout;
      this.amount = amount;
      this.address = address;
      this.scriptPubKey = scriptPubKey;
      this.derivedPath = derivedPath;
      this.sequence = sequence;
    }

    public int getVout() {
      return vout;
    }

    public void setVout(int vout) {
      this.vout = vout;
    }

    public long getAmount() {
      return amount;
    }

    public void setAmount(long amount) {
      this.amount = amount;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public String getTxHash() {
      return txHash;
    }

    public void setTxHash(String txHash) {
      this.txHash = txHash;
    }

    public String getScriptPubKey() {
      return scriptPubKey;
    }

    public void setScriptPubKey(String scriptPubKey) {
      this.scriptPubKey = scriptPubKey;
    }

    public String getDerivedPath() {
      return derivedPath;
    }

    public void setDerivedPath(String derivedPath) {
      this.derivedPath = derivedPath;
    }

    public long getSequence() {
      return sequence;
    }

    public void setSequence(long sequence) {
      this.sequence = sequence;
    }
  }


  public TxSignResult signTransaction(String chainID, String password, Wallet wallet) {
      collectPrvKeysAndAddress(Metadata.NONE, password, wallet);

    Transaction tran = new Transaction(network);
    long totalAmount = 0L;

    for (UTXO output : getOutputs()) {
      totalAmount += output.getAmount();
    }

    if (totalAmount < getAmount()) {
      throw new TokenException(Messages.INSUFFICIENT_FUNDS);
    }

    //add send to output
    tran.addOutput(Coin.valueOf(getAmount()), Address.fromBase58(network, getTo()));

    //add change output
    long changeAmount = totalAmount - (getAmount() + getFee());
    if (changeAmount >= DUST_THRESHOLD) {
      tran.addOutput(Coin.valueOf(changeAmount), changeAddress);
    }

    for (UTXO output : getOutputs()) {
      tran.addInput(Sha256Hash.wrap(output.getTxHash()), output.getVout(), new Script(NumericUtil.hexToBytes(output.getScriptPubKey())));
    }

    for (int i = 0; i < getOutputs().size(); i++) {
      UTXO output = getOutputs().get(i);

      BigInteger privateKey = wallet.getMetadata().getSource().equals(Metadata.FROM_WIF) ? prvKeys.get(0) : prvKeys.get(i);
      ECKey ecKey;
      if (output.getAddress().equals(ECKey.fromPrivate(privateKey).toAddress(network).toBase58())) {
        ecKey = ECKey.fromPrivate(privateKey);
      } else if (output.getAddress().equals(ECKey.fromPrivate(privateKey, false).toAddress(network).toBase58())) {
        ecKey = ECKey.fromPrivate(privateKey, false);
      } else {
        throw new TokenException(Messages.CAN_NOT_FOUND_PRIVATE_KEY);
      }

      TransactionInput transactionInput = tran.getInput(i);
      Script scriptPubKey = ScriptBuilder.createOutputScript(Address.fromBase58(network, output.getAddress()));
      Sha256Hash hash = tran.hashForSignature(i, scriptPubKey, Transaction.SigHash.ALL, false);
      ECKey.ECDSASignature ecSig = ecKey.sign(hash);
      TransactionSignature txSig = new TransactionSignature(ecSig, Transaction.SigHash.ALL, false);
      if (scriptPubKey.isSentToRawPubKey()) {
        transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig));
      } else {
        if (!scriptPubKey.isSentToAddress()) {
          throw new TokenException(Messages.UNSUPPORT_SEND_TARGET);
        }
        transactionInput.setScriptSig(ScriptBuilder.createInputScript(txSig, ecKey));
      }
    }

    String signedHex = NumericUtil.bytesToHex(tran.bitcoinSerialize());
    String txHash = NumericUtil.beBigEndianHex(Hash.sha256(Hash.sha256(signedHex)));
    return new TxSignResult(signedHex, txHash);
  }

  public TxSignResult signSegWitTransaction(String chainId, String password, Wallet wallet) {
    collectPrvKeysAndAddress(Metadata.P2WPKH, password, wallet);

    long totalAmount = 0L;
    boolean hasChange = false;

    for (UTXO output : getOutputs()) {
      totalAmount += output.getAmount();
    }

    if (totalAmount < getAmount()) {
      throw new TokenException(Messages.INSUFFICIENT_FUNDS);
    }

    long changeAmount = totalAmount - (getAmount() + getFee());
    Address toAddress = Address.fromBase58(network, to);
    byte[] targetScriptPubKey;
    if (toAddress.isP2SHAddress()) {
      targetScriptPubKey = ScriptBuilder.createP2SHOutputScript(toAddress.getHash160()).getProgram();
    } else {
      targetScriptPubKey = ScriptBuilder.createOutputScript(toAddress).getProgram();
    }

    byte[] changeScriptPubKey = ScriptBuilder.createP2SHOutputScript(changeAddress.getHash160()).getProgram();

    byte[] hashPrevouts;
    byte[] hashOutputs;
    byte[] hashSequence;

    try {
      // calc hash prevouts
      UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
      for (UTXO utxo : getOutputs()) {
        TransactionOutPoint outPoint = new TransactionOutPoint(this.network, utxo.vout, Sha256Hash.wrap(utxo.txHash));
        outPoint.bitcoinSerialize(stream);
      }
      hashPrevouts = Sha256Hash.hashTwice(stream.toByteArray());

      // calc hash outputs
      stream = new UnsafeByteArrayOutputStream();

      TransactionOutput targetOutput = new TransactionOutput(this.network, null, Coin.valueOf(amount), toAddress);
      targetOutput.bitcoinSerialize(stream);

      if (changeAmount >= DUST_THRESHOLD) {
        hasChange = true;
        TransactionOutput changeOutput = new TransactionOutput(this.network, null, Coin.valueOf(changeAmount), changeAddress);
        changeOutput.bitcoinSerialize(stream);
      }

//
//      Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
//      stream.write(new VarInt(targetScriptPubKey.length).encode());
//      stream.write(targetScriptPubKey);
//      Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
//      stream.write(new VarInt(changeScriptPubKey.length).encode());
//      stream.write(changeScriptPubKey);

      hashOutputs = Sha256Hash.hashTwice(stream.toByteArray());

      // calc hash sequence
      stream = new UnsafeByteArrayOutputStream();

      for (UTXO utxo : getOutputs()) {
        Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
      }
      hashSequence = Sha256Hash.hashTwice(stream.toByteArray());

      // calc witnesses and redemScripts
      List<byte[]> witnesses = new ArrayList<>();
      List<String> redeemScripts = new ArrayList<>();
      for (int i = 0; i < getOutputs().size(); i++) {
        UTXO utxo = getOutputs().get(i);
        BigInteger prvKey = Metadata.FROM_WIF.equals(wallet.getMetadata().getSource()) ? prvKeys.get(0) : prvKeys.get(i);
        ECKey key = ECKey.fromPrivate(prvKey, true);
        String redeemScript = String.format("0014%s", NumericUtil.bytesToHex(key.getPubKeyHash()));
        redeemScripts.add(redeemScript);

        // calc outpoint
        stream = new UnsafeByteArrayOutputStream();
        TransactionOutPoint txOutPoint = new TransactionOutPoint(this.network, utxo.vout, Sha256Hash.wrap(utxo.txHash));
        txOutPoint.bitcoinSerialize(stream);
        byte[] outpoint = stream.toByteArray();

        // calc scriptCode
        byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(key.getPubKeyHash())));

        // before sign
        stream = new UnsafeByteArrayOutputStream();
        Utils.uint32ToByteStreamLE(2L, stream);
        stream.write(hashPrevouts);
        stream.write(hashSequence);
        stream.write(outpoint);
        stream.write(scriptCode);
        Utils.uint64ToByteStreamLE(BigInteger.valueOf(utxo.getAmount()), stream);
        Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
        stream.write(hashOutputs);
        Utils.uint32ToByteStreamLE(locktime, stream);
        // hashType 1 = all
        Utils.uint32ToByteStreamLE(1L, stream);

        byte[] hashPreimage = stream.toByteArray();
        byte[] sigHash = Sha256Hash.hashTwice(hashPreimage);
        ECKey.ECDSASignature signature = key.sign(Sha256Hash.wrap(sigHash));
        byte hashType = 0x01;
        // witnesses
        byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
        witnesses.add(sig);
      }


      // the second stream is used to calc the traditional txhash
      UnsafeByteArrayOutputStream[] serialStreams = new UnsafeByteArrayOutputStream[]{
          new UnsafeByteArrayOutputStream(), new UnsafeByteArrayOutputStream()
      };
      for (int idx = 0; idx < 2; idx++) {
        stream = serialStreams[idx];
        Utils.uint32ToByteStreamLE(2L, stream); // version
        if (idx == 0) {
          stream.write(0x00); // maker
          stream.write(0x01); // flag
        }
        // inputs
        stream.write(new VarInt(getOutputs().size()).encode());
        for (int i = 0; i < getOutputs().size(); i++) {
          UTXO utxo = getOutputs().get(i);
          stream.write(NumericUtil.reverseBytes(NumericUtil.hexToBytes(utxo.txHash)));
          Utils.uint32ToByteStreamLE(utxo.getVout(), stream);

          // the length of byte array that follows, and this length is used by OP_PUSHDATA1
          stream.write(0x17);
          // the length of byte array that follows, and this length is used by cutting array
          stream.write(0x16);
          stream.write(NumericUtil.hexToBytes(redeemScripts.get(i)));
          Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
        }

        // outputs
        // outputs size
        int outputSize = hasChange ? 2 : 1;
        stream.write(new VarInt(outputSize).encode());
        Utils.uint64ToByteStreamLE(BigInteger.valueOf(amount), stream);
        stream.write(new VarInt(targetScriptPubKey.length).encode());
        stream.write(targetScriptPubKey);
        if (hasChange) {
          Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
          stream.write(new VarInt(changeScriptPubKey.length).encode());
          stream.write(changeScriptPubKey);
        }

        // the first stream is used to calc the segwit hash
        if (idx == 0) {
          for (int i = 0; i < witnesses.size(); i++) {
            BigInteger prvKey = Metadata.FROM_WIF.equals(wallet.getMetadata().getSource()) ? prvKeys.get(0) : prvKeys.get(i);

            ECKey ecKey = ECKey.fromPrivate(prvKey);
            byte[] wit = witnesses.get(i);
            stream.write(new VarInt(2).encode());
            stream.write(new VarInt(wit.length).encode());
            stream.write(wit);
            stream.write(new VarInt(ecKey.getPubKey().length).encode());
            stream.write(ecKey.getPubKey());
          }
        }

        Utils.uint32ToByteStreamLE(locktime, stream);
      }
      byte[] signed = serialStreams[0].toByteArray();
      String signedHex = NumericUtil.bytesToHex(signed);
      String wtxID = NumericUtil.bytesToHex(Sha256Hash.hashTwice(signed));
      wtxID = NumericUtil.beBigEndianHex(wtxID);
      String txHash = NumericUtil.bytesToHex(Sha256Hash.hashTwice(serialStreams[1].toByteArray()));
      txHash = NumericUtil.beBigEndianHex(txHash);
      return new TxSignResult(signedHex, txHash, wtxID);
    } catch (IOException ex) {
      throw new TokenException("OutputStream error");
    }
  }


  private void collectPrvKeysAndAddress(String segWit, String password, Wallet wallet) {
    this.network = wallet.getMetadata().isMainNet() ? MainNetParams.get() : TestNet3Params.get();
    if (wallet.getMetadata().getSource().equals(Metadata.FROM_WIF)) {
      changeAddress = Address.fromBase58(network, wallet.getAddress());
      BigInteger prvKey = DumpedPrivateKey.fromBase58(network, wallet.exportPrivateKey(password)).getKey().getPrivKey();
      prvKeys = Collections.singletonList(prvKey);
    } else {
      prvKeys = new ArrayList<>(getOutputs().size());
      String xprv = new String(wallet.decryptMainKey(password), Charset.forName("UTF-8"));
      DeterministicKey xprvKey = DeterministicKey.deserializeB58(xprv, network);
      DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(xprvKey, ChildNumber.ONE);
      DeterministicKey indexKey = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(getChangeIdx(), false));
      if (Metadata.P2WPKH.equals(segWit)) {
        changeAddress = new SegWitBitcoinAddressCreator(network).fromPrivateKey(indexKey);
      } else {
        changeAddress = indexKey.toAddress(network);
      }

      for (UTXO output : getOutputs()) {
        String derivedPath = output.getDerivedPath().trim();
        String[] pathIdxs = derivedPath.replace('/', ' ').split(" ");
        int accountIdx = Integer.parseInt(pathIdxs[0]);
        int changeIdx = Integer.parseInt(pathIdxs[1]);

        DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(xprvKey, new ChildNumber(accountIdx, false));
        DeterministicKey externalChangeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(changeIdx, false));
        prvKeys.add(externalChangeKey.getPrivKey());
      }
    }
  }


}
