package com.edu.spring.ai.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Entity
@Table(name = "CUSTOMER")
@Data
public class Customer {
    @Id
    private String id;
    @Column(name = "customerid")
    private String customerId;
    private String name;
    private Double balance;
}
