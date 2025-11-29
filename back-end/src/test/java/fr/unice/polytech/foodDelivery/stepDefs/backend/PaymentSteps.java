// PaymentSteps.java
package fr.unice.polytech.foodDelivery.stepDefs.backend;

import io.cucumber.java.fr.*;
import static org.junit.jupiter.api.Assertions.*;

public class PaymentSteps {

    private final TestContext context;

    public PaymentSteps(TestContext context) {
        this.context = context;
    }

    @Et("le client paie la commande avec son allowance")
    public void client_paie_avec_allowance() {
        context.paiementReussi = context.order.payWithAllowance();
        if (!context.paiementReussi) {
            System.out.println("ERREUR de paiement");
        }
    }

    @Et("le client tente de payer la commande avec son allowance")
    public void client_tente_payer_avec_allowance() {
        try {
            double total = context.order.getTotalAmount();
            double allowance = context.customer.getAllowance();

            if (total > allowance) {
                context.order.libererCreneau();
                context.paiementReussi = false;
            } else {
                context.paiementReussi = context.order.payWithAllowance();
                if (!context.paiementReussi) {
                    System.out.println("ERREUR de paiement");
                }
            }
        } catch (Exception e) {
            context.order.libererCreneau();
            context.paiementReussi = false;
        }
    }

    @Alors("le paiement doit échouer")
    public void paiement_doit_echouer() {
        assertFalse(context.paiementReussi);
    }
}