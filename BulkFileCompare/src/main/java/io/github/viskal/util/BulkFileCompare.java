package io.github.viskal.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/*
 * TODO : Refactor , optimize, unit tests etc 
 */
public class BulkFileCompare {

	// public static final ExecutorService writerExecutorPool =
	// Executors.newSingleThreadExecutor();

	public static final String DEFAULT_FILE_COMP_META_DATA = "meta.txt";
	public static final String fileCompDetails = DEFAULT_FILE_COMP_META_DATA;
	public static final String DEFAULT_DELIMITTER = " ";
	public static final int NO_OF_COLS = 9;

	public List<FileCompare> fileCompareList = new ArrayList<FileCompare>();

	public List<FileCompare> loadFileComparisonMetaData(String fileCompDetails) throws IOException {

		final BufferedReader reader = new BufferedReader(new FileReader(fileCompDetails));

		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty())
					continue;
				final String[] lineFromMetaDataFile = line.split(Pattern.quote(DEFAULT_DELIMITTER), -1);

				if (lineFromMetaDataFile == null || lineFromMetaDataFile.length != NO_OF_COLS) {
					throw new IOException(String.format(
							"lineFromMetaDataFile : (%s) read from file is null or meta data columns (%d) does not match the expected (%d) number of columns  ",
							line, lineFromMetaDataFile.length, NO_OF_COLS));
				}

				fileCompareList
						.add(new FileCompare(lineFromMetaDataFile[0], lineFromMetaDataFile[1], lineFromMetaDataFile[2],
								lineFromMetaDataFile[3], lineFromMetaDataFile[4], lineFromMetaDataFile[5],
								lineFromMetaDataFile[6], lineFromMetaDataFile[7], lineFromMetaDataFile[8]));

			}
		} finally {
			reader.close();
		}
		return fileCompareList;

	}

	public static void main(final String[] args) throws InterruptedException, ExecutionException, IOException {
		if (args.length != 0 && args.length != 1) {
			System.out.println("Arguments should be 1 : Usage : java -jar bfc.jar <fileComparisonMetaDataFile>");
			System.out.println("Example ( default): java -jar bfc.jar meta.txt");
			return;
		}
		BulkFileCompare bulkFileCompare = new BulkFileCompare();

		String fileCompareMetaData = DEFAULT_FILE_COMP_META_DATA;
		if (args.length == 1) {
			fileCompareMetaData = args[0];
		}
		bulkFileCompare.loadFilesAndGenerateXls(fileCompareMetaData);
	}

	public void loadFilesAndGenerateXls(String fileCompareMetaData)
			throws InterruptedException, ExecutionException, IOException {
		loadFileComparisonMetaData(fileCompareMetaData);
		generateXlsOutput(fileCompareMetaData);

	}

	private void generateXlsOutput(String fileCompareMetaData)
			throws InterruptedException, ExecutionException, IOException {

		final int noOfCores = Runtime.getRuntime().availableProcessors();

		final ExecutorService mainExecutorPool = Executors.newFixedThreadPool(noOfCores * 2);
		// final ExecutorService executorPool =
		// Executors.newFixedThreadPool(noOfCores * 3);

		// long start = System.currentTimeMillis();
		// for (final FileCompare fileCompare : fileCompareList) {
		//
		// fileCompare.generateXlsOutput();
		//
		// }
		// long end = System.currentTimeMillis();
		// long diff = end - start;
		//
		// System.out.println("direct diff:" + diff);

		long start = System.currentTimeMillis();

		try {
			CompletionService<Void> completionService = new ExecutorCompletionService<Void>(mainExecutorPool);
			for (final FileCompare fileCompare : fileCompareList) {
				completionService.submit(new Callable<Void>() {
					public Void call() throws Exception {
						fileCompare.generateXlsOutput();
						return null;
					}

				});
			}

			for (final FileCompare fileCompare : fileCompareList) {
				Future<Void> xlsGenerationFuture = completionService.take();
				xlsGenerationFuture.get();

			}
		} finally {
			mainExecutorPool.shutdown();
			// writerExecutorPool.shutdown();
			long end = System.currentTimeMillis();
			long diff = end - start;
			System.out.println("\nTotal Time taken ( in milli secs) :" + diff);

		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BulkFileCompare [fileCompareList=");
		builder.append(fileCompareList);
		builder.append("]");
		return builder.toString();
	}

}
