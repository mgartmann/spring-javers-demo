package com.itensis.javersdemo;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Employee {
    @Id
    private int id;

    @Column
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
