package com.api.federalHolidays.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidCountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    // Regex to allow only alphanumeric characters
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]{1,3}$");

    @Override
    public void initialize(ValidCountryCode constraintAnnotation) {
    }

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext context) {
        if (countryCode == null) {
            return false;
        }

        return ALPHANUMERIC_PATTERN.matcher(countryCode).matches();
    }
}