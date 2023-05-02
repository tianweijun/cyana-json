package cyana.json;

import cyana.ast.runtime.Ast;
import cyana.ast.runtime.RuntimeAutomataAstApplication;
import cyana.json.reflector.Primitives;
import cyana.json.visitor.Ast2ContextAstConvertor;
import cyana.json.visitor.JsonVisitor;
import java.io.IOException;
import java.io.InputStream;

/**
 * JsonParseApplication.
 *
 * @author tian wei jun
 */
public class JsonParseApplication {

  private static RuntimeAutomataAstApplication runtimeAstApplication = null;

  /**
   * 序列化数据转为json实体.
   *
   * @param jsonByteInputStream 输入流
   * @return entity
   */
  public static <T> T fromJson(InputStream jsonByteInputStream, Class<T> classOfT) {
    construct();
    Ast ast = runtimeAstApplication.buildAst(jsonByteInputStream);
    if (null != jsonByteInputStream) {
      try {
        jsonByteInputStream.close();
      } catch (IOException e) {
        throw new CyanaJsonParseRuntimeException(e);
      }
    }
    Ast contextAst = new Ast2ContextAstConvertor(ast).convert();
    Object object = new JsonVisitor(contextAst, classOfT).parseObject();
    return Primitives.wrap(classOfT).cast(object);
  }

  private static void construct() {
    if (null == runtimeAstApplication) {
      InputStream jsonAutomataInputStream =
          JsonParseApplication.class.getClassLoader()
              .getResourceAsStream("resources/automata.data");
      runtimeAstApplication = new RuntimeAutomataAstApplication();
      runtimeAstApplication.setContext(jsonAutomataInputStream);
      if (null != jsonAutomataInputStream) {
        try {
          jsonAutomataInputStream.close();
        } catch (IOException e) {
          throw new CyanaJsonParseRuntimeException(e);
        }
      }
    }
  }

  public static void destruct() {
    runtimeAstApplication = null;//helper gc
  }
}
