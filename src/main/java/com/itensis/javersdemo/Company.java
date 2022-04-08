package com.itensis.javersdemo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Company {
    @Id
    private int id;

    @Column
    private String name;
}
