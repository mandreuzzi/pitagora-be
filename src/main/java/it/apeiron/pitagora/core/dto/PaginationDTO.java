package it.apeiron.pitagora.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationDTO {
    private int number;
    private int size;
    private int total;
    private String sortBy;
    private String sortDirection;
}
