package io.github.khda91.cucumber5.step.console.logger;

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

import gherkin.ast.Step;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepStarted;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Cucumber5StepConsoleLogger implements EventListener, ColorAware {

    private static final long BYTES_IN_MEGABYTE = 1048576;

    private static int testScenariosCount;
    private static int testScenariosPassedCount;
    private static int testScenariosFailedCount;
    private static long totalMemory;
    private static long freeMemory;
    private static long startScenarioTimeInMillis;

    private final TestSourceModel testSourceModel = new TestSourceModel();

    private String currentFeatureFile;

    private EventHandler<TestSourceRead> testSourceReadEventHandler = this::handleTestSourceRead;
    private EventHandler<TestCaseStarted> caseStartedEventHandler = this::handleTestCaseStarted;
    private EventHandler<TestCaseFinished> caseFinishedEventHandler = this::handleTestCaseFinished;
    private EventHandler<TestStepStarted> stepStartedEventHandler = this::handleTestStepStarted;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadEventHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, caseStartedEventHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedEventHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedEventHandler);
    }

    @Override
    public void setMonochrome(boolean monochrome) {

    }

    public void handleTestSourceRead(TestSourceRead event) {
        testSourceModel.addTestSourceReadEvent(event.getUri().toString(), event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        handleStartOfFeature(event);
        startScenarioTimeInMillis = System.currentTimeMillis();
        getMemory();
        String testCaseStartedLog = String.format("\n\n########### Scenario Execution Started ###########\n\n" +
                        "\t\tScenario: %s\n" +
                        "\t\tTags: %s\n" +
                        "\t\tTotal Memory (MB): %s\n" +
                        "\t\tFree Memory (MB): %s" +
                        "\n\n########### Scenario Execution Stared ###########\n\n", event.getTestCase().getName(),
                event.getTestCase().getTags(), totalMemory, freeMemory);
        log.info(testCaseStartedLog);
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        long executionTime = estimateScenarioExecutionTimeInSeconds();
        countTestScenariosPassedFailed(event);
        getMemory();
        String testCaseFinishedLog = String.format("\n\n########### Scenario Execution Finished ###########\n\n" +
                        "\t\tCompleted Scenarios count: %s\n" +
                        "\t\tLast Scenario: %s\n" +
                        "\t\tTags: %s\n" +
                        "\t\tStatus of last executed Scenario: %s\n" +
                        "\t\tExecution Time (Sec): %s\n" +
                        "\t\tPassed Scenarios count: %s\n" +
                        "\t\tFailed Scenarios count: %s\n" +
                        "\t\tTotal Memory (MB): %s\n" +
                        "\t\tFree Memory (MB): %s" +
                        "\n\n########### Scenario Execution Finished ###########\n\n", testScenariosCount,
                event.getTestCase().getName(), event.getTestCase().getTags(), event.getResult().getStatus(),
                executionTime, testScenariosPassedCount, testScenariosFailedCount,
                totalMemory, freeMemory);
        log.info(testCaseFinishedLog);
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        currentFeatureFile = event.getTestCase().getUri().toString();
    }

    private void handleTestStepStarted(TestStepStarted event) {
        TestStep step = event.getTestStep();
        if (step instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) step;
            String keyword = getStepKeyword(testStep);
            String stepText = testStep.getStep().getText();
            StringBuilder stepLogName = new StringBuilder(keyword + stepText);

            StepArgument argument = testStep.getStep().getArgument();
            if (argument != null) {
                if (argument instanceof DataTableArgument) {
                    stepLogName.append(formatTable((DataTableArgument) argument));
                } else if (argument instanceof DocStringArgument) {
                    stepLogName.append(formatDocString((DocStringArgument) argument));
                }
            }

            String fullTextStep = stepLogName.toString();
            log.info(buildStepExecutionMessage(fullTextStep));
        }
    }

    private String formatTable(DataTableArgument table) {
        StringBuilder stepLogName = new StringBuilder();
        if (table.cells().size() != 0) {
            List<Integer> maxElementInColumn = getMaxLengthCells(table.cells());
            for (List<String> row : table.cells()) {
                stepLogName.append("\n");
                for (int i = 0; i < row.size(); i++) {
                    String cellValue = row.get(i);
                    char[] whiteSpaces = new char[maxElementInColumn.get(i) - cellValue.length()];
                    Arrays.fill(whiteSpaces, ' ');
                    stepLogName.append("\t|\t").append(cellValue).append(String.valueOf(whiteSpaces));
                }
                stepLogName.append("\t|\t");
            }
        }
        return stepLogName.toString();
    }

    private String formatDocString(DocStringArgument docString) {
        return String.format("\n\"\"\"\n%s\n\"\"\"", docString.getContent());
    }

    private List<Integer> getMaxLengthCells(List<List<String>> stepRows) {
        int cellNumber = stepRows.get(0).size();
        List<Integer> maxElementInColumn = new ArrayList<>();
        for (int i = 0; i < cellNumber; i++) {
            int maxValue = 0;
            for (List<String> row : stepRows) {
                int length = row.get(i).length();
                if (length > maxValue) {
                    maxElementInColumn.add(i, length);
                    maxValue = length;
                }
            }
        }
        return maxElementInColumn;
    }

    private String buildStepExecutionMessage(String stepLogName) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n---------- Test Step Execution ----------\n");
        sb.append(stepLogName);
        sb.append("\n---------- Test Step Execution ----------\n");
        return sb.toString();
    }

    private String getStepKeyword(PickleStepTestStep testStep) {
        TestSourceModel.AstNode astNode = testSourceModel.getAstNode(currentFeatureFile, testStep.getStepLine());
        if (astNode != null) {
            Step step = (Step) astNode.node;
            return step.getKeyword();
        } else {
            return "";
        }
    }

    private void countTestScenariosPassedFailed(TestCaseFinished event) {
        testScenariosCount++;
        if (event.getResult().getStatus() == Status.PASSED) {
            testScenariosPassedCount++;
        } else {
            testScenariosFailedCount++;
        }
    }

    private void getMemory() {
        totalMemory = Runtime.getRuntime().totalMemory() / BYTES_IN_MEGABYTE;
        freeMemory = Runtime.getRuntime().freeMemory() / BYTES_IN_MEGABYTE;
    }

    private long estimateScenarioExecutionTimeInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startScenarioTimeInMillis);
    }
}
