package org.consenlabs.tokencore.testutils;

import org.consenlabs.tokencore.wallet.KeystoreStorage;

import java.io.File;

/**
 * Created by xyz on 2018/4/8.
 */

public class LocalFileStorage implements KeystoreStorage {
  @Override
  public File getKeystoreDir() {
    return new File("/tmp/imtoken");
  }
}
