package edu.wz.streamsapi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wz.streamsapi.parse.CompanyFiller;
import edu.wz.streamsapi.parse.EmployeeFiller;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class StreamsTest {
    private List<Company> companies;

    private final CompanyFiller companyFiller = new CompanyFiller();

    private final EmployeeFiller employeeFiller = new EmployeeFiller();

    @BeforeEach
    void init(){
        companies = new ArrayList<>();
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
            throw new RuntimeException("Error when reading file %s".formatted(filePath), e);
        }
    }

    /**
     * Finds first oldest employee for each company.
     * @result A map with the company id as a key and the oldest worker as a value
     */
    @Test
    public void findsOldestEmployeeInEachCompany(){

        Map<String, Employee> result = companies.stream()
                .collect(HashMap::new,
                        (m, company) ->
                                m.put(company.getId(), company.getEmployees().stream()
                                        .max(Comparator.comparingInt(Employee::getAge))
                                        .orElse(null)),
                        HashMap::putAll);

        assertThat(result).isNotNull().isNotEmpty().hasSize(companies.size());
        assertThat(result).allSatisfy((cId, employee) -> {
            Company company = companies.stream().filter(c -> Objects.equals(c.getId(), cId)).findFirst().orElseThrow();
            Employee actualEmployee = company.getEmployees().stream().max(Comparator.comparingInt(Employee::getAge)).orElse(null);
            assertThat(employee).isSameAs(actualEmployee);
        });
    }

    /**
     * Create a list of employees-novices that just started working in companies
     * @result A list of employees sorted by hiredOn field desc.
     */
    @Test
    public void findsFirstTenNewestWorkersAmongAllCompanies(){
        List<Employee> result = companies.stream()
                .map(Company::getEmployees)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Employee::getHiredOn, LocalDate::compareTo).reversed())
                .limit(10)
                .toList();

        assertThat(result)
                .isNotNull()
                .isNotEmpty()
                .hasSize(10);

        assertThat(result).isSortedAccordingTo(Comparator.comparing(Employee::getHiredOn, LocalDate::compareTo).reversed());
        result.forEach(System.out::println);
    }

    /**
     * Calculates how much employees of each company get paid on average
     * @result A map with the company id as a key and average salary in this company as a value
     */
    @Test
    public void calculatesHowMuchCompanyPaysOnAverage(){

        //when
        Map<String, BigDecimal> averageSalaries = companies.stream()
                .collect(Collectors.toMap(
                        Company::getId,
                        entry -> entry.getEmployees().stream()
                                .map(Employee::getSalary)
                                .collect(Averager::new, Averager::accept, Averager::combine)
                                .average()
                ));

        //then
        assertThat(averageSalaries).allSatisfy((cId, avg) -> {
            Company company = companies.stream().filter(c -> Objects.equals(c.getId(), cId)).findFirst().orElseThrow();
            BigDecimal total = company.getEmployees().stream()
                    .map(Employee::getSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal actualAvg = company.getEmployees().size() > 0 ?
                    total.divide(new BigDecimal(company.getEmployees().size()), RoundingMode.HALF_UP) : BigDecimal.ZERO;

            assertThat(avg).isEqualTo(actualAvg);
        });
    }

    /**
     * Calculates how much employees of each gender get paid on average in every company
     * @result A map with the company id as a key, and another map as value where gender is a key and avg salary is a value
     */
    @Test
    public void calculatesAverageSalariesInCompaniesForEveryGender(){
        Map<String, Map<Employee.Gender, BigDecimal>> avgsByGenders = companies.stream()
                .collect(Collectors.toMap(Company::getId,
                        company -> EnumSet.allOf(Employee.Gender.class).stream()
                        .parallel()
                        .collect(Collectors.toMap(
                                gender -> gender,
                                gender -> company.getEmployees().stream()
                                        .filter(employee -> employee.getGender().equals(gender))
                                        .map(Employee::getSalary)
                                        .collect(Averager::new, Averager::accept, Averager::combine)
                                        .average()
                        ))));
        //then
        assertThat(avgsByGenders)
                .isNotNull()
                .isNotEmpty()
                .hasSize(companies.size());
        assertThat(avgsByGenders).allSatisfy((cId, genderSalaries) -> {
            Company company = companies.stream().filter(c -> Objects.equals(c.getId(), cId)).findFirst().orElseThrow();
            for (Employee.Gender gender : EnumSet.allOf(Employee.Gender.class)){
                List<Employee> employeesOfGender = company.getEmployees().stream().filter(e -> e.getGender().equals(gender)).toList();
                BigDecimal total = BigDecimal.ZERO;
                for(Employee employee : employeesOfGender){
                    total = total.add(employee.getSalary());
                }
                BigDecimal avg = employeesOfGender.size() > 0 ?
                        total.divide(new BigDecimal(employeesOfGender.size()), RoundingMode.HALF_UP) : BigDecimal.ZERO;
                assertThat(genderSalaries.get(gender)).isEqualTo(avg);
            }
        });
    }

    @Data
    @AllArgsConstructor
    public static class Averager {
        private BigDecimal total;
        private int count;

        public Averager() {
            total = BigDecimal.ZERO;
            count = 0;
        }

        public BigDecimal average(){
            if(count > 0)
                return total.divide(new BigDecimal(count), RoundingMode.HALF_UP);
            else
                return BigDecimal.ZERO;
        }

        public void accept(BigDecimal salary){
            this.count ++;
            this.total = this.total.add(salary);
        }

        public Averager combine(Averager that){
            return new Averager(this.getTotal().add(that.getTotal()), this.count + that.getCount());
        }

    }

}
