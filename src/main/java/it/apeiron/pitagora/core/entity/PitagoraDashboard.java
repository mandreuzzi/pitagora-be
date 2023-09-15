package it.apeiron.pitagora.core.entity;


import it.apeiron.pitagora.core.dto.DashboardDTO;
import it.apeiron.pitagora.core.entity.Widget.ChartWidget;
import it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("dashboard")
public class PitagoraDashboard extends AbstractPitagoraRecord {

    private List<Widget> widgets;

    public PitagoraDashboard(DashboardDTO dto) {
        widgets = new ArrayList<>();
        update(dto);
    }

    public void update(DashboardDTO d) {
        superUpdate(d);

        widgets = d.getWidgets() != null ?
                d.getWidgets().stream().map(dto ->
                                ChartWidget.builder()
                                        .query(dto.getQuery())
                                        .columnNameX(dto.getColumnNameX())
                                        .columnNameY(dto.getColumnNameY())
                                        .size(dto.getSize())
                                        .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

}
