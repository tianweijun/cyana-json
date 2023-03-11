package cyana.json;

/**
 * 自定义运行时异常.
 *
 * @author tian wei jun
 */
public class CyanaJsonParseRuntimeException extends RuntimeException {

  public CyanaJsonParseRuntimeException(Throwable cause) {
    super(cause);
  }

  public CyanaJsonParseRuntimeException(String message) {
    super(message);
  }
}
