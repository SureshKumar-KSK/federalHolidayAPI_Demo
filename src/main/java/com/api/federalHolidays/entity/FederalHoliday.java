package com.api.federalHolidays.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "federal_holiday", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"countryCode", "holidayDate"})
})
public class FederalHoliday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String countryName;

    @NotNull
    private LocalDate holidayDate;

    @NotNull
    private String holidayName;

    @NotNull
    private String countryCode;

    private String dayOfWeek; // Populated by application or trigger

    @ManyToOne
    @JoinColumn(name = "countryCode", referencedColumnName = "countryCode", insertable = false, updatable = false)
    private Country country;

}
