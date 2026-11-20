package com.rubp.whattoeat.server.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

/**
 * 校验字符序列按 UTF-8 编码后的字节长度
 */
@Documented
@Constraint(validatedBy = ByteSize.Validator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ByteSize {

    String message() default "size must be between {min} and {max} bytes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 0;

    int max() default Integer.MAX_VALUE;

    final class Validator implements ConstraintValidator<ByteSize, CharSequence> {

        private int min;
        private int max;

        @Override
        public void initialize(ByteSize constraint) {
            min = constraint.min();
            max = constraint.max();
            if (min < 0 || max < min) {
                throw new IllegalArgumentException("Byte size limits must satisfy 0 <= min <= max");
            }
        }

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }

            int size = value.toString().getBytes(StandardCharsets.UTF_8).length;
            return size >= min && size <= max;
        }
    }
}
