package it.apeiron.pitagora.core.dto;


import it.apeiron.pitagora.core.entity.enums.PitagoraDocumentScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationDTO {

    private PitagoraDocumentScope scope;
    private String category;
    private String subCategory;
    private String template;
}
