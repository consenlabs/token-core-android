package org.consenlabs.tokencore.wallet.address;

public interface AddressCreator {
    String fromPrivateKey(String prvKeyHex);
    String fromPrivateKey(byte[] prvKeyBytes);
}
