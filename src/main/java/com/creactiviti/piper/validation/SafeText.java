package com.creactiviti.piper.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = {SafeTextValidator.class})
@Documented
public @interface SafeText {
    String message() default "Given text is not safe.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String regex() default "[\\w-_]*";
}
