package io.github.khda91.cucumber4.step.console.logger.tests.steps;

/*
 *  Copyright 2020 the original author of authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ExamplesFeatureSteps {

    @Given("a is {int}")
    public void aIs(int a) {
    }

    @Given("b is {int}")
    public void bIs(int b) {
    }

    @When("I add a to b")
    public void iAddAToB() {
    }

    @Then("result is {int}")
    public void resultIs(int result) {
    }
}
