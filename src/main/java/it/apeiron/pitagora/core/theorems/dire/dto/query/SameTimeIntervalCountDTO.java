package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SameTimeIntervalCountDTO {

  private double expenses;
  private double variationPercentage;

  public void addExpense(double value) {
    expenses += value;
  }
}
