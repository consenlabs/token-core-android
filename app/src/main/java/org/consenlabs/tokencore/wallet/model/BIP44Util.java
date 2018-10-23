package org.consenlabs.tokencore.wallet.model;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.ChildNumber;

import java.util.ArrayList;
import java.util.List;

public class BIP44Util {
  public final static String BITCOIN_MAINNET_PATH = "m/44'/0'/0'";
  public final static String BITCOIN_TESTNET_PATH = "m/44'/1'/0'";
  public final static String BITCOIN_SEGWIT_MAIN_PATH = "m/49'/0'/0'";
  public final static String BITCOIN_SEGWIT_TESTNET_PATH = "m/49'/1'/0'";
  public final static String ETHEREUM_PATH = "m/44'/60'/0'/0/0";
  public final static String EOS_PATH = "m/44'/194'";
  public final static String EOS_SLIP48 = "m/48'/4'/0'/0'/0',m/48'/4'/1'/0'/0'";
  public final static String EOS_LEDGER = "m/44'/194'/0'/0/0";


  public static ImmutableList<ChildNumber> generatePath(String path) {
    List<ChildNumber> list = new ArrayList<>();
    for (String p : path.split("/")) {
      if ("m".equalsIgnoreCase(p) || "".equals(p.trim())) {
        continue;
      } else if (p.charAt(p.length() - 1) == '\'') {
        list.add(new ChildNumber(Integer.parseInt(p.substring(0, p.length() - 1)), true));
      } else {
        list.add(new ChildNumber(Integer.parseInt(p), false));
      }
    }

    ImmutableList.Builder<ChildNumber> builder = ImmutableList.builder();
    return builder.addAll(list).build();
  }

  public static String getBTCMnemonicPath(String segWit, boolean isMainnet) {
    if (Metadata.P2WPKH.equalsIgnoreCase(segWit)) {
      return isMainnet ? BITCOIN_SEGWIT_MAIN_PATH : BITCOIN_SEGWIT_TESTNET_PATH;
    } else {
      return isMainnet ? BITCOIN_MAINNET_PATH : BITCOIN_TESTNET_PATH;
    }
  }

}
