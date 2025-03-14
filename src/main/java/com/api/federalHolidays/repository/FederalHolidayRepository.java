package com.api.federalHolidays.repository;

import com.api.federalHolidays.entity.FederalHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FederalHolidayRepository extends JpaRepository<FederalHoliday, Long> {

    // holidays by country code
    List<FederalHoliday> findByCountryCode(String countryCode);

    // holiday by country code and holiday date
    Optional<FederalHoliday> findByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);

    // holiday by ID and country code
    Optional<FederalHoliday> findByIdAndCountryCode(Long id, String countryCode);

    // Check if a holiday exists with the same country code, country name and holiday date
    boolean existsByCountryCodeAndHolidayName(String countryCode, String holidayName);

    //void deleteByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);

    boolean existsByCountryCode(String countryCode);

    boolean existsByCountryName(String countryName);

    //void deleteByCountryCode(String countryCode);

    void deleteByCountryName(String countryName);

    boolean existsByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);

    @Modifying
    @Query("DELETE FROM FederalHoliday f WHERE f.countryCode = :countryCode")
    int deleteByCountryCode(String countryCode);

    @Modifying
    @Query("DELETE FROM FederalHoliday f WHERE f.countryCode = :countryCode AND f.holidayDate = :holidayDate")
    int deleteByCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
}