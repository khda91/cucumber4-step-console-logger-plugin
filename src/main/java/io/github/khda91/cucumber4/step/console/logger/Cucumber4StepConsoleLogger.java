package io.github.khda91.cucumber4.step.console.logger;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepStarted;
import cucumber.api.formatter.ColorAware;
import gherkin.ast.Step;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Cucumber4StepConsoleLogger implements EventListener, ColorAware {

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
        testSourceModel.addTestSourceReadEvent(event.uri, event);
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
                event.getTestCase().getTags().stream().map(PickleTag::getName).collect(Collectors.toList()),
                totalMemory, freeMemory);
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
                event.getTestCase().getName(),
                event.getTestCase().getTags().stream().map(PickleTag::getName).collect(Collectors.toList()),
                event.result.getStatus(), executionTime, testScenariosPassedCount, testScenariosFailedCount,
                totalMemory, freeMemory);
        log.info(testCaseFinishedLog);
    }

    private void handleStartOfFeature(TestCaseStarted event) {
        currentFeatureFile = event.testCase.getUri();
    }

    private void handleTestStepStarted(TestStepStarted event) {
        TestStep step = event.testStep;
        if (step instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) step;
            String keyword = getStepKeyword(testStep);
            String stepText = testStep.getStepText();
            StringBuilder stepLogName = new StringBuilder(keyword + stepText);

            if (testStep.getStepArgument().size() != 0) {
                Argument argument = testStep.getStepArgument().get(0);
                if (argument instanceof PickleTable) {
                    stepLogName.append(formatTable((PickleTable) argument));
                } else if (argument instanceof PickleString) {
                    stepLogName.append(formatDocString((PickleString) argument));
                }
            }

            String fullTextStep = stepLogName.toString();
            log.info(buildStepExecutionMessage(fullTextStep));
        }
    }

    private String formatTable(PickleTable table) {
        StringBuilder stepLogName = new StringBuilder();
        if (table.getRows().size() != 0) {
            List<Integer> maxElementInColumn = getMaxLengthCells(table.getRows());
            for (PickleRow row : table.getRows()) {
                stepLogName.append("\n");
                for (int i = 0; i < row.getCells().size(); i++) {
                    String cellValue = row.getCells().get(i).getValue();
                    char[] whiteSpaces = new char[maxElementInColumn.get(i) - cellValue.length()];
                    Arrays.fill(whiteSpaces, ' ');
                    stepLogName.append("\t|\t").append(cellValue).append(String.valueOf(whiteSpaces));
                }
                stepLogName.append("\t|\t");
            }
        }
        return stepLogName.toString();
    }

    private String formatDocString(PickleString pickleString) {
        return String.format("\n\"\"\"\n%s\n\"\"\"", pickleString.getContent());
    }

    private List<Integer> getMaxLengthCells(List<PickleRow> stepRows) {
        int cellNumber = stepRows.get(0).getCells().size();
        List<Integer> maxElementInColumn = new ArrayList<>();
        for (int i = 0; i < cellNumber; i++) {
            int maxValue = 0;
            for (PickleRow row : stepRows) {
                int length = row.getCells().get(i).getValue().length();
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
        if (event.result.getStatus() == Result.Type.PASSED) {
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
