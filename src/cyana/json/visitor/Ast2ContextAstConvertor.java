package cyana.json.visitor;

import cyana.ast.runtime.Ast;
import cyana.ast.runtime.Grammar;
import cyana.json.CyanaJsonParseRuntimeException;
import cyana.json.visitor.ContextAst.TerminalAst;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Ast2ContextAstConvertor.
 *
 * @author tian wei jun
 */
public class Ast2ContextAstConvertor {
  Ast source;
  Map<Ast, Ast> sourceDestAstMap;
  LinkedList<Ast> sourceAsts = new LinkedList<>();

  public Ast2ContextAstConvertor(Ast source) {
    this.source = source;
    setSourceAsts(source);
  }

  public Ast convert() {
    mapSourceDestAst();
    cloneAsts();
    return sourceDestAstMap.get(source);
  }

  private void cloneAsts() {
    for (Ast sourceAst : sourceAsts) {
      Ast destAst = sourceDestAstMap.get(sourceAst);
      destAst.token = sourceAst.token;
      if (null != sourceAst.parent) {
        destAst.parent = sourceDestAstMap.get(sourceAst.parent);
      }
      for (Ast childOfSourceAst : sourceAst.children) {
        destAst.children.add(sourceDestAstMap.get(childOfSourceAst));
      }
    }
  }

  private void mapSourceDestAst() {
    sourceDestAstMap = new HashMap<>(sourceAsts.size());
    for (Ast sourceAst : sourceAsts) {
      Ast ast = createContextAstByAst(sourceAst);
      sourceDestAstMap.put(sourceAst, ast);
    }
  }

  private Ast createContextAstByAst(Ast ast) {
    Ast contextAst = null;
    Grammar grammar = ast.grammar;
    switch (grammar.getType()) {
      case TERMINAL_FRAGMENT:
        break;
      case TERMINAL:
        contextAst = new TerminalAst(ast.token);
        break;
      case NONTERMINAL:
        try {
          String className = getContextAstClassName(ast);
          Class contextClass = Class.forName(className);
          Constructor constructor =
              contextClass.getDeclaredConstructor(Grammar.class, String.class);
          contextAst = (Ast) constructor.newInstance(grammar, ast.alias);
        } catch (ClassNotFoundException
            | NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException e) {
          throw new CyanaJsonParseRuntimeException(e);
        }
        break;
      default:
    }
    return contextAst;
  }

  /**
   * json--->JsonAst.
   *
   * @param ast
   * @return
   */
  private String getContextAstClassName(Ast ast) {
    String className = ast.grammar.getName();
    char[] chars = className.toCharArray();
    char fchar = chars[0];
    if (97 <= fchar && fchar <= 122) { // 首字母小写转大写
      fchar ^= 32;
      chars[0] = fchar;
      className = new String(chars);
    }
    className = "cyana.json.visitor.ContextAst$" + className + "Ast";
    return className;
  }

  private void setSourceAsts(Ast ast) {
    sourceAsts.add(ast);
    for (Ast child : ast.children) {
      setSourceAsts(child);
    }
  }
}
