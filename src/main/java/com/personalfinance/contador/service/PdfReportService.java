package com.personalfinance.contador.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.personalfinance.contador.model.Gasto;
import com.personalfinance.contador.model.GastoFijo;
import com.personalfinance.contador.model.Ingreso;
import com.personalfinance.contador.model.Presupuesto;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PdfReportService {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores corporativos premium
    private static final Color COLOR_PRIMARY = new Color(31, 58, 86);     // #1f3a56
    private static final Color COLOR_ACCENT = new Color(63, 114, 175);    // #3f72af
    private static final Color COLOR_LIGHT_BG = new Color(247, 249, 252); // #f7f9fc
    private static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125); // #6c757d

    public static void generateFinancialReport(String filePath, String title, LocalDate start, LocalDate end,
                                               List<Ingreso> ingresos, List<Gasto> gastos,
                                               List<GastoFijo> gastosFijos, List<Presupuesto> presupuestos) throws IOException, DocumentException {

        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // 1. Título y Cabecera del Reporte
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COLOR_PRIMARY);
        Paragraph titleParagraph = new Paragraph(title, fontTitle);
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(5);
        document.add(titleParagraph);

        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_TEXT_MUTED);
        Paragraph subtitleParagraph = new Paragraph("Rango de fechas: " + start.format(DATE_FORMAT) + " al " + end.format(DATE_FORMAT) +
                " | Generado el: " + LocalDate.now().format(DATE_FORMAT), fontSubtitle);
        subtitleParagraph.setAlignment(Element.ALIGN_CENTER);
        subtitleParagraph.setSpacingAfter(25);
        document.add(subtitleParagraph);

        // 2. Sección Resumen (Balance Neto)
        double totalIngresos = ingresos.stream().mapToDouble(Ingreso::getValor).sum();
        double totalGastos = gastos.stream().mapToDouble(Gasto::getValor).sum();
        double totalFijos = gastosFijos.stream().filter(g -> g.getEstado().equalsIgnoreCase("Activo")).mapToDouble(GastoFijo::getValor).sum();
        double balanceNeto = totalIngresos - totalGastos - totalFijos;

        document.add(new Paragraph("RESUMEN GENERAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5))); // Separador

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);

        addSummaryCard(summaryTable, "Total Ingresos", CURRENCY_FORMAT.format(totalIngresos), new Color(40, 167, 69)); // Verde
        addSummaryCard(summaryTable, "Total Gastos Diarios", CURRENCY_FORMAT.format(totalGastos), new Color(220, 53, 69)); // Rojo
        addSummaryCard(summaryTable, "Gastos Fijos", CURRENCY_FORMAT.format(totalFijos), new Color(255, 193, 7)); // Amarillo
        addSummaryCard(summaryTable, "Balance Neto", CURRENCY_FORMAT.format(balanceNeto), balanceNeto >= 0 ? new Color(0, 123, 255) : new Color(220, 53, 69)); // Azul o Rojo

        document.add(summaryTable);

        // 3. Detalle de Ingresos
        document.add(new Paragraph("INGRESOS REGISTRADOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (ingresos.isEmpty()) {
            document.add(new Paragraph("No se registraron ingresos en este período.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable tableIngresos = new PdfPTable(new float[]{1.5f, 4f, 2f, 2.5f});
            tableIngresos.setWidthPercentage(100);
            tableIngresos.setSpacingAfter(20);

            addTableHeader(tableIngresos, new String[]{"Fecha", "Descripción", "Tipo", "Valor"});
            for (Ingreso i : ingresos) {
                tableIngresos.addCell(createCell(i.getFecha().format(DATE_FORMAT), Element.ALIGN_CENTER));
                tableIngresos.addCell(createCell(i.getDescripcion(), Element.ALIGN_LEFT));
                tableIngresos.addCell(createCell(i.getTipo(), Element.ALIGN_CENTER));
                tableIngresos.addCell(createCell(CURRENCY_FORMAT.format(i.getValor()), Element.ALIGN_RIGHT));
            }
            document.add(tableIngresos);
        }

        // 4. Detalle de Gastos
        document.add(new Paragraph("GASTOS REGISTRADOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (gastos.isEmpty()) {
            document.add(new Paragraph("No se registraron gastos en este período.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable tableGastos = new PdfPTable(new float[]{1.5f, 3f, 2.5f, 2f, 3f});
            tableGastos.setWidthPercentage(100);
            tableGastos.setSpacingAfter(20);

            addTableHeader(tableGastos, new String[]{"Fecha", "Descripción", "Categoría", "Valor", "Observación"});
            for (Gasto g : gastos) {
                tableGastos.addCell(createCell(g.getFecha().format(DATE_FORMAT), Element.ALIGN_CENTER));
                tableGastos.addCell(createCell(g.getDescripcion(), Element.ALIGN_LEFT));
                tableGastos.addCell(createCell(g.getCategoria(), Element.ALIGN_CENTER));
                tableGastos.addCell(createCell(CURRENCY_FORMAT.format(g.getValor()), Element.ALIGN_RIGHT));
                tableGastos.addCell(createCell(g.getObservacion() != null ? g.getObservacion() : "", Element.ALIGN_LEFT));
            }
            document.add(tableGastos);
        }

        // 5. Gastos Fijos
        document.add(new Paragraph("GASTOS FIJOS DEL MES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (gastosFijos.isEmpty()) {
            document.add(new Paragraph("No se configuraron gastos fijos para este mes.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable tableFijos = new PdfPTable(new float[]{4f, 2.5f, 1.5f, 2f});
            tableFijos.setWidthPercentage(100);
            tableFijos.setSpacingAfter(20);

            addTableHeader(tableFijos, new String[]{"Nombre del Servicio", "Valor Mensual", "Día de Cobro", "Estado"});
            for (GastoFijo gf : gastosFijos) {
                tableFijos.addCell(createCell(gf.getNombre(), Element.ALIGN_LEFT));
                tableFijos.addCell(createCell(CURRENCY_FORMAT.format(gf.getValor()), Element.ALIGN_RIGHT));
                tableFijos.addCell(createCell(String.valueOf(gf.getDiaCobro()), Element.ALIGN_CENTER));
                tableFijos.addCell(createCell(gf.getEstado(), Element.ALIGN_CENTER));
            }
            document.add(tableFijos);
        }

        document.close();
    }

    private static void addSummaryCard(PdfPTable table, String label, String value, Color colorVal) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setPadding(10);
        cell.setBorderWidth(1);
        cell.setBorderColor(new Color(226, 232, 240));

        Paragraph pLabel = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXT_MUTED));
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);

        Paragraph pValue = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, colorVal));
        pValue.setAlignment(Element.ALIGN_CENTER);
        pValue.setSpacingBefore(5);
        cell.addElement(pValue);

        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String[] headers) {
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String title : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(title, fontHeader));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String content, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(content, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(new Color(240, 240, 240));
        return cell;
    }
}
