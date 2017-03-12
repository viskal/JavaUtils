package io.github.viskal.util;

import java.util.HashMap;
import java.util.List;

public class FileComparisonResult {

	private List<RecordComparisonResult> recordComparisonResultList;

	private HashMap<String, Integer> mismatchColumnCountMap;

	public FileComparisonResult(List<RecordComparisonResult> recordComparisonResultList,
			HashMap<String, Integer> mismatchColumnCountMap) {
		super();
		this.recordComparisonResultList = recordComparisonResultList;
		this.mismatchColumnCountMap = mismatchColumnCountMap;
	}

	public List<RecordComparisonResult> getRecordComparisonResultList() {
		return recordComparisonResultList;
	}

	public HashMap<String, Integer> getMismatchColumns() {
		return mismatchColumnCountMap;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileComparisonResult [recordComparisonResultList=");
		builder.append(recordComparisonResultList);
		builder.append(", mismatchColumnCountMap=");
		builder.append(mismatchColumnCountMap);
		builder.append("]");
		return builder.toString();
	}

}
