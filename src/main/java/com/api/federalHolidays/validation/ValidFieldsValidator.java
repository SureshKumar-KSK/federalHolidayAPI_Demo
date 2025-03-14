package com.api.federalHolidays.validation;

import com.api.federalHolidays.exception.CustomException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.HttpStatus;

public class ValidFieldsValidator implements ConstraintValidator<ValidFields, String> {


    @Override
    public boolean isValid(String fieldValue, ConstraintValidatorContext context) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            //throw new CustomException("Field value cannot be empty, null, or blank", HttpStatus.BAD_REQUEST);
            return false;
        }
        return true;
    }
}