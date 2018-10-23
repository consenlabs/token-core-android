package org.consenlabs.tokencore.testutils;

import com.google.common.base.Joiner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by xyz on 2018/3/9.
 */

public class ResourcesManager {

  public static JSONObject loadTestJSON(String filename) {
    JSONObject jsonObject = null;
    try {
      String content = readFileContent(filename);
      jsonObject = new JSONObject(content);
    } catch (JSONException ex) {
      ex.printStackTrace();
    }
    return jsonObject;
  }

  public static String readFileContent(String filename) {

    try {
      URL url = ResourcesManager.class.getClassLoader().getResource(filename);
      return Joiner.on("").join(Files.readAllLines(Paths.get(url.getFile())));
    } catch (IOException e) {
      return "";
    }
  }


}
