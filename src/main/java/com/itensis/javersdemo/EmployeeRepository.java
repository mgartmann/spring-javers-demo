package com.itensis.javersdemo;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

@JaversSpringDataAuditable
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
