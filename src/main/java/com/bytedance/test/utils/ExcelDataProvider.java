package com.bytedance.test.utils;

import org.apache.poi.ss.usermodel.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelDataProvider {

    public static List<Map<String, String>> readTestData(String fileName) {
        List<Map<String, String>> testDataList = new ArrayList<>();

        // 用ClassLoader读取resources下的文件
        try (InputStream is = ExcelDataProvider.class.getClassLoader().getResourceAsStream(fileName);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row currentRow = sheet.getRow(i);
                Map<String, String> rowData = new HashMap<>();

                for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                    String headerName = headerRow.getCell(j).getStringCellValue();
                    String cellValue = getCellValueAsString(currentRow.getCell(j));
                    rowData.put(headerName, cellValue);
                }
                testDataList.add(rowData);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取Excel失败：" + fileName, e);
        }
        return testDataList;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int)cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}