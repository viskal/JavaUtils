# BulkFileCompare : Java Bulk File comparison utility

Compares text files with *columns* separated by a *delimtter*. 

**meta.txt** should contain the details of *what files are being compared , their columns , key columns , ignore columns , delimitters and more details* ( see below for all the details you need to provide). Sample files are provided for reference.  

Build from source or use prebuilt jar *bfc.jar* ( compatible with java 1.5) under *BulkFileCompare*.  

**Sample Test:**  

1. Copy the jar onto a dir
2. Copy colnames1.txt, colnames2.txt, file1.dat, file2.dat, ignore.txt, keycolnames.txt, meta.txt files from \BulkFileCompare\src\test onto the above dir
3. From command line run  : 
```java -Xms1g -Xmx1g -jar bfc.jar```
This would generate bunch of xls files with details on differences between files that are being compared.

**meta.txt** can  contain  multiple lines with each line in the following form:

*<colnamesfile1><space>[colnamesfile2][space][keycolumnames][space][ignorecolumns][space][file1data][space][file2data][space][resultxlsfile][space][delimitterinfile1][space][delimitterinfile2]*

- colnamesfile1 -file containing column names from file1
- colnamesfile2 - file containing column names from file2
- keycolumnames - file containing key column names (the values will be compared in both files against  this key )
- ignorecolumns - file conatining columns that should be skipped for comparison
- file1data - first file ( with delimitters as specified in [delimitterinfile1])
- file2data - second file ( with delimitters as specified in [delimitterinfile2])
- resultxlsfile - results will be in this xls file - the xls has various sheets with details on comparison results including matching columns, mismatch columns and their counts, duplicate rows of each file, error records and more..
- delimitterinfile1 - demiltter in first file
- delimitterinfile2 - demiltter in second file


e.g. The contents of *meta.txt* could contain the following two lines:  
*colnames1.txt colnames2.txt keycolnames.txt ignore.txt file1.dat file2.dat comp1.xls | |*  
*colnames3.txt colnames4.txt keycolnames3.txt ignore3.txt file14.dat file15.dat comp2.xls , |*  

Here, the first line of the meta.txt is comparing two files file1.dat and file2.dat, each with pipe delimitted columns; the column names of file1 are in colnames1.txt and column names of file2 are in colnames2.txt; the key columns are specified in keycolnames.txt; the ignore columns in ignore.txt. The result of the comparison is stored  in comp1.xls.


