package com.api.federalHolidays.util;

import com.api.federalHolidays.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateValidator {
    private static final String DATE_FORMAT = "uuuu-MM-dd";
    public static LocalDate validateAndParseDate(String dateStr) {
        try {
            // Use ResolverStyle.STRICT to ensure the date is valid for the given month
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_FORMAT).withResolverStyle(ResolverStyle.STRICT));

            // Check if the year of the date matches the current year
            int currentYear = Year.now().getValue();
            if (date.getYear() != currentYear) {
                throw new CustomException("Holiday date must be within the current year. Provided: " + dateStr, HttpStatus.BAD_REQUEST);
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new CustomException("Invalid date or Holiday date must be in the format yyyy-MM-dd. Provided: " + dateStr, HttpStatus.BAD_REQUEST);
        }
    }
}