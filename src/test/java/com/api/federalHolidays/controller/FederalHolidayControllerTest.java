package com.api.federalHolidays.controller;

import com.api.federalHolidays.dto.FederalHolidayResponse;
import com.api.federalHolidays.dto.FileUploadResponse;
import com.api.federalHolidays.exception.CustomException;
import com.api.federalHolidays.exception.DuplicateRecordException;
import com.api.federalHolidays.exception.InvalidInputException;
import com.api.federalHolidays.exception.ResourceNotFoundException;
import com.api.federalHolidays.service.FederalHolidayService;
import com.api.federalHolidays.dto.FederalHolidayRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FederalHolidayControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FederalHolidayService federalHolidayService;

    @InjectMocks
    private FederalHolidayController federalHolidayController;

    @BeforeEach
    public void setup() {
        // Manually initialize MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(federalHolidayController).build();
    }

    // Test for getAllHolidays
    @Test
    public void testGetAllHolidays_Success() {
        FederalHolidayResponse response1 = new FederalHolidayResponse(1L, "001", "USA", LocalDate.of(2025, 1, 1), "New Year", "Success");
        FederalHolidayResponse response2 = new FederalHolidayResponse(2L, "002", "Canada", LocalDate.of(2025, 7, 1), "Canada Day", "Success");
        List<FederalHolidayResponse> mockResponse = Arrays.asList(response1, response2);

        when(federalHolidayService.getAllHolidays()).thenReturn(mockResponse);

        ResponseEntity<List<FederalHolidayResponse>> response = federalHolidayController.getAllHolidays();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetAllHolidays_EmptyList() {
        when(federalHolidayService.getAllHolidays()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> federalHolidayController.getAllHolidays());
    }

    // Test for getHolidaysByCountryCode
    @Test
    public void testGetHolidaysByCountryCode_Success() {
        FederalHolidayResponse response1 = new FederalHolidayResponse(1L, "001", "USA", LocalDate.of(2025, 1, 1), "New Year", "Success");
        List<FederalHolidayResponse> mockResponse = Collections.singletonList(response1);

        when(federalHolidayService.getHolidaysByCountryCode("001")).thenReturn(mockResponse);

        ResponseEntity<List<FederalHolidayResponse>> response = federalHolidayController.getHolidaysByCountryCode("001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetHolidaysByCountryCode_NotFound() {
        when(federalHolidayService.getHolidaysByCountryCode("999")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> federalHolidayController.getHolidaysByCountryCode("999"));
    }

    // Test for addHoliday
    @Test
    public void testAddHoliday_Success() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        FederalHolidayResponse mockResponse = new FederalHolidayResponse(1L, "001", "USA", LocalDate.of(2025, 1, 1), "New Year", "Holiday added successfully");

        when(federalHolidayService.addHoliday(any(FederalHolidayRequest.class))).thenReturn(mockResponse);

        ResponseEntity<FederalHolidayResponse> response = federalHolidayController.addHoliday(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Holiday added successfully", response.getBody().getMessage());
    }

    // Test for updateHolidayByIdAndCountryCode
    @Test
    public void testUpdateHolidayByIdAndCountryCode_Success() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year Updated");
        FederalHolidayResponse mockResponse = new FederalHolidayResponse(1L, "001", "USA", LocalDate.of(2025, 1, 1), "New Year Updated", "Holiday updated successfully");

        when(federalHolidayService.updateHolidayByIdAndCountryCode(1L, "001", request)).thenReturn(mockResponse);

        ResponseEntity<FederalHolidayResponse> response = federalHolidayController.updateHolidayByIdAndCountryCode(1L, "001", request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Holiday updated successfully", response.getBody().getMessage());
    }

    // Test for updateHolidayByCountryCodeAndDate
    @Test
    public void testUpdateHolidayByCountryCodeAndDate_Success() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year Updated");
        FederalHolidayResponse mockResponse = new FederalHolidayResponse(1L, "001", "USA", LocalDate.of(2025, 1, 1), "New Year Updated", "Holiday updated successfully");

        when(federalHolidayService.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request)).thenReturn(mockResponse);

        ResponseEntity<FederalHolidayResponse> response = federalHolidayController.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Holiday updated successfully", response.getBody().getMessage());
    }

    // Test for uploadHolidays
    @Test
    public void testUploadHolidays_Success() {
        // Mock file
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", "001,USA,2025-01-01,New Year".getBytes());

        // Create mock FileResult
        FileUploadResponse.FileResult mockFileResult = new FileUploadResponse.FileResult();
        mockFileResult.setFileName("test.csv");
        mockFileResult.setTotalRecords(1);
        mockFileResult.setSuccessRecords(1);
        mockFileResult.setFailedRecords(0);
        mockFileResult.setDuplicateRecords(0);
        mockFileResult.setFailedRecordsDetails(Collections.emptyList());
        mockFileResult.setDuplicateRecordsDetails(Collections.emptyList());

        // Create mock FileUploadResponse
        FileUploadResponse mockResponse = new FileUploadResponse();
        mockResponse.setMessage("Files processed successfully");
        mockResponse.setFileResults(Collections.singletonList(mockFileResult));

        // Mock service behavior
        when(federalHolidayService.uploadHolidays(any(List.class))).thenReturn(mockResponse);

        // Call controller method
        ResponseEntity<FileUploadResponse> response = federalHolidayController.uploadHolidays(Collections.singletonList(file));

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Files processed successfully", response.getBody().getMessage());

        FileUploadResponse.FileResult result = response.getBody().getFileResults().get(0);
        assertEquals("test.csv", result.getFileName());
        assertEquals(1, result.getTotalRecords());
        assertEquals(1, result.getSuccessRecords());
        assertEquals(0, result.getFailedRecords());
        assertEquals(0, result.getDuplicateRecords());
        assertTrue(result.getFailedRecordsDetails().isEmpty());
        assertTrue(result.getDuplicateRecordsDetails().isEmpty());
    }


    // Test for deleteByCountryCode
    @Test
    public void testDeleteByCountryCode_Success() {
        when(federalHolidayService.deleteByCountryCode("001")).thenReturn(1);

        ResponseEntity<String> response = federalHolidayController.deleteByCountryCode("001");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1 records deleted for country code 001", response.getBody());
    }

    // Test for deleteByCountryCodeAndHolidayDate
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_Success() {
        when(federalHolidayService.deleteByCountryCodeAndHolidayDate("001", "2025-01-01")).thenReturn(1);

        ResponseEntity<String> response = federalHolidayController.deleteByCountryCodeAndHolidayDate("001", "2025-01-01");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1 records deleted for country code 001 and date 2025-01-01", response.getBody());
    }
    // Failure scenario for getAllHolidays
    @Test
    public void testGetAllHolidays_NoHolidaysFound() {
        when(federalHolidayService.getAllHolidays()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> federalHolidayController.getAllHolidays());
    }

    // Failure scenario for getHolidaysByCountryCode
    @Test
    public void testGetHolidaysByCountryCode_NoHolidaysFound() {
        when(federalHolidayService.getHolidaysByCountryCode("999")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> federalHolidayController.getHolidaysByCountryCode("999"));
    }

    // Failure scenario for addHoliday
    @Test
    public void testAddHoliday_DuplicateRecord() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        when(federalHolidayService.addHoliday(any(FederalHolidayRequest.class)))
                .thenThrow(new DuplicateRecordException("Duplicate record found"));

        assertThrows(DuplicateRecordException.class, () -> federalHolidayController.addHoliday(request));
    }

    @Test
    public void testAddHoliday_InvalidInput() {
        FederalHolidayRequest request = new FederalHolidayRequest("", "", "", "");
        when(federalHolidayService.addHoliday(any(FederalHolidayRequest.class)))
                .thenThrow(new InvalidInputException("Invalid input data"));

        assertThrows(InvalidInputException.class, () -> federalHolidayController.addHoliday(request));
    }

    // Failure scenario for updateHolidayByIdAndCountryCode
    @Test
    public void testUpdateHolidayByIdAndCountryCode_HolidayNotFound() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        when(federalHolidayService.updateHolidayByIdAndCountryCode(1L, "001", request))
                .thenThrow(new ResourceNotFoundException("Holiday not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayController.updateHolidayByIdAndCountryCode(1L, "001", request));
    }

    @Test
    public void testUpdateHolidayByIdAndCountryCode_DuplicateRecord() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        when(federalHolidayService.updateHolidayByIdAndCountryCode(1L, "001", request))
                .thenThrow(new DuplicateRecordException("Duplicate record found"));

        assertThrows(DuplicateRecordException.class, () ->
                federalHolidayController.updateHolidayByIdAndCountryCode(1L, "001", request));
    }

    // Failure scenario for updateHolidayByCountryCodeAndDate
    @Test
    public void testUpdateHolidayByCountryCodeAndDate_HolidayNotFound() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        when(federalHolidayService.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request))
                .thenThrow(new ResourceNotFoundException("Holiday not found"));

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayController.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request));
    }

    @Test
    public void testUpdateHolidayByCountryCodeAndDate_DuplicateRecord() {
        FederalHolidayRequest request = new FederalHolidayRequest("001", "USA", "2025-01-01", "New Year");
        when(federalHolidayService.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request))
                .thenThrow(new DuplicateRecordException("Duplicate record found"));

        assertThrows(DuplicateRecordException.class, () ->
                federalHolidayController.updateHolidayByCountryCodeAndDate("001", "2025-01-01", request));
    }

    // Failure scenario for uploadHolidays
    @Test
    public void testUploadHolidays_InvalidFileFormat() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Invalid data".getBytes());
        when(federalHolidayService.uploadHolidays(any(List.class)))
                .thenThrow(new InvalidInputException("Invalid file format"));

        assertThrows(InvalidInputException.class, () ->
                federalHolidayController.uploadHolidays(Collections.singletonList(file)));
    }

    @Test
    public void testUploadHolidays_EmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", new byte[0]);
        when(federalHolidayService.uploadHolidays(any(List.class)))
                .thenThrow(new InvalidInputException("File is empty"));

        assertThrows(InvalidInputException.class, () ->
                federalHolidayController.uploadHolidays(Collections.singletonList(file)));
    }

    // Failure scenario for deleteByCountryCode
    @Test
    public void testDeleteByCountryCode_NoRecordsFound() {
        when(federalHolidayService.deleteByCountryCode("999")).thenThrow(new ResourceNotFoundException("No records found"));

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayController.deleteByCountryCode("999"));
    }

    // Failure scenario for deleteByCountryCodeAndHolidayDate
    @Test
    public void testDeleteByCountryCodeAndHolidayDate_NoRecordsFound() {
        when(federalHolidayService.deleteByCountryCodeAndHolidayDate("999", "2025-01-01"))
                .thenThrow(new ResourceNotFoundException("No records found"));

        assertThrows(ResourceNotFoundException.class, () ->
                federalHolidayController.deleteByCountryCodeAndHolidayDate("999", "2025-01-01"));
    }

}