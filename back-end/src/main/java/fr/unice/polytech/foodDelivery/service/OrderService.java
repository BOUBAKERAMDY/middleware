package fr.unice.polytech.foodDelivery.service;

import java.util.List;
import java.util.UUID;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.NoCurrentOrderException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.repository.OrderRepository;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryOrderRepository;

public class OrderService {

    private final OrderRepository orderRepository = InMemoryOrderRepository.getInstance();

    public OrderService() {
    }

    public Order createOrder(CustomerAccount customer, RestaurantAccount restaurant) {
        if (customer == null || restaurant == null) {
            throw new IllegalArgumentException("Customer and Restaurant cannot be null");
        }

        Order order = new Order(customer, restaurant);

        return orderRepository.save(order);
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }


        public void clearData() {
        orderRepository.clear();
    }

    public void addMealToOrder(Order order, Meal meal) {
        if (order == null) {
            throw new NoCurrentOrderException();
        }
        if (meal == null) {
            throw new IllegalArgumentException("Meal cannot be null");
        }

        // Validate meal belongs to restaurant menu - CORRIGÉ avec .equals()
        boolean mealInMenu = order.getRestaurant().getMenu().getMeals().stream()
                .anyMatch(m -> m.getMealId().equals(meal.getMealId())); // ← .equals() au lieu de ==

        if (!mealInMenu) {
            throw new MealNotInMenuException(meal.getName(), order.getRestaurant().getRestaurantId());
        }

        order.addMeal(meal);
        orderRepository.save(order);
    }

    public List<Order> getCustomerHistory(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }
}