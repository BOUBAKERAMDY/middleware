package fr.unice.polytech.foodDelivery.repository.impl;

import fr.unice.polytech.foodDelivery.domain.model.Order;
import fr.unice.polytech.foodDelivery.repository.OrderRepository;

import java.util.*;
import java.util.stream.Collectors;

public class

InMemoryOrderRepository implements OrderRepository {

    private static InMemoryOrderRepository instance = new InMemoryOrderRepository();

    private final Map<UUID, Order> orders = new HashMap<>();

    private InMemoryOrderRepository() {
    }

    public static OrderRepository getInstance() {
        return instance;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {

        return orders.values().stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomer().getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRestaurantId(UUID restaurantId) {
        return orders.values().stream()
                .filter(order -> order.getRestaurant().getRestaurantId().equals(restaurantId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    @Override
    public boolean deleteById(UUID orderId) {

        Optional<Order> orderToDelete = findById(orderId);
        if (orderToDelete.isPresent()) {
            orders.remove(orderToDelete.get().getOrderId());
            return true;
        }
        return false;
    }

    @Override
    public int count() {
        return orders.size();
    }

    @Override
    public void clear() {
        orders.clear();
    }
}
