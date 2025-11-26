package fr.unice.polytech.foodDelivery.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import fr.unice.polytech.foodDelivery.domain.ENUM.CuisineType;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.ENUM.RestaurantType;
import lombok.Getter;
import lombok.Setter;

public class RestaurantAccount implements OrderObserveur {
    @Getter
    private final UUID restaurantId;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String publicInfo;
    @Getter
    private Menu menu;
    @Getter
    @Setter
    private Planning planning;
    private List<Order> orders;
    @Getter
    private int[] priceRange;
    @Getter
    private List<CuisineType> cuisineTypes;
    @Getter
    private RestaurantType restaurantType;

    @JsonCreator
    public RestaurantAccount(@JsonProperty("name") String name) {
        this(name, new Menu());
    }

    public RestaurantAccount(String name, Menu menu) {
        this(name, menu, new ArrayList<CuisineType>(), RestaurantType.DEFAULT, new int[] { 0, 100 });
    }

    public RestaurantAccount(String name, Menu menu, List<CuisineType> cuisineType,
            RestaurantType restaurantType, int[] priceRange) {
        this.restaurantId = UUID.randomUUID();
        this.name = name;
        this.planning = new Planning();
        this.menu = menu;
        this.restaurantType = restaurantType;
        if (priceRange.length != 2 || priceRange[0] < 0 || priceRange[1] < priceRange[0]) {
            throw new IllegalArgumentException("Invalid price range");
        }
        this.priceRange = priceRange;
        this.cuisineTypes = cuisineType;
        this.orders = new LinkedList<>();
    }

    public void addToPlanning(String slotId, DayOfWeek day, LocalTime startTime, LocalTime endTime, int capacity) {
        if (this.planning == null) {
            this.planning = new Planning();
        }
        this.planning.addTimeSlot(slotId, day, startTime, endTime, capacity);
    }

    public void notifyNewOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public void addMeal(Meal meal) {
        this.menu.addMeal(meal);
    }

    public void removeMeal(Meal meal) {
        this.menu.removeMeal(meal);
    }

    public void removeMealByName(String mealName) {
        this.menu.removeMeal(this.menu.getMealByName(mealName));
    }

    public boolean propose(Meal meal) {
        return this.menu.containsMeal(meal);
    }

    public Meal getMealByName(String mealName) {
        return this.menu.getMealByName(mealName);
    }

    @Override
    public void onOrderStatusChange(Order order) {
        if (order.getStatus() == OrderStatus.PAID) {
            System.out.println(
                    "Restaurant " + this.name + " notified: Order " + order.getOrderId() + " has been paid.");
        }
    }

}
