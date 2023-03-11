package cyana.json.reflector;

import cyana.json.reflector.util.TypeParameterResolver.GenericArrayTypeImpl;
import cyana.json.reflector.util.TypeParameterResolver.ParameterizedTypeImpl;
import cyana.json.reflector.util.TypeParameterResolver.WildcardTypeImpl;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Field反射器，Field反射相关操作的核心接口。
 *
 * @param <F> 字段所对应的类型。
 * @author: twj
 */
public interface FieldReflector<F> extends Reflector {

  /**
   * 读取目标对象字段的值。
   *
   * @param target 目标对象。
   * @return 目标对象字段的值。
   */
  F readValue(Object target);

  /**
   * 给目标对象写入值。
   *
   * @param target 目标对象。
   * @param value 要写入的值。
   */
  void writeValue(Object target, F value);

  /**
   * 是否是当前字段反射器。
   *
   * @param fieldName 字段名称。
   * @return 是否是当前字段反射器。
   */
  boolean isThisFieldReflector(String fieldName);

  /**
   * 获取当前操作的field。
   *
   * @return 当前操作的field。
   */
  Field field();

  /**
   * 获取字段的泛型类型。 如果是参数化类型{@link java.lang.reflect.ParameterizedType}，则返回实际的类型为{@link
   * ParameterizedTypeImpl} 如果是通配符类型{@link java.lang.reflect.WildcardType}，则返回实际的类型为{@link
   * WildcardTypeImpl} 如果是泛型数组类型{@link java.lang.reflect.GenericArrayType}，则返回实际的类型为{@link
   * GenericArrayTypeImpl}
   *
   * @return 字段的泛型类型。
   */
  public Type generic();
}
