package com.tracejp.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/6 20:43
 */
// 实现ConstraintValidator 接口
// 传入两个泛型参数，T1 为处理的注解，T2 为注解标注Bean对应的属性类型
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {


    private Set<Integer> set;

    // 初始化方法：用于初始化该对象的一些属性，类似子类构造器
    // ListValue constraintAnnotation: 上下文信息，即注解的属性值
    @Override
    public void initialize(ListValue constraintAnnotation) {
        set = new HashSet<>();
        // @ListValue(value = {0, 1}) => values = [0, 1]
        int[] values = constraintAnnotation.value();
        for (int value : values) {
            set.add(value);
        }
    }

    // 校验方法：用于校验逻辑，返回通过或不通过
    // Interger value: 注解对应属性传来的值
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }


}
