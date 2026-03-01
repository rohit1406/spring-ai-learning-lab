package com.edu.spring.ai.repository;

import com.edu.spring.ai.entity.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, String> {
    Customer findByCustomerId(String customerId);
}
