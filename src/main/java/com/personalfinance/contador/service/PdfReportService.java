package com.personalfinance.contador.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.personalfinance.contador.model.Expenditure;
import com.personalfinance.contador.model.FixedExpense;
import com.personalfinance.contador.model.Income;

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

    // Premium corporate colors
    private static final Color COLOR_PRIMARY = new Color(31, 58, 86);      // #1f3a56
    private static final Color COLOR_ACCENT = new Color(63, 114, 175);     // #3f72af
    private static final Color COLOR_LIGHT_BG = new Color(247, 249, 252);  // #f7f9fc
    private static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125); // #6c757d

    public static void generateFinancialReport(String filePath, String title, LocalDate start, LocalDate end,
                                               List<Income> incomes, List<Expenditure> expenses,
                                               List<FixedExpense> fixedExpenses) throws IOException, DocumentException {

        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // 1. Report Title and Header
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

        // 2. Summary Section (Net Balance)
        double totalIncomes = incomes.stream().mapToDouble(Income::getValor).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expenditure::getValor).sum();
        double totalFixed = fixedExpenses.stream().mapToDouble(FixedExpense::getValor).sum();
        double netBalance = totalIncomes - totalExpenses - totalFixed;

        document.add(new Paragraph("RESUMEN GENERAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5))); // Separator

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(20);

        addSummaryCard(summaryTable, "Total Ingresos", CURRENCY_FORMAT.format(totalIncomes), new Color(40, 167, 69));    // Green
        addSummaryCard(summaryTable, "Total Gastos Diarios", CURRENCY_FORMAT.format(totalExpenses), new Color(220, 53, 69)); // Red
        addSummaryCard(summaryTable, "Gastos Fijos", CURRENCY_FORMAT.format(totalFixed), new Color(255, 193, 7));        // Yellow
        addSummaryCard(summaryTable, "Balance Neto", CURRENCY_FORMAT.format(netBalance), netBalance >= 0 ? new Color(0, 123, 255) : new Color(220, 53, 69)); // Blue or Red

        document.add(summaryTable);

        // 3. Income Detail
        document.add(new Paragraph("INGRESOS REGISTRADOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (incomes.isEmpty()) {
            document.add(new Paragraph("No se registraron ingresos en este período.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable incomesTable = new PdfPTable(new float[]{1.5f, 4f, 2f, 2.5f});
            incomesTable.setWidthPercentage(100);
            incomesTable.setSpacingAfter(20);

            addTableHeader(incomesTable, new String[]{"Fecha", "Descripción", "Tipo", "Valor"});
            for (Income income : incomes) {
                incomesTable.addCell(createCell(income.getFecha().format(DATE_FORMAT), Element.ALIGN_CENTER));
                incomesTable.addCell(createCell(income.getDescripcion(), Element.ALIGN_LEFT));
                incomesTable.addCell(createCell(income.getTipo(), Element.ALIGN_CENTER));
                incomesTable.addCell(createCell(CURRENCY_FORMAT.format(income.getValor()), Element.ALIGN_RIGHT));
            }
            document.add(incomesTable);
        }

        // 4. Expense Detail
        document.add(new Paragraph("GASTOS REGISTRADOS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (expenses.isEmpty()) {
            document.add(new Paragraph("No se registraron gastos en este período.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable expensesTable = new PdfPTable(new float[]{1.5f, 3f, 2.5f, 2f, 3f});
            expensesTable.setWidthPercentage(100);
            expensesTable.setSpacingAfter(20);

            addTableHeader(expensesTable, new String[]{"Fecha", "Descripción", "Categoría", "Valor", "Observación"});
            for (Expenditure expense : expenses) {
                expensesTable.addCell(createCell(expense.getFecha().format(DATE_FORMAT), Element.ALIGN_CENTER));
                expensesTable.addCell(createCell(expense.getDescripcion(), Element.ALIGN_LEFT));
                expensesTable.addCell(createCell(expense.getCategoria(), Element.ALIGN_CENTER));
                expensesTable.addCell(createCell(CURRENCY_FORMAT.format(expense.getValor()), Element.ALIGN_RIGHT));
                expensesTable.addCell(createCell(expense.getObservacion() != null ? expense.getObservacion() : "", Element.ALIGN_LEFT));
            }
            document.add(expensesTable);
        }

        // 5. Fixed Expenses
        document.add(new Paragraph("GASTOS FIJOS DEL MES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 5)));

        if (fixedExpenses.isEmpty()) {
            document.add(new Paragraph("No se configuraron gastos fijos para este mes.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
        } else {
            PdfPTable fixedTable = new PdfPTable(new float[]{4f, 2.5f, 1.5f, 2f});
            fixedTable.setWidthPercentage(100);
            fixedTable.setSpacingAfter(20);

            addTableHeader(fixedTable, new String[]{"Nombre del Servicio", "Valor Mensual", "Día de Cobro", "Estado"});
            for (FixedExpense fixedExpense : fixedExpenses) {
                fixedTable.addCell(createCell(fixedExpense.getNombre(), Element.ALIGN_LEFT));
                fixedTable.addCell(createCell(CURRENCY_FORMAT.format(fixedExpense.getValor()), Element.ALIGN_RIGHT));
                fixedTable.addCell(createCell(String.valueOf(fixedExpense.getDiaCobro()), Element.ALIGN_CENTER));
                fixedTable.addCell(createCell(fixedExpense.getEstado(), Element.ALIGN_CENTER));
            }
            document.add(fixedTable);
        }

        document.close();
    }

    private static void addSummaryCard(PdfPTable table, String label, String value, Color colorVal) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_LIGHT_BG);
        cell.setPadding(10);
        cell.setBorderWidth(1);
        cell.setBorderColor(new Color(226, 232, 240));

        Paragraph labelParagraph = new Paragraph(label.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_TEXT_MUTED));
        labelParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelParagraph);

        Paragraph valueParagraph = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, colorVal));
        valueParagraph.setAlignment(Element.ALIGN_CENTER);
        valueParagraph.setSpacingBefore(5);
        cell.addElement(valueParagraph);

        table.addCell(cell);
    }

    private static void addTableHeader(PdfPTable table, String[] headers) {
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, fontHeader));
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
