package it.apeiron.pitagora.core.theorems.dire.service;

import it.apeiron.pitagora.core.dto.ExportRequestDTO;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.EnergyTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.ExportRequestItem;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.FinanceTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.ReactiveMaintenanceTabExportDetail;
import it.apeiron.pitagora.core.dto.ExportRequestDTO.SustainabilityTabExportDetail;
import it.apeiron.pitagora.core.dto.FileDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.util.EncodingUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TableRowAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Service;

@CommonsLog
@Service
@RequiredArgsConstructor
public class ThmDireExportService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final double IMAGE_HW_RATIO = 430D/530D;
    private final int IMAGE_WIDTH = 450;
    private final int IMAGE_HEIGHT = (int) (IMAGE_WIDTH * IMAGE_HW_RATIO);
    private final String DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    @SneakyThrows
    public FileDTO export(ExportRequestDTO dto) {
        XWPFDocument doc = _setupDocument();

        _addTitle(doc, dto.getTitle());
        _addLogo(doc, dto.getLogoBase64());
        _addSitesAndYearsTable(doc, dto.getAddresses(), dto.getYears());

        for (int i = 0; i < dto.getContents().size(); i++) {
            ExportRequestItem item = dto.getContents().get(i);
            if ("CHART".equals(item.getType())) {
                _addImage(doc, (String) item.getData());
            } else if ("FINANCE_STATISTICS_INFO".equals(item.getType())) {
                if ("FINANCE".equals(dto.getTabCode())) {
                    ThmDireExportUtils.addFinanceInfoParagraphs(doc, sp.om.convertValue(item.getData(), FinanceTabExportDetail.class));
                } else if ("REACTIVE_MAINTENANCE".equals(dto.getTabCode())) {
                    ThmDireExportUtils.addReactiveMaintenanceInfoParagraphs(doc, sp.om.convertValue(item.getData(), ReactiveMaintenanceTabExportDetail.class));
                }
            } else if ("INFO".equals(item.getType())) {
                if ("ENERGY".equals(dto.getTabCode())) {
                    ThmDireExportUtils.addEnergyInfoParagraphs(doc, sp.om.convertValue(item.getData(), EnergyTabExportDetail.class));
                } else if ("SUSTAINABILITY".equals(dto.getTabCode())) {
                    ThmDireExportUtils.addSustainabilityInfoParagraphs(doc, sp.om.convertValue(item.getData(), SustainabilityTabExportDetail.class));
                }
            } else if ("TABLE".equals(item.getType())) {
                ThmDireExportUtils.addFinanceTableParagraph(doc, sp.om.convertValue(item.getData(), FinanceTabExportDetail.class).getExpensesBySiteStatsTable());
            }
        }

        return _closeDocument(doc, dto.getTitle());
    }

    private XWPFDocument _setupDocument() {
        XWPFDocument doc = new XWPFDocument();
        doc.setMirrorMargins(true);
        CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.addNewPgMar();
        pageMar.setLeft("700");
        pageMar.setTop("1000");
        pageMar.setRight("700");
        pageMar.setBottom("1000");
        return doc;
    }

    private void _addTitle(XWPFDocument doc, String title) {
        XWPFParagraph titlePar = doc.createParagraph();
        titlePar.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = titlePar.createRun();
        titleRun.setText("Report " + title);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(16);
    }

    private void _addSitesAndYearsTable(XWPFDocument doc, List<String> addresses, List<String> years) {
        XWPFParagraph par = doc.createParagraph();
        XWPFRun run = par.createRun();
        run.setFontSize(10);
        run.setText("Analisi relativa a:");
        run.addBreak();

        XWPFTable table = doc.createTable(2,2);
        table.setWidth("100%");
        table.setTableAlignment(TableRowAlign.CENTER);
        table.getCTTbl().addNewTblGrid().addNewGridCol().setW("1000");
        table.getCTTbl().getTblGrid().addNewGridCol().setW("6000");
        table.setLeftBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setTopBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setRightBorder(XWPFBorderType.SINGLE, 1, 1, "777777");
        table.setBottomBorder(XWPFBorderType.SINGLE, 1, 1, "777777");

        XWPFTableRow tableRowOne = table.getRow(0);
        _setTableCell(tableRowOne, 0, "sedi", 8);
        _setTableCell(tableRowOne, 1, String.join("; ", addresses), 9);
        XWPFTableRow tableRowTwo = table.getRow(1);
        _setTableCell(tableRowTwo, 0,"anni", 8);
        _setTableCell(tableRowTwo, 1, "dal " + years.get(0) + " al " + years.get(1), 9);
    }

    private void _setTableCell(XWPFTableRow tableRow, int col, String text, int fontSize) {
        XWPFRun cellRun = tableRow.getCell(col).getParagraphs().get(0).createRun();
        cellRun.setFontSize(fontSize);
        cellRun.setText(text);
    }
    private void _addLogo(XWPFDocument doc, String imageBase64) throws InvalidFormatException, IOException {
        XWPFParagraph imagePar = doc.createParagraph();
        imagePar.setAlignment(ParagraphAlignment.RIGHT);
        imagePar.setSpacingAfter(2000);
        XWPFRun imageRun = imagePar.createRun();
        imageRun.setTextPosition(20);

        byte[] fileDecoded = EncodingUtils.base64toByteArray(imageBase64);
        imageRun.addPicture(new ByteArrayInputStream(fileDecoded),
                XWPFDocument.PICTURE_TYPE_PNG, "",
                Units.toEMU(50), Units.pixelToEMU(70));
    }

    private void _addImage(XWPFDocument doc, String imageBase64) throws InvalidFormatException, IOException {
        XWPFParagraph imagePar = doc.createParagraph();
        imagePar.setAlignment(ParagraphAlignment.CENTER);
        imagePar.setSpacingBefore(1000);
        XWPFRun imageRun = imagePar.createRun();
        imageRun.setTextPosition(20);

        byte[] fileDecoded = EncodingUtils.base64toByteArray(imageBase64);
        imageRun.addPicture(new ByteArrayInputStream(fileDecoded),
                XWPFDocument.PICTURE_TYPE_PNG, "",
                Units.toEMU(IMAGE_WIDTH), Units.pixelToEMU(IMAGE_HEIGHT));
    }

    private FileDTO _closeDocument(XWPFDocument doc, String filename) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        doc.write(bos);
        bos.close();
        doc.close();

        FileDTO file = new FileDTO();
        file.setFileName("Report " + filename);
        file.setDataBase64("data:" + DOCX_CONTENT_TYPE + ";base64," + EncodingUtils.encodeBase64(bos.toByteArray()));
        file.setContentType(DOCX_CONTENT_TYPE);

        return file;
    }

}
