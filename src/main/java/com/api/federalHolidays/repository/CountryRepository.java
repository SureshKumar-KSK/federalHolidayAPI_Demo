package com.api.federalHolidays.repository;

import com.api.federalHolidays.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, String> {
    Optional<Country> findByCountryCode(String countryCode);

    boolean existsByCountryCode(String countryCode);

    boolean existsByCountryName(String countryName);

    Optional<Country> findByCountryName(String countryName);

    Optional<Country> findByCountryCodeAndCountryName(String countryCode, String countryName);

    boolean existsByCountryCodeAndCountryName(String countryCode, String countryName);
}

