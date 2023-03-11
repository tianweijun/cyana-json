package cyana.json.visitor;

import cyana.ast.runtime.Ast;
import cyana.ast.runtime.Grammar;
import cyana.json.reflector.ClassReflector;
import cyana.json.reflector.ConstructorReflector;
import cyana.json.reflector.Primitives;
import cyana.json.reflector.impl.ClassReflectorImpl;
import cyana.json.visitor.ContextAst.ArrAst;
import cyana.json.visitor.ContextAst.ValueAst;
import java.lang.reflect.Array;

/**
 * JsonClassReflector.
 *
 * @author tian wei jun
 */
public class JsonClassReflector {
  public Class<?> klass;
  public ClassReflector classReflector;

  public JsonClassReflector(Class<?> klass) {
    this.klass = klass;
    if (!(isArray() || isPrimitive())) {
      classReflector = new ClassReflectorImpl<>(klass);
    }
  }

  public boolean isFinal() {
    return !isArray() && classReflector.isFinal();
  }

  public boolean isPrimitive() {
    return Primitives.isPrimitive(klass);
  }

  public boolean isArray() {
    return klass.isArray();
  }

  public Object newInstanceIfNoPrimitive(ValueAst valueAst) {
    if (isPrimitive() || isFinal()) { // primitive final就不用构造了
      return null;
    }
    if (klass.isArray()) { // array
      int lengthOfArray = getLengthOfArray(valueAst);
      Object arr = Array.newInstance(klass.getComponentType(), lengthOfArray);
      return arr;
    }
    // normal class
    ConstructorReflector constructorReflector = classReflector.constructorReflector();
    return constructorReflector.newInstance();
  }

  private int getLengthOfArray(ValueAst valueAst) {
    int lengthOfArray = 0;
    ContextAst productionRuleAst = (ContextAst) valueAst.children.getFirst();
    String grammarName = productionRuleAst.grammar.getName();
    if (grammarName.equals("arr")) {
      ArrAst arrAst = (ArrAst) productionRuleAst;
      for (Ast eleArrAst : arrAst.children) {
        Grammar eleArrGrammar = eleArrAst.grammar;
        if (eleArrGrammar.getName().equals("value")) {
          ++lengthOfArray;
        }
      }
    }
    return lengthOfArray;
  }
}
