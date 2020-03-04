@BackgroundFeature
Feature: Test scenarios with background

  Background:
    Given Dog is happy
    And Cat is happy

  @ScenarioWithBackground
  Scenario: Scenario with background (1)
    When Pet the dog
    Then Dog play with cat

  @ScenarioWithBackground
  Scenario: Scenario with background (2)
    When Pet the cat
    Then Cat play with dog