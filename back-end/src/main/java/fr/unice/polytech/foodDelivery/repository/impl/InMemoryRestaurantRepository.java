package fr.unice.polytech.foodDelivery.repository.impl;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.domain.model.RestaurantAccount;
import fr.unice.polytech.foodDelivery.repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryRestaurantRepository implements RestaurantRepository {

    private static InMemoryRestaurantRepository instance = new InMemoryRestaurantRepository();

    private final Map<UUID, RestaurantAccount> restaurants = new HashMap<>();

    private InMemoryRestaurantRepository() {
    }

    public static InMemoryRestaurantRepository getInstance() {
        return instance;
    }

    public void addMealToMenu(UUID name, Meal meal) {
        Optional<RestaurantAccount> restaurantOpt = findById(name);
        if (restaurantOpt.isPresent()) {
            RestaurantAccount restaurant = restaurantOpt.get();
            restaurant.addMeal(meal);
        }
    }

    public void removeMealFromMenu(UUID name, Meal meal) {
        Optional<RestaurantAccount> restaurantOpt = findById(name);
        if (restaurantOpt.isPresent()) {
            RestaurantAccount restaurant = restaurantOpt.get();
            restaurant.removeMeal(meal);
        }
    }

    public boolean isMealInMenu(UUID restaurantName, Meal meal) {
        Optional<RestaurantAccount> restaurantOpt = findById(restaurantName);
        return restaurantOpt.map(r -> r.propose(meal)).orElse(false);
    }

    @Override
    public RestaurantAccount save(RestaurantAccount restaurant) {
        restaurants.put(restaurant.getRestaurantId(), restaurant);
        return restaurant;
    }

    @Override
    public Optional<RestaurantAccount> findById(UUID id) {
        return Optional.ofNullable(restaurants.get(id));
    }

    @Override
    public Optional<RestaurantAccount> findByName(String name) {
        return restaurants.values().stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<RestaurantAccount> findAll() {
        return new ArrayList<>(restaurants.values());
    }

    @Override
    public void delete(UUID id) {
        restaurants.remove(id);
    }

    @Override
    public List<RestaurantAccount> getByCuisineType(List<CuisineType> cuisineTypes) {
        return restaurants.values().stream()
                .filter(r -> r.getCuisineTypes() != null &&
                        !r.getCuisineTypes().isEmpty() &&
                        cuisineTypes.stream().anyMatch(ct -> r.getCuisineTypes().contains(ct)))
                .toList();
    }

    @Override
    public List<RestaurantAccount> findByRestaurantType(RestaurantType type) {
        return restaurants.values().stream()
                .filter(r -> r.getRestaurantType() == type)
                .toList();
    }

    @Override
    public List<RestaurantAccount> findByIsOpen() {
        return restaurants.values().stream()
                .filter(r -> r.getPlanning() != null && r.getPlanning().isOpen())
                .toList();
    }

    @Override
    public List<RestaurantAccount> findByPriceRange(int minPrice, int maxPrice) {
        return restaurants.values().stream()
                .filter(r -> {
                    int[] priceRange = r.getPriceRange();
                    return priceRange != null &&
                            priceRange[0] >= minPrice &&
                            priceRange[1] <= maxPrice;
                })
                .toList();
    }

    @Override
    public int count() {
        return restaurants.size();
    }

    public void clear() {
        restaurants.clear();
    }
}
