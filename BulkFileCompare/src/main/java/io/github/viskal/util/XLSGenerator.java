/**
 * 
 */
package io.github.viskal.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Vishy TODO Refactor ,optimize..
 */
public class XLSGenerator {
	// public static final ExecutorService processorPool =
	// Executors.newSingleThreadExecutor();
	private final FileData firstFileData;
	private final FileData secondFileData;
	private final FileComparisonResult fileComparisonResult;
	private final String outPutFileName;
	private final Workbook workbook;// = new XSSFWorkbook();

	public XLSGenerator(FileData firstFileData, FileData secondFileData, FileComparisonResult recordComparisonResult,
			String outPutFileName) {
		super();
		this.firstFileData = firstFileData;
		this.secondFileData = secondFileData;
		this.fileComparisonResult = recordComparisonResult;
		this.outPutFileName = outPutFileName;

		// this.workbook = createWorkbook("HSSF");
		if (firstFileData.isBigFile() || secondFileData.isBigFile()) {
			this.workbook = createWorkbook("SXSSF");
		} else {
			this.workbook = createWorkbook("HSSF");
		}
		// this.workbook = createWorkbook("HSSF");
		// this.workbook = createWorkbook("XHSSF");

	}

	static Workbook createWorkbook(String type) {
		if ("HSSF".equals(type))
			return new HSSFWorkbook();
		else if ("XSSF".equals(type))
			return new XSSFWorkbook();
		else if ("SXSSF".equals(type))
			return new SXSSFWorkbook();
		else
			return null;
	}

	public void createXlsFileData() {
		createDuplicateDataSheet(workbook, "FirstFileDuplicates", firstFileData);

		createDuplicateDataSheet(workbook, "SecondFileDuplicates", secondFileData);

		createErrorsDataSheet(workbook, "FirstFileErrors", firstFileData);

		createErrorsDataSheet(workbook, "SecondFileErrors", secondFileData);

		createDataSheets(workbook, fileComparisonResult);

	}

	public void persisitFile() throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outPutFileName)));
		workbook.write(out);
		out.close();
	}

	private void createDuplicateDataSheet(Workbook workbook, String sheetName, FileData fileData) {
		Sheet sheet = workbook.createSheet(sheetName);

		List<String> firstFileDuplicatesList = fileData.getDuplicates();

		int rownum = 0;
		Row row = sheet.createRow(rownum++);
		Cell cell = row.createCell(0);
		CellStyle style = createCellStyle(workbook);
		cell.setCellStyle(style);

		cell.setCellValue("Duplicates");
		for (String firstFileDuplicates : firstFileDuplicatesList) {
			row = sheet.createRow(rownum++);
			int cellnum = 0;
			cell = row.createCell(cellnum++);
			cell.setCellValue(firstFileDuplicates);

		}
	}

	private void createErrorsDataSheet(Workbook workbook, String sheetName, FileData fileData) {
		Sheet sheet = workbook.createSheet(sheetName);

		List<String> errorsList = fileData.getErrors();

		int rownum = 0;
		Row row = sheet.createRow(rownum++);
		Cell cell = row.createCell(0);
		CellStyle style = createCellStyle(workbook);
		cell.setCellStyle(style);

		cell.setCellValue("Errors");
		for (String errors : errorsList) {
			row = sheet.createRow(rownum++);
			int cellnum = 0;
			cell = row.createCell(cellnum++);
			cell.setCellValue(errors);

		}
	}

	// static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>>
	// entriesSortedByValues(Map<K, V> map) {
	// SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K,
	// V>>(new Comparator<Map.Entry<K, V>>() {
	// @Override
	// public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
	// int res = e1.getValue().compareTo(e2.getValue());
	// return res != 0 ? res : 1;
	// }
	// });
	// sortedEntries.addAll(map.entrySet());
	// return sortedEntries;
	// }

	private void createDataSheets(Workbook workbook, FileComparisonResult fileComparisonResult) {
		Sheet sheetPartialMatch = workbook.createSheet("Partial Match");
		Sheet sheetPerfectMatch = workbook.createSheet("Perfect Match");
		Sheet sheetMissingInFirst = workbook.createSheet("Missing In First");
		Sheet sheetMissingInSecond = workbook.createSheet("Missing In Second");
		CellStyle style = createCellStyle(workbook);

		createTopHeaderRow(workbook, sheetPartialMatch, style);
		createTopHeaderRowPerfectMatch(workbook, sheetPerfectMatch, style);
		createTopHeaderRow(workbook, sheetMissingInFirst, style);
		createTopHeaderRow(workbook, sheetMissingInSecond, style);

		int rownumForSheetPartialMatch = 1;
		int rownumForSheetPerfectMatch = 1;
		int rownumForSheetMissingInFirst = 1;
		int rownumForSheetMissingInSecond = 1;

		Row row = null;
		List<RecordComparisonResult> recordComparisonResultList = fileComparisonResult.getRecordComparisonResultList();
		Collections.sort(recordComparisonResultList, RecordComparisonResult.fileComparisonResultComparator);
		for (RecordComparisonResult recordComparisonResult : recordComparisonResultList) {
			if (recordComparisonResult.isPartialMatch()) {
				row = sheetPartialMatch.createRow(rownumForSheetPartialMatch++);
				createRowCellData(row, recordComparisonResult.getKey(),
						recordComparisonResult.getDifferences().toString());
			} else if (recordComparisonResult.isMissingInFirst()) {
				row = sheetMissingInFirst.createRow(rownumForSheetMissingInFirst++);
				createRowCellData(row, recordComparisonResult.getKey(),
						recordComparisonResult.getSecondDataFileRecords().toString());
			} else if (recordComparisonResult.isMissingInSecond()) {
				row = sheetMissingInSecond.createRow(rownumForSheetMissingInSecond++);
				createRowCellData(row, recordComparisonResult.getKey(),
						recordComparisonResult.getFirstDataFileRecords().toString());
			} else if (recordComparisonResult.isPerfectMatch()) {
				row = sheetPerfectMatch.createRow(rownumForSheetPerfectMatch++);
				createRowCellData(row, recordComparisonResult.getKey(),
						recordComparisonResult.getFirstDataFileRecords().toString(),
						recordComparisonResult.getSecondDataFileRecords().toString());
			}
		}

		Sheet summarySheet = workbook.createSheet("Summary");
		createSummaryTopHeaderRow(workbook, summarySheet, style);

		row = summarySheet.createRow(1);

		int cellnum = 0;
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(firstFileData.getDuplicates().size());
		cell = row.createCell(cellnum++);
		cell.setCellValue(secondFileData.getDuplicates().size());
		cell = row.createCell(cellnum++);
		cell.setCellValue(rownumForSheetMissingInFirst - 1);
		cell = row.createCell(cellnum++);
		cell.setCellValue(rownumForSheetMissingInSecond - 1);
		cell = row.createCell(cellnum++);
		cell.setCellValue(rownumForSheetPerfectMatch - 1);
		cell = row.createCell(cellnum++);
		cell.setCellValue(rownumForSheetPartialMatch - 1);
		cell = row.createCell(cellnum++);
		final HashMap<String, Integer> mismatchCols = fileComparisonResult.getMismatchColumns();

		List<String> values = new ArrayList<String>(mismatchCols.keySet());
		Collections.sort(values, new Comparator<String>() {
			public int compare(String a, String b) {
				Integer aInt1 = mismatchCols.get(a);
				Integer aInt2 = mismatchCols.get(b);
				return aInt2.intValue() - aInt1.intValue();
			}
		});
		StringBuilder sb = new StringBuilder();
		for (String val : values) {
			sb.append(String.format("[%s = %d] ", val, mismatchCols.get(val).intValue()));
		}
		cell.setCellValue(sb.toString());

	}

	private void createRowCellData(Row row, String key, String... cellNoText) {
		int cellnum = 0;
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(key);

		for (String cellNo : cellNoText) {
			cell = row.createCell(cellnum++);
			cell.setCellValue(cellNo);
		}

	}

	private void createTopHeaderRowPerfectMatch(Workbook workbook2, Sheet sheetPerfectMatch, CellStyle style) {
		Row row = sheetPerfectMatch.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Key");

		cell.setCellStyle(style);

		cell = row.createCell(1);
		cell.setCellValue("First File Records");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("Second File Records");
		cell.setCellStyle(style);

	}

	private void createSummaryTopHeaderRow(Workbook workbook, Sheet summarySheet, CellStyle style) {
		Row row = summarySheet.createRow(0);

		Cell cell = row.createCell(0);
		cell.setCellValue("Duplicate Count ( File1)");
		cell.setCellStyle(style);

		cell = row.createCell(1);
		cell.setCellValue("Duplicate Count ( File2)");
		cell.setCellStyle(style);

		cell = row.createCell(2);
		cell.setCellValue("Missing in  File1 Count");
		cell.setCellStyle(style);

		cell = row.createCell(3);
		cell.setCellValue("Missing in  File2 Count");
		cell.setCellStyle(style);

		cell = row.createCell(4);
		cell.setCellValue("Perfect Match Count");
		cell.setCellStyle(style);

		cell = row.createCell(5);
		cell.setCellValue("Partial Match Count");
		cell.setCellStyle(style);

		cell = row.createCell(6);
		cell.setCellValue("Partial Match Mismatch Column Count");
		cell.setCellStyle(style);

	}

	private void createTopHeaderRow(Workbook workbook, Sheet sheetPartialMatch, CellStyle style) {
		Row row = sheetPartialMatch.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Key");

		cell.setCellStyle(style);

		cell = row.createCell(1);
		cell.setCellValue("Details");
		cell.setCellStyle(style);

	}

	private CellStyle createCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);// setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		return style;

	}
}
