package com.enterprise.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel Utility class using Apache POI to support data-driven testing.
 */
public class ExcelUtil {

    private static final Logger log = LogManager.getLogger(ExcelUtil.class);

    /**
     * Reads an Excel sheet and returns data as a 2D Object array (ideal for TestNG DataProviders).
     *
     * @param filePath  Path to the Excel file
     * @param sheetName Sheet name to read data from
     * @return 2D array of Objects
     */
    public static Object[][] getSheetData(String filePath, String sheetName) {
        log.info("Reading Excel sheet '{}' from file '{}'", sheetName, filePath);
        Object[][] data = null;

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in Excel file.");
            }

            int rowCount = sheet.getLastRowNum();
            int colCount = sheet.getRow(0).getLastCellNum();

            // Exclude header row from test data count
            data = new Object[rowCount][colCount];

            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < colCount; j++) {
                    if (row != null) {
                        Cell cell = row.getCell(j);
                        data[i - 1][j] = formatter.formatCellValue(cell);
                    } else {
                        data[i - 1][j] = "";
                    }
                }
            }
            log.info("Successfully read {} rows of data.", rowCount);

        } catch (Exception e) {
            log.error("Failed to read Excel data.", e);
            throw new RuntimeException("Excel reading failure.", e);
        }

        return data;
    }

    /**
     * Reads an Excel sheet and returns data as a List of Maps.
     * Each Map represents a row with column headers as keys and cell contents as values.
     *
     * @param filePath  Path to the Excel file
     * @param sheetName Sheet name to read data from
     * @return List of Maps
     */
    public static List<Map<String, String>> getSheetDataAsMapList(String filePath, String sheetName) {
        log.info("Reading Excel sheet '{}' from file '{}' as Map List", sheetName, filePath);
        List<Map<String, String>> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in Excel file.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return dataList;
            }

            int colCount = headerRow.getLastCellNum();
            int rowCount = sheet.getLastRowNum();
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < colCount; j++) {
                    String columnName = formatter.formatCellValue(headerRow.getCell(j));
                    String cellValue = formatter.formatCellValue(row.getCell(j));
                    rowData.put(columnName, cellValue);
                }
                dataList.add(rowData);
            }
            log.info("Successfully read {} rows into Map List.", dataList.size());

        } catch (Exception e) {
            log.error("Failed to read Excel data into Map List.", e);
            throw new RuntimeException("Excel reading failure.", e);
        }

        return dataList;
    }
}
