package cyana.json.visitor;

import cyana.ast.runtime.Ast;
import cyana.ast.runtime.Grammar;
import cyana.json.CyanaJsonParseRuntimeException;
import cyana.json.reflector.ClassReflector;
import cyana.json.visitor.ContextAst.ArrAst;
import cyana.json.visitor.ContextAst.JsonAst;
import cyana.json.visitor.ContextAst.ObjAst;
import cyana.json.visitor.ContextAst.PairAst;
import cyana.json.visitor.ContextAst.ValueAst;
import cyana.json.visitor.ContextAstValue.ArrayContextAstValue;
import cyana.json.visitor.ContextAstValue.ArrayElementContextAstValue;
import cyana.json.visitor.ContextAstValue.FieldOfObjectContextAstValue;
import cyana.json.visitor.ContextAstValue.RefContextAstValue;

/**
 * JsonVisitor.
 *
 * @author tian wei jun
 */
public class JsonVisitor implements AstVisitor {
  private Ast sourceAst;
  private Class classOfT;

  public JsonVisitor(Ast contextAst, Class classOfT) {
    this.sourceAst = contextAst;
    this.classOfT = classOfT;
  }

  public Object parseObject() {
    JsonAst jsonAst = (JsonAst) sourceAst;
    visitJson(jsonAst);
    return jsonAst.contextAstValue.getValue();
  }

  @Override
  public void visitJson(JsonAst jsonAst) {
    JsonClassReflector jsonClassReflector = new JsonClassReflector(classOfT);
    ValueAst valueAst = (ValueAst) jsonAst.children.getFirst();

    // create root value 引用
    Object value = jsonClassReflector.newInstanceIfNoPrimitive(valueAst);
    jsonAst.initContextAstValue(jsonClassReflector, value);
    // 传递引用（父子等价）
    valueAst.passValueByParent();
    visitValue(valueAst);
  }

  @Override
  public void visitValue(ValueAst valueAst) {
    ContextAst productionRuleAst = (ContextAst) valueAst.children.getFirst();
    productionRuleAst.passValueByParent();

    String grammarName = productionRuleAst.grammar.getName();
    switch (grammarName) {
      case "Null":
        productionRuleAst.setValue(null);
        break;
      case "False":
        productionRuleAst.setValue(false);
        break;
      case "True":
        productionRuleAst.setValue(true);
        break;
      case "NUMBER":
        Class klass = valueAst.contextAstValue.classOfValue.klass;
        String textOfNumber = productionRuleAst.token.text;
        productionRuleAst.setValue(getNumber(klass, textOfNumber));
        break;
      case "STRING":
        String textOfString = productionRuleAst.token.text;
        textOfString = textOfString.substring(1, textOfString.length() - 1); // "xxx"--->xxx
        productionRuleAst.setValue(textOfString);
        break;
      case "obj": // （引用）【json.value、pair.String ':' value、arr.'[' value ( ',' value)* ']'、】
        ObjAst objAst = (ObjAst) productionRuleAst;
        visitObj(objAst);
        break;
      case "arr": // （引用）【json.value、pair.String ':' value、arr.'[' value ( ',' value)* ']'、】
        ArrAst arrAst = (ArrAst) productionRuleAst;
        visitArr(arrAst);
        break;
      default:
    }
  }

  @Override
  public void visitArr(ArrAst arrAst) {
    JsonClassReflector classOfArr = arrAst.contextAstValue.classOfValue;
    Class<?> nakedArrClass = classOfArr.klass;
    if (!nakedArrClass.isArray()) {
      throw new CyanaJsonParseRuntimeException("text is a array,but entity isn't.");
    }
    Class<?> componentType = nakedArrClass.getComponentType();
    JsonClassReflector eleArrClassReflector = new JsonClassReflector(componentType);
    int indexOfArray = 0;
    ArrayContextAstValue arrayContextAstValue = (ArrayContextAstValue) arrAst.contextAstValue;
    for (Ast eleArrAst : arrAst.children) {
      Grammar eleArrGrammar = eleArrAst.grammar;
      if (eleArrGrammar.getName().equals("value")) {
        ValueAst valueEleArrAst = (ValueAst) eleArrAst;
        // 新建引用
        Object vOfEleArr = eleArrClassReflector.newInstanceIfNoPrimitive(valueEleArrAst);
        // 设置引用arr[indexOfArray]=v
        if (!eleArrClassReflector.isPrimitive()) {
          arrAst.setArrayElementValueByArrayAst(vOfEleArr, indexOfArray); // 设置当前元素ele被包含的数组arr的值
        }
        // 传引用
        if (componentType.isArray()) { // 转引用为arr
          ArrayContextAstValue eleArrayContextAstValue = new ArrayContextAstValue();
          eleArrayContextAstValue.set(vOfEleArr);
          valueEleArrAst.contextAstValue = eleArrayContextAstValue;
        } else if (eleArrClassReflector.isPrimitive() || eleArrClassReflector.isFinal()) {
          // 传引用arr[index](int,String,Double,...)
          ArrayElementContextAstValue arrayElementContextAstValue =
              new ArrayElementContextAstValue();
          arrayElementContextAstValue.set(arrayContextAstValue.array, indexOfArray);
          valueEleArrAst.contextAstValue = arrayElementContextAstValue;
        } else { // 转引用为ref(object...)
          RefContextAstValue refContextAstValue = new RefContextAstValue();
          refContextAstValue.setValue(vOfEleArr);
          valueEleArrAst.contextAstValue = refContextAstValue;
        }
        valueEleArrAst.contextAstValue.classOfValue = eleArrClassReflector;
        // 递归遍历数组元素
        visitValue(valueEleArrAst);
        indexOfArray++;
      }
    }
  }

  @Override
  public void visitObj(ObjAst objAst) {
    ClassReflector objClassReflector = objAst.contextAstValue.classOfValue.classReflector;
    Object obj = objAst.contextAstValue.getValue();
    for (Ast objEleAst : objAst.children) {
      Grammar eleGrammar = objEleAst.grammar;
      if (eleGrammar.getName().equals("pair")) {
        PairAst pair = (PairAst) objEleAst;
        // 传引用(field)
        String fieldName = pair.children.getFirst().token.text;
        fieldName = formatFieldName(fieldName);
        FieldOfObjectContextAstValue fieldOfObjectContextAstValue =
            new FieldOfObjectContextAstValue(obj, fieldName);
        fieldOfObjectContextAstValue.setClassByObjClassReflector(objClassReflector);
        pair.contextAstValue = fieldOfObjectContextAstValue;
        // 递归遍历语法树
        visitPair(pair);
      }
    }
  }

  @Override
  public void visitPair(PairAst pairAst) {
    FieldOfObjectContextAstValue fieldOfObjectContextAstValue =
        (FieldOfObjectContextAstValue) pairAst.contextAstValue;
    JsonClassReflector fieldJsonClassReflector = fieldOfObjectContextAstValue.classOfValue;
    // 创建引用
    ValueAst pairValueAst = (ValueAst) pairAst.children.getLast();
    Object pairValue = fieldJsonClassReflector.newInstanceIfNoPrimitive(pairValueAst);
    // 设置值引用
    if (!fieldJsonClassReflector.isPrimitive()) {
      fieldOfObjectContextAstValue.setValue(pairValue);
    }
    // 传引用
    if (fieldJsonClassReflector.isArray()) { // 转引用arr
      ArrayContextAstValue arrayContextAstValue = new ArrayContextAstValue();
      arrayContextAstValue.set(pairValue);
      pairValueAst.contextAstValue = arrayContextAstValue;
    } else if (fieldJsonClassReflector.isPrimitive() || fieldJsonClassReflector.isFinal()) {
      // 传引用fieldOfObject(int,String,Double,...)
      pairValueAst.contextAstValue = fieldOfObjectContextAstValue;
    } else { // 转引用为ref(object...)
      RefContextAstValue refContextAstValue = new RefContextAstValue();
      refContextAstValue.setValue(pairValue);
      pairValueAst.contextAstValue = refContextAstValue;
    }
    pairValueAst.contextAstValue.classOfValue = fieldJsonClassReflector;
    // 递归遍历语法树
    visitValue(pairValueAst);
  }

  private Object getNumber(Class klass, String text) {
    if (klass == Integer.class || klass == int.class) {
      return Integer.valueOf(text);
    }
    if (klass == Double.class || klass == double.class) {
      return Double.valueOf(text);
    }
    if (klass == Float.class || klass == float.class) {
      return Float.valueOf(text);
    }
    if (klass == Byte.class || klass == byte.class) {
      return Byte.valueOf(text);
    }
    if (klass == Short.class || klass == short.class) {
      return Short.valueOf(text);
    }
    if (klass == Long.class || klass == long.class) {
      return Long.valueOf(text);
    }
    throw new CyanaJsonParseRuntimeException(
        String.format("%s is not class of number.", klass.toString()));
  }

  private String formatFieldName(String fieldName) {
    int lengthOfFieldName = fieldName.length();
    if (lengthOfFieldName <= 2) {
      return fieldName;
    }
    if (fieldName.charAt(0) == '"' && fieldName.charAt(lengthOfFieldName - 1) == '"') {
      return fieldName.substring(1, lengthOfFieldName - 1);
    }
    return null;
  }
}
