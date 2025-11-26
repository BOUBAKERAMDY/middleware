package fr.unice.polytech.foodDelivery.domain.model;

import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.service.RestaurantService;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Order { // subject de l'oberservation
    @Getter
    private final UUID orderId;
    private final RestaurantService restaurantService = new RestaurantService();
    @Getter
    private CustomerAccount customer;
    @Getter
    private RestaurantAccount restaurant;
    @Getter
    private List<Meal> meals;
    @Getter
    @Setter
    private Planning schedule;
    @Getter
    @Setter
    private Address location;
    private double amount;
    @Getter
    private OrderStatus status;

    private List<OrderObserveur> observers = new ArrayList<>();
    private String reservedTimeSlotId;

    public Order(CustomerAccount customer, RestaurantAccount restaurant) {
        this.orderId = UUID.randomUUID();
        this.customer = customer;
        this.restaurant = restaurant;
        this.meals = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.observers.add(this.customer);
        this.observers.add(this.restaurant);
    }

    public double getAmount() {
        calculateValueOrder();
        return amount;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        notifyObservers();
    }

    public void addMeal(Meal meal) {
        meals.add(meal);
        calculateValueOrder();
    }

    public void calculateValueOrder() {
        double total = 0;
        for (Meal meal : meals) {
            total += meal.getPrice();
        }
        this.amount = total;
    }

    public boolean payWithAllowance() {
        double total = this.getTotalAmount();
        double customerAllowance = this.customer.getAllowance();

        if (total <= customerAllowance) {
            // Paiement réussi
            this.customer.setAllowance(customerAllowance - total);
            this.status = OrderStatus.PAID;
            return true;
        } else {
            // Paiement échoué - annuler la commande
            this.annulerCommande();
            // throw new InsufficientAllowanceException();
            return false;
        }
    }

    public double getTotalAmount() {
        return meals.stream()
                .mapToDouble(Meal::getPrice)
                .sum();
    }

    private void notifyObservers() {
        for (OrderObserveur observer : observers) {
            observer.onOrderStatusChange(this);
        }
    }

    public void annulerCommande() {
        if (this.status == OrderStatus.PAID) {
            // Annulation complète d'une commande payée
            this.libererCreneau();
            this.status = OrderStatus.CANCELLED;
            this.customer.setAllowance(this.customer.getAllowance() + this.getTotalAmount());
        }
        // Pour les commandes PENDING, on ne fait rien ou on libère juste le créneau
    }

    public void reserverCreneau(String timeSlotId) {
        this.reservedTimeSlotId = timeSlotId;
        restaurantService.reserveSlotForOrder(restaurant.getRestaurantId(), timeSlotId);
    }

    public String getReservedTimeSlotId() {
        return reservedTimeSlotId;
    }

    public void libererCreneau() {
        if (this.schedule != null) {
            Map<String, Planning.TimeSlot> timeSlots = this.schedule.getTimeSlots();
            System.out.println("DEBUG libererCreneau: Recherche dans " + timeSlots.size() + " créneaux");

            for (Map.Entry<String, Planning.TimeSlot> entry : timeSlots.entrySet()) {
                Planning.TimeSlot timeSlot = entry.getValue();
                System.out.println("DEBUG: Vérification créneau " + entry.getKey() + " - commandes: "
                        + timeSlot.getCurrentOrders());

                if (timeSlot.getCurrentOrders() > 0) {
                    timeSlot.decrementOrders();
                    System.out.println("DEBUG: Créneau " + entry.getKey() + " libéré. Nouvelles commandes: "
                            + timeSlot.getCurrentOrders());
                    return;
                }
            }
            System.out.println("DEBUG: Aucun créneau avec des commandes trouvé");
        } else {
            System.out.println("DEBUG: Schedule est null - impossible de libérer le créneau");
        }
    }
}