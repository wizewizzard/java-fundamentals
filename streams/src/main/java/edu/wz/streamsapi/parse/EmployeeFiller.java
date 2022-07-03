package edu.wz.streamsapi.parse;

import com.fasterxml.jackson.databind.JsonNode;
import edu.wz.streamsapi.domain.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.BiConsumer;

public class EmployeeFiller implements BiConsumer<JsonNode, Employee> {

    @Override
    public void accept(JsonNode employeeNode, Employee employee) {
        employee.setId(employeeNode.get("_id").asText());
        employee.setName(employeeNode.get("name").asText());
        employee.setAge(employeeNode.get("age").asInt());
        employee.setPhone(employeeNode.get("phone").asText());
        employee.setSalary(BigDecimal.valueOf(employeeNode.get("salary").asInt()));
        employee.setHiredOn(LocalDate.parse(employeeNode.get("hiredOn").asText()));
        employee.setGender(Enum.valueOf(Employee.Gender.class, employeeNode.get("gender").asText().toUpperCase(Locale.ROOT)));
    }
}
