package com.itensis.javersdemo;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;

@JaversSpringDataAuditable
public interface CompanyRepository extends JpaRepository<Company, Integer> {
}
