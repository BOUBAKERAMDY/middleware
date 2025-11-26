package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import fr.unice.polytech.foodDelivery.domain.exception.CapacityExceededException;
import fr.unice.polytech.foodDelivery.domain.exception.MealNotInMenuException;
import fr.unice.polytech.foodDelivery.domain.exception.NoSuchRestaurantException;
import fr.unice.polytech.foodDelivery.domain.exception.PlanningException;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.repository.impl.InMemoryRestaurantRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RestaurantService {
    private InMemoryRestaurantRepository value = InMemoryRestaurantRepository.getInstance();

    public RestaurantService() {
    }

    public void clearData() {
        value.clear();
    }

    public void registerRestaurant(RestaurantAccount restaurant) {
        value.save(restaurant);
    }

    public List<RestaurantAccount> findAll() {
        return value.findAll();
    }

    public RestaurantAccount findByName(String name) {
        return value.findByName(name).orElse(null);
    }

    public Menu getRestaurantMenu(UUID restaurantId) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent())
            return optionalRestaurant.get().getMenu();
        else
            throw new NoSuchRestaurantException(restaurantId);
    }

    public InMemoryRestaurantRepository getRestaurantMemory() {
        return value;
    }

    public void addMealToMenu(UUID restaurantId, Meal meal) {
        value.addMealToMenu(restaurantId, meal);
    }

    public void removeMealFromMenu(UUID restaurant, Meal meal) {
        value.removeMealFromMenu(restaurant, meal);
    }

    public void updateMeal(UUID restaurant, Meal oldMeal, Meal newMeal) {
        this.removeMealFromMenu(restaurant, oldMeal);
        this.addMealToMenu(restaurant, newMeal);
    }

    public boolean isMealInMenu(UUID restaurant, Meal meal) {
        return value.isMealInMenu(restaurant, meal);
    }

    public Meal getMealByName(UUID restaurantId, String mealName) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent()) {
            Meal meal = optionalRestaurant.get().getMealByName(mealName);
            if (meal == null) {
                throw new MealNotInMenuException(mealName, restaurantId);
            }
            return meal;
        } else
            throw new NoSuchRestaurantException(restaurantId);
    }

    public void updatePublicInfo(UUID restaurantId, String publicInfo) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent())
            optionalRestaurant.get().setPublicInfo(publicInfo);
        else
            throw new NoSuchRestaurantException(restaurantId);
    }

    public void updateRestaurantName(UUID restaurantId, String name) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent())
            optionalRestaurant.get().setName(name);
        else
            throw new NoSuchRestaurantException(restaurantId);
    }

    public void addTimeSlot(UUID restaurant, String slotId, DayOfWeek day, LocalTime startTime,
            LocalTime endTime,
            int capacity) {
        if (capacity <= 0) {
            throw new PlanningException("Capacity must be positive");
        }
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent())
            optionalRestaurant.get().getPlanning().addTimeSlot(slotId, day, startTime, endTime,
                    capacity);
        else
            throw new NoSuchRestaurantException(restaurant);
    }

    public void updateSlotCapacity(UUID restaurant, String slotId, int newCapacity) {
        if (newCapacity < 0) {
            throw new PlanningException("Capacity cannot be negative");
        }
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent())
            optionalRestaurant.get().getPlanning().updateCapacity(slotId, newCapacity);
        else
            throw new NoSuchRestaurantException(restaurant);
    }

    public void removeTimeSlot(UUID restaurant, String slotId) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent())
            optionalRestaurant.get().getPlanning().removeTimeSlot(slotId);
        else
            throw new NoSuchRestaurantException(restaurant);
    }

    public boolean isSlotAvailable(UUID restaurant, String slotId) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent())
            return optionalRestaurant.get().getPlanning().isAvailable(slotId);
        else
            throw new NoSuchRestaurantException(restaurant);
    }

    public void validateOrderCapacity(UUID restaurant, String timeSlot) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent()) {
            if (!optionalRestaurant.get().getPlanning().isAvailable(timeSlot)) {
                throw new CapacityExceededException(timeSlot);
            }
        } else
            throw new NoSuchRestaurantException(restaurant);
    }

    public void reserveSlotForOrder(UUID restaurant, String timeSlot) {
        validateOrderCapacity(restaurant, timeSlot);
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent()) {
            Planning.TimeSlot slot = optionalRestaurant.get().getPlanning().getTimeSlot(timeSlot);
            if (slot != null) {
                slot.incrementOrders();
            }
        } else
            throw new NoSuchRestaurantException(restaurant);
    }

    public List<RestaurantAccount> searchForARestaurant(String name,
            List<CuisineType> cuisineTypes, RestaurantType restaurantType, boolean isOpen, int min, int max) {
        if (name != null) {
            List<RestaurantAccount> res = new ArrayList<>();
            value.findByName(name).ifPresent(res::add);
            return res;

        }
        List<RestaurantAccount> restaurants = value.findAll();
        if (cuisineTypes != null) {
            restaurants.retainAll(value.getByCuisineType(cuisineTypes));
        }
        if (restaurantType != null) {
            restaurants.retainAll(value.findByRestaurantType(restaurantType));
        }
        if (isOpen) {
            restaurants.retainAll(value.findByIsOpen());
        }
        if (min != 0 || max != 0) {
            restaurants.retainAll(value.findByPriceRange(min, max));
        }
        return restaurants;
    }

    public RestaurantAccount findById(UUID restaurantId) {
        return value.findById(restaurantId).orElse(null);
    }

    public Meal getMealById(UUID restaurantId, UUID mealId) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent()) {
            return optionalRestaurant.get().getMenu().getMeals().stream()
                    .filter(meal -> meal.getMealId().equals(mealId))
                    .findFirst()
                    .orElse(null);
        } else {
            throw new NoSuchRestaurantException(restaurantId);
        }
    }

    public void addTimeSlot(UUID restaurant, Planning planning) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurant);
        if (optionalRestaurant.isPresent()) {
            optionalRestaurant.get().setPlanning(planning);
        } else {
            throw new NoSuchRestaurantException(restaurant);
        }
    }

    public void updateRestaurant(UUID restaurantId, RestaurantAccount updatedRestaurant) {
        Optional<RestaurantAccount> optionalRestaurant = this.value.findById(restaurantId);
        if (optionalRestaurant.isPresent()) {
            RestaurantAccount restaurant = optionalRestaurant.get();
            if (updatedRestaurant.getName() != null) {
                restaurant.setName(updatedRestaurant.getName());
            }
            if (updatedRestaurant.getPublicInfo() != null) {
                restaurant.setPublicInfo(updatedRestaurant.getPublicInfo());
            }
        } else {
            throw new NoSuchRestaurantException(restaurantId);
        }
    }
}
