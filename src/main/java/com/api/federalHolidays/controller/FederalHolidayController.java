package com.api.federalHolidays.controller;

import com.api.federalHolidays.dto.FederalHolidayRequest;
import com.api.federalHolidays.dto.FederalHolidayResponse;
import com.api.federalHolidays.dto.FileUploadResponse;
import com.api.federalHolidays.exception.ResourceNotFoundException;
import com.api.federalHolidays.service.FederalHolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/federal-holidays")
@Tag(name = "Federal Holidays API", description = "API for managing federal holidays of countries")
public class FederalHolidayController {

    @Autowired
    private FederalHolidayService federalHolidayService;

    @Operation(summary = "Get all federal holidays", description = "Retrieve a list of all federal holidays")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<FederalHolidayResponse>> getAllHolidays() {
        List<FederalHolidayResponse> holidays = federalHolidayService.getAllHolidays();
        if (holidays.isEmpty()) {
            throw new ResourceNotFoundException("No holidays found.");
        }
        return new ResponseEntity<>(holidays, HttpStatus.OK);
    }

    @Operation(summary = "Get holidays by country code", description = "Retrieve a list of federal holidays for a specific country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/get/code/{countryCode}")
    public ResponseEntity<List<FederalHolidayResponse>> getHolidaysByCountryCode(
            @Parameter(description = "Country code (e.g., US, IN)", required = true)
            @PathVariable String countryCode) {
        List<FederalHolidayResponse> holidays = federalHolidayService.getHolidaysByCountryCode(countryCode);
        if (holidays.isEmpty()) {
            throw new ResourceNotFoundException("No holidays found for country code: " + countryCode);
        }
        return new ResponseEntity<>(holidays, HttpStatus.OK);
    }

    @Operation(summary = "Add a new federal holiday", description = "Add a new federal holiday for a country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Holiday successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Duplicate record"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add")
    public ResponseEntity<FederalHolidayResponse> addHoliday(
            @Parameter(description = "Federal holiday details", required = true)
            @Valid @RequestBody FederalHolidayRequest request) {
        FederalHolidayResponse response = federalHolidayService.addHoliday(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a holiday by ID and country code", description = "Update an existing federal holiday by its ID and country code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holiday successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Holiday not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate record"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/id/{id}/code/{countryCode}")
    public ResponseEntity<FederalHolidayResponse> updateHolidayByIdAndCountryCode(
            @Parameter(description = "ID of the holiday to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Country code of the holiday to update", required = true)
            @PathVariable String countryCode,
            @Parameter(description = "Updated holiday details", required = true)
            @Valid @RequestBody FederalHolidayRequest request) {
        FederalHolidayResponse response = federalHolidayService.updateHolidayByIdAndCountryCode(id, countryCode, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update a holiday by country code and date", description = "Update an existing federal holiday by its country code and holiday date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holiday successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Holiday not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate record"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/code/{countryCode}/date/{holidayDate}")
    public ResponseEntity<FederalHolidayResponse> updateHolidayByCountryCodeAndDate(
            @Parameter(description = "Country code of the holiday to update", required = true)
            @PathVariable String countryCode,

            @Parameter(description = "Holiday date of the holiday to update", required = true)
            @PathVariable String holidayDate,

            @Parameter(description = "Updated holiday details", required = true)
            @Valid @RequestBody FederalHolidayRequest request) {
        FederalHolidayResponse response = federalHolidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Upload holidays from multiple files", description = "Uploads holidays from multiple CSV or Excel files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadHolidays(@RequestParam("files")List<MultipartFile> files) {
        FileUploadResponse response = federalHolidayService.uploadHolidays(files);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete all holidays by country code", description = "Deletes all holidays for a specific country code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holidays deleted successfully"),
            @ApiResponse(responseCode = "404", description = "No holidays found for the country code")
    })
    @DeleteMapping("/delete/code/{countryCode}")
    public ResponseEntity<String> deleteByCountryCode(@PathVariable String countryCode) {
        int rowsDeleted = federalHolidayService.deleteByCountryCode(countryCode);
        return ResponseEntity.ok(rowsDeleted + " records deleted for country code " + countryCode);
    }

    @Operation(summary = "Delete holiday by country code and date", description = "Deletes a specific holiday for a country code and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Holiday deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Country code or holiday not found")
    })
    @DeleteMapping("/delete/code/{countryCode}/date/{holidayDate}")
    public ResponseEntity<String> deleteByCountryCodeAndHolidayDate(
            @PathVariable String countryCode,
            @PathVariable String holidayDate) {
        int rowsDeleted = federalHolidayService.deleteByCountryCodeAndHolidayDate(countryCode, holidayDate);
        return ResponseEntity.ok(rowsDeleted + " records deleted for country code " + countryCode + " and date " + holidayDate);
    }
}