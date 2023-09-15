package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.enums.ExportFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportFileCallDTO {

    private QueryDatasetDTO query;
    private ExportFormat exportFormat;

}
