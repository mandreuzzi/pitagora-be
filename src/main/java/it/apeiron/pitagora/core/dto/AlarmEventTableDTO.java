package it.apeiron.pitagora.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmEventTableDTO {
    private List<AlarmEventDTO> data;
    private PaginationDTO page;
}