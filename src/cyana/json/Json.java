package cyana.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Json api.
 *
 * @author tian wei jun
 */
public class Json {

  public static <T> T fromJson(String strOfJson, Class<T> classOfT) {
    InputStream jsonInputStream = new ByteArrayInputStream(strOfJson.getBytes());
    T object = fromJson(jsonInputStream, classOfT);
    if (null != jsonInputStream) {
      try {
        jsonInputStream.close();
      } catch (IOException e) {
        throw new CyanaJsonParseRuntimeException(e);
      }
    }
    return object;
  }

  public static <T> T fromJson(InputStream jsonInputStream, Class<T> classOfT) {
    return JsonParseApplication.fromJson(jsonInputStream, classOfT);
  }

  public static String toJson(Object src) {
    return "";
  }
}
