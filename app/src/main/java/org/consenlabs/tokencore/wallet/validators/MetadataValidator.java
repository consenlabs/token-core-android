package org.consenlabs.tokencore.wallet.validators;

import com.google.common.base.Strings;

import org.consenlabs.tokencore.wallet.model.ChainType;
import org.consenlabs.tokencore.wallet.model.Metadata;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by xyz on 2018/4/10.
 */

public final class MetadataValidator implements Validator<Metadata> {
  private HashMap<String, Object> map;
  private String source;

  public MetadataValidator(HashMap<String, Object> map) {
    this.map = map;
  }

  public MetadataValidator(HashMap<String, Object> map, String source) {
    this.map = map;
    this.source = source;
  }

  @Override
  public Metadata validate() {
    String name = (String) map.get("name");
    String passwordHint = (String) map.get("passwordHint");
    String chainType = (String) map.get("chainType");
    String network = null;
    String segWit = null;
    if (!ChainType.ETHEREUM.equalsIgnoreCase(chainType)) {
      if (map.containsKey("network")) {
        network = ((String) map.get("network")).toUpperCase();
      }
      if (map.containsKey("segWit")) {
        segWit = ((String) map.get("segWit")).toUpperCase();
      }
    }

    checkState(!Strings.isNullOrEmpty(name), "Can't allow empty name");
    ChainType.validate(chainType);

    Metadata metadata = new Metadata(chainType, network, name, passwordHint);
    if (!Strings.isNullOrEmpty(this.source)) {
      metadata.setSource(this.source);
    }
    metadata.setSegWit(segWit);
    return metadata;
  }


}