[license]: http://www.apache.org/licenses/LICENSE-2.0 "Apache License 2.0"
# Cucumber Console Step Logger plugin 
[![Maven Central](https://img.shields.io/maven-central/v/io.github.khda91/step-logger-cucumber-plugin)](https://search.maven.org/artifact/io.github.khda91/step-logger-cucumber-plugin)
[![Apache License](https://img.shields.io/badge/license-Apache-green.svg)](https://github.com/khda91/cucumber-step-console-logger-plugin/blob/master/LICENSE)

Cucumber plugin for the printing steps into console.
For the console output used slf4 logger

Supported *Cucumber 4* and *Cucumber 5* versions

### Example of the output from the step
```text
---------- Test Step Execution ----------
When I populate mandatory fields with valid values
	|	gazetteYear	|	startDate	|	endDate	|	
	|	2019       	|	1        	|	5      	|	
---------- Test Step Execution ----------
```

## How to use Cucumber 4 console step logger plugin
Add following dependency to you project
**Maven**
```xml
<dependency>
    <groupId>io.github.khda91</groupId>
    <artifactId>step-logger-cucumber4-plugin</artifactId>
     <version>$LATEST_VERSION</version>
    <scope>test</scope>
</dependency>
```

**Gradle**
```groovy
testRuntime "io.github.khda91:step-logger-cucumber4-plugin"
```

### Junit 4
```java
@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"io.github.khda91.cucumber4.step.console.logger.Cucumber4StepConsoleLogger"}
)
public class Runner {
}
```

### TestNg
```java
@CucumberOptions(
    plugin = {"io.github.khda91.cucumber4.step.console.logger.Cucumber4StepConsoleLogger"}
)
public class Runner extends AbstractTestNGCucumberTests {
}
```

### Maven
```bash
mvn clean test -Dcucumber.options="--plugin io.github.khda91.cucumber4.step.console.logger.Cucumber4StepConsoleLogger"
```

### Gradle
See [official Gradle documentation for Cucumber](https://cucumber.io/docs/tools/java/)

## How to use Cucumber 5 console step logger plugin
Add following dependency to you project
**Maven**
```xml
<dependency>
    <groupId>io.github.khda91</groupId>
    <artifactId>step-logger-cucumber5-plugin</artifactId>
     <version>$LATEST_VERSION</version>
    <scope>test</scope>
</dependency>
```

**Gradle**
```groovy
testRuntime "io.github.khda91:step-logger-cucumber5-plugin"
```

### Junit 4
```java
@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"io.github.khda91.cucumber5.step.console.logger.Cucumber5StepConsoleLogger"}
)
public class Runner {
}
```

### TestNg
```java
@CucumberOptions(
    plugin = {"io.github.khda91.cucumber5.step.console.logger.Cucumber5StepConsoleLogger"}
)
public class Runner extends AbstractTestNGCucumberTests {
}
```

### Maven
```bash
mvn clean test -Dcucumber.options="--plugin io.github.khda91.cucumber5.step.console.logger.Cucumber5StepConsoleLogger"
```

### Gradle
See [official Gradle documentation for Cucumber](https://cucumber.io/docs/tools/java/)
