/**
 * 
 */
package io.github.viskal.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Vishy
 *
 */
public class RecordComparisonResult {
	private final RecordComparisonResultEnum recordComparisonResultEnum;
	private final String key;
	private final List<String> firstDataFileRecords;
	private final List<String> secondDataFileRecords;
	private final List<String> differences;

	
	public static Comparator<RecordComparisonResult> fileComparisonResultComparator   = new Comparator<RecordComparisonResult>() {

		public int compare(RecordComparisonResult recordComparisonResult1, RecordComparisonResult recordComparisonResult2) {
			String key1 = recordComparisonResult1.getKey();
			String key2 = recordComparisonResult2.getKey();
			
			//ascending order
			return key1.compareTo(key2);
		}

	};
	public RecordComparisonResultEnum getRecordComparisonResultEnum() {
		return recordComparisonResultEnum;
	}

	public String getKey() {
		return key;
	}

	public List<String> getFirstDataFileRecords() {
		return firstDataFileRecords;
	}

	public List<String> getSecondDataFileRecords() {
		return secondDataFileRecords;
	}

	public List<String> getDifferences() {
		return differences;
	}

	public boolean isMissingInSecond() {
		return RecordComparisonResultEnum.MISSING_IN_SECOND.equals(recordComparisonResultEnum);
	}

	public boolean isMissingInFirst() {
		return RecordComparisonResultEnum.MISSING_IN_FIRST.equals(recordComparisonResultEnum);
	}

	public boolean isPartialMatch() {
		return RecordComparisonResultEnum.PARTIAL_MATCH.equals(recordComparisonResultEnum);
	}

	public boolean isPerfectMatch() {
		return RecordComparisonResultEnum.PERFECT_MATCH.equals(recordComparisonResultEnum);
	}

	public static RecordComparisonResult missingInSecondComparisonResult(String key, List<String> dataFileRecords) {
		return new RecordComparisonResult(RecordComparisonResultEnum.MISSING_IN_SECOND, key, dataFileRecords,
				new ArrayList<String>(), new ArrayList<String>());

	}

	public static RecordComparisonResult missingInFirstComparisonResult(String key, List<String> dataFileRecords) {
		return new RecordComparisonResult(RecordComparisonResultEnum.MISSING_IN_FIRST, key, new ArrayList<String>(),
				dataFileRecords, new ArrayList<String>());

	}

	public static RecordComparisonResult matchRecordComparisonResult(String key, List<String> firstDataFileRecords,
			List<String> secondDataFileRecords) {
		return new RecordComparisonResult(RecordComparisonResultEnum.PERFECT_MATCH, key, firstDataFileRecords,
				secondDataFileRecords);

	}

	public static RecordComparisonResult partialMatchRecordComparisonResult(String key,
			List<String> firstDataFileRecords, List<String> secondDataFileRecords, List<String> differences) {
		return new RecordComparisonResult(RecordComparisonResultEnum.PARTIAL_MATCH, key, firstDataFileRecords,
				secondDataFileRecords, differences);

	}

	private RecordComparisonResult(RecordComparisonResultEnum recordComparisonResultEnum, String key,
			List<String> firstDataFileRecords, List<String> secondDataFileRecords) {
		this(recordComparisonResultEnum, key, firstDataFileRecords, secondDataFileRecords, new ArrayList<String>());
	}

	private RecordComparisonResult(RecordComparisonResultEnum recordComparisonResultEnum, String key,
			List<String> firstDataFileRecords, List<String> secondDataFileRecords, List<String> differences) {
		super();
		this.recordComparisonResultEnum = recordComparisonResultEnum;
		this.key = key;
		this.firstDataFileRecords = firstDataFileRecords;
		this.secondDataFileRecords = secondDataFileRecords;
		this.differences = differences;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecordComparisonResult [recordComparisonResultEnum=");
		builder.append(recordComparisonResultEnum);
		builder.append(", key=");
		builder.append(key);
		builder.append(", firstDataFileRecords=");
		builder.append(firstDataFileRecords);
		builder.append(", secondDataFileRecords=");
		builder.append(secondDataFileRecords);
		builder.append(", differences=");
		builder.append(differences);
		builder.append("]");
		return builder.toString();
	}

}
