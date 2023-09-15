package it.apeiron.pitagora.core.theorems.dire.dto.query;

import it.apeiron.pitagora.core.dto.charts.NumericNumericDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SumFieldByYearAndMonthDTO {

  private Map<Integer, List<NumericNumericDTO>> byYearData;
  private Map<Integer, Double> monthAverage;
  private Map<Integer, SameTimeIntervalCountDTO> sameTimeIntervalCount;
  private LinkedHashMap<String, LinkedHashMap<String, Double>> stackedValues;

}
