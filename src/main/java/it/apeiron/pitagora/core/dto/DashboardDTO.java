package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.dto.WidgetDTO.ChartWidgetDTO;
import it.apeiron.pitagora.core.entity.PitagoraDashboard;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO extends AbstractRecordDTO {

    private List<ChartWidgetDTO> widgets;

    public DashboardDTO(PitagoraDashboard dashboard) {
        this.id = dashboard.getId().toString();
        this.name = dashboard.getNameForClient();
    }
}
