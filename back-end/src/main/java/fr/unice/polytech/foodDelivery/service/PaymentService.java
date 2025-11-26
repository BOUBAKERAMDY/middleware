package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.model.CustomerAccount;
import fr.unice.polytech.foodDelivery.domain.ENUM.OrderStatus;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentMethod;
import fr.unice.polytech.foodDelivery.domain.ENUM.PaymentStatus;
import fr.unice.polytech.foodDelivery.domain.exception.PaymentException;
import fr.unice.polytech.foodDelivery.domain.model.*;

public class PaymentService {

    public Payment pay(Order order, PaymentMethod method) throws PaymentException {
        CustomerAccount customer = order.getCustomer();
        double amount = order.getAmount();

        Payment payment = new Payment(order, amount, method);
        payment.setStatus(PaymentStatus.PENDING);

        boolean success = processPayment(method, amount, customer);

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.FAILED);
            throw new PaymentException("Payment not validated: " + method);
        }

        return payment;
    }

    private boolean processPayment(PaymentMethod method, double amount, CustomerAccount customer) {
        if (method == PaymentMethod.ALLOWANCE) {
            if (customer.getAllowance() >= amount) {
                customer.setAllowance(customer.getAllowance() - amount);
                return true;
            }
            return false;
        }

        if (method == PaymentMethod.CARD || method == PaymentMethod.PAYPAL) {
            return simulateExternalPayment(amount);
        }

        return false;
    }

    private boolean simulateExternalPayment(double amount) {
        // Simule un échec 30% du temps (pour tester)
        return Math.random() > 0.3;
    }
}
