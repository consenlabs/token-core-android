package org.consenlabs.tokencore.wallet.validators;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.foundation.crypto.Hash;
import org.consenlabs.tokencore.foundation.utils.NumericUtil;
import org.consenlabs.tokencore.wallet.model.Messages;
import org.consenlabs.tokencore.wallet.model.TokenException;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by xyz on 2018/3/12.
 */

public class ETHAddressValidator implements Validator<Void> {
  private String address;

  private static final Pattern ignoreCaseAddrPattern = Pattern.compile("^(0x)?[0-9a-f]{40}$",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern lowerCaseAddrPattern = Pattern.compile("^(0x)?[0-9a-f]{40}$");
  private static final Pattern upperCaseAddrPattern = Pattern.compile("^(0x)?[0-9A-F]{40}$");


  public ETHAddressValidator(String address) {
    this.address = address;
  }

  @Override
  public Void validate() {
    if (!isValidAddress(this.address)) {
      throw new TokenException(Messages.WALLET_INVALID_ADDRESS);
    }
    return null;
  }


  private boolean isValidAddress(String address) {

    // if not [0-9]{40} return false
    if (Strings.isNullOrEmpty(address) || !ignoreCaseAddrPattern.matcher(address).find()) {
      return false;
    } else if (lowerCaseAddrPattern.matcher(address).find() || upperCaseAddrPattern.matcher(address).find()) {
      // if it's all small caps or caps return true
      return true;
    } else {
      // if it is mixed caps it is a checksum address and needs to be validated
      return validateChecksumAddress(address);
    }
  }

  private boolean validateChecksumAddress(String address) {
    address = NumericUtil.cleanHexPrefix(address);
    checkState(address.length() == 40);

    String lowerAddress = NumericUtil.cleanHexPrefix(address).toLowerCase();
    String hash = NumericUtil.bytesToHex(Hash.keccak256(lowerAddress.getBytes()));

    for (int i = 0; i < 40; i++) {
      if (Character.isLetter(address.charAt(i))) {
        // each uppercase letter should correlate with a first bit of 1 in the hash
        // char with the same index, and each lowercase letter with a 0 bit
        int charInt = Integer.parseInt(Character.toString(hash.charAt(i)), 16);
        if ((Character.isUpperCase(address.charAt(i)) && charInt <= 7)
            || (Character.isLowerCase(address.charAt(i)) && charInt > 7)) {
          return false;
        }
      }
    }
    return true;
  }

}
