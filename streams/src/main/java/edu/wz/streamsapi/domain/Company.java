package edu.wz.streamsapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    private String id;
    private String name;
    private List<Employee> employees;
    private String address;
    private String about;
    private double latitude;
    private double longitude;
}
