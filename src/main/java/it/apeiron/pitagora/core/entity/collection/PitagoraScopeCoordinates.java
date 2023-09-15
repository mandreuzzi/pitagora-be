package it.apeiron.pitagora.core.entity.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("scope_coordinates")
public class PitagoraScopeCoordinates {

    @Id
    @JsonIgnore
    private String id;
    @CreatedDate
    @JsonIgnore
    LocalDateTime createdAt;

    private ValueDescriptionDTO valueDescription;
    private Set<Category> categories;

    public static PitagoraScopeCoordinates empty() {
        return PitagoraScopeCoordinates.builder().categories(new HashSet<>()).build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private Set<ValueDescriptionDTO> subCategories;
        private ValueDescriptionDTO valueDescription;
    }

}
