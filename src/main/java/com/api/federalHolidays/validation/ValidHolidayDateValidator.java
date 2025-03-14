package com.api.federalHolidays.validation;

import com.api.federalHolidays.util.DateValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidHolidayDateValidator implements ConstraintValidator<ValidHolidayDate, String> {

    @Override
    public boolean isValid(String dateStr, ConstraintValidatorContext context) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false; // Null or empty values are invalid
        }

        try {
            // Validate and parse the date
            DateValidator.validateAndParseDate(dateStr);
            return true; // Date is valid
        } catch (IllegalArgumentException e) {
            // Set the error message in the context
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false; // Date is invalid
        }
    }
}