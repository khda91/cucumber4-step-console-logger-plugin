package io.github.khda91.cucumber4.step.console.logger.tests;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = "io.github.khda91.cucumber4.step.console.logger.Cucumber4StepConsoleLogger"
)
public class RunnerTest {
}
