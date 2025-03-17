package com.api.federalHolidays.service;


import com.api.federalHolidays.dto.FederalHolidayRequest;
import com.api.federalHolidays.dto.FederalHolidayResponse;
import com.api.federalHolidays.dto.FileUploadResponse;
import com.api.federalHolidays.entity.Country;
import com.api.federalHolidays.entity.FederalHoliday;
import com.api.federalHolidays.exception.*;
import com.api.federalHolidays.repository.CountryRepository;
import com.api.federalHolidays.repository.FederalHolidayRepository;
import com.api.federalHolidays.util.DateValidator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FederalHolidayServiceImplTest {


    @Mock
    private FederalHolidayRepository federalHolidayRepository;

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private FederalHolidayServiceImpl federalHolidayService;

    @Test
    public void testGetAllHolidays_Success() {

        FederalHoliday holiday1 = new FederalHoliday();
        holiday1.setId(1L);
        holiday1.setCountryCode("001");
        holiday1.setCountryName("United States");
        holiday1.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday1.setHolidayName("New Year");
        FederalHoliday holiday2 = new FederalHoliday();
        holiday2.setId(2L);
        holiday2.setCountryCode("002");
        holiday2.setCountryName("Canada");
        holiday2.setHolidayDate(LocalDate.of(2025, 07, 01));
        holiday2.setHolidayName("Canada Day");

        when(federalHolidayRepository.findAll()).thenReturn(List.of(holiday1, holiday2));

        List<FederalHolidayResponse> response = federalHolidayService.getAllHolidays();

        assertNotNull(response);
        assertEquals(2, response.size());


        assertEquals(1L, response.get(0).getId());
        assertEquals("001", response.get(0).getCountryCode());
        assertEquals("United States", response.get(0).getCountryName());
        assertEquals("New Year", response.get(0).getHolidayName());
        assertEquals(LocalDate.of(2025, 1, 1), response.get(0).getHolidayDate());


        assertEquals(2L, response.get(1).getId());
        assertEquals("002", response.get(1).getCountryCode());
        assertEquals("Canada", response.get(1).getCountryName());
        assertEquals("Canada Day", response.get(1).getHolidayName());
        assertEquals(LocalDate.of(2025, 7, 1), response.get(1).getHolidayDate());

        verify(federalHolidayRepository, times(1)).findAll();
    }
    @Test
    public void testGetHolidaysByCountryCode_Success() {

        String countryCode = "001";
        FederalHoliday holiday1 = new FederalHoliday();
        holiday1.setId(1L);
        holiday1.setCountryCode("001");
        holiday1.setCountryName("United States");
        holiday1.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday1.setHolidayName("New Year");
        FederalHoliday holiday2 = new FederalHoliday();
        holiday2.setId(2L);
        holiday2.setCountryCode("001");
        holiday2.setCountryName("United States");
        holiday2.setHolidayDate(LocalDate.of(2025, 07, 04));
        holiday2.setHolidayName("Independence Day");

        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(List.of(holiday1, holiday2));

        List<FederalHolidayResponse> response = federalHolidayService.getHolidaysByCountryCode(countryCode);

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).getId());
        assertEquals("001", response.get(0).getCountryCode());
        assertEquals("United States", response.get(0).getCountryName());
        assertEquals("New Year", response.get(0).getHolidayName());
        assertEquals(LocalDate.of(2025, 1, 1), response.get(0).getHolidayDate());

        assertEquals(2L, response.get(1).getId());
        assertEquals("001", response.get(1).getCountryCode());
        assertEquals("United States", response.get(1).getCountryName());
        assertEquals("Independence Day", response.get(1).getHolidayName());
        assertEquals(LocalDate.of(2025, 7, 4), response.get(1).getHolidayDate());

        verify(federalHolidayRepository, times(1)).findByCountryCode(countryCode);
    }
    @Test
    public void testGetHolidaysByCountryCode_CountryCodeNotFound_Failure() {
        String countryCode = "003"; 
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(List.of());

        
        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.getHolidaysByCountryCode(countryCode));
        assertEquals("No holidays found for country code: 003", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()); 

        verify(federalHolidayRepository, times(1)).findByCountryCode(countryCode);
    }
    @Test
    public void testGetAllHolidays_NoHolidaysFound() {

        when(federalHolidayRepository.findAll()).thenReturn(List.of());

        List<FederalHolidayResponse> response = federalHolidayService.getAllHolidays();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(federalHolidayRepository, times(1)).findAll();
    }

    // Success Scenarios

    @Test
    public void testAddHoliday_NewCountryCodeAndNewHoliday_Success() {

        FederalHolidayRequest request = new FederalHolidayRequest();
        request.setCountryCode("001");
        request.setCountryName("United States");
        request.setHolidayDate("2025-01-01");
        request.setHolidayName("New Year");


        Country country = new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");

        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday.setHolidayName("New Year");


        //when(countryRepository.findByCountryCode("001")).thenReturn(Optional.of(country));
        when(countryRepository.findByCountryCode("001")).thenReturn(Optional.empty());
        when(countryRepository.save(any(Country.class))).thenReturn(country);
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(holiday);

        FederalHolidayResponse response = federalHolidayService.addHoliday(request);

        assertNotNull(response);
        assertEquals("001", response.getCountryCode());
        assertEquals("United States", response.getCountryName());
        assertEquals("New Year", response.getHolidayName());
        assertEquals(LocalDate.of(2025, 1, 1), response.getHolidayDate());
        assertEquals("Holiday added successfully", response.getMessage());

        verify(countryRepository, times(1)).save(any(Country.class));
        verify(federalHolidayRepository, times(1)).save(any(FederalHoliday.class));
    }

    @Test
    public void testAddHoliday_ExistingCountryCodeWithMatchingName_Success() {

        Country country = new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");

        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 07, 04));
        holiday.setHolidayName("Independence Day");
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-07-04","Independence Day");

        when(countryRepository.findByCountryCode("001")).thenReturn(Optional.of(country));
       when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(holiday);


        FederalHolidayResponse response = federalHolidayService.addHoliday(request);

        assertNotNull(response);
        assertEquals("001", response.getCountryCode());
        assertEquals("United States", response.getCountryName());
        assertEquals("Independence Day", response.getHolidayName());
        assertEquals(LocalDate.of(2025, 7, 4), response.getHolidayDate());
        assertEquals("Holiday added successfully", response.getMessage());

        verify(countryRepository, never()).save(any(Country.class));
        verify(federalHolidayRepository, times(1)).save(any(FederalHoliday.class));
    }

    // Failure Scenarios

    @Test
    public void testAddHoliday_CountryCodeExistsWithMismatchedName_Failure() {

        Country country = new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");

        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 12, 25));
        FederalHolidayRequest request = new FederalHolidayRequest("001", "Canada",  "2025-01-01","New Year");
        when(countryRepository.findByCountryCode("001")).thenReturn(Optional.of(country));


        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
        assertEquals("Country name does not match the existing record for country code: 001", exception.getMessage());

        verify(countryRepository, never()).save(any(Country.class));
        verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));
    }

    @Test
    public void testAddHoliday_DuplicateHolidayForSameCountryCodeAndDate_Failure() {

        Country country = new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");

        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01","New Year");
        when(federalHolidayRepository.existsByCountryCodeAndHolidayName("001","New Year")).thenReturn(true);


            CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
            assertEquals("Duplicate holiday record for country code: 001 and name: New Year", exception.getMessage());
            assertEquals(HttpStatus.CONFLICT, exception.getStatus());

            verify(federalHolidayRepository, times(1)).existsByCountryCodeAndHolidayName("001", "New Year");
            verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));
        }

    @Test
    public void testAddHoliday_DuplicateHolidayNameForSameCountryCodeAndDate_Failure() {


        Country country = new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");

        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 12, 25));

        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year");

        when(countryRepository.findByCountryCode("001")).thenReturn(Optional.of(country));

        when(federalHolidayRepository.existsByCountryCodeAndHolidayName("001", "New Year")).thenReturn(true);


        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
        assertEquals("Duplicate holiday record for country code: 001 and name: New Year", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        verify(federalHolidayRepository, times(1)).existsByCountryCodeAndHolidayName("001", "New Year");
        verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));

    }

    @Test
    public void testAddHoliday_InvalidCountryCode_Failure() {

        FederalHolidayRequest request = new FederalHolidayRequest("1234", "United States", "2025-01-01","New Year");

        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
        assertEquals("Country code must be 1 to 3 alphanumeric characters. Provided: 1234.", exception.getMessage());

        verify(countryRepository, never()).findByCountryCode(anyString());
        verify(countryRepository, never()).save(any(Country.class));
        verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));
    }

    @Test
    public void testAddHoliday_InvalidHolidayDate_Failure() {

        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2024-01-01","New Year");


        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
        assertEquals("Holiday date must be within the current year. Provided: 2024-01-01", exception.getMessage());

        verify(countryRepository, never()).findByCountryCode(anyString());
        verify(countryRepository, never()).save(any(Country.class));
        verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));
    }

    @Test
    public void testAddHoliday_MissingRequiredFields_Failure() {

        FederalHolidayRequest request = new FederalHolidayRequest(null, "United States",  "2025-01-01","New Year");


        CustomException exception = assertThrows(CustomException.class, () -> federalHolidayService.addHoliday(request));
        assertEquals("Country code is required.", exception.getMessage());

        verify(countryRepository, never()).findByCountryCode(anyString());
        verify(countryRepository, never()).save(any(Country.class));
        verify(federalHolidayRepository, never()).save(any(FederalHoliday.class));
    }
    // Test for getAllHolidays

    @Test
    public void testUpdateHolidayByIdAndCountryCode_NotFound() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year");
        when(federalHolidayRepository.findByIdAndCountryCode(1L, "001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayService.updateHolidayByIdAndCountryCode(1L, "001", request));
    }


    @Test
    public void testUpdateHolidayByCountryCodeAndDate_NotFound() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year");
        when(federalHolidayRepository.findByCountryCodeAndHolidayDate("001", LocalDate.of(2025, 1, 1))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayService.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request));
    }



    // Test for deleteByCountryCode 
    @Test
    public void testDeleteByCountryCode_Success_CountryDeleted() {
        String countryCode = "001";

        
        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCode(countryCode)).thenReturn(1); // 1 record deleted
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(Collections.emptyList());

        // Call service method
        int deleteRecordCount = federalHolidayService.deleteByCountryCode(countryCode);

        
        assertEquals(1, deleteRecordCount);
        verify(countryRepository, times(1)).deleteById(countryCode); // Country should be deleted
    }

    // Error Scenario: No holidays found for the country code
    @Test
    public void testDeleteByCountryCode_NoHolidaysFound() {
        String countryCode = "999";


        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenReturn(false);


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.deleteByCountryCode(countryCode));
        assertEquals("No holidays found for country code: 999", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // Error Scenario: Database error while checking if holidays exist
    @Test
    public void testDeleteByCountryCode_DatabaseError_ExistsCheck() {
        String countryCode = "001";


        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenThrow(new DataAccessException("Database error") {});


        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCode(countryCode));
    }

    // Error Scenario: Database error while deleting holidays
    @Test
    public void testDeleteByCountryCode_DatabaseError_DeleteHolidays() {
        String countryCode = "001";


        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCode(countryCode)).thenThrow(new DataAccessException("Database error") {});


        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCode(countryCode));
    }

    // Error Scenario: Database error while fetching remaining holidays
    @Test
    public void testDeleteByCountryCode_DatabaseError_FetchRemainingHolidays() {
        String countryCode = "001";


        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCode(countryCode)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenThrow(new DataAccessException("Database error") {});


        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCode(countryCode));
    }

    // Error Scenario: Database error while deleting the country
    @Test
    public void testDeleteByCountryCode_DatabaseError_DeleteCountry() {
        String countryCode = "001";


        when(federalHolidayRepository.existsByCountryCode(countryCode)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCode(countryCode)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(Collections.emptyList());
        doThrow(new DataAccessException("Database error") {}).when(countryRepository).deleteById(countryCode);


        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCode(countryCode));
    }

    // Success Scenario: Delete holiday by country code and holiday date
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_Success() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(Collections.emptyList());

        // Call service method
        int deleteCount = federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate);


        assertEquals(1, deleteCount);
        verify(countryRepository, times(1)).deleteById(countryCode); // Country should be deleted
    }

    // Success Scenario: Delete holiday by country code and holiday date (country not deleted)
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_Success_CountryNotDeleted() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(List.of(new FederalHoliday()));

        // Call service method
        int deleteCount = federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate);


        assertEquals(1, deleteCount);
        verify(countryRepository, never()).deleteById(countryCode); // Country should not be deleted
    }

    // Error Scenario: Country code not found
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_CountryCodeNotFound() {
        String countryCode = "999";
        String holidayDate = "2025-01-01";


        when(countryRepository.findById(countryCode)).thenReturn(Optional.empty());


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
        assertEquals("Country code not found: 999", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // Error Scenario: Holiday not found for the given country code and date
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_HolidayNotFound() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");

        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(false);

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
        assertEquals("No holiday found for country code 001 on date 2025-01-01", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // Error Scenario: Database error while checking if holiday exists
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_DatabaseError_ExistsCheck() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");
        
        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate))
                .thenThrow(new DataAccessException("Database error") {});

        
        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }

    // Error Scenario: Database error while deleting holiday
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_DatabaseError_DeleteHoliday() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");
        
        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate))
                .thenThrow(new DataAccessException("Database error") {});

        
        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }

    // Error Scenario: Database error while fetching remaining holidays
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_DatabaseError_FetchRemainingHolidays() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        Country country= new Country();
        country.setCountryCode(countryCode);
        country.setCountryName("United States");
        
        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode))
                .thenThrow(new DataAccessException("Database error") {});

        
        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }

    // Error Scenario: Database error while deleting the country
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_DatabaseError_DeleteCountry() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
Country country=new Country();
country.setCountryCode(countryCode);
country.setCountryName("United States");
        
        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(true);
        when(federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)).thenReturn(1);
        when(federalHolidayRepository.findByCountryCode(countryCode)).thenReturn(Collections.emptyList());
        doThrow(new DataAccessException("Database error") {}).when(countryRepository).deleteById(countryCode);

        
        assertThrows(DataAccessException.class, () ->
                federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate));
    }

    // Success Scenario: Upload CSV file
    @Test
    public void testUploadHolidays_Success_CSV() throws IOException {
        // Mock CSV file
        String csvContent = "countryCode,countryName,holidayDate,holidayName\n001,United States,2025-01-01,New Year";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        // Call service method
        FileUploadResponse response = federalHolidayService.uploadHolidays(Collections.singletonList(file));

        
        assertEquals("Files processed successfully", response.getMessage());
        assertEquals(1, response.getFileResults().size());
        assertEquals("test.csv", response.getFileResults().get(0).getFileName());
        assertEquals(0, response.getFileResults().get(0).getFailedRecords());
        assertEquals(0, response.getFileResults().get(0).getDuplicateRecords());
    }

    // Success Scenario: Upload Excel file
    @Test
    public void testUploadHolidays_Success_Excel() throws IOException {
        // Mock Excel file
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Holidays");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("countryCode");
        headerRow.createCell(1).setCellValue("countryName");
        headerRow.createCell(2).setCellValue("holidayDate");
        headerRow.createCell(3).setCellValue("holidayName");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("001");
        dataRow.createCell(1).setCellValue("United States");
        dataRow.createCell(2).setCellValue("2025-01-01");
        dataRow.createCell(3).setCellValue("New Year");

        MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookToByteArray(workbook));

        // Call service method
        FileUploadResponse response = federalHolidayService.uploadHolidays(Collections.singletonList(file));

        
        assertEquals("Files processed successfully", response.getMessage());
        assertEquals(1, response.getFileResults().size());
        assertEquals("test.xlsx", response.getFileResults().get(0).getFileName());
        assertEquals(0, response.getFileResults().get(0).getFailedRecords());
        assertEquals(0, response.getFileResults().get(0).getDuplicateRecords());
    }

    // Error Scenario: No files uploaded
    @Test
    public void testUploadHolidays_NoFilesUploaded() {
        // Call service method with empty file list
        FileUploadResponse response = federalHolidayService.uploadHolidays(Collections.emptyList());

        
        assertEquals("No files uploaded. Please upload at least one file.", response.getMessage());
        assertTrue(response.getFileResults().isEmpty());
    }

    // Error Scenario: Unsupported file format
    @Test
    public void testUploadHolidays_UnsupportedFileFormat() {
        // Mock unsupported file
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Invalid data".getBytes());

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.uploadHolidays(Collections.singletonList(file)));
        assertEquals("Unsupported file format. Only CSV and Excel files are allowed.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Error Scenario: Failed to process CSV file
    @Test
    public void testUploadHolidays_FailedToProcessCSV() throws IOException {
        // Mock CSV file with invalid content
        String csvContent = "countryCode,countryName,holidayDate,holidayName\n001,United States,Invalid Date,New Year";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

        // Call service method
        FileUploadResponse response = federalHolidayService.uploadHolidays(Collections.singletonList(file));

        
        assertEquals("Files processed successfully", response.getMessage());
        assertEquals(1, response.getFileResults().size());
        assertEquals("test.csv", response.getFileResults().get(0).getFileName());
        assertEquals(1, response.getFileResults().get(0).getFailedRecords());
    }

    // Error Scenario: Failed to process Excel file
    @Test
    public void testUploadHolidays_FailedToProcessExcel() throws IOException {
        // Mock Excel file with invalid content
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Holidays");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("countryCode");
        headerRow.createCell(1).setCellValue("countryName");
        headerRow.createCell(2).setCellValue("holidayDate");
        headerRow.createCell(3).setCellValue("holidayName");

        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("001");
        dataRow.createCell(1).setCellValue("United States");
        dataRow.createCell(2).setCellValue("Invalid Date");
        dataRow.createCell(3).setCellValue("New Year");

        MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookToByteArray(workbook));

        // Call service method
        FileUploadResponse response = federalHolidayService.uploadHolidays(Collections.singletonList(file));

        
        assertEquals("Files processed successfully", response.getMessage());
        assertEquals(1, response.getFileResults().size());
        assertEquals("test.xlsx", response.getFileResults().get(0).getFileName());
        assertEquals(1, response.getFileResults().get(0).getFailedRecords());
    }

    // Helper method to convert Workbook to byte array
    private byte[] workbookToByteArray(Workbook workbook) throws IOException {
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    // Success Scenario: Update holiday by ID and country code
    @Test
    public void testUpdateHolidayByIdAndCountryCode_Success() {
        Long id = 1L;
        String countryCode = "001";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year Updated");
        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday.setHolidayName("New Year");
        Country country=new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");
        
        when(federalHolidayRepository.findByIdAndCountryCode(id, countryCode)).thenReturn(Optional.of(holiday));
        when(countryRepository.existsByCountryCodeAndCountryName(countryCode,request.getCountryName())).thenReturn(true);

        when(federalHolidayRepository.existsByCountryCodeAndHolidayName(countryCode, "New Year Updated")).thenReturn(false);
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDateAndHolidayName(countryCode, LocalDate.of(2025, 1, 1), request.getHolidayName())).thenReturn(false);

        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(holiday);

        // Call service method
        FederalHolidayResponse response = federalHolidayService.updateHolidayByIdAndCountryCode(id, countryCode, request);

        
        assertEquals("New Year Updated", response.getHolidayName());
        assertEquals("Holiday updated successfully", response.getMessage());
    }

    // Error Scenario: Holiday not found for the given ID and country code
    @Test
    public void testUpdateHolidayByIdAndCountryCode_HolidayNotFound() {
        Long id = 1L;
        String countryCode = "001";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year");

        
        when(federalHolidayRepository.findByIdAndCountryCode(id, countryCode)).thenReturn(Optional.empty());

        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayService.updateHolidayByIdAndCountryCode(id, countryCode, request));
        assertEquals("Holiday not found with ID: 1 and country code: 001", exception.getMessage());
    }

    // Error Scenario: Duplicate holiday name or date for the country
    @Test
    public void testUpdateHolidayByIdAndCountryCode_DuplicateHoliday() {
        Long id = 1L;
        String countryCode = "001";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year Day");
        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday.setHolidayName("New Year");
        FederalHoliday holiday2 = new FederalHoliday();
        holiday2.setId(1L);
        holiday2.setCountryCode("001");
        holiday2.setCountryName("United States");
        holiday2.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday2.setHolidayName("New Year ");

        
        Country country=new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");
        when(federalHolidayRepository.findByIdAndCountryCode(id, countryCode)).thenReturn(Optional.of(holiday));
        when(countryRepository.existsByCountryCodeAndCountryName(countryCode, request.getCountryName())).thenReturn(true);
        when(federalHolidayRepository.existsByCountryCodeAndHolidayName(countryCode, request.getHolidayName())).thenReturn(true);

        
        DuplicateRecordException exception = assertThrows(DuplicateRecordException.class, () ->
                federalHolidayService.updateHolidayByIdAndCountryCode(id, countryCode, request));
        assertEquals("Holiday name already exists for the country.", exception.getMessage());
    }

    // Success Scenario: Update holiday by country code and date
    @Test
    public void testUpdateHolidayByCountryCodeAndDate_Success() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year Updated");
        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday.setHolidayName("New Year");
        
        Country country=new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");
        when(countryRepository.existsByCountryCodeAndCountryName(countryCode,request.getCountryName())).thenReturn(true);

        when(federalHolidayRepository.findByCountryCodeAndHolidayDate(countryCode, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.of(holiday));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayName(countryCode, "New Year Updated")).thenReturn(false);
        when(federalHolidayRepository.save(any(FederalHoliday.class))).thenReturn(holiday);

        // Call service method
        FederalHolidayResponse response = federalHolidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, request);

        
        assertEquals("New Year Updated", response.getHolidayName());
        assertEquals("Holiday updated successfully", response.getMessage());
    }

    // Error Scenario: Holiday not found for the given country code and date
    @Test
    public void testUpdateHolidayByCountryCodeAndDate_HolidayNotFound() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-01-01", "New Year");

        
        when(federalHolidayRepository.findByCountryCodeAndHolidayDate(countryCode, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.empty());

        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, request));
        assertEquals("Holiday not found with country code: 001 and date: 2025-01-01", exception.getMessage());
    }

    // Error Scenario: Duplicate holiday name or date for the country
    @Test
    public void testUpdateHolidayByCountryCodeAndDate_DuplicateHoliday() {
        String countryCode = "001";
        String holidayDate = "2025-01-01";
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-02-01", "New Year");
        FederalHoliday holiday = new FederalHoliday();
        holiday.setId(1L);
        holiday.setCountryCode("001");
        holiday.setCountryName("United States");
        holiday.setHolidayDate(LocalDate.of(2025, 01, 01));
        holiday.setHolidayName("New Year");
        Country country=new Country();
        country.setCountryCode("001");
        country.setCountryName("United States");
        when(countryRepository.existsByCountryCodeAndCountryName(countryCode,request.getCountryName())).thenReturn(true);
        when(federalHolidayRepository.findByCountryCodeAndHolidayDate(countryCode, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.of(holiday));
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, LocalDate.parse(request.getHolidayDate()))).thenReturn(true);

        
        DuplicateRecordException exception = assertThrows(DuplicateRecordException.class, () ->
                federalHolidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, request));
        assertEquals("Holiday date already exists for the country.", exception.getMessage());
    }
    // Date Validation: Invalid Date Format (e.g., 2025-02-30)
    @Test
    public void testAddHoliday_InvalidDateFormat() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "2025-02-30", "Invalid Date");

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Invalid date or Holiday date must be in the format yyyy-MM-dd. Provided: 2025-02-30", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Date Validation: Invalid Date Format (e.g., 15-15-2025)
    @Test
    public void testAddHoliday_InvalidDateFormat_InvalidMonthAndDay() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", "15-15-2025", "Invalid Date");

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Invalid date or Holiday date must be in the format yyyy-MM-dd. Provided: 15-15-2025", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Date Validation: Past Date
    @Test
    public void testAddHoliday_PastDate() {
        LocalDate pastDate = LocalDate.now().minusYears(1);
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", pastDate.toString(), "Past Date");

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Holiday date must be within the current year. Provided: "+request.getHolidayDate(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Date Validation: Future Date (beyond current year)
    @Test
    public void testAddHoliday_FutureDate() {
        LocalDate futureDate = LocalDate.now().plusYears(2);
        FederalHolidayRequest request = new FederalHolidayRequest("001", "United States", futureDate.toString(), "Future Date");

        
        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Holiday date must be within the current year. Provided: "+futureDate.toString(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Country Code Validation: Invalid Characters (e.g., more than 3 characters)
    @Test
    public void testAddHoliday_InvalidCountryCode_MoreThanThreeCharacters() {
        FederalHolidayRequest request = new FederalHolidayRequest("1234", "United States", "2025-01-01", "Invalid Country Code");


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Country code must be 1 to 3 alphanumeric characters. Provided: 1234.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Country Code Validation: Invalid Characters (e.g., special characters)
    @Test
    public void testAddHoliday_InvalidCountryCode_SpecialCharacters() {
        FederalHolidayRequest request = new FederalHolidayRequest("00@", "United States", "2025-01-01", "Invalid Country Code");


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Country code must be 1 to 3 alphanumeric characters. Provided: 00@.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Country Code Validation: Empty Country Code
    @Test
    public void testAddHoliday_EmptyCountryCode() {
        FederalHolidayRequest request = new FederalHolidayRequest("", "United States", "2025-01-01", "Empty Country Code");


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Country code is required.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    // Country Code Validation: Null Country Code
    @Test
    public void testAddHoliday_NullCountryCode() {
        FederalHolidayRequest request = new FederalHolidayRequest(null, "United States", "2025-01-01", "Null Country Code");


        CustomException exception = assertThrows(CustomException.class, () ->
                federalHolidayService.addHoliday(request));
        assertEquals("Country code is required.", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }


    // Test data
    private static final String COUNTRY_CODE = "USA";
    private static final String COUNTRY_NAME = "United States";
    private static final String HOLIDAY_NAME = "New Year";
    private static final String HOLIDAY_DATE = "2025-01-01";


    // Error Scenarios

    @Test
    public void testUpdateHolidayByIdAndCountryCode_DuplicateRecord_ThrowsException() {
        // Arrange
        FederalHolidayRequest request = new FederalHolidayRequest();
        request.setCountryCode(COUNTRY_CODE);
        request.setCountryName(COUNTRY_NAME);
        request.setHolidayName(HOLIDAY_NAME);
        request.setHolidayDate(HOLIDAY_DATE);

        FederalHoliday existingHoliday = new FederalHoliday();
        existingHoliday.setId(1L);
        existingHoliday.setCountryCode(COUNTRY_CODE);
        existingHoliday.setCountryName(COUNTRY_NAME);
        existingHoliday.setHolidayName(HOLIDAY_NAME);
        existingHoliday.setHolidayDate(LocalDate.parse("2025-01-01"));

        when(federalHolidayRepository.findByIdAndCountryCode(1L, request.getCountryCode())).thenReturn(Optional.of(existingHoliday));
        when(countryRepository.existsByCountryCodeAndCountryName(COUNTRY_CODE, COUNTRY_NAME)).thenReturn(true);
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDateAndHolidayName(COUNTRY_CODE, LocalDate.parse(HOLIDAY_DATE), HOLIDAY_NAME))
                .thenReturn(true);

        // Act & Assert
        DuplicateRecordException exception = assertThrows(DuplicateRecordException.class, () -> {
            federalHolidayService.updateHolidayByIdAndCountryCode(1L, COUNTRY_CODE, request);
        });

        assertEquals("A record with the same country code, holiday date, and holiday name already exists.", exception.getMessage());
    }

    @Test
    public void testUpdateHolidayByIdAndCountryCode_InvalidInput_ThrowsException() {
        // Arrange
        FederalHolidayRequest request = new FederalHolidayRequest();
        request.setCountryCode(COUNTRY_CODE);
        request.setCountryName(COUNTRY_NAME);
        request.setHolidayName(HOLIDAY_NAME);
        request.setHolidayDate(HOLIDAY_DATE);

        when(federalHolidayRepository.findByIdAndCountryCode(1L,request.getCountryCode())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            federalHolidayService.updateHolidayByIdAndCountryCode(1L, COUNTRY_CODE, request);
        });

        assertEquals("Holiday not found with ID: 1 and country code: USA", exception.getMessage());
    }

    @Test
    public void testUpdateHolidayByCountryCodeAndDate_DuplicateRecord_ThrowsException() {
        // Arrange
        FederalHolidayRequest request = new FederalHolidayRequest();
        request.setCountryCode(COUNTRY_CODE);
        request.setCountryName(COUNTRY_NAME);
        request.setHolidayName(HOLIDAY_NAME);
        request.setHolidayDate(HOLIDAY_DATE);

        FederalHoliday existingHoliday = new FederalHoliday();
        existingHoliday.setId(1L);
        existingHoliday.setCountryCode(COUNTRY_CODE);
        existingHoliday.setHolidayName("Old Holiday");
        existingHoliday.setHolidayDate(LocalDate.parse("2025-01-01"));

        when(federalHolidayRepository.findByCountryCodeAndHolidayDate(COUNTRY_CODE, LocalDate.parse(HOLIDAY_DATE)))
                .thenReturn(Optional.of(existingHoliday));
        when(countryRepository.existsByCountryCodeAndCountryName(COUNTRY_CODE, COUNTRY_NAME)).thenReturn(true);
        when(federalHolidayRepository.existsByCountryCodeAndHolidayDateAndHolidayName(COUNTRY_CODE, LocalDate.parse(HOLIDAY_DATE), HOLIDAY_NAME))
                .thenReturn(true);

        // Act & Assert
        DuplicateRecordException exception = assertThrows(DuplicateRecordException.class, () -> {
            federalHolidayService.updateHolidayByCountryCodeAndDate(COUNTRY_CODE, HOLIDAY_DATE, request);
        });

        assertEquals("A record with the same country code, holiday date, and holiday name already exists.", exception.getMessage());
    }

    @Test
    public void testUpdateHolidayByCountryCodeAndDate_ResourceNotFound_ThrowsException() {
        // Arrange
        FederalHolidayRequest request = new FederalHolidayRequest();
        request.setCountryCode(COUNTRY_CODE);
        request.setCountryName(COUNTRY_NAME);
        request.setHolidayName(HOLIDAY_NAME);
        request.setHolidayDate(HOLIDAY_DATE);

        when(federalHolidayRepository.findByCountryCodeAndHolidayDate(COUNTRY_CODE, LocalDate.parse(HOLIDAY_DATE)))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            federalHolidayService.updateHolidayByCountryCodeAndDate(COUNTRY_CODE, HOLIDAY_DATE, request);
        });

        assertEquals("Holiday not found with country code: USA and date: 2025-01-01", exception.getMessage());
    }
}
