package com.api.federalHolidays.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FederalHolidayResponse {
    private Long id;
    private String countryCode;
    private String countryName;
    private LocalDate holidayDate;
    private String holidayName;
    private String message;


}