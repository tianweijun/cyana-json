package cyana.json.test.config;

import cyana.json.Json;
import cyana.json.test.school.School;
import java.io.IOException;
import java.io.InputStream;

/**
 * CyanaLanguageConfigTest.
 *
 * @author tian wei jun
 */
public class CyanaLanguageConfigTest {

  public static void main(String[] args) {

    InputStream jsonInputStream =
        CyanaLanguageConfigTest.class
            .getClassLoader()
            .getResourceAsStream("cyana/json/test/config/cyanaLanguageConfig.json");

    CyanaLanguageConfig cyanaLanguageConfig = Json.fromJson(jsonInputStream, CyanaLanguageConfig.class);

    if (null != jsonInputStream) {
      try {
        jsonInputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    System.out.println("CyanaLanguageConfigTest success.");
  }
}
