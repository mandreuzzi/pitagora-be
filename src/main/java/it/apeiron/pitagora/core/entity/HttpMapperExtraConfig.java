package it.apeiron.pitagora.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpMapperExtraConfig {

    private String rootJsonPath;
    private boolean rootIsArray;

}
