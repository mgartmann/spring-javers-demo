package com.itensis.javersdemo;

import org.javers.core.Changes;
import org.javers.core.ChangesByCommit;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.javers.repository.jql.ShadowScope;
import org.javers.shadow.Shadow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class IntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private Javers javers;

    @Test
    public void javersHasInitialCommit() {
        // GIVEN
        Company company = new Company();
        company.setName("Company V1");
        Employee employee = new Employee();
        employee.setName("Employee V1");
        employee.setCompany(company);

        // WHEN
        companyRepository.save(company);
        employeeRepository.save(employee);

        // THEN
        QueryBuilder query = QueryBuilder.byInstance(employee);
        List<CdoSnapshot> snapshots = javers.findSnapshots(query.build());
        System.out.print(javers.getJsonConverter().toJson(snapshots));
    }

    @Test
    public void employeeHasNoNewVersionWhenCompanyIsUpdated() {
        // GIVEN
        Company company = new Company();
        company.setName("Company V1");
        Employee employee = new Employee();
        employee.setName("Employee V1");
        employee.setCompany(company);
        Company dbCompany = companyRepository.save(company);
        employeeRepository.save(employee);

        // WHEN
        dbCompany.setName("Company V2");
        companyRepository.save(dbCompany);

        // THEN
        QueryBuilder query = QueryBuilder.byInstance(employee);
        List<CdoSnapshot> snapshots = javers.findSnapshots(query.build());
        System.out.print(javers.getJsonConverter().toJson(snapshots));
        assertEquals(1, snapshots.size());
    }

    @Test
    public void normalDBEmployeeHasNewestVersionOfCompany() {
        // GIVEN
        Company company = new Company();
        company.setName("Company V1");
        Employee employee = new Employee();
        employee.setName("Employee V1");
        employee.setCompany(company);
        Company dbCompany = companyRepository.save(company);
        Employee dbEmployee = employeeRepository.save(employee);

        // WHEN
        dbCompany.setName("Company V2");
        companyRepository.save(dbCompany);

        // THEN
        Employee persistedEmployee = employeeRepository.getById(dbEmployee.getId());
        assertEquals("Company V2", persistedEmployee.getCompany().getName());
    }

    @Test
    public void employeeShadowHasInitialVersionOfCompany() {
        // GIVEN
        Company company = new Company();
        company.setName("Company V1");
        Employee employee = new Employee();
        employee.setName("Employee V1");
        employee.setCompany(company);
        Company dbCompany = companyRepository.save(company);
        Employee dbEmployee = employeeRepository.save(employee);

        // WHEN
        dbCompany.setName("Company V2");
        companyRepository.save(dbCompany);

        // THEN
        // Attention: ShadowScope.DEEP_PLUS might generate a lot of DB queries!
        QueryBuilder query = QueryBuilder.byInstance(dbEmployee).withShadowScope(ShadowScope.DEEP_PLUS);
        List<Shadow<Employee>> shadows = javers.findShadows(query.build());
        Employee shadowEmployee = shadows.get(0).get();
        assertEquals("Employee V1", shadowEmployee.getName());
        assertEquals("Company V1", shadowEmployee.getCompany().getName());
//        System.out.print(javers.getJsonConverter().toJson(shadows));
        Changes changes = javers.findChanges(QueryBuilder.byInstance(dbEmployee).build());
        System.out.print(javers.getJsonConverter().toJson(changes));
    }

    @Test
    public void changesOfEmployeeDoesNotContainChangesToReferencedObjects() {
        // GIVEN
        Company company = new Company();
        company.setName("Company V1");
        Employee employee = new Employee();
        employee.setName("Employee V1");
        employee.setCompany(company);
        Company dbCompany = companyRepository.save(company);
        Employee dbEmployee = employeeRepository.save(employee);

        // WHEN
        dbCompany.setName("Company V2");
        companyRepository.save(dbCompany);

        // THEN
        // Attention: ShadowScope.DEEP_PLUS might generate a lot of DB queries!
        Changes changes = javers.findChanges(QueryBuilder.byInstance(dbEmployee).build());
        List<ChangesByCommit> commitChanges = changes.groupByCommit();
        assertEquals(1, commitChanges.size()); // only initial commit which created employee
        System.out.print(javers.getJsonConverter().toJson(commitChanges));
    }
}
