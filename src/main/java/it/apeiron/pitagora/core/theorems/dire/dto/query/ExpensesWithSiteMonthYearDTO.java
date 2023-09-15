package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ExpensesWithSiteMonthYearDTO {

  @Id
  private String _id;
    private String sede;
    private double costi;
    private int month;
    private int year;

}
