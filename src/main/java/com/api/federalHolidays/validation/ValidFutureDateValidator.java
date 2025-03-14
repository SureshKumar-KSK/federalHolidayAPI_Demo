package com.api.federalHolidays.validation;

import com.api.federalHolidays.exception.CustomException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ValidFutureDateValidator implements ConstraintValidator<ValidFutureDate, String> {
    private static final String DATE_FORMAT = "uuuu-MM-dd";

    @Override
    public void initialize(ValidFutureDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(String holidayDate, ConstraintValidatorContext context) {
        if (holidayDate == null) {
            return false;
        }
        //  Parsed date values are considered valid
        LocalDate parsedDate;
        try {
             parsedDate = LocalDate.parse(holidayDate, DateTimeFormatter.ofPattern(DATE_FORMAT).withResolverStyle(ResolverStyle.STRICT));
            LocalDate today = LocalDate.now();
            if (parsedDate.getYear() != today.getYear()) {
                return false;
            }
        } catch (DateTimeParseException e) {
            return false;
        }

            return true;
    }
    public static void validCurrentDate(LocalDate holidayDate) {
        LocalDate currentDate = LocalDate.now();
        if (holidayDate.getYear() != currentDate.getYear()) {
            throw new CustomException(" Invalid Holiday Date:" + " ' " + holidayDate + " .' " + " It must be in the current year.", HttpStatus.BAD_REQUEST);
        }
    }
}