package com.personalfinance.contador.service;

import com.personalfinance.contador.model.Gasto;
import com.personalfinance.contador.model.GastoFijo;
import com.personalfinance.contador.model.Ingreso;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ExcelExportService {

    public static void exportToExcel(String filePath, LocalDate start, LocalDate end,
                                     List<Ingreso> ingresos, List<Gasto> gastos, List<GastoFijo> gastosFijos) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // 1. Estilos Comunes
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle boldStyle = createBoldStyle(workbook);

            // 2. Creación de la pestaña "Resumen"
            createResumenSheet(workbook, start, end, ingresos, gastos, gastosFijos, currencyStyle, boldStyle, dateStyle);

            // 3. Creación de la pestaña "Ingresos"
            createIngresosSheet(workbook, ingresos, headerStyle, dateStyle, currencyStyle);

            // 4. Creación de la pestaña "Gastos"
            createGastosSheet(workbook, gastos, headerStyle, dateStyle, currencyStyle);

            // 5. Creación de la pestaña "Gastos Fijos"
            createGastosFijosSheet(workbook, gastosFijos, headerStyle, currencyStyle);

            // Guardar el archivo
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

    private static CellStyle createDefaultBorderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void createResumenSheet(Workbook workbook, LocalDate start, LocalDate end,
                                           List<Ingreso> ingresos, List<Gasto> gastos, List<GastoFijo> gastosFijos,
                                           CellStyle currencyStyle, CellStyle boldStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Resumen");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);

        int rowNum = 0;

        // Título del Reporte
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE FINANCIERO PERSONAL");
        titleCell.setCellStyle(boldStyle);

        // Rango de fechas
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

        rowNum++; // Espacio en blanco

        // Cálculos
        double totalIngresos = ingresos.stream().mapToDouble(Ingreso::getValor).sum();
        double totalGastos = gastos.stream().mapToDouble(Gasto::getValor).sum();
        double totalFijos = gastosFijos.stream().filter(g -> g.getEstado().equalsIgnoreCase("Activo")).mapToDouble(GastoFijo::getValor).sum();
        double balanceNeto = totalIngresos - totalGastos - totalFijos;

        // Escribir Tarjetas de Balance
        Row rIng = sheet.createRow(rowNum++);
        rIng.createCell(0).setCellValue("Total Ingresos:");
        Cell cIng = rIng.createCell(1);
        cIng.setCellValue(totalIngresos);
        cIng.setCellStyle(currencyStyle);

        Row rGast = sheet.createRow(rowNum++);
        rGast.createCell(0).setCellValue("Total Gastos Diarios:");
        Cell cGast = rGast.createCell(1);
        cGast.setCellValue(totalGastos);
        cGast.setCellStyle(currencyStyle);

        Row rFij = sheet.createRow(rowNum++);
        rFij.createCell(0).setCellValue("Gastos Fijos Activos:");
        Cell cFij = rFij.createCell(1);
        cFij.setCellValue(totalFijos);
        cFij.setCellStyle(currencyStyle);

        Row rBal = sheet.createRow(rowNum++);
        Cell lblBal = rBal.createCell(0);
        lblBal.setCellValue("Balance Neto:");
        lblBal.setCellStyle(boldStyle);
        Cell cBal = rBal.createCell(1);
        cBal.setCellValue(balanceNeto);
        cBal.setCellStyle(currencyStyle);
        cBal.setCellStyle(boldStyle);
    }

    private static void createIngresosSheet(Workbook workbook, List<Ingreso> ingresos,
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
        for (Ingreso ing : ingresos) {
            Row row = sheet.createRow(rowNum++);
            
            Cell c0 = row.createCell(0);
            c0.setCellValue(ing.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(ing.getFecha().toString());
            c1.setCellStyle(dateStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(ing.getDescripcion());
            c2.setCellStyle(borderStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(ing.getTipo());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(ing.getValor());
            c4.setCellStyle(currencyStyle);
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createGastosSheet(Workbook workbook, List<Gasto> gastos,
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
        for (Gasto g : gastos) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(g.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(g.getFecha().toString());
            c1.setCellStyle(dateStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(g.getDescripcion());
            c2.setCellStyle(borderStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(g.getCategoria());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(g.getValor());
            c4.setCellStyle(currencyStyle);

            Cell c5 = row.createCell(5);
            c5.setCellValue(g.getObservacion() != null ? g.getObservacion() : "");
            c5.setCellStyle(borderStyle);
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createGastosFijosSheet(Workbook workbook, List<GastoFijo> fijos,
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
        for (GastoFijo gf : fijos) {
            Row row = sheet.createRow(rowNum++);

            Cell c0 = row.createCell(0);
            c0.setCellValue(gf.getId());
            c0.setCellStyle(borderStyle);

            Cell c1 = row.createCell(1);
            c1.setCellValue(gf.getNombre());
            c1.setCellStyle(borderStyle);

            Cell c2 = row.createCell(2);
            c2.setCellValue(gf.getValor());
            c2.setCellStyle(currencyStyle);

            Cell c3 = row.createCell(3);
            c3.setCellValue(gf.getDiaCobro());
            c3.setCellStyle(borderStyle);

            Cell c4 = row.createCell(4);
            c4.setCellValue(gf.getEstado());
            c4.setCellStyle(borderStyle);
        }

        // Autoajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
