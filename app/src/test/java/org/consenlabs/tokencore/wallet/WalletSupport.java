package org.consenlabs.tokencore.wallet;

import org.consenlabs.tokencore.testutils.LocalFileStorage;
import org.consenlabs.tokencore.wallet.validators.PrivateKeyValidator;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class WalletSupport {

  public static final String KEYSTORE_DIR = "/tmp/imtoken/wallets";

  @Before
  public void setUp() {
    try {
      Files.createDirectories(Paths.get(KEYSTORE_DIR));
    } catch (IOException ignored) {
    }
    WalletManager.storage = new LocalFileStorage();
    WalletManager.scanWallets();

    Identity identity = mock(Identity.class);
    doNothing().when(identity).addWallet(isA(Wallet.class));
    Identity.currentIdentity = identity;
  }

  @After
  public void tearDown() {
    File dir = new File(KEYSTORE_DIR);
    String[] children = dir.list();
    if (children == null) return;
    for (String aChildren : children) {
      (new File(dir, aChildren)).delete();
    }
    WalletManager.clearKeystoreMap();
  }
}
