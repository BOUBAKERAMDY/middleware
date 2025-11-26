package fr.unice.polytech.foodDelivery.stepDefs.backend;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.domain.model.*;
import fr.unice.polytech.foodDelivery.service.OrderService;
import fr.unice.polytech.foodDelivery.service.PaymentService;

import java.util.HashMap;
import java.util.Map;

public class TestContext {
    public CustomerAccount customer;
    public RestaurantAccount restaurant;
    public Order order;
    public Meal selectedMeal;
    public PaymentService paymentService;
    public PaymentMethod paymentMethod;
    public double initialAllowance;
    public boolean paiementReussi;
    public Planning.TimeSlot selectedTimeSlot;
    public String selectedTimeSlotId;
    public OrderService orderService;

    public Map<String, Integer> timeSlotReservations = new HashMap<>();

    public void reset() {
        this.customer = null;
        this.restaurant = null;
        this.order = null;
        this.selectedMeal = null;
        this.paymentService = null;
        this.paymentMethod = null;
        this.initialAllowance = 0.0;
        this.paiementReussi = false;
        this.selectedTimeSlot = null;
        this.selectedTimeSlotId = null;
        this.timeSlotReservations.clear();
    }
}