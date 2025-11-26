package fr.unice.polytech.foodDelivery.repository;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing RestaurantAccount entities
 */
public interface RestaurantRepository {

    /**
     * Save a restaurant
     * @param restaurant the restaurant to save
     * @return the saved restaurant
     */
    RestaurantAccount save(RestaurantAccount restaurant);

    /**
     * Find a restaurant by ID
     * @param id the restaurant ID
     * @return Optional containing the restaurant if found
     */
    Optional<RestaurantAccount> findById(UUID id);

    /**
     * Find a restaurant by name
     * @param name the restaurant name
     * @return Optional containing the restaurant if found
     */
    Optional<RestaurantAccount> findByName(String name);

    /**
     * Find all restaurants
     * @return list of all restaurants
     */
    List<RestaurantAccount> findAll();

    /**
     * Delete a restaurant
     * @param id the restaurant ID to delete
     */
    void delete(UUID id);

    /**
     * Find restaurants by cuisine type
     * @param cuisineTypes list of cuisine types to search for
     * @return list of restaurants matching the cuisine types
     */
    List<RestaurantAccount> getByCuisineType(List<CuisineType> cuisineTypes);

    /**
     * Find restaurants by restaurant type
     * @param type the restaurant type
     * @return list of restaurants of the given type
     */
    List<RestaurantAccount> findByRestaurantType(RestaurantType type);

    /**
     * Find restaurants that are currently open
     * @return list of open restaurants
     */
    List<RestaurantAccount> findByIsOpen();

    /**
     * Find restaurants within a price range
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of restaurants within the price range
     */
    List<RestaurantAccount> findByPriceRange(int minPrice, int maxPrice);

    /**
     * Count total number of restaurants
     * @return total count
     */
    int count();
}

