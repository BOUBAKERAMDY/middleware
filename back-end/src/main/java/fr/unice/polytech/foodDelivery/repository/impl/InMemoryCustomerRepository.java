package fr.unice.polytech.foodDelivery.repository.impl;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.repository.CustomerRepository;

import java.util.*;

public class InMemoryCustomerRepository implements CustomerRepository {
    private static InMemoryCustomerRepository instance;
    private final Map<UUID, CustomerAccount> customers = new HashMap<>();

    private InMemoryCustomerRepository() {
    }

    public static InMemoryCustomerRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryCustomerRepository();
        }
        return instance;
    }

    @Override
    public CustomerAccount save(CustomerAccount customer) {
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    @Override
    public Optional<CustomerAccount> findById(UUID customerId) {
        return Optional.ofNullable(customers.get(customerId));
    }

    @Override
    public List<CustomerAccount> findAll() {
        return new ArrayList<>(customers.values());
    }

    @Override
    public void clear() {
        customers.clear();
    }
}

