package com.api.federalHolidays.dto;

import com.api.federalHolidays.validation.ValidHolidayDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FederalHolidayRequest {

    @NotBlank(message = "Country code cannot be null or blank")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,3}+$", message = "Country code must be 1 to 3 alphanumeric characters.")
    private String countryCode;

    @NotBlank(message = "Country name cannot be null or blank")
    private String countryName;

    @NotBlank(message = "Holiday date cannot be null or blank")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Invalid date or Holiday date must be in the format yyyy-MM-dd.")
    @ValidHolidayDate
    private String holidayDate;

    @NotBlank(message = "Holiday name cannot be null or blank")
    private String holidayName;

    // Custom setter to trim holidayName
    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName != null ? holidayName.trim() : null;
    }

    // Custom setter to trim countryCode
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode != null ? countryCode.trim() : null;
    }

    // Custom setter to trim countryName
    public void setCountryName(String countryName) {
        this.countryName = countryName != null ? countryName.trim() : null;
    }

    // Custom setter to trim holidayDate
    public void setHolidayDate(String holidayDate) {
        this.holidayDate = holidayDate != null ? holidayDate.trim() : null;
    }
}