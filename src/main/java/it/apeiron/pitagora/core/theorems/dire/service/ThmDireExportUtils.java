package it.apeiron.pitagora.core.theorems.dire.service;

import it.apeiron.pitagora.core.dto.ExportRequestDTO.EnergyTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.FinanceTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.FinanceTabExportDetail.FinanceOtherStats;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.ReactiveMaintenanceTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.SustainabilityTabExportDetail;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class ThmDireExportUtils {

    public static void addFinanceInfoParagraphs(XWPFDocument doc, FinanceTabExportDetail data) {

        XWPFParagraph par1 = doc.createParagraph();
        par1.setSpacingBefore(1000);
        XWPFRun par1Run = _bold(par1, data.getSiteWithTheLargestIncreaseAnnualExpenses());
        par1Run.setFontSize(14);
        par1Run.addBreak();
        _default(par1, "Sede con il maggiore aumento delle spese annuali.");

        XWPFParagraph par2 = doc.createParagraph();
        par2.setSpacingBefore(300);
        XWPFRun par2Run = _bold(par2, data.getSiteWithHighestAverageExpensesPerSquareMeter());
        par2Run.setFontSize(14);
        par2Run.addBreak();
        _default(par2, "Sede con la spesa media a metro quadro maggiore.");

        FinanceOtherStats otherStats = data.getOtherStats();
        XWPFParagraph par3 = doc.createParagraph();
        par3.setSpacingBefore(500);
        par3.setAlignment(ParagraphAlignment.LEFT);

        _default(par3, "La sede ");
        _bold(par3, otherStats.getSiteWithMaxExpensesDesc());
        _default(par3, " genera il ");
        _bold(par3, otherStats.getSiteWithMaxExpensesValue());
        _default(par3, " % delle ");
        _bold(par3, "Spese Totali");
        XWPFRun par3Run = _default(par3, " del fondo.");
        par3Run.addBreak();

        if (otherStats.getMinYear().equals(otherStats.getMaxYear())) {
            _default(par3, "Nel ");
            _underlined(par3, otherStats.getMinYear());
            _default(par3, " ");
        } else {
            _default(par3, "Tra il ");
            _underlined(par3, otherStats.getMinYear());
            _default(par3, " e il ");
            _underlined(par3, otherStats.getMaxYear());
            _default(par3, " ");
        }

        _underlined(par3, otherStats.getSiteWithMaxExpensesIncreaseOrDecreaseDesc());
        _default(par3, otherStats.getMaxIncreaseOrDecrease());
        _underlined(par3, otherStats.getMaxIncreaseOrDecreaseValue());
        _default(par3, " % ), mentre ");
        _underlined(par3, otherStats.getSiteWithMaxOrMinExpensesIncreaseOrDecreaseDesc());
        _default(par3, otherStats.getMaxOrMinIncreaseOrDecrease());
        _underlined(par3, otherStats.getSiteWithMaxOrMinExpensesIncreaseOrDecreaseValue());
        _default(par3, " % ).");

    }

    public static void addFinanceTableParagraph(XWPFDocument doc, List<Map<String, String>> tableData) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setSpacingBefore(1000);
        XWPFTable table = doc.createTable(tableData.size() + 1,5);
        table.setWidth("100%");
        table.setTableAlignment(TableRowAlign.CENTER);
        table.setLeftBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setTopBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setRightBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setBottomBorder(XWPFBorderType.SINGLE, 1, 1, "777777");

        XWPFTableRow tableRow0 = table.getRow(0);
        setTableCellBold(tableRow0, 0, "Sede", 8, ParagraphAlignment.CENTER,"777777");
        setTableCellBold(tableRow0, 1, "Spesa totale €", 8, ParagraphAlignment.CENTER,"777777");
        setTableCellBold(tableRow0, 2, "Spesa energia €", 8, ParagraphAlignment.CENTER,"777777");
        setTableCellBold(tableRow0, 3, "Spesa gas €", 8, ParagraphAlignment.CENTER,"777777");
        setTableCellBold(tableRow0, 4, "Spesa manutenzione €", 8, ParagraphAlignment.CENTER,"777777");

        List<String> keys = List.of("site", "totalExpenses", "electricityExpenses", "gasExpenses", "maintenanceExpenses");
        for (int r = 0; r < tableData.size() - 1; r++) {
            for (int c = 0; c < 5; c++) {
                String bgColor = r % 2 == 0 ? "ffffff" : "cccccc";
                ParagraphAlignment align = ParagraphAlignment.LEFT;
                String text = tableData.get(r).get(keys.get(c));
                if (c > 0) {
                    align = ParagraphAlignment.RIGHT;
                    text = withTwoDecimals(text);
                }
                setTableCell(table.getRow(r+1), c, text, 10, align, bgColor);
            }
        }
    }
    public static void addEnergyInfoParagraphs(XWPFDocument doc, EnergyTabExportDetail data) {
        XWPFParagraph par1 = doc.createParagraph();
        par1.setSpacingBefore(1000);
        XWPFRun par1Run = _bold(par1, withTwoDecimals(data.getValue()) + data.getLabelSuffix());
        par1Run.setFontSize(14);
        par1Run.addBreak();
        _default(par1, data.getDescription());
    }

    public static void addReactiveMaintenanceInfoParagraphs(XWPFDocument doc, ReactiveMaintenanceTabExportDetail data) {
        XWPFParagraph par1 = doc.createParagraph();
        par1.setSpacingBefore(1000);
        _default(par1, "Le spese della manutenzione " + data.getIncreaseOrDecrease() + " ");
        _underlined(par1, withTwoDecimals(data.getIncreaseOrDecreaseValue()) + "% ");
        _default(par1, " tra ");
        _underlined(par1, data.getFirstMonth() + " " + data.getFirstYear());
        _default(par1, " e ");
        XWPFRun par1Run = _underlined(par1, data.getLastMonth() + " " + data.getLastYear());
        par1Run.addBreak();
    }

    public static void addSustainabilityInfoParagraphs(XWPFDocument doc, SustainabilityTabExportDetail data) {
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.setSpacingBefore(1000);
        XWPFTable table = doc.createTable(3,4);
        table.setWidth("95%");
        table.setTableAlignment(TableRowAlign.CENTER);
        table.setLeftBorder(XWPFBorderType.SINGLE, 1, 1, "ffffff");
        table.setTopBorder(XWPFBorderType.SINGLE, 1, 1, "ffffff");
        table.setRightBorder(XWPFBorderType.SINGLE, 1, 1, "ffffff");
        table.setBottomBorder(XWPFBorderType.SINGLE, 1, 1, "ffffff");

        XWPFTableRow tableRow0 = table.getRow(0);
        setTableCellBold(tableRow0, 0, withTwoDecimals(data.getFirstAnnualScope()), 12);
        setTableCellBold(tableRow0, 1, withTwoDecimals(data.getSecondAnnualScope()), 12);
        setTableCellBold(tableRow0, 2, withTwoDecimals(data.getFirstMonthScope()), 12);
        setTableCellBold(tableRow0, 3, withTwoDecimals(data.getSecondMonthScope()), 12);
        XWPFTableRow tableRow1 = table.getRow(1);
        setTableCell(tableRow1, 0,"Kg di CO2 annuali ", 8);
        setTableCell(tableRow1, 1,"Kg di CO2 annuali ", 8);
        setTableCell(tableRow1, 2,"Kg di CO2 mensili in media ", 8);
        setTableCell(tableRow1, 3,"Kg di CO2 mensili in media ", 8);
        XWPFTableRow tableRow2 = table.getRow(2);
        setTableCell(tableRow2, 0, "prodotti (Scope 1)", 8);
        setTableCell(tableRow2, 1, "prodotti (Scope 2)", 8);
        setTableCell(tableRow2, 2, "prodotti (Scope 1)", 8);
        setTableCell(tableRow2, 3, "prodotti (Scope 2)", 8);
    }

//    public static XWPFRun setTxableCell(XWPFTableRow tableRow, int col, String text, int fontSize) {
//        XWPFRun cellRun = tableRow.getCell(col).getParagraphs().get(0).createRun();
//        cellRun.setFontSize(fontSize);
//        cellRun.setText(text);
//        return cellRun;
//    }

    public static XWPFRun setTableCell(XWPFTableRow tableRow, int col, String text, int fontSize) {
        return setTableCell(tableRow, col, text, fontSize, ParagraphAlignment.LEFT, "ffffff");
    }
    public static XWPFRun setTableCell(XWPFTableRow tableRow, int col, String text, int fontSize, ParagraphAlignment align, String bgColor) {
        XWPFTableCell cell = tableRow.getCell(col);
        cell.getCTTc().addNewTcPr().addNewShd().setFill(bgColor);
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(align);
        XWPFRun cellRun = paragraph.createRun();
        cellRun.setFontSize(fontSize);
        cellRun.setText(text);
        return cellRun;
    }
    public static XWPFRun setTableCellBold(XWPFTableRow tableRow, int col, String text, int fontSize) {
        return setTableCellBold(tableRow, col, text, fontSize, ParagraphAlignment.LEFT, "ffffff");
    }
    public static XWPFRun setTableCellBold(XWPFTableRow tableRow, int col, String text, int fontSize, ParagraphAlignment align, String bgColor) {
        XWPFRun xwpfRun = setTableCell(tableRow, col, text, fontSize, align, bgColor);
        xwpfRun.setBold(true);
        return xwpfRun;
    }

    private static String withTwoDecimals(String value) {
        return String.format("%.2f", Double.parseDouble(value));
    }
    private static XWPFRun _default(XWPFParagraph paragraph, String text) {
        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setText(text);
        return run;
    }

    private static XWPFRun _bold(XWPFParagraph paragraph, String text) {
        XWPFRun run = _default(paragraph, text);
        run.setBold(true);
        return run;
    }

    private static XWPFRun _underlined(XWPFParagraph paragraph, String text) {
        XWPFRun run = _default(paragraph, text);
        run.setUnderline(UnderlinePatterns.SINGLE);
        return run;
    }
}
