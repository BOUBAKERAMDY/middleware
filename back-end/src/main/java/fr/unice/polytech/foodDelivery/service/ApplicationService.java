package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentMethod;
import fr.unice.polytech.foodDelivery.domain.model.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ApplicationService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final MockAIPhotoAnalysisService analysisService = MockAIPhotoAnalysisService.getInstance();
    private Order currentOrder;

    public ApplicationService() {
        this.orderService = new OrderService();
        this.paymentService = new PaymentService();
        this.orderService.clearData();
    }


    public Order createOrder(CustomerAccount customer, RestaurantAccount restaurant) {
        currentOrder = orderService.createOrder(customer, restaurant);
        return currentOrder;
    }

    public Planning choosePlanning(RestaurantAccount account) {
        account.getPlanning().displayAvailableSlots();
        Planning res = null; // ask user
        return res;
    }

    public boolean validateOrder(Planning schedule, Address location, PaymentMethod method) {
        setOrderInfo(schedule, location);
        paymentService.pay(currentOrder, method);
        if (currentOrder.getStatus().equals(OrderStatus.PAID)) {
            return true;
        } else {
            return false;
        }
    }

    public void setOrderInfo(Planning schedule, Address location) {
        currentOrder.setSchedule(schedule);
        currentOrder.setLocation(location);
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public void addMeal(Meal meal) {
        orderService.addMealToOrder(currentOrder, meal);
    }

    public void addMealWithPhoto(String mealPhoto) {
        this.addMeal(analysisService.analyzePhoto(mealPhoto));
    }

    public List<Order> getCustomerHistory(UUID customerId) {
        return orderService.getCustomerHistory(customerId);
    }
}
