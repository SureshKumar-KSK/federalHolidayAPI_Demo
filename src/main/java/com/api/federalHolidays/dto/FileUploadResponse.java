package com.api.federalHolidays.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileUploadResponse {
    private List<FileResult> fileResults = new ArrayList<>();
    private String message;

    @Data
    public static class FileResult {
        private String fileName;
        private int totalRecords;
        private int successRecords;
        private int failedRecords;
        private int duplicateRecords;
        private List<FailedRecord> failedRecordsDetails;
        private List<DuplicateRecord> duplicateRecordsDetails;

        @Data
        public static class FailedRecord {
            private int rowNumber;
            private String errorMessage;
        }

        @Data
        public static class DuplicateRecord {
            private int rowNumber;
            private String errorMessage;
        }
    }
}