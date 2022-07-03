package edu.wz.streamsapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wz.streamsapi.parse.CompanyFiller;
import edu.wz.streamsapi.parse.EmployeeFiller;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class JsonParseTest {

    private final List<Company> companies = new ArrayList<>();

    private final CompanyFiller companyFiller = new CompanyFiller();

    private final EmployeeFiller employeeFiller = new EmployeeFiller();

    @Test
    public void testJsonToObjectMapping(){

        String filePath = "data.json";
        URL resource = JsonParseTest.class.getClassLoader().getResource(filePath);
        try {
            Objects.requireNonNull(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(reader);

            if (jsonNode.isArray()) {
                jsonNode.elements().forEachRemaining(companyNode -> {
                    Company company = new Company();
                    companyFiller.accept(companyNode, company);
                    company.setEmployees(new ArrayList<>());
                    companyNode.get("employees").elements().forEachRemaining(employeeNode -> {
                        Employee employee = new Employee();
                        employeeFiller.accept(employeeNode, employee);
                        company.getEmployees().add(employee);
                    });
                    companies.add(company);
                });
            }
            else{
                throw new RuntimeException("Unexpected data format");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        assertThat(companies).hasSizeGreaterThan(0);
        assertThat(companies).allSatisfy(company -> {
            assertThat(company.getId()).isNotNull();
            assertThat(company.getName()).isNotNull();
            assertThat(company.getAddress()).isNotNull();
            assertThat(company.getAbout()).isNotNull();
            assertThat(company.getEmployees())
                    .hasSizeGreaterThan(0)
                    .allSatisfy(employee -> {
                        assertThat(employee.getId()).isNotNull();
                        assertThat(employee.getName()).isNotNull();
                        assertThat(employee.getAge()).isGreaterThan(0);
                        assertThat(employee.getHiredOn()).isAfterOrEqualTo(LocalDate.of(2014, 1, 1));
                        assertThat(employee.getGender()).isNotNull();
                        assertThat(employee.getSalary()).isNotNull();
                    });
            });
    }

}