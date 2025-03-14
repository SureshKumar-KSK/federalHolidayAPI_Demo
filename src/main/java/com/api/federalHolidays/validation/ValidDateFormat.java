package com.api.federalHolidays.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDateFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {
    String message() default "Invalid date format or invalid date. Expected format: yyyy-MM-dd .";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}