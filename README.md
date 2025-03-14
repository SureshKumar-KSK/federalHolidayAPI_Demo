Federal Holiday Application API
==========================

The Federal Holiday Application API is a RESTful service that allows users to manage federal holidays for different countries. It provides functionalities to add, update, delete, and retrieve holiday data.The Federal Holiday Application API is a RESTful service that allows users to manage federal holidays for different countries. It provides functionalities to add, update, delete, and retrieve holiday data. The API ensures Of validating inputs (e.g., date format, country code).

## Table of Contents
* #### Features

* #### Technologies Used

* Setup Instructions

* API Documentation

* Examples

* Testing

**Features**

* **a new federal holiday with validation for country code, holiday name, and date.


* Update existing holidays by ID, country code, or date.


* Delete holidays by country code or date.


* Retrieve holidays by country code or list all holidays.


* Upload holidays from CSV or Excel files.


* Validation for date format, country code, and duplicate records.****

**Technologies Used**

* Java 18

* Spring Boot 3.4

* H2 Database (in-memory database for development)

* Gradle (build tool)

* Swagger/OpenAPI (API documentation)

* JUnit 5 and Mockito (unit testing)

* Apache POI (Excel file processing)

* Apache Commons CSV (CSV file processing)

## **Setup Instructions**

### Prerequisites

Java 18 or higher
Gradle 7.1 or higher

**Steps**
**Clone the repository**

**Build the project:**
./gradlew build

Run the application:

./gradlew bootRun

## API Documentation

Base URL

http://localhost:8080/api/federal-holidays


Endpoints
1. Add a Holiday
URL: /add

Method: POST

Request Body:


**Endpoints**
1. Add a Holiday
URL: /add

Method: POST


2.Update a Holiday by ID and Country Code
URL: /update/id/{id}/code/{countryCode}

Method: PUT


3. Delete Holidays by Country Code
URL: /delete/code/{countryCode}

Method: DELETE


4. Get All Holidays
URL: /getAll

Method: GET


5. Upload Holidays from File
URL: /upload

Method: POST

Request Body: Multipart file (CSV or Excel)
