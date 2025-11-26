package fr.unice.polytech.foodDelivery.service;

import fr.unice.polytech.foodDelivery.domain.ENUM.DiateryPreference;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealCategory;
import fr.unice.polytech.foodDelivery.domain.ENUM.MealType;
import fr.unice.polytech.foodDelivery.domain.model.Meal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MockAIPhotoAnalysisServiceExtendedTest {

    private MockAIPhotoAnalysisService aiService;

    @BeforeEach
    void setUp() {
        aiService = MockAIPhotoAnalysisService.getInstance();
        assertNotNull(aiService);
    }

    @Test
    void testSingletonInstance() {
        MockAIPhotoAnalysisService instance1 = MockAIPhotoAnalysisService.getInstance();
        MockAIPhotoAnalysisService instance2 = MockAIPhotoAnalysisService.getInstance();
        assertSame(instance1, instance2, "Le service doit être un singleton");
    }

    @Test
    void testAnalyzePhoto_knownMeals() {
        Meal pizza = aiService.analyzePhoto("photo_pizza.jpg");
        assertNotNull(pizza);
        assertEquals("Pizza Margherita", pizza.getName());

        Meal sushi = aiService.analyzePhoto("IMG_sushi_test.png");
        assertNotNull(sushi);
        assertEquals("Sushi Assortiment", sushi.getName());
        
        Meal curry = aiService.analyzePhoto("plat_curry.png");
        assertNotNull(curry);
        assertEquals("Curry de Légumes", curry.getName());
    }

    @Test
    void testAnalyzePhoto_unknownMeal() {
        Meal unknown = aiService.analyzePhoto("photo_inconnue.jpg");
        assertNull(unknown, "Un plat inconnu doit retourner null");
    }

    @Test
    void testCheckResultIA_noModification() {
        Meal meal = new Meal.Builder("TestMeal", 5.0)
                .category(MealCategory.STARTER)
                .type(MealType.REGULAR)
                .toppings(Arrays.asList("Ingredient1", "Ingredient2"))
                .dietaryTags(Arrays.asList(DiateryPreference.GLUTEN_FREE))
                .build();

        Meal result = aiService.checkResultIA(meal);
        assertEquals("TestMeal", result.getName());
        assertEquals(5.0, result.getPrice());
        assertEquals(MealCategory.STARTER, result.getCategory());
        assertEquals(MealType.REGULAR, result.getType());
        assertTrue(result.getToppings().contains("Ingredient1"));
        assertTrue(result.getDietaryTags().contains(DiateryPreference.GLUTEN_FREE));
    }

    @Test
    void testCheckResultIA_withAllBranchesSimulation() {
        Meal meal = new Meal.Builder("Sample", 7.0)
                .category(MealCategory.MAIN_COURSE)
                .type(MealType.VEGAN)
                .toppings(Arrays.asList("Tomate", "Salade"))
                .dietaryTags(Arrays.asList(DiateryPreference.VEGAN))
                .build();

       
        Meal result = aiService.checkResultIA(meal);

        assertEquals("Sample", result.getName());
        assertEquals(7.0, result.getPrice());
        assertEquals(MealCategory.MAIN_COURSE, result.getCategory());
        assertEquals(MealType.VEGAN, result.getType());
        assertEquals(Arrays.asList("Tomate", "Salade"), result.getToppings());
        assertEquals(Arrays.asList(DiateryPreference.VEGAN), result.getDietaryTags());
    }

    @Test
    void testAnalyzePhoto_caseInsensitive() {
        Meal pizza = aiService.analyzePhoto("PHOTO_PIZZA.JPG");
        assertNotNull(pizza);
        assertEquals("Pizza Margherita", pizza.getName());
    }

    @Test
    void testAnalyzePhoto_partialNameMatching() {
        Meal biryani = aiService.analyzePhoto("my_biryani_dish.png");
        assertNotNull(biryani);
        assertEquals("Biryani au Poulet", biryani.getName());
    }
}
