package org.consenlabs.tokencore.wallet.keystore;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.foundation.crypto.Crypto;
import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.KeyPair;
import org.consenlabs.tokencore.wallet.model.Metadata;
import org.consenlabs.tokencore.wallet.model.TokenException;
import org.consenlabs.tokencore.wallet.transaction.EOSKey;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Deprecated
public class LegacyEOSKeystore extends V3Keystore {

    public LegacyEOSKeystore() {
    }

    public static LegacyEOSKeystore create(Metadata metadata, String accountName, String password, String prvKeyHex) {
        return new LegacyEOSKeystore(metadata, accountName, password, prvKeyHex, "");
    }

    public List<KeyPair> exportPrivateKeys(String password) {
        byte[] decrypted = decryptCiphertext(password);
        String wif = new String(decrypted);
        EOSKey key = EOSKey.fromWIF(wif);
        KeyPair keyPair = new KeyPair();
        keyPair.setPrivateKey(wif);
        keyPair.setPublicKey(key.getPublicKeyAsHex());
        return Collections.singletonList(keyPair);
    }


    @Deprecated
    public LegacyEOSKeystore(Metadata metadata, String address, String password, String prvKeyHex, String id) {

        if (!metadata.getChainType().equals(ChainType.EOS)) {
            throw new TokenException("Only init eos keystore in this constructor");
        }
        byte[] prvKeyBytes = prvKeyHex.getBytes();
        this.address = address;
        this.crypto = Crypto.createPBKDF2Crypto(password, prvKeyBytes);
        metadata.setWalletType(Metadata.V3);
        this.metadata = metadata;
        this.version = VERSION;
        this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;
    }
}
