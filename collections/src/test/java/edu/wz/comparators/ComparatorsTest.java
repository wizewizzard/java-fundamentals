package edu.wz.comparators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class ComparatorsTest {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Employee{
        int id;
        String name;
        LocalDate dateBirth;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Employee employee = (Employee) o;
            return id == employee.id && name.equals(employee.name) && dateBirth.equals(employee.dateBirth);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, dateBirth);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class EmployeeComparable implements Comparable<EmployeeComparable>{
        int id;
        String name;
        LocalDate dateBirth;

        @Override
        public int compareTo(EmployeeComparable that) {
            return this.id - that.id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EmployeeComparable employee = (EmployeeComparable) o;
            return id == employee.id && name.equals(employee.name) && dateBirth.equals(employee.dateBirth);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, dateBirth);
        }
    }

    private List<Employee> employeeData;
    private List<EmployeeComparable> employeeComparableData;

    @BeforeEach
    public void setUp(){
        employeeData = List.of(
                new Employee(1, "John", LocalDate.parse("1980-11-22", DateTimeFormatter.ISO_DATE)),
                new Employee(3, "Andrew", LocalDate.parse("1998-07-13", DateTimeFormatter.ISO_DATE)),
                new Employee(56, "Bill", LocalDate.parse("1985-05-02", DateTimeFormatter.ISO_DATE)),
                new Employee(4, "Lamar", LocalDate.parse("1991-04-13", DateTimeFormatter.ISO_DATE)));

        employeeComparableData = List.of(
                new EmployeeComparable(1, "John", LocalDate.parse("1980-11-22", DateTimeFormatter.ISO_DATE)),
                new EmployeeComparable(3, "Andrew", LocalDate.parse("1998-07-13", DateTimeFormatter.ISO_DATE)),
                new EmployeeComparable(56, "Bill", LocalDate.parse("1985-05-02", DateTimeFormatter.ISO_DATE)),
                new EmployeeComparable(4, "Lamar", LocalDate.parse("1991-04-13", DateTimeFormatter.ISO_DATE)));
    }

    /**
     * One cannot transform collection of objects that do not implement Comparable into TreeSet that requires either
     * Comparable objects or the Comparator that allows to compare objects.
     */
    @Test
    public void tryingToApplyComparisonAgainstObjectsThatDoNotInheritComparableLeadsToException(){

        Set<EmployeeComparable> sortedComparableEmployees = new TreeSet<>(employeeComparableData);
        Throwable throwable = catchThrowable(() -> new TreeSet<>(employeeData));

        assertThat(throwable)
                .isNotNull()
                .hasMessageContaining("Comparable");
        assertThat(sortedComparableEmployees)
                .containsExactly(new EmployeeComparable(1, "John", LocalDate.parse("1980-11-22", DateTimeFormatter.ISO_DATE)),
                        new EmployeeComparable(3, "Andrew", LocalDate.parse("1998-07-13", DateTimeFormatter.ISO_DATE)),
                        new EmployeeComparable(4, "Lamar", LocalDate.parse("1991-04-13", DateTimeFormatter.ISO_DATE)),
                        new EmployeeComparable(56, "Bill", LocalDate.parse("1985-05-02", DateTimeFormatter.ISO_DATE))
                );
    }

    /**
     * Using custom comparator to push data into TreeSet
     */
    @Test
    public void customComparatorsSortedThroughTreeSet(){
        Comparator<Employee> comparatorByDate = Comparator.comparing(Employee::getDateBirth, LocalDate::compareTo);

        Set<Employee> employeeTreeSet = new TreeSet<>(comparatorByDate);
        employeeTreeSet.addAll(employeeData);

        assertThat(employeeTreeSet).containsExactly(
                new Employee(1, "John", LocalDate.parse("1980-11-22", DateTimeFormatter.ISO_DATE)),
                new Employee(56, "Bill", LocalDate.parse("1985-05-02", DateTimeFormatter.ISO_DATE)),
                new Employee(4, "Lamar", LocalDate.parse("1991-04-13", DateTimeFormatter.ISO_DATE)),
                new Employee(3, "Andrew", LocalDate.parse("1998-07-13", DateTimeFormatter.ISO_DATE))
                );
    }

    /**
     * Using custom comparator to sort List collection
     */
    @Test
    public void customComparatorsSortedByCollectionSort(){
        List<Employee> employeeList = new ArrayList<>(employeeData);
        Comparator<Employee> comparatorByName = Comparator.comparing(Employee::getName);

        Collections.sort(employeeList, comparatorByName);

        assertThat(employeeList).containsExactly(
                new Employee(3, "Andrew", LocalDate.parse("1998-07-13", DateTimeFormatter.ISO_DATE)),
                new Employee(56, "Bill", LocalDate.parse("1985-05-02", DateTimeFormatter.ISO_DATE)),
                new Employee(1, "John", LocalDate.parse("1980-11-22", DateTimeFormatter.ISO_DATE)),
                new Employee(4, "Lamar", LocalDate.parse("1991-04-13", DateTimeFormatter.ISO_DATE))

        );
    }

    /**
     * Binary search can be applied only to a list, but not to any collection. For example standard set interface does not allow user to
     * access a specific element by its index that is why there's no point to apply BS. And it is understandable as a HashSet can not guarantee the ordering of elements.
     * But at the same time a sorted set representative of which treeset is in theory could provide such opportunity. Unfortunately it does not.
     */
    @Test
    public void binarySearchInSortedCollection(){
        List<Employee> employeeList = new ArrayList<>(employeeData);
        Comparator<Employee> comparatorByName = Comparator.comparing(Employee::getName);
        Collections.sort(employeeList, comparatorByName);

        int lamarIndex = Collections.binarySearch(employeeList, new Employee(9999, "Lamar", LocalDate.parse("1900-01-01")), comparatorByName);
        int luizaIndex = Collections.binarySearch(employeeList, new Employee(9999, "Luiza", LocalDate.parse("1900-01-01")), comparatorByName);

        assertThat(employeeList.get(lamarIndex).getName()).isEqualTo("Lamar");
        assertThat(luizaIndex).isLessThanOrEqualTo(-1);
    }


}
