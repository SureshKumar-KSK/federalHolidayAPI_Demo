package com.api.federalHolidays.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "country", uniqueConstraints = {
        @UniqueConstraint(columnNames = "countryCode"),
        @UniqueConstraint(columnNames = "countryName")
})
public class Country {

    @Id
    @NotNull
    @Size(min = 1, max = 3, message = "Country code must be 1 to 3 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Country code must be alphanumeric")
    private String countryCode;

    @NotNull
    private String countryName;
}