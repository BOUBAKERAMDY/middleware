package fr.unice.polytech.foodDelivery.stepDefs.userStories.hooks;

import fr.unice.polytech.foodDelivery.stepDefs.userStories.context.UserStoriesContext;
import io.cucumber.java.Before;

/**
 * Cucumber hooks for User Stories scenarios
 */
public class UserStoriesHooks {

    private final UserStoriesContext context;

    public UserStoriesHooks(UserStoriesContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario() {
        context.reset();
    }
}

