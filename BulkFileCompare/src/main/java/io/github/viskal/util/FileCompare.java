/**
 * 
 */
package io.github.viskal.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Vishy TODO Refactor ,optimize etc
 */
public class FileCompare {

	public static final String DEFAULT_ALL_COLUMN_NAME_LIST_FILE_NAME = "colnames.txt";
	public static final String DEFAULT_ALL_COLUMN_NAME_LIST_FILE1_NAME = "colnames1.txt";

	public static final String DEFAULT_ALL_COLUMN_NAME_LIST_FILE2_NAME = "colnames2.txt";

	public static final String DEFAULT_ALL_KEY_COLUMN_NAME_LIST_FILE_NAME = "keycolnames.txt";
	public static final String DEFAULT_IGNORE_COLUMN_NAME_LIST_FILE_NAME = "ignore.txt";
	public static final String DEFAULT_FILE1_NAME = "file1.dat";
	public static final String DEFAULT_FILE2_NAME = "file2.dat";
	public static final String DEFAULT_OUTPUT_FILE = "comp.xls";
	public static final int NO_OF_FILES = 2;
	public static final String DEFAULT_DELIMITTER = "|";

	private Set<String> keyColumnNamesSet = new LinkedHashSet<String>();
	private Set<String> ignoreColumnNamesSet = new HashSet<String>();
	private Map<String, Integer> allColumnNamesFile1Map = new HashMap<String, Integer>();
	private Map<Integer, String> allColumnNamesFile1ReverseMap = new HashMap<Integer, String>();

	private Map<String, Integer> allColumnNamesFile2Map = new HashMap<String, Integer>();
	private Map<Integer, Integer> file1File2ColumnMapIndex = new HashMap<Integer, Integer>();

	private Map<Integer, String> ignoreColumnNameIndexMap = new HashMap<Integer, String>();

	private FileData file1Data;
	private FileData file2Data;
	private String allColumnNamesFile1 = DEFAULT_ALL_COLUMN_NAME_LIST_FILE1_NAME;
	private String allColumnNamesFile2 = DEFAULT_ALL_COLUMN_NAME_LIST_FILE2_NAME;

	private String keyColumnNamesFile = DEFAULT_ALL_KEY_COLUMN_NAME_LIST_FILE_NAME;
	private String ignoreColumnNamesFile = DEFAULT_IGNORE_COLUMN_NAME_LIST_FILE_NAME;

	private List<RecordComparisonResult> recordComparisonResultList = new ArrayList<RecordComparisonResult>();

	private HashMap<String, Integer> mismatchColumnCountMap = new HashMap<String, Integer>();

	private String outPutFileName = DEFAULT_OUTPUT_FILE;

	private void loadColumnNames(String columns, Set<String> columnNameSet) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(columns));
		String columnName = null;
		try {
			while ((columnName = reader.readLine()) != null) {
				if (!columnName.trim().isEmpty()) {
					if (!columnNameSet.add(columnName.trim())) {
						throw new IOException(
								String.format("Duplicate Column (%s) in the file (%s), remove it and try again",
										columnName, columns));
					}
				}
			}
		} finally {
			reader.close();
		}
	}

	private void createFile1File2ColumnMapIndex() {
		Set<String> file1ColumnKeySet = allColumnNamesFile1Map.keySet();
		for (String fileColKey : file1ColumnKeySet) {
			Integer file1ColumnIndex = allColumnNamesFile1Map.get(fileColKey);
			Integer file2ColumnIndex = allColumnNamesFile2Map.get(fileColKey);
			file1File2ColumnMapIndex.put(file1ColumnIndex, file2ColumnIndex);
		}
	}

	private void loadFileColumnNamesMap(String fileNameWithColumns, Map<String, Integer> allColumnNamesFile1Map)
			throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(fileNameWithColumns));
		String columnName = null;
		int i = 0;
		try {
			while ((columnName = reader.readLine()) != null) {
				if (!columnName.trim().isEmpty()) {
					if (allColumnNamesFile1Map.containsKey(columnName.trim())) {
						throw new IOException(
								String.format("Duplicate Column (%s) in the file (%s), remove it and try again",
										columnName, fileNameWithColumns));
					}
					allColumnNamesFile1Map.put(columnName.trim(), Integer.valueOf(i++));
				}
			}
		} finally {
			reader.close();
		}
	}

	private void loadAllColumnNamesFile1Map() throws IOException {
		loadFileColumnNamesMap(allColumnNamesFile1, allColumnNamesFile1Map);
		for (String key : allColumnNamesFile1Map.keySet()) {
			allColumnNamesFile1ReverseMap.put(allColumnNamesFile1Map.get(key), key);
		}
	}

	private void loadAllColumnNamesFile2Map() throws IOException {
		loadFileColumnNamesMap(allColumnNamesFile2, allColumnNamesFile2Map);
	}

	private void loadKeyColumnNamesSet() throws IOException {
		loadColumnNames(keyColumnNamesFile, keyColumnNamesSet);
	}

	private void loadIgnoreColumnNamesSet() throws IOException {
		loadColumnNames(ignoreColumnNamesFile, ignoreColumnNamesSet);
	}

	private void performSanityChecksForMap() throws IOException {
		if (allColumnNamesFile1Map.isEmpty())
			throw new IOException("Column Names Empty in :" + allColumnNamesFile1);
		if (allColumnNamesFile2Map.isEmpty())
			throw new IOException("Column Names Empty in :" + allColumnNamesFile2);
		if (keyColumnNamesSet.isEmpty())
			throw new IOException("No Key Columns Specified in :" + keyColumnNamesFile);
		for (String ignoreColumnNames : ignoreColumnNamesSet) {
			List<String> ignoreInKeyList = new ArrayList<String>();
			if (keyColumnNamesSet.contains(ignoreColumnNames)) {
				ignoreInKeyList.add(ignoreColumnNames);
			}
			if (!ignoreInKeyList.isEmpty()) {
				throw new IOException(String.format("IgnoreColumnNames :(%s) Should not appear in Keycolumns (%s) ",
						ignoreInKeyList, keyColumnNamesSet));
			}
		}
	}

	public static void main(final String[] args) throws InterruptedException, ExecutionException, IOException {
		if (args.length != 0 && args.length != 9) {
			System.out.println(
					"Arguments should be  : Usage : java -jar fc.jar <columnNamesFile1> <columnNamesFile2> <keyColumnNamesFile> <ignorecollist> <firstFile> <secondFile> <outPutFile> <delimitterFile1> <delimitterFile2>");
			System.out.println(
					"Example ( default): java -jar fc.jar colnamesfile1.txt  colnamesfile2.txt keycolnames.txt ignore.txt file1.dat file2.dat comp.xls | |");
			return;
		}

		FileCompare fileCompare;
		String keyColumnNamesFile = DEFAULT_ALL_KEY_COLUMN_NAME_LIST_FILE_NAME;
		String allColumnNamesFile1 = DEFAULT_ALL_COLUMN_NAME_LIST_FILE1_NAME;
		String allColumnNamesFile2 = DEFAULT_ALL_COLUMN_NAME_LIST_FILE2_NAME;

		String ignoreColumnNamesFile = DEFAULT_IGNORE_COLUMN_NAME_LIST_FILE_NAME;

		String firstFileName = DEFAULT_FILE1_NAME;
		String secondFileName = DEFAULT_FILE2_NAME;
		String outPutFileName = DEFAULT_FILE1_NAME;
		String delimitterFile1 = DEFAULT_DELIMITTER;
		String delimitterFile2 = DEFAULT_DELIMITTER;

		if (args.length == 9) {
			// allColumnNamesFile = args[0];
			allColumnNamesFile1 = args[0];
			allColumnNamesFile2 = args[1];
			keyColumnNamesFile = args[2];
			ignoreColumnNamesFile = args[3];
			firstFileName = args[4];
			secondFileName = args[5];
			outPutFileName = args[6];
			delimitterFile1 = args[7];
			delimitterFile2 = args[8];

		}
		fileCompare = new FileCompare(allColumnNamesFile1, allColumnNamesFile2, keyColumnNamesFile,
				ignoreColumnNamesFile, firstFileName, secondFileName, outPutFileName, delimitterFile1, delimitterFile2);
		// final int noOfCores = Runtime.getRuntime().availableProcessors();

		// final ExecutorService executorPool =
		// Executors.newFixedThreadPool(noOfCores * 2);

		fileCompare.generateXlsOutput();

	}

	public FileCompare(String allColumnNamesFile1, String allColumnNamesFile2, String keyColumnNamesFile,
			String ignoreColumnNamesFile, String firstFileName, String secondFileName, String outPutFileName,
			String delimitterFile1, String delimitteFile2) {
		super();
		this.allColumnNamesFile1 = allColumnNamesFile1;
		this.allColumnNamesFile2 = allColumnNamesFile2;

		this.keyColumnNamesFile = keyColumnNamesFile;
		this.ignoreColumnNamesFile = ignoreColumnNamesFile;
		this.outPutFileName = outPutFileName;
		file1Data = new FileData(firstFileName, delimitterFile1);
		file2Data = new FileData(secondFileName, delimitteFile2);
	}

	private void loadFile1Data() throws IOException {
		file1Data.loadData();
	}

	private void loadFile2Data() throws IOException {
		file2Data.loadData();
	}

	private void createIgnoreColumnMap() {
		for (String columnName : ignoreColumnNamesSet) {
			ignoreColumnNameIndexMap.put(allColumnNamesFile1Map.get(columnName), columnName);
		}
	}

	private void preProcessFile1Data() throws IOException {
		file1Data.preProcessData(allColumnNamesFile1Map, keyColumnNamesSet);
	}

	private void preProcessFile2Data() throws IOException {
		file2Data.preProcessData(allColumnNamesFile2Map, keyColumnNamesSet);
	}

	private void generateXlsFile(FileComparisonResult fileComparisonResult)
			throws IOException, InterruptedException, ExecutionException {
		final XLSGenerator xlsGenerator = new XLSGenerator(file1Data, file2Data, fileComparisonResult, outPutFileName);
		long start = System.currentTimeMillis();

		xlsGenerator.createXlsFileData();
		long end = System.currentTimeMillis();
		long diff = end - start;
		System.out.format("\nTime taken ( in milli secs) : (%d) for creating (%s) data  ", diff, outPutFileName);
		start = System.currentTimeMillis();

		xlsGenerator.persisitFile();
		end = System.currentTimeMillis();
		diff = end - start;

		System.out.format("\nTime taken ( in milli secs) : (%d) for writing the (%s) data onto disk  ", diff,
				outPutFileName);

	}

	public void cleanup() {
		file1Data = null;
		file2Data = null;
		keyColumnNamesSet = null;
		ignoreColumnNamesSet = null;
		allColumnNamesFile1Map = null;
		allColumnNamesFile1ReverseMap = null;
		allColumnNamesFile2Map = null;
		file1File2ColumnMapIndex = null;
		ignoreColumnNameIndexMap = null;
		recordComparisonResultList = null;
		mismatchColumnCountMap = null;

	}

	public void generateXlsOutput() throws InterruptedException, ExecutionException, IOException {
		final ExecutorService executorPool = Executors.newFixedThreadPool(2);

		try {

			loadAllColumnNamesFile1Map();
			loadAllColumnNamesFile2Map();
			createFile1File2ColumnMapIndex();
			loadKeyColumnNamesSet();
			loadIgnoreColumnNamesSet();
			performSanityChecksForMap();
			createIgnoreColumnMap();
			loadFile1Data();
			loadFile2Data();
			CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorPool);

			completionService.submit(new Callable<Void>() {
				public Void call() throws Exception {
					preProcessFile1Data();
					return null;
				}

			});

			completionService.submit(new Callable<Void>() {
				public Void call() throws Exception {
					preProcessFile2Data();
					return null;
				}

			});

			for (int i = 0; i < 2; i++) {
				Future<Void> xlsGenerationFuture = completionService.take();
				xlsGenerationFuture.get();

			}

			FileComparisonResult fileComparisonResult = compareFiles(executorPool);
			System.out.format("\nFile comparison between %s and %s completed.. Starting with %s File generation...",
					file1Data.getFileName(), file2Data.getFileName(), outPutFileName);
			generateXlsFile(fileComparisonResult);
			cleanup();
		} finally {
			executorPool.shutdown();
		}

	}

	public FileComparisonResult compareFiles(ExecutorService executorPool)
			throws InterruptedException, ExecutionException, IOException {

		CompletionService<List<RecordComparisonResult>> completionService = new ExecutorCompletionService<List<RecordComparisonResult>>(
				executorPool);

		completionService.submit(new Callable<List<RecordComparisonResult>>() {
			public List<RecordComparisonResult> call() throws Exception {
				List<RecordComparisonResult> recordComparisonResultListFromIteratingFirstFile = new ArrayList<RecordComparisonResult>();

				for (final String key : file1Data.getContent().keySet()) {

					Map<String, List<String>> firstFileContentMap = file1Data.getContent();
					List<String> firstFileContent = firstFileContentMap.get(key);
					Map<String, List<String>> secondFileContentMap = file2Data.getContent();
					List<String> secondFileContent = secondFileContentMap.get(key);

					if (secondFileContent == null) {
						// no match in second file
						recordComparisonResultListFromIteratingFirstFile
								.add(RecordComparisonResult.missingInSecondComparisonResult(key, firstFileContent));
						continue;

					}

					List<String> differences = new ArrayList<String>();
					for (int i = 0; i < firstFileContent.size(); i++) {
						if (ignoreColumnNameIndexMap.containsKey(Integer.valueOf(i))) {
							// System.out.println(String.format("Will ignore
							// column {%s} at column no {%d}",
							// ignoreColumnNameIndexMap.get(i), i + 1));
							continue;
						}
						if (file1File2ColumnMapIndex.get(i) == null) {
							// System.out.println(String.format(
							// "There is no mapping column (%s) in second file
							// for the column no (%d) in first file, so will
							// skip this column in comparison ",
							// allColumnNamesFile1ReverseMap.get(i), i + 1));
							continue;
						}
						if (!firstFileContent.get(i).equals(secondFileContent.get(file1File2ColumnMapIndex.get(i)))) {
							differences.add(String.format(
									" At First File Column %d Second File Column %d, %s  [%s] vs [%s]  ", i + 1,
									file1File2ColumnMapIndex.get(i) + 1, allColumnNamesFile1ReverseMap.get(i),
									firstFileContent.get(i), secondFileContent.get(file1File2ColumnMapIndex.get(i))));
							String currentKey = allColumnNamesFile1ReverseMap.get(i);
							if (!mismatchColumnCountMap.containsKey(currentKey)) {
								mismatchColumnCountMap.put(currentKey, Integer.valueOf(1));
							} else {
								mismatchColumnCountMap.put(currentKey,
										Integer.valueOf(mismatchColumnCountMap.get(currentKey).intValue() + 1));
							}

						}
					}

					if (differences.isEmpty()) {
						// perfect match
						recordComparisonResultListFromIteratingFirstFile.add(RecordComparisonResult
								.matchRecordComparisonResult(key, firstFileContent, secondFileContent));
						continue;

					}

					recordComparisonResultListFromIteratingFirstFile.add(RecordComparisonResult
							.partialMatchRecordComparisonResult(key, firstFileContent, secondFileContent, differences));

				}

				return recordComparisonResultListFromIteratingFirstFile;
			}

		});

		completionService.submit(new Callable<List<RecordComparisonResult>>() {
			public List<RecordComparisonResult> call() throws Exception {
				return addMissingInFirst();
			}
		});

		for (int i = 0; i < 2; i++) {
			Future<List<RecordComparisonResult>> xlsGenerationFuture = completionService.take();
			recordComparisonResultList.addAll(xlsGenerationFuture.get());
		}

		return new FileComparisonResult(recordComparisonResultList, mismatchColumnCountMap);
	}

	private List<RecordComparisonResult> addMissingInFirst() {
		List<RecordComparisonResult> recordComparisonResultListFromIteratingSecondFile = new ArrayList<RecordComparisonResult>();
		for (final String secondFileKey : file2Data.getContent().keySet()) {
			Map<String, List<String>> firstFileContentMap = file1Data.getContent();
			List<String> firstFileContent = firstFileContentMap.get(secondFileKey);
			Map<String, List<String>> secondFileContentMap = file2Data.getContent();
			List<String> secondFileContent = secondFileContentMap.get(secondFileKey);
			if (firstFileContent == null) {
				recordComparisonResultListFromIteratingSecondFile
						.add(RecordComparisonResult.missingInFirstComparisonResult(secondFileKey, secondFileContent));
			}
		}
		return recordComparisonResultListFromIteratingSecondFile;

	}

}
