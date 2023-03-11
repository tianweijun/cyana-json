package cyana.json.visitor;

import cyana.ast.runtime.Ast;
import cyana.ast.runtime.Grammar;
import cyana.ast.runtime.Token;
import cyana.json.visitor.ContextAstValue.ArrayContextAstValue;
import cyana.json.visitor.ContextAstValue.RefContextAstValue;

/**
 * ContextAst.
 *
 * @author tian wei jun
 */
public class ContextAst extends Ast {
  ContextAstValue contextAstValue = null;

  public ContextAst(Grammar grammar, String alias) {
    super(grammar, alias);
  }

  public ContextAst(Token token) {
    super(token);
  }

  public void passValueByParent() {
    ContextAst parentContextAst = (ContextAst) parent;
    this.contextAstValue = parentContextAst.contextAstValue;
  }

  /**
   * (ARRAY_ELEMENT, FIELD_OF_OBJ,不可能是最开始的值); array,REF.
   *
   * @param value
   */
  public void initContextAstValue(JsonClassReflector jsonClassReflector, Object value) {
    if (jsonClassReflector.klass.isArray()) {
      ArrayContextAstValue arrayContextAstValue = new ArrayContextAstValue();
      arrayContextAstValue.array = value;
      this.contextAstValue = arrayContextAstValue;
    } else {
      RefContextAstValue refContextAstValue = new RefContextAstValue();
      refContextAstValue.ref[0] = value;
      this.contextAstValue = refContextAstValue;
    }
    this.contextAstValue.classOfValue = jsonClassReflector;
  }

  public void setValue(Object v) {
    contextAstValue.setValue(v);
  }

  public void setArrayElementValueByArrayAst(Object v, int indexOfArray) {
    ArrayContextAstValue arrayContextAstValue = (ArrayContextAstValue) this.contextAstValue;
    arrayContextAstValue.set(v, indexOfArray);
  }

  public static class TerminalAst extends ContextAst {

    public TerminalAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public TerminalAst(Token token) {
      super(token);
    }
  }

  public static class ArrAst extends ContextAst {

    public ArrAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public ArrAst(Token token) {
      super(token);
    }
  }

  public static class JsonAst extends ContextAst {

    public JsonAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public JsonAst(Token token) {
      super(token);
    }
  }

  public static class ObjAst extends ContextAst {

    public ObjAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public ObjAst(Token token) {
      super(token);
    }
  }

  public static class PairAst extends ContextAst {

    public PairAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public PairAst(Token token) {
      super(token);
    }
  }

  public static class ValueAst extends ContextAst {
    public ValueAst(Grammar grammar, String alias) {
      super(grammar, alias);
    }

    public ValueAst(Token token) {
      super(token);
    }
  }
}
