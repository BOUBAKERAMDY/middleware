package fr.unice.polytech.foodDelivery.stepDefs.userStories.context;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.service.*;

import java.util.*;

/**
 * Shared context for User Stories step definitions
 */
public class UserStoriesContext {
    // Services
    public ApplicationService appService = new ApplicationService();
    public RestaurantService restaurantService = new RestaurantService();
    public OrderService orderService = new OrderService();

    // Customers and restaurants
    public Map<String, CustomerAccount> customers = new HashMap<>();
    public List<RestaurantAccount> restaurants = restaurantService.getRestaurantMemory().findAll();
    public CustomerAccount currentCustomer;
    public RestaurantAccount currentRestaurant;

    // Orders
    public Order currentOrder;

    // Meals
    public Meal currentMeal;
    public Map<String, Meal> meals = new HashMap<>();

    // Filtering results
    public List<RestaurantAccount> filteredRestaurants = new ArrayList<>();

    // Delivery and time slots
    public List<String> deliveryAddresses = new ArrayList<>();
    public String selectedDeliveryAddress;
    public List<Planning.TimeSlot> availableTimeSlots = new ArrayList<>();
    public Planning.TimeSlot selectedTimeSlot;
    public Map<String, Integer> timeSlotCapacities = new HashMap<>();
    public Map<String, Integer> timeSlotReservations = new HashMap<>();

    // Payment
    public PaymentService paymentService = new PaymentService();
    public PaymentMethod paymentMethod;
    public boolean paymentSuccess;
    public String paymentStatus = "PENDING"; // PENDING, PAID, FAILED
    public double studentCredit;
    public List<Order> orderHistory = new ArrayList<>();

    // Exceptions
    public Exception caughtException;
    public String errorMessage;

    // Toppings
    public Map<String, List<Topping>> mealToppings = new HashMap<>();
    public List<String> selectedToppings = new ArrayList<>();

    // Dietary tags
    public Map<String, List<String>> mealTags = new HashMap<>();

    // Capacity management
    public Map<String, Map<String, Integer>> dailyCapacities = new HashMap<>();

    // Other tracking
    public int remainingCapacity;
    public boolean paymentRedirected;
    public String paymentMessage;

    public void reset() {
        this.customers.clear();
        this.currentCustomer = null;
        this.currentRestaurant = null;
        this.currentOrder = null;
        this.currentMeal = null;
        this.meals.clear();
        this.filteredRestaurants.clear();
        this.deliveryAddresses.clear();
        this.selectedDeliveryAddress = null;
        this.availableTimeSlots.clear();
        this.selectedTimeSlot = null;
        this.timeSlotCapacities.clear();
        this.timeSlotReservations.clear();
        this.paymentMethod = null;
        this.paymentSuccess = false;
        this.paymentStatus = "PENDING";
        this.studentCredit = 0.0;
        this.orderHistory.clear();
        this.caughtException = null;
        this.errorMessage = null;
        this.mealToppings.clear();
        this.selectedToppings.clear();
        this.mealTags.clear();
        this.dailyCapacities.clear();
        this.remainingCapacity = 0;
        this.paymentRedirected = false;
        this.paymentMessage = null;
    }
}
