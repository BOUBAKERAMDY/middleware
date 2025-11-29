package fr.unice.polytech.foodDelivery.repository;

import fr.unice.polytech.foodDelivery.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order persistence operations
 * Following Repository Pattern for separation of concerns
 */
public interface OrderRepository {

    /**
     * Save or update an order
     * 
     * @param order the order to save
     * @return the saved order
     */
    Order save(Order order);

    /**
     * Find an order by its ID
     * 
     * @param orderId the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(UUID orderId);

    /**
     * Find all orders for a specific customer
     * 
     * @param customerId the customer ID
     * @return list of orders
     */
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Find all orders for a specific restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of orders
     */
    List<Order> findByRestaurantId(UUID restaurantId);

    /**
     * Find all orders
     * 
     * @return list of all orders
     */
    List<Order> findAll();

    /**
     * Delete an order by ID
     * 
     * @param orderId the order ID
     * @return true if deleted, false if not found
     */
    boolean deleteById(UUID orderId);

    /**
     * Count total number of orders
     * 
     * @return the count
     */
    int count();

    /**
     * Clear the repository
     */
    void clear();

}
