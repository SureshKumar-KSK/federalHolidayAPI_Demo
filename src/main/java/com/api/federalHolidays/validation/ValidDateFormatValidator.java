package com.api.federalHolidays.validation;

import com.api.federalHolidays.exception.CustomException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ValidDateFormatValidator implements ConstraintValidator<ValidDateFormat, String> {

    private static final String DATE_FORMAT = "uuuu-MM-dd";

    @Override
    public boolean isValid(String date, ConstraintValidatorContext context) {
        if (date == null) {
            return false;
        }
        //  Parsed date values are considered valid
        try {
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT).withResolverStyle(ResolverStyle.STRICT));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    public static LocalDate validateAndParseDate(String dateStr) {
        try {
            // Use ResolverStyle.STRICT to ensure the date is valid for the given month
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT).withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format or invalid date: " + dateStr);
        }
    }
}