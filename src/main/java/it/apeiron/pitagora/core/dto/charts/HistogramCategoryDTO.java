package it.apeiron.pitagora.core.dto.charts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistogramCategoryDTO {
        private String category;
        private double count;
        private double customValue;

        public HistogramCategoryDTO(String category, double count) {
                this.category = category;
                this.count = count;
        }

}
