package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.Filter;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QueryDatasetDTO {

    private String datasetId;
    private List<Filter> filters;
    private PaginationDTO page;
    private boolean delete;
    private LocalDateTime startingDate;
    private LocalDateTime endingDate;

}
