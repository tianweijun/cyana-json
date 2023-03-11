package cyana.json.test;

import cyana.json.Json;
import java.io.IOException;
import java.io.InputStream;

/**
 * JsonApplicationTest.
 *
 * @author tian wei jun
 */
public class JsonApplicationTest {

  public static void main(String[] args) {

    // int.class
    int number = Json.fromJson("32", int.class);

    InputStream jsonInputStream =
        JsonApplicationTest.class
            .getClassLoader()
            .getResourceAsStream("cyana/json/test/school.json");

    School school = Json.fromJson(jsonInputStream, School.class);

    if (null != jsonInputStream) {
      try {
        jsonInputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    System.out.println("JsonApplicationTest success.");
  }
}
