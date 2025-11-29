package fr.unice.polytech.foodDelivery.stepDefs.backend;

import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import fr.unice.polytech.foodDelivery.service.MockAIPhotoAnalysisService;
import io.cucumber.java.en.*;

import static org.junit.jupiter.api.Assertions.*;

public class PhotoAnalysisSteps {

    private String imagePath;
    private Meal meal;
    private final MockAIPhotoAnalysisService aiService = MockAIPhotoAnalysisService.getInstance();

    @Given("une image de plat {string}")
    public void une_image_de_plat(String image) {
        this.imagePath = image;
    }

    @When("l'IA analyse la photo")
    public void l_IA_analyse_la_photo() {
        this.meal = aiService.analyzePhoto(imagePath);
    }

    @Then("le plat identifié doit être {string}")
    public void le_plat_identifie_doit_etre(String expectedName) {
        assertNotNull(meal, "Aucun plat identifié");
        assertEquals(expectedName, meal.getName());
    }

    @Then("la catégorie doit être {string}")
    public void la_categorie_doit_etre(String expectedCategory) {
        assertEquals(MealCategory.valueOf(expectedCategory), meal.getCategory());
    }

    @Then("le type doit être {string}")
    public void le_type_doit_etre(String expectedType) {
        assertEquals(MealType.valueOf(expectedType), meal.getType());
    }

    @Then("aucun plat ne doit être identifié")
    public void aucun_plat_ne_doit_etre_identifie() {
        assertNull(meal, "Un plat a été identifie alors qu'il ne devait pas l'etre");
    }
}

