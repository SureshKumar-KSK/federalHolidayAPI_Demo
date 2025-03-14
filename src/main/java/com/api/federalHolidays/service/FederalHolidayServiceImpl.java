package com.api.federalHolidays.service;

import com.api.federalHolidays.dto.FederalHolidayRequest;
import com.api.federalHolidays.dto.FederalHolidayResponse;
import com.api.federalHolidays.dto.FileUploadResponse;
import com.api.federalHolidays.entity.Country;
import com.api.federalHolidays.entity.FederalHoliday;
import com.api.federalHolidays.exception.CustomException;
import com.api.federalHolidays.exception.DuplicateRecordException;
import com.api.federalHolidays.exception.ResourceNotFoundException;
import com.api.federalHolidays.repository.CountryRepository;
import com.api.federalHolidays.repository.FederalHolidayRepository;
import com.api.federalHolidays.util.DateValidator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FederalHolidayServiceImpl implements FederalHolidayService {

    @Autowired
    private FederalHolidayRepository federalHolidayRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Override
    public List<FederalHolidayResponse> getAllHolidays() {
        return federalHolidayRepository.findAll().stream()
                .map(this::getHoliday_mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FederalHolidayResponse> getHolidaysByCountryCode(String countryCode) {
        List<FederalHoliday> holidays = federalHolidayRepository.findByCountryCode(countryCode);
        if (holidays.isEmpty()) {
            throw new CustomException("No holidays found for country code: " + countryCode, HttpStatus.NOT_FOUND);
        }
        return holidays.stream().map(this::getHoliday_mapToResponse).toList();
    }

    @Transactional
    @Override
    public FederalHolidayResponse addHoliday(FederalHolidayRequest request) {

        // Validate Request
        FederalHoliday holiday = new FederalHoliday();
        String message="Holiday added successfully"; // Add the success message

        validateRequest(request, holiday);
        //save to entity
        FederalHoliday savedHoliday = mapToEntityAndSave(request, holiday);
        return mapToResponse(savedHoliday,message);

    }

    // Update Holiday Date & Holiday name
    @Override
    @Transactional
    public FederalHolidayResponse updateHolidayByIdAndCountryCode(Long id, String countryCode, FederalHolidayRequest request) {
        //check id & country code exist in federal holiday table
        FederalHoliday holiday = federalHolidayRepository.findByIdAndCountryCode(id, countryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found with ID: " + id + " and country code: " + countryCode));
        validateUpdateRequest(request);
        LocalDate holidayParsedDate = DateValidator.validateAndParseDate(request.getHolidayDate());
        // Check if the new holiday name or date already exists for the country
        if (isHolidayNameOrDateExistsForCountry(countryCode, request.getHolidayName(), holidayParsedDate)) {
            throw new CustomException("Holiday name or date already exists for the country", HttpStatus.CONFLICT);
        }
        FederalHoliday updatedHoliday = mapToEntityAndSave(request, holiday);// set to an entity and save
        String message="Holiday updated successfully";
        return mapToResponse(updatedHoliday,message);
    }

    // Update Holiday Name
    @Override
    @Transactional
    public FederalHolidayResponse updateHolidayByCountryCodeAndDate(String countryCode, String holidayDate, FederalHolidayRequest request) {
        // date validation
        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);
        // check country code & holiday date exist in federal holiday table
        FederalHoliday holiday = federalHolidayRepository.findByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Holiday not found with country code: " + countryCode + " and date: " + holidayDate));
        validateUpdateRequest(request);
        LocalDate holidayParsedDate = DateValidator.validateAndParseDate(request.getHolidayDate());
        // Check if the new holiday name or date already exists for the country
        if (isHolidayNameOrDateExistsForCountry(countryCode, request.getHolidayName(), holidayParsedDate)) {
            throw new CustomException("Holiday name or date already exists for the country", HttpStatus.CONFLICT);
        }
        FederalHoliday updatedHoliday = mapToEntityAndSave(request, holiday);
        String message="Holiday updated successfully";
        return mapToResponse(updatedHoliday, message);
    }

    @Override
    @Transactional
    public int deleteByCountryCode(String countryCode) {

        if (!federalHolidayRepository.existsByCountryCode(countryCode)) {
            throw new CustomException("No holidays found for country code: " + countryCode, HttpStatus.NOT_FOUND);
        }

        int deleteRecordCount = federalHolidayRepository.deleteByCountryCode(countryCode);

        long count = federalHolidayRepository.findByCountryCode(countryCode).size();

        if (count == 0) {
            countryRepository.deleteById(countryCode);
        }
        return deleteRecordCount;
    }

    @Override
    @Transactional
    public int deleteByCountryCodeAndHolidayDate(String countryCode, String holidayDate) {

        // Validate country code and name
        Country country = countryRepository.findById(countryCode)
                .orElseThrow(() -> new CustomException("Country code not found: "+ countryCode, HttpStatus.NOT_FOUND));


        LocalDate parsedHolidayDate = DateValidator.validateAndParseDate(holidayDate);

        if (!federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate)) {
            throw new CustomException("No holiday found for country code " + countryCode + " on date " + holidayDate, HttpStatus.NOT_FOUND);
        }
        int deleteCount = federalHolidayRepository.deleteByCountryCodeAndHolidayDate(countryCode, parsedHolidayDate);
        long count = federalHolidayRepository.findByCountryCode(countryCode).size();

        if (count == 0) {
            countryRepository.deleteById(countryCode);
        }
        return deleteCount;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadHolidays(List<MultipartFile> files) {
        FileUploadResponse response = new FileUploadResponse();
        response.setMessage("Files processed successfully");
        if (files == null || files.isEmpty()) {
            response.setMessage("No files uploaded. Please upload at least one file.");
            return response;
        }

        for (MultipartFile file : files) {
            FileUploadResponse.FileResult fileResult = new FileUploadResponse.FileResult();
            fileResult.setFileName(file.getOriginalFilename());

            List<FileUploadResponse.FileResult.FailedRecord> failedRecords = new ArrayList<>();
            List<FileUploadResponse.FileResult.DuplicateRecord> duplicateRecords = new ArrayList<>();
            Set<String> uniqueKeys = new HashSet<>();
            int successRecords = 0;

            try {
                if (file.getOriginalFilename().endsWith(".csv")) {
                    // Process CSV file
                    try (Reader reader = new InputStreamReader(file.getInputStream());
                         CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                        for (CSVRecord csvRecord : csvParser) {
                            processRecord(csvRecord.toList(), fileResult, failedRecords, duplicateRecords, uniqueKeys);
                        }
                    }
                } else if (file.getOriginalFilename().endsWith(".xlsx")) {
                    // Process Excel file
                    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                        Sheet sheet = workbook.getSheetAt(0);
                        for (Row row : sheet) {
                            if (row.getRowNum() == 0) continue; // Skip header row
                            List<String> rowData = new ArrayList<>();
                            for (Cell cell : row) {
                                if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                                    // Convert Excel date to LocalDate
                                    LocalDate date = cell.getDateCellValue().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                    rowData.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                                } else {
                                    rowData.add(cell.toString());
                                }
                            }
                            processRecord(rowData, fileResult, failedRecords, duplicateRecords, uniqueKeys);
                        }
                    }
                } else {
                    throw new CustomException("Unsupported file format. Only CSV and Excel files are allowed.", HttpStatus.BAD_REQUEST);
                }
            } catch (IOException e) {
                throw new CustomException("Failed to process the file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            //fileResult.setSuccessRecords(successRecords);
            fileResult.setFailedRecords(failedRecords.size());
            fileResult.setDuplicateRecords(duplicateRecords.size());
            fileResult.setFailedRecordsDetails(failedRecords);
            fileResult.setDuplicateRecordsDetails(duplicateRecords);

            response.getFileResults().add(fileResult);
        }

        return response;
    }

    private void processRecord(List<String> record, FileUploadResponse.FileResult fileResult, List<FileUploadResponse.FileResult.FailedRecord> failedRecords, List<FileUploadResponse.FileResult.DuplicateRecord> duplicateRecords, Set<String> uniqueKeys) {
        fileResult.setTotalRecords(fileResult.getTotalRecords() + 1);
        try {
// Check if the row has the expected number of columns
            if (record.size() < 4) {
                throw new CustomException("Row has missing data. Expected 4 columns, found " + record.size(), HttpStatus.BAD_REQUEST);
            }
            FederalHolidayRequest request = new FederalHolidayRequest();
            request.setCountryCode(record.get(0).trim());
            request.setCountryName(record.get(1).trim());
            request.setHolidayDate(record.get(2).trim());
            request.setHolidayName(record.get(3).trim());

            /*//validateField null,empty, blank
            validateField(request.getCountryCode(), "Country code");
            validateField(request.getCountryName(), "Country name");
            validateField(request.getHolidayDate(), "Holiday date");
            validateField(request.getHolidayName(), "Holiday name");*/

            FederalHoliday holiday = new FederalHoliday();
            validateRequest(request, holiday);
            mapToEntityAndSave(request, holiday);

            // Increment success records
            fileResult.setSuccessRecords(fileResult.getSuccessRecords() + 1);
        } catch (Exception e) {
            if (e.getMessage().contains("Duplicate")) {
                FileUploadResponse.FileResult.DuplicateRecord duplicateRecord = new FileUploadResponse.FileResult.DuplicateRecord();
                duplicateRecord.setRowNumber(fileResult.getTotalRecords());
                duplicateRecord.setErrorMessage(e.getMessage());
                duplicateRecords.add(duplicateRecord);
            } else {
                FileUploadResponse.FileResult.FailedRecord failedRecord = new FileUploadResponse.FileResult.FailedRecord();
                failedRecord.setRowNumber(fileResult.getTotalRecords());
                failedRecord.setErrorMessage(e.getMessage());
                failedRecords.add(failedRecord);
            }
        }
    }

    private void getOrCreateCountry(FederalHoliday holiday, String countryCode, String countryName) {

        // get or create new country
        Optional<Country> existingCountry = countryRepository.findByCountryCode(countryCode);
        if (existingCountry.isPresent()) {
            // If the country code exists, check if the country name matches
            if (!existingCountry.get().getCountryName().equals(countryName)) {
                throw new CustomException("Country name does not match the existing record for country code: " + existingCountry.get().getCountryCode(), HttpStatus.BAD_REQUEST);

            }
        } else {
            // If the country code does not exist, check if the country name already exists
            if (countryRepository.existsByCountryName(countryName)) {
                throw new CustomException("Country code does not match the existing record for country name: "+ countryName, HttpStatus.BAD_REQUEST);
            }

            // Save the country if it doesn't exist
            Country country = new Country();
            country.setCountryCode(countryCode);
            country.setCountryName(countryName);
            countryRepository.save(country);
            holiday.setCountry(country);
        }
    }

    private void validateRequest(FederalHolidayRequest request, FederalHoliday holiday) {

        if (request.getCountryCode() == null || request.getCountryCode().trim().isEmpty()) {
                throw new CustomException("Country code is required.",HttpStatus.BAD_REQUEST);
            }
            if (request.getCountryName() == null || request.getCountryName().trim().isEmpty()) {
                throw new CustomException("Country name is required.",HttpStatus.BAD_REQUEST);
            }
            if (request.getHolidayName() == null || request.getHolidayName().trim().isEmpty()) {
                throw new CustomException("Holiday name is required.",HttpStatus.BAD_REQUEST);
            }
            if (request.getHolidayDate() == null) {
                throw new CustomException("Holiday date is required.",HttpStatus.BAD_REQUEST);
            }

        //Validate Country code format
        if (!request.getCountryCode().matches("^[a-zA-Z0-9]{1,3}$")) {
            throw new CustomException("Country code must be 1 to 3 alphanumeric characters. Provided: " + request.getCountryCode() + ".", HttpStatus.BAD_REQUEST);
        }
        // valid holiday date format & current year
        LocalDate holidayDate = DateValidator.validateAndParseDate(request.getHolidayDate());

        //get or create new country
        getOrCreateCountry(holiday, request.getCountryCode(), request.getCountryName());
        // Validate unique constraints
        if (federalHolidayRepository.existsByCountryCodeAndHolidayDate(request.getCountryCode(), holidayDate)) {
            throw new CustomException("Duplicate holiday record for country code: " + request.getCountryCode() + " and date: " + request.getHolidayDate(),HttpStatus.CONFLICT);
        }
        // Check for duplicate records
        if (federalHolidayRepository.existsByCountryCodeAndHolidayName(request.getCountryCode(), request.getHolidayName())) {
            throw new CustomException("Duplicate holiday record for country code: " + request.getCountryCode() + " and name: " + request.getHolidayName(),HttpStatus.CONFLICT);
        }
    }

    private FederalHoliday mapToEntityAndSave(FederalHolidayRequest request, FederalHoliday holiday) {
        FederalHoliday savedHoliday;
        LocalDate holidayDate = DateValidator.validateAndParseDate(request.getHolidayDate());
        holiday.setCountryCode(request.getCountryCode());
        holiday.setCountryName(request.getCountryName());
        holiday.setHolidayName(request.getHolidayName());
        holiday.setHolidayDate(holidayDate);
        // day of the week
        String dayOfWeek = holidayDate.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        holiday.setDayOfWeek(dayOfWeek);
        try {
            savedHoliday = federalHolidayRepository.save(holiday);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateRecordException(" Duplicate Record: A holiday with the same country code,  name and date already exists.");
        }
        return savedHoliday;
    }


    private FederalHolidayResponse mapToResponse(FederalHoliday holiday , String message) {
        FederalHolidayResponse response = new FederalHolidayResponse();
        response.setId(holiday.getId());
        response.setCountryCode(holiday.getCountryCode());
        response.setCountryName(holiday.getCountryName());
        response.setHolidayName(holiday.getHolidayName());
        response.setHolidayDate(holiday.getHolidayDate());
        response.setMessage(message); // Add the success message
        return response;
    }

    private FederalHolidayResponse getHoliday_mapToResponse(FederalHoliday holiday) {
        FederalHolidayResponse response = new FederalHolidayResponse();
        response.setId(holiday.getId());
        response.setCountryCode(holiday.getCountryCode());
        response.setCountryName(holiday.getCountryName());
        response.setHolidayName(holiday.getHolidayName());
        response.setHolidayDate(holiday.getHolidayDate());
        response.setMessage("Holiday fetched successfully"); // Add the success message
        return response;
    }

    private String validateField(String fieldValue, String fieldName) {
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            throw new CustomException(fieldName + " cannot be empty, null, or blank", HttpStatus.BAD_REQUEST);
        }
        return fieldValue.trim();
    }

    private boolean isHolidayNameOrDateExistsForCountry(String countryCode, String holidayName, LocalDate holidayDate) {
        // Check if the holiday name already exists for the country
        boolean isHolidayNameExists = federalHolidayRepository.existsByCountryCodeAndHolidayName(countryCode, holidayName);

        // Check if the holiday date already exists for the country
        boolean isHolidayDateExists = federalHolidayRepository.existsByCountryCodeAndHolidayDate(countryCode, holidayDate);

        // Return true if either the holiday name or date already exists
        return isHolidayNameExists || isHolidayDateExists;
    }

    private void validateUpdateRequest(FederalHolidayRequest request) {
        // Validate country code and name
        Country country = countryRepository.findById(request.getCountryCode())
                .orElseThrow(() -> new CustomException("Country code not found: " +request.getCountryCode() , HttpStatus.BAD_REQUEST));

        if (!country.getCountryName().equals(request.getCountryName())) {
            throw new CustomException("Country name: " + request.getCountryName() + " does not match the country code", HttpStatus.BAD_REQUEST);
        }

    }
}
