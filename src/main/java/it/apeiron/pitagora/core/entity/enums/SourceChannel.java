package it.apeiron.pitagora.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum SourceChannel {
    HTTP("Https"),
    FILE_EXCEL("File EXCEL"),
    FILE_CSV("File CSV"),
    EXPOSED_API("Pitagora API"),
    FILE_MP4("Video MP4");

    private String description;

}
