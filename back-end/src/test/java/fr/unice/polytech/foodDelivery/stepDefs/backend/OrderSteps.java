// OrderSteps.java
package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;
import fr.unice.polytech.foodDelivery.domain.ENUM.*;
import fr.unice.polytech.foodDelivery.service.PaymentService;
import fr.unice.polytech.foodDelivery.domain.model.Payment;

public class OrderSteps {

    private final TestContext context;

    public OrderSteps(TestContext context) {
        this.context = context;
    }



    @Et("le client choisit de payer avec son allowance")
    public void client_choisit_paiement_allowance() {
        context.paymentMethod = PaymentMethod.ALLOWANCE;
        context.paymentService = new PaymentService();
    }

    @Quand("le client paie sa commande")
    public void client_paie_commande() {
        Payment paiement = context.paymentService.pay(context.order, context.paymentMethod);
        assertNotNull(paiement, "Le paiement ne devrait pas être null");
        assertEquals(PaymentStatus.SUCCESS, paiement.getStatus(), "Le paiement aurait dû réussir");
    }

    @Alors("la commande doit être marquée comme payée")
    public void commande_marquee_comme_payee() {
        assertEquals(OrderStatus.PAID, context.order.getStatus());
    }
}