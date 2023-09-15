package it.apeiron.pitagora.core.theorems.dire.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrichmentProcessing {
    PitagoraDataset dataset;
    PitagoraModel model;
    List<Map<String, Object>> enrichedRecords;
}
