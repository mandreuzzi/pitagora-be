package it.apeiron.pitagora.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileExcelExtraConfig {

    private int sheet;
    private int startingRow;
    private int endingRow;
    private int startingColumn;
    private int endingColumn;

    public static FileExcelExtraConfig fromSourceConfiguration(FileUploadSource conf) {
        return FileExcelExtraConfig.builder()
                .sheet(conf.getSheet())
                .startingRow(conf.getStartingRow())
                .endingRow(conf.getEndingRow())
                .startingColumn(conf.getStartingColumn())
                .endingColumn(conf.getEndingColumn())
                .build();
    }
}
