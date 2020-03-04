@ExamplesFeature
Feature: Test Scenarios with Examples

  Scenario Outline: Add <a> to <b>
    Given a is <a>
    And b is <b>
    When I add a to b
    Then result is <result>

    Examples:
      | a | b | result |
      | 1 | 2 | 3      |
      | 4 | 4 | 8      |
      | 8 | 9 | 17     |