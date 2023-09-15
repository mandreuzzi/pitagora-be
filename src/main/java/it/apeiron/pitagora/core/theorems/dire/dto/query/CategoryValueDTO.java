package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class CategoryValueDTO {

  @Id
  private String category;
    private double value;

}
