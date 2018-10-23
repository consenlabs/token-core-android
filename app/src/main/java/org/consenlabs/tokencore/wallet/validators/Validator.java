package org.consenlabs.tokencore.wallet.validators;

/**
 * Created by xyz on 2018/2/27.
 */

public interface Validator<T> {
  T validate();
}
