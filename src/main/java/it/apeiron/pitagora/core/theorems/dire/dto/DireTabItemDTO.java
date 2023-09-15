package it.apeiron.pitagora.core.theorems.dire.dto;

import it.apeiron.pitagora.core.dto.WidgetSize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DireTabItemDTO {
    private String identifier;
    private int indexPosition;
    private WidgetSize size;
    private Object content;

    public DireTabItemDTO(int indexPosition, String identifier) {
        this.indexPosition = indexPosition;
        this.identifier = identifier;
        size = WidgetSize.w97;
    }
}
