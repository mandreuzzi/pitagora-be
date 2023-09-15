package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetDashboardPreviewDTO {

    private String name;
    private List<String> dataPreview;

    public static DatasetDashboardPreviewDTO fromModel(PitagoraDataset model) {
        return DatasetDashboardPreviewDTO.builder()
                .name(model.getName())
                .build();
    }
}
