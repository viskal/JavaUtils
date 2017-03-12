package io.github.viskal.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.viskal.util.FileData;

public class UTestFileDataUTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPreProcessData() {

		FileData fileData = new FileData("src/test/resources/file1.dat", "|");
		try {
			fileData.loadData();
			fileData.preProcessData(createAllColumnNamesMap(), createKeyColumnNamesSet());
			List<String> rowData = fileData.getContent().get("col1-value1|col2-value1");
			Assert.assertTrue(rowData.get(0).equals("col1-value1"));
			Assert.assertTrue(rowData.get(1).equals("col2-value1"));
			Assert.assertTrue(rowData.get(2).equals("col3-value1"));
			Assert.assertTrue(rowData.get(3).equals("col4-value1"));
			Assert.assertTrue(rowData.get(4).equals("col5-value1"));
			Assert.assertTrue(rowData.get(5).equals("col6-value1"));
			Assert.assertTrue(rowData.get(6).equals("col7-value1"));
			Assert.assertTrue(rowData.get(7).equals("col8-value1-123.00"));

			Assert.assertTrue(fileData.getTotalNumberOfLines() == 8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, Integer> createAllColumnNamesMap() {
		Map<String, Integer> allColumnNamesMap = new HashMap<String, Integer>();
		allColumnNamesMap.put("col1", Integer.valueOf(0));
		allColumnNamesMap.put("col2", Integer.valueOf(1));
		allColumnNamesMap.put("col3", Integer.valueOf(2));
		allColumnNamesMap.put("col4", Integer.valueOf(3));
		allColumnNamesMap.put("col5", Integer.valueOf(4));
		allColumnNamesMap.put("col6", Integer.valueOf(5));
		allColumnNamesMap.put("col7", Integer.valueOf(6));
		allColumnNamesMap.put("col8", Integer.valueOf(7));

		return allColumnNamesMap;

	}

	private Set<String> createKeyColumnNamesSet() {
		Set<String> keyColumnNamesSet = new LinkedHashSet<String>();
		keyColumnNamesSet.add("col1");
		keyColumnNamesSet.add("col2");
		return keyColumnNamesSet;
	}

}
