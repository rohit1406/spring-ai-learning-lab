package me.ai.training.tools;

import lombok.extern.slf4j.Slf4j;
import me.ai.training.entity.Customer;
import me.ai.training.repository.CustomerRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/16/2026
 *
 *
 */
@Service
@Slf4j
public class BankingTool {
    @Autowired
    private CustomerRepository customerRepository;

    @Tool(name = "customer_details",
        description = "fetch the customer details from the database table",
        returnDirect = true)
    public Customer getCustomerDetails(String customerId){
        log.debug("Fetching the customer details from the internal database for customer id {}", customerId);
        return customerRepository.findByCustomerId(customerId);
    }
}
