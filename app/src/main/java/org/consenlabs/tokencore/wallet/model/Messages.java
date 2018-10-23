package org.consenlabs.tokencore.wallet.model;

public class Messages {
  public static final String UNKNOWN = "unknown";

  public static final String WALLET_INVALID_PASSWORD = "password_incorrect";
  public static final String PASSWORD_BLANK = "password_blank";
  public static final String PASSWORD_WEAK = "password_weak";

  public static final String MNEMONIC_BAD_WORD = "mnemonic_word_invalid";
  public static final String MNEMONIC_INVALID_LENGTH = "mnemonic_length_invalid";
  public static final String MNEMONIC_CHECKSUM = "mnemonic_checksum_invalid";

  public static final String INVALID_MNEMONIC_PATH = "invalid_mnemonic_path";

  public static final String APPLICATION_NOT_READY = "application_not_ready";

  public static final String WALLET_NOT_FOUND = "wallet_not_found";

  public static final String WALLET_INVALID_KEYSTORE = "keystore_invalid";
  public static final String WALLET_STORE_FAIL = "store_wallet_failed";
  public static final String WALLET_INVALID = "keystore_invalid";
  public static final String KDF_UNSUPPORTED = "kdf_unsupported";

  public static final String WALLET_INVALID_TYPE = "unsupported_chain";
  public static final String WALLET_INVALID_ADDRESS = "address_invalid";

  public static final String INVALID_TRANSACTION_DATA = "transaction_data_invalid";
  public static final String WALLET_EXISTS = "address_already_exist";
  public static final String INVALID_WALLET_VERSION = "keystore_version_invalid";
  public static final String WALLET_HD_NOT_SUPPORT_PRIVATE = "hd_not_support_private";
  public static final String WALLET_SHA256 = "sha256";

  public static final String IPFS_CHECK_SIGNATURE = "check_signature";
  public static final String NOT_UTF8 = "not_utf8";

  public static final String MAC_UNMATCH = "mac_unmatch";
  public static final String PRIVATE_KEY_ADDRESS_NOT_MATCH = "private_key_address_not_match";


  public static final String CAN_NOT_TO_JSON = "can_not_to_json" ;
  public static final String CAN_NOT_FROM_JSON = "can_not_from_json" ;

  public static final String SCRYPT_PARAMS_INVALID = "scrypt_params_invalid";
  public static final String PRF_UNSUPPORTED = "prf_unsupported";
  public static final String KDF_PARAMS_INVALID = "kdf_params_invalid";
  public static final String CIPHER_FAIL = "cipher_unsupported";



  public static final String INVALID_BIG_NUMBER = "big_number_invalid";
  public static final String INVALID_NEGATIVE = "negative_invalid";
  public static final String INVALID_HEX = "hex_invalid";

  public static final String INSUFFICIENT_FUNDS = "insufficient_funds";
  public static final String CAN_NOT_FOUND_PRIVATE_KEY = "can_not_found_private_key";
  public static final String WIF_WRONG_NETWORK = "wif_wrong_network";
  public static final String WIF_INVALID = "wif_invalid";
  public static final String PRIVATE_KEY_INVALID = "privatekey_invalid";
  public static final String CAN_NOT_EXPORT_MNEMONIC = "not_support_export_keystore";

  public static final String INVALID_IDENTITY = "invalid_identity";

  public static final String UNSUPPORT_SEND_TARGET = "not_support_send_target";
  public static final String ILLEGAL_OPERATION = "illegal_operation";
  public static final String UNSUPPORT_ENCRYPTION_DATA_VERSION = "unsupport_encryption_data_version";
  public static final String INVALID_ENCRYPTION_DATA_SIGNATURE = "invalid_encryption_data_signature";

  public static final String ENCRYPT_XPUB_ERROR = "encrypt_xpub_error";

  public static final String SEGWIT_NEEDS_COMPRESS_PUBLIC_KEY = "segwit_needs_compress_public_key";
  public static final String EOS_PRIVATE_PUBLIC_NOT_MATCH = "eos_private_public_not_match";
  public static final String EOS_PUBLIC_KEY_NOT_FOUND = "eos_public_key_not_found";
  public static final String EOS_ACCOUNT_NAME_INVALID = "eos_account_name_invalid";

  public static final String AMOUNT_LESS_THAN_MINIMUM = "amount_less_than_minimum";
  public static final String KEYSTORE_CONTAINS_INVALID_PRIVATE_KEY = "keystore_contains_invalid_private_key";

  public static final String REQUIRED_EOS_WALLET = "required_eos_wallet";



}
