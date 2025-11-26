package fr.unice.polytech.foodDelivery;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features.foodDelivery/userStories")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "fr.unice.polytech.foodDelivery.stepDefs.userStories,fr.unice.polytech.foodDelivery.stepDefs")

public class RunUserStoriesCucumberTest {
    /*
     This will run all user stories features found in the configuration.
     Cucumber scans all classes in the GLUE package (and subpackages by default) and collects:
       - All step definitions (@Given, @When, @Then, @Et, @Étantdonné, @Quand, @Alors)
       - All hooks (@Before, @After, @BeforeAll, @AfterAll)
     All @Before hooks found in the glue path will execute before every scenario,
     regardless of which step definition class they're defined in.
     */
}
