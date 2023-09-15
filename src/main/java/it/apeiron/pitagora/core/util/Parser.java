package it.apeiron.pitagora.core.util;

import com.google.gson.JsonElement;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper.FieldMapperRule;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

@CommonsLog
public class Parser {

    public static final List<String> PATTERNS = Arrays.asList(
            "AUTO",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "MM-dd-yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "MM-dd-yyyy",
            "yyyy-MM",
            "MM-yyyy",
            "yyyy/MM",
            "MM/yyyy",
            "EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss",
            "EEE, dd MMM yyyy"
    );

    public static Object parse(Object source, FieldMapperRule rule, SourceChannel channel, String errorDetail) {
        try {
            if (channel != null) {
                switch (channel) {
                    case HTTP:
                        return _fromJsonElementToModelType((JsonElement) source, rule);
                    case FILE_EXCEL:
                        return _fromCellToModelType((Cell) source, rule);
                    case FILE_CSV:
                        return _fromStringToModelType((String) source, rule);
                }
            } else {
                return _fromStringToModelType((String) source, rule);
            }

        } catch (Exception e) {
            if (e instanceof ParseException) {
                log.error("Error during DateCell parsing due to date pattern mismatch");
                throw PitagoraException.timestampPattern(rule.getDescription(), rule.getType(),
                        errorDetail, channel);
            } else {
                log.error("Error on parsing due to Object value and ModelField Type mismatch");
                throw PitagoraException.numberFormatEx(rule.getDescription(), rule.getType(),
                        errorDetail, channel, e.getMessage());
            }
        }
        throw PitagoraException.internalServerError();
    }

    public static String fromCellToString(Cell cell) {
        String res = "";
        if (cell == null) {
            return res;
        }
        List<CellType> cellTypes = Arrays.asList(CellType.STRING, CellType.NUMERIC, CellType.BOOLEAN);
        loop: for (CellType type : cellTypes) {
            try {
                switch (type) {
                    case STRING:
                        res = cell.getStringCellValue();
                        break loop;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            res = cell.getDateCellValue().toString();
                            break loop;
                        } else {
                            res = Double.toString(cell.getNumericCellValue());
                            break loop;
                        }
                    case BOOLEAN:
                        res = Boolean.toString(cell.getBooleanCellValue());
                        break loop;
                    case BLANK:
                    case _NONE:
                    case ERROR:
                        break loop;
                }
            } catch (Exception ignore) {
            }
        }

        return res;
    }

    private static Object _fromCellToModelType(Cell cell, FieldMapperRule rule) throws ParseException {
        String cellToString = fromCellToString(cell);
        if (cell == null || Arrays.asList(CellType.BLANK, CellType._NONE, CellType.ERROR).contains(cell.getCellType())) {
            return null;
        } else if (CellType.FORMULA.equals(cell.getCellType())) {
            cell.setCellValue(cellToString);
        }

        switch (rule.getType()) {
            default:
            case STRING:
                return cellToString;
            case INTEGER:
            case DOUBLE:
                try {
                    return cell.getNumericCellValue();
                } catch (IllegalStateException e) {
                    return _fromStringToModelType(cellToString, rule);
                }
            case BOOLEAN:
                try {
                    return cell.getBooleanCellValue();
                } catch (IllegalStateException e) {
                    String booleanAsString = StringUtils.isNotEmpty(cellToString) ? cellToString.toLowerCase() : "false";
                    return _fromStringToModelType(booleanAsString, rule);
                }
            case TIMESTAMP:
                try {
                    return cell.getDateCellValue().toInstant().toEpochMilli();
                } catch (Exception e) {
                    return _fromStringToModelType(cellToString, rule);
                }
        }
    }

    private static Object _fromStringToModelType(String string, FieldMapperRule rule) throws ParseException {
        if (StringUtils.isEmpty(string) && !FieldType.STRING.equals(rule.getType()) && !FieldType.BOOLEAN.equals(rule.getType())) {
            return null;
        }
        switch (rule.getType()) {
            case STRING:
                return string;
            case INTEGER:
                int dot = string.indexOf(".");
                if (dot == -1) {
                    return Integer.parseInt(string);
                } else {
                    String decimal = string.substring(dot + 1);
                    if (decimal.chars().allMatch(c -> c == '0')) {
                        return Integer.parseInt(string.substring(0,dot));
                    }
                }
            case DOUBLE:
                return Double.parseDouble(string);
            case BOOLEAN:
                return "true".equals(string) || "1".equals(string);
            case TIMESTAMP:
                try {
                    return Long.parseLong(string);
                } catch (Exception e) {
                    try {
                        return _fromStringToModelTimestamp(string, rule.getTimestampPattern());
                    } catch (ParseException ignored) {
                    }
                    throw new ParseException("Error on date parsing", 0);
                }
            default:
                break;
        }
        throw new RuntimeException();
    }

    private static Object _fromJsonElementToModelType(JsonElement currentJsonElement, FieldMapperRule rule)
            throws ParseException {
        String asString = currentJsonElement != null && !currentJsonElement.isJsonNull() ? currentJsonElement.getAsString() : null;
        return _fromStringToModelType(asString, rule);
    }

    private static long _fromStringToModelTimestamp(String stringDate, String pattern) throws ParseException {
        if (pattern == null || "AUTO".equals(pattern)) {
            for (String pat : PATTERNS) {
                if ("AUTO".equals(pat)) {
                    continue;
                }
                try {
                    return _fromStringToModelTimestamp(stringDate, pat);
                } catch (ParseException ignored) {
                }
            }
            throw new ParseException("Unparsable date", 0);

        } else {
            long dateLong;
            String dateLongFormatted;
            try {

                dateLong = new SimpleDateFormat(pattern).parse(stringDate).toInstant().toEpochMilli();
                dateLongFormatted = new SimpleDateFormat(pattern).format(new Date(dateLong));
            } catch (ParseException ignored) {
                stringDate = stringDate.replaceAll("GMT", "").trim();
                dateLong = new SimpleDateFormat(pattern).parse(stringDate).toInstant().toEpochMilli();
                dateLongFormatted = new SimpleDateFormat(pattern).format(new Date(dateLong));
            }
            if (!stringDate.equals(dateLongFormatted)) {
                throw new ParseException("Unparsable date", 0);
            }
            return dateLong;

        }
    }

}
