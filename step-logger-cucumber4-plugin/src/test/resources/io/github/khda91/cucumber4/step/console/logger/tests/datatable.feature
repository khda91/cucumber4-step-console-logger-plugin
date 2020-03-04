@DataTableFeature
Feature: Test Scenarios with Data Tables

  Scenario: Simple data table
    Given users are
      | first_name | second_name | email        |
      | john       | smith       | js@email.com |
      | johnny     | white       | jw@email.com |
      | cat        | black       | cb@email.com |