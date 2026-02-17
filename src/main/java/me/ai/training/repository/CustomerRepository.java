package me.ai.training.repository;

import me.ai.training.entity.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    Customer findByCustomerId(String customerId);
}
