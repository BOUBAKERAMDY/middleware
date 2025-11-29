package fr.unice.polytech.foodDelivery;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features.foodDelivery/back")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "fr.unice.polytech.foodDelivery.stepDefs.backend,fr.unice.polytech.foodDelivery.stepDefs")

public class RunBackendCucumberTest {
    /*
     This will run all features found on the configuration.
     Cucumber scans all classes in the GLUE package (and subpackages by default) and collects:
       - All step definitions (@Given, @When, @Then)
       - All hooks (@Before, @After, @BeforeAll, @AfterAll)
     All @Before hooks found in the glue path will execute before every scenario,
     regardless of which step definition class they're defined in.
     */
}