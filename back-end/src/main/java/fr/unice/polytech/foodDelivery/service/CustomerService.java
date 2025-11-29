package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.repository.CustomerRepository;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryCustomerRepository;

import java.util.List;
import java.util.UUID;

public class CustomerService {
    private final CustomerRepository customerRepository = InMemoryCustomerRepository.getInstance();

    public void registerCustomer(CustomerAccount customer) {
        customerRepository.save(customer);
    }

    public List<CustomerAccount> findAll() {
        return customerRepository.findAll();
    }

    public CustomerAccount findById(UUID customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    public void clearData() {
        customerRepository.clear();
    }
}
