package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.Filter;
import java.util.List;
import lombok.Data;

@Data
public class QueryEventsDTO {

    private List<Filter> filters;
    private PaginationDTO page;

}
