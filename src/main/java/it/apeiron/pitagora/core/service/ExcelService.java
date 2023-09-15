package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.Parser.fromCellToString;
import static it.apeiron.pitagora.core.util.Parser.parse;

import it.apeiron.pitagora.core.entity.FileExcelExtraConfig;
import it.apeiron.pitagora.core.entity.FileUploadSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.util.EncodingUtils;
import it.apeiron.pitagora.core.util.MessagesCore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ExcelService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public void checkTypeMatching(PitagoraMapper mapper, String excelFileDataBase64) {
        _generateDataset(mapper, excelFileDataBase64, true);
    }

    public List<Map<String, Object>> generateDatasetRecords(PitagoraMapper mapper, String excelFileDataBase64) {
        return _generateDataset(mapper, excelFileDataBase64, false);
    }

    public List<Map<String, Object>> _generateDataset(PitagoraMapper mapper, String excelFileDataBase64, boolean justACheck) {

        FileExcelExtraConfig extraConfig = (FileExcelExtraConfig) mapper.getExtraConfig();
        Sheet sheet = _getSheet(excelFileDataBase64, extraConfig.getSheet());

        List<Map<String, Object>> mappedDataset = new ArrayList<>();

        int startingRow = extraConfig.getStartingRow();
        int endingRow = extraConfig.getEndingRow() + 1;
        int startingColumn = extraConfig.getStartingColumn();

        for (int r = startingRow; r < endingRow; r++) {

            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }

            Map<String, Object> datasetRow = new HashMap<>();
            mapper.getRules().forEach((fieldName, fieldAndRuleDefinition) -> {
                int columnNumber = Integer.parseInt(fieldAndRuleDefinition.getRuleImpl());

                Cell cell = row.getCell(columnNumber + startingColumn);

                Object typifiedValue = parse(cell, fieldAndRuleDefinition, SourceChannel.FILE_EXCEL, Integer.toString(columnNumber + 1));

                datasetRow.put(fieldName, typifiedValue);

            });
            if (justACheck) {
                break;
            }

            mappedDataset.add(datasetRow);
        }

        return mappedDataset;
    }

    private Sheet _getSheet(String excelFileDataBase64, int sheetNumber) {
        byte[] fileDecoded = EncodingUtils.base64toByteArray(excelFileDataBase64);
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(fileDecoded);

        try {
            XSSFWorkbook wb = new XSSFWorkbook(byteArrayInput);
            return wb.getSheetAt(sheetNumber);
        } catch (Exception e) {
            try {
                HSSFWorkbook hwb = new HSSFWorkbook(byteArrayInput);
                return hwb.getSheetAt(sheetNumber);
            } catch (Exception i) {
                log.error("Error instancing HSSFWorkbook and XSSFWorkbook");
                throw PitagoraException.badRequest(t(MessagesCore.FILE_EXTENSION_NOT_VALID));
            }
        }
    }

    public List<List<String>> buildPreview(PitagoraSource excelSource) {
        FileUploadSource cfg = excelSource.getFileUploadConfiguration();

        PitagoraFile file = sp.sourceService.getFileById(cfg.getFileId());

        Sheet sheet = _getSheet(file.getDataBase64(), cfg.getSheet());

        List<List<String>> strings = new ArrayList<>();

        final int PREVIEW_MAX_LENGTH = 10;

        int startingRow = cfg.getStartingRow();
        int endingRow = cfg.getEndingRow();

        int desiredNumberOfRows = endingRow - startingRow + 1;
        endingRow = startingRow + Math.min(PREVIEW_MAX_LENGTH, desiredNumberOfRows) - 1;

        int startingColumn = cfg.getStartingColumn();
        int endingColumn = cfg.getEndingColumn();

        for (int r = startingRow; r <= endingRow; r++) {

            Row row = sheet.getRow(r);
            List<String> rowString = new ArrayList<>(endingColumn - startingColumn);

            for (int c = startingColumn; c <= endingColumn; c++) {
                String value = row != null ? fromCellToString(row.getCell(c)) : "";
                rowString.add(value);
            }
            strings.add(rowString);
        }

        return strings;
    }

    protected ByteArrayOutputStream _exportExcel(List<Map<String, Object>> data, String datasetName, List<String> headerDescription,
            List<String> headersKeys, List<String> extraDetails) {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet(datasetName);

        int currentDataRowCount = 0;
        int currentCellCount = 0;

        for (String s : extraDetails) {
            XSSFRow extraDetailsRow = sheet.createRow(currentDataRowCount);
            XSSFCell currentExtraDetailsCell = extraDetailsRow.createCell(0);
            currentExtraDetailsCell.setCellValue(s);
            currentDataRowCount++;
        }
        currentDataRowCount++;

        XSSFRow headers = sheet.createRow(currentDataRowCount);
        for (String s : headerDescription) {
            XSSFCell currentHeaderCell = headers.createCell(currentCellCount);
            currentHeaderCell.setCellValue(s);
            currentCellCount++;
        }
        currentDataRowCount++;

        for (Map<String, Object> datasetRow : data) {
            XSSFRow excelRow = sheet.createRow(currentDataRowCount);
            currentCellCount = 0;
            for (String header : headersKeys) {
                XSSFCell currentCell = excelRow.createCell(currentCellCount);
                currentCell.setCellValue(datasetRow.get(header) != null ? datasetRow.get(header).toString() : "");
                currentCellCount++;
            }
            currentDataRowCount++;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (bos) {
            wb.write(bos);
        } catch (IOException e) {
            throw PitagoraException.internalServerError();
        }

        return bos;
    }
}
