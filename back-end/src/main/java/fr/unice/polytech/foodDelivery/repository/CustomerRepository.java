package fr.unice.polytech.foodDelivery.repository;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    CustomerAccount save(CustomerAccount customer);
    Optional<CustomerAccount> findById(UUID customerId);
    List<CustomerAccount> findAll();
    void clear();
}

