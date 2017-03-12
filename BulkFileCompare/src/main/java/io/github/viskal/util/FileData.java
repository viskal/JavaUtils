package io.github.viskal.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class FileData {

	public static final int MAX_SIZE = 60000;
	private final String fileName;
	private String delimitter;
	public static final String DEFAULT_KEY_DELIMITTER = "|";

	public FileData(String fileName, String delimitter) {
		super();
		this.fileName = fileName;
		this.delimitter = delimitter;
	}

	public String getFileName() {
		return fileName;
	}

	public Map<String, List<String>> getContent() {
		return content;
	}

	public void setContent(Map<String, List<String>> content) {
		this.content = content;
	}

	public List<String> getDuplicates() {
		return duplicates;
	}

	public void setDuplicates(List<String> duplicates) {
		this.duplicates = duplicates;
	}

	public int getTotalNumberOfLines() {
		return dataList.size();
	}

	public boolean isBigFile() {
		return getTotalNumberOfLines() > MAX_SIZE ? true : false;
	}

	private Map<String, List<String>> content = new HashMap<String, List<String>>();
	private List<String> duplicates = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();

	private List<String> dataList = new ArrayList<String>();

	public void loadData() throws IOException {

		final BufferedReader reader = new BufferedReader(new FileReader(fileName));
		try {
			String lineFromFile = null;
			while ((lineFromFile = reader.readLine()) != null) {
				dataList.add(lineFromFile);
			}
		} finally {
			reader.close();
		}

	}

	public void preProcessData(Map<String, Integer> allColumnNamesMap, Set<String> keyColumnNamesSet) {
		List<Integer> keyColumnIndexes = new ArrayList<Integer>();
		for (String keyColumnNames : keyColumnNamesSet) {
			keyColumnIndexes.add(allColumnNamesMap.get(keyColumnNames));
		}

		int firstKeyColIndex = keyColumnIndexes.get(0).intValue();
		keyColumnIndexes.remove(0);
		Integer[] remainingKeyColArray = keyColumnIndexes.toArray(new Integer[keyColumnIndexes.size()]);
		int noOfColumns = allColumnNamesMap.size();
		int countLines = 0;
		for (String data : dataList) {
			countLines++;
			final String[] lineFromFileData = data.split(Pattern.quote(delimitter), -1);

			if (lineFromFileData == null || lineFromFileData.length != noOfColumns) {
				errors.add(String.format(
						"lineFromFile : (%s) read from file (%s) is null or no of columns (%d) in the line (%d) does not match the expected (%d) number of columns.",
						data, fileName, lineFromFileData.length, countLines, noOfColumns));
				continue;
			}

			StringBuilder keyBuilder = new StringBuilder();
			keyBuilder.append(lineFromFileData[firstKeyColIndex]);
			for (Integer colNo : remainingKeyColArray) {
				keyBuilder.append(DEFAULT_KEY_DELIMITTER).append(lineFromFileData[colNo.intValue()]);
			}
			String key = keyBuilder.toString();
			if (content.containsKey(key)) {
				duplicates.add(data);
				continue;
			}
			content.put(key, Arrays.asList(lineFromFileData));
		}

	}

	public List<String> getErrors() {
		return errors;
	}

}
