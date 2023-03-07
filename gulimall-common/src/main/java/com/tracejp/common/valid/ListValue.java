package com.tracejp.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/6 20:35
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
// validateBy: 指定自定义的校验器
@Constraint(validatedBy = {ListValueConstraintValidator.class})
public @interface ListValue {

    // 默认三个属性
    // message default "{}" 会默认在配置文件中取出对应key的值作为默认错误提示信息
    // 其 key的定义默认在文件 ValidationMessages.properties 中
    String message() default "{javax.validation.constraints.NotNull.message}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };

    // 自定义注解属性 value
    int[] value() default { };

}
