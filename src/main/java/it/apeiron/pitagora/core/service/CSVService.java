package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.Parser.parse;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import it.apeiron.pitagora.core.entity.FileExcelExtraConfig;
import it.apeiron.pitagora.core.entity.FileUploadSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.util.EncodingUtils;
import it.apeiron.pitagora.core.util.MessagesCore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class CSVService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public void checkTypeMatching(PitagoraMapper mapper, String csvFileDataBase64) {
        _generateDataset(mapper, csvFileDataBase64, true);
    }

    public List<Map<String, Object>> generateDatasetRecords(PitagoraMapper mapper, String csvFileDataBase64) {
        return _generateDataset(mapper, csvFileDataBase64, false);
    }

    @SneakyThrows
    private List<Map<String, Object>> _generateDataset(PitagoraMapper mapper, String csvFileDataBase64, boolean justACheck) {

        FileExcelExtraConfig config = (FileExcelExtraConfig) mapper.getExtraConfig();

        byte[] initialArray = EncodingUtils.base64toByteArray(csvFileDataBase64);
        Reader targetReader = new StringReader(new String(initialArray));

        CSVReader csvReader = new CSVReader(targetReader);
        String[] nextRecord;

        List<Map<String, Object>> finalCsv = new ArrayList<>();

        int startingRow = config.getStartingRow();
        int endingRow = config.getEndingRow();

        try {

            int counterRows = 0;

            while ((nextRecord = csvReader.readNext()) != null && counterRows <= endingRow) {

                Map<String, Object> lista = new HashMap<>();
                List<String> evaluationRow = Arrays.asList(nextRecord);

                if (counterRows >= startingRow) {

                    mapper.getRules().forEach((fieldName, fieldAndRuleDefinition) -> {
                        int columnNumber = Integer.parseInt(fieldAndRuleDefinition.getRuleImpl()) + config.getStartingColumn();

                        Object typifiedValue = parse(evaluationRow.get(columnNumber), fieldAndRuleDefinition, SourceChannel.FILE_CSV, Integer.toString(columnNumber + 1));

                        lista.put(fieldName, typifiedValue);
                    });
                    if (justACheck) {
                        break;
                    }
                    finalCsv.add(lista);
                }
                counterRows++;
            }
        } catch (PitagoraException e) {
            throw e;
        } catch (Exception e) {
            throw PitagoraException.internalServerError();
        }

        targetReader.close();
        return finalCsv;
    }

    public List<List<String>> buildPreview(PitagoraSource source) {
        return _buildPreviewOrCheckBoundaries(source, false);
    }
    public void checkBoundaries(PitagoraSource source) {
        _buildPreviewOrCheckBoundaries(source, true);
    }

    private List<List<String>> _buildPreviewOrCheckBoundaries(PitagoraSource source, boolean checkBoundaries) {

        FileUploadSource config = source.getFileUploadConfiguration();
        PitagoraFile file = sp.sourceService.getFileById(config.getFileId());

        List<List<String>> finalCsv = new ArrayList<>();

        try {
            byte[] initialArray = EncodingUtils.base64toByteArray(file.getDataBase64());
            Reader targetReader = new StringReader(new String(initialArray));
            CSVReader csvReader = new CSVReader(targetReader);
            String[] nextRecord;

            final int PREVIEW_MAX_LENGTH = 10;
            int startingRow = config.getStartingRow();
            int endingRow = config.getEndingRow();
            int desiredNumberOfRows = endingRow - startingRow + 1;
            endingRow = startingRow + Math.min(checkBoundaries ? Integer.MAX_VALUE : PREVIEW_MAX_LENGTH, desiredNumberOfRows) - 1;
            int startingColumn = config.getStartingColumn();
            int endingColumn = config.getEndingColumn();
            int counterRows = 0;

            while ((nextRecord = csvReader.readNext()) != null && counterRows <= endingRow) {

                if (counterRows >= startingRow) {
                    List<String> row = Arrays.asList(nextRecord);
                    if (!row.isEmpty()) {
                        if (startingColumn > row.size() - 1) {
                            throw PitagoraException.notAcceptable(t(MessagesCore.FILE_NOT_VALID_MISSING_COLUMN).replace("COLUMN_NUMBER", Integer.toString(startingColumn + 1)));
                        }
                        if (!checkBoundaries) {
                            finalCsv.add(row.subList(startingColumn, Math.min(row.size(),endingColumn + 1)));
                        }
                    }
                }
                counterRows++;
            }

            targetReader.close();
        }
        catch (PitagoraException e) {
            throw e;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return finalCsv;
    }

    protected ByteArrayOutputStream _exportCsv(List<Map<String, Object>> data, List<String> headerDescription,
            List<String> headersKeys, List<String> extraDetails) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(bos);
        CSVWriter writer = new CSVWriter(streamWriter);

        try {

            extraDetails.forEach(details -> {
                List<String> detailsCsv = new ArrayList<>();
                detailsCsv.add(details);
                writer.writeNext(detailsCsv.toArray(new String[0]));
            });
            writer.writeNext(new String[0]);

            writer.writeNext(headerDescription.toArray(new String[0]));

            data.forEach(dataRow -> {
                List<String> rowCsv = new ArrayList<>();
                headersKeys.forEach(header -> {
                    rowCsv.add(dataRow.get(header) != null ? dataRow.get(header).toString() : "");
                });
                writer.writeNext(rowCsv.toArray(new String[0]));
            });

            writer.close();
        }
        catch (IOException e) {
            throw PitagoraException.internalServerError();
        }

        return bos;
    }
}
