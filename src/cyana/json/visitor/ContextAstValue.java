package cyana.json.visitor;

import cyana.json.reflector.ClassReflector;
import cyana.json.reflector.FieldReflector;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * 类型包含1数组元素 2类成员 3数组 4其他. ContextAstValue.
 *
 * @author tian wei jun
 */
public abstract class ContextAstValue {
  public Type type = Type.REF;
  public JsonClassReflector classOfValue = null;

  public abstract Object getValue();

  public abstract void setValue(Object value);

  public static enum Type {
    REF,
    ARRAY,
    FIELD_OF_OBJ,
    ARRAY_ELEMENT;
  }

  public static class FieldOfObjectContextAstValue extends ContextAstValue {
    public Object ref = null;
    public String fieldName = null;

    // 方便反射调用
    FieldReflector fieldReflector = null;

    public FieldOfObjectContextAstValue(Object ref, String fieldName) {
      type = Type.FIELD_OF_OBJ;
      this.ref = ref;
      this.fieldName = fieldName;
    }

    @Override
    public Object getValue() {
      return fieldReflector.readValue(ref);
    }

    @Override
    public void setValue(Object value) {
      fieldReflector.writeValue(ref, value);
    }

    public void setClassByObjClassReflector(ClassReflector objClassReflector) {
      fieldReflector = objClassReflector.fieldReflector(fieldName);
      Field field = fieldReflector.field();
      Class<?> fieldClass = field.getType();
      classOfValue = new JsonClassReflector(fieldClass);
    }
  }

  public static class RefContextAstValue extends ContextAstValue {
    public Object[] ref = new Object[1];

    public RefContextAstValue() {
      type = Type.REF;
    }

    @Override
    public Object getValue() {
      return ref[0];
    }

    @Override
    public void setValue(Object value) {
      ref[0] = value;
    }
  }

  public static class ArrayElementContextAstValue extends ContextAstValue {
    public Object array = null;
    public int indexOfArray = -1;

    public ArrayElementContextAstValue() {
      type = Type.ARRAY_ELEMENT;
    }

    @Override
    public Object getValue() {
      return Array.get(array, indexOfArray);
    }

    @Override
    public void setValue(Object value) {
      Array.set(array, indexOfArray, value);
    }

    public void set(Object array, int indexOfArray) {
      this.array = array;
      this.indexOfArray = indexOfArray;
    }
  }

  public static class ArrayContextAstValue extends ContextAstValue {
    public Object array = null;

    public ArrayContextAstValue() {
      type = Type.ARRAY;
    }

    @Override
    public Object getValue() {
      return array;
    }

    @Override
    public void setValue(Object value) {
      this.array = value;
    }

    public void set(Object elementValue, int indexOfArray) {
      Array.set(array, indexOfArray, elementValue);
    }

    public void set(Object array) {
      this.array = array;
    }
  }
}
