package it.apeiron.pitagora.core.theorems.dire.dto.query;

import java.util.Map;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class ValueGroupByDTO {

  @Id
  private Map<String, Object> _id;
    private double value;

}
