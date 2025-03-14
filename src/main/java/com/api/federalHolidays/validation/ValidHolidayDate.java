package com.api.federalHolidays.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidHolidayDateValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHolidayDate {
    String message() default "Invalid date format or invalid date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}