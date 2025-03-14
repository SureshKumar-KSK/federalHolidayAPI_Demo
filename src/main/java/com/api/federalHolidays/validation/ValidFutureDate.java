package com.api.federalHolidays.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFutureDateValidator.class)
public @interface ValidFutureDate {
    String message() default "Invalid date. It must be a in the current year.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}