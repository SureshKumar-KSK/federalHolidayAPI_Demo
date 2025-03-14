package com.api.federalHolidays.service;


import com.api.federalHolidays.dto.FederalHolidayRequest;
import com.api.federalHolidays.dto.FederalHolidayResponse;
import com.api.federalHolidays.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface FederalHolidayService {
    List<FederalHolidayResponse> getAllHolidays();

    List<FederalHolidayResponse> getHolidaysByCountryCode(String countryCode);

    FederalHolidayResponse addHoliday(FederalHolidayRequest request);

    FederalHolidayResponse updateHolidayByIdAndCountryCode(Long id, String countryCode, FederalHolidayRequest request);

    FederalHolidayResponse updateHolidayByCountryCodeAndDate(String countryCode, String holidayDate, FederalHolidayRequest request);

    FileUploadResponse uploadHolidays(List<MultipartFile> file);

    int deleteByCountryCode(String countryCode);

    int deleteByCountryCodeAndHolidayDate(String countryCode, String holidayDate);
}
