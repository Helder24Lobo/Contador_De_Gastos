package com.personalfinance.contador.service;

import com.personalfinance.contador.model.Expenditure;
import com.personalfinance.contador.model.FixedExpense;
import com.personalfinance.contador.model.Income;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ExcelExportService {

    public static void exportToExcel(String filePath, LocalDate start, LocalDate end,
                                     List<Income> incomes, List<Expenditure> expenses, List<FixedExpense> fixedExpenses) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            // 1. Common Styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);
            CellStyle boldCurrencyStyle = createBoldCurrencyStyle(workbook);

            // 2. Create "Summary" sheet
            createSummarySheet(workbook, start, end, incomes, expenses, fixedExpenses, currencyStyle, boldStyle, dateStyle, boldCurrencyStyle);

            // 3. Create "Incomes" sheet
            createIncomesSheet(workbook, incomes, headerStyle, dateStyle, currencyStyle);

            // 4. Create "Expenses" sheet
            createExpensesSheet(workbook, expenses, headerStyle, dateStyle, currencyStyle);

            // 5. Create "Fixed Expenses" sheet
            createFixedExpensesSheet(workbook, fixedExpenses, headerStyle, currencyStyle);

            // Save the file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createBoldCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createDefaultBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void createSummarySheet(Workbook workbook, LocalDate start, LocalDate end,
                                           List<Income> incomes, List<Expenditure> expenses, List<FixedExpense> fixedExpenses,
                                           CellStyle currencyStyle, CellStyle boldStyle, CellStyle dateStyle, CellStyle boldCurrencyStyle) {
        Sheet sheet = workbook.createSheet("Resumen");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);

        int rowNum = 0;

        // Report Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE FINANCIERO PERSONAL");
        titleCell.setCellStyle(boldStyle);

        // Date range
        Row rangeRow = sheet.createRow(rowNum++);
        rangeRow.createCell(0).setCellValue("Fecha Inicio:");
        Cell startCell = rangeRow.createCell(1);
        startCell.setCellValue(start.toString());
        startCell.setCellStyle(dateStyle);

        Row rangeEndRow = sheet.createRow(rowNum++);
        rangeEndRow.createCell(0).setCellValue("Fecha Fin:");
        Cell endCell = rangeEndRow.createCell(1);
        endCell.setCellValue(end.toString());
        endCell.setCellStyle(dateStyle);

        rowNum++; // Blank space

        // Calculations
        double totalIncomes = incomes.stream().mapToDouble(Income::getValor).sum();
        double totalExpenses = expenses.stream().mapToDouble(Expenditure::getValor).sum();
        double totalFixed = fixedExpenses.stream().mapToDouble(FixedExpense::getValor).sum();
        double netBalance = totalIncomes - totalExpenses - totalFixed;

        // Write Balance Cards
        Row incomeRow = sheet.createRow(rowNum++);
        incomeRow.createCell(0).setCellValue("Total Ingresos:");
        Cell incomeCell = incomeRow.createCell(1);
        incomeCell.setCellValue(totalIncomes);
        incomeCell.setCellStyle(currencyStyle);

        Row expenseRow = sheet.createRow(rowNum++);
        expenseRow.createCell(0).setCellValue("Total Gastos Diarios:");
        Cell expenseCell = expenseRow.createCell(1);
        expenseCell.setCellValue(totalExpenses);
        expenseCell.setCellStyle(currencyStyle);

        Row fixedRow = sheet.createRow(rowNum++);
        fixedRow.createCell(0).setCellValue("Gastos Fijos:");
        Cell fixedCell = fixedRow.createCell(1);
        fixedCell.setCellValue(totalFixed);
        fixedCell.setCellStyle(currencyStyle);

        Row balanceRow = sheet.createRow(rowNum++);
        Cell balanceLabel = balanceRow.createCell(0);
        balanceLabel.setCellValue("Balance Neto:");
        balanceLabel.setCellStyle(boldStyle);
        Cell balanceCell = balanceRow.createCell(1);
        balanceCell.setCellValue(netBalance);
        balanceCell.setCellStyle(boldCurrencyStyle);
    }

    private static void createIncomesSheet(Workbook workbook, List<Income> incomes,
                                            CellStyle headerStyle, CellStyle dateStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Ingresos");
        String[] headers = {"ID", "Fecha", "Descripción", "Tipo", "Valor"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle borderStyle = createDefaultBorderStyle(workbook);

        int rowNum = 1;
        for (Income income : incomes) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(income.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(income.getFecha().toString());
            c1.setCellStyle(dateStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(income.getDescripcion());
            c2.setCellStyle(borderStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(income.getTipo());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(income.getValor());
            c4.setCellStyle(currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createExpensesSheet(Workbook workbook, List<Expenditure> expenses,
                                          CellStyle headerStyle, CellStyle dateStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Gastos");
        String[] headers = {"ID", "Fecha", "Descripción", "Categoría", "Valor", "Observación"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle borderStyle = createDefaultBorderStyle(workbook);

        int rowNum = 1;
        for (Expenditure expense : expenses) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(expense.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(expense.getFecha().toString());
            c1.setCellStyle(dateStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(expense.getDescripcion());
            c2.setCellStyle(borderStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(expense.getCategoria());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(expense.getValor());
            c4.setCellStyle(currencyStyle);

            Cell c5 = row.createCell(5);
            c5.setCellValue(expense.getObservacion() != null ? expense.getObservacion() : "");
            c5.setCellStyle(borderStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createFixedExpensesSheet(Workbook workbook, List<FixedExpense> fixedExpenses,
                                               CellStyle headerStyle, CellStyle currencyStyle) {
        Sheet sheet = workbook.createSheet("Gastos Fijos");
        String[] headers = {"ID", "Nombre", "Valor Mensual", "Día Cobro", "Estado"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        CellStyle borderStyle = createDefaultBorderStyle(workbook);

        int rowNum = 1;
        for (FixedExpense fixedExpense : fixedExpenses) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(fixedExpense.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(fixedExpense.getNombre());
            c1.setCellStyle(borderStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(fixedExpense.getValor());
            c2.setCellStyle(currencyStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(fixedExpense.getDiaCobro());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(fixedExpense.getEstado());
            c4.setCellStyle(borderStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
