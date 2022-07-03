package edu.wz.streamsapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    public static enum Gender{
        MALE, FEMALE, OTHER
    }
    private String id;
    private String name;
    private int age;
    private LocalDate hiredOn;
    private String phone;
    private BigDecimal salary;
    private Gender gender;
}
