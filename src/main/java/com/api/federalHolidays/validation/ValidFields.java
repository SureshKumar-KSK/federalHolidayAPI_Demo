package com.api.federalHolidays.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidFieldsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFields {
    String message() default "Field value cannot be empty, null, or blank";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}