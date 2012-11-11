package magdysyuk.backuper.test.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magdysyuk.backuper.source.xml.report.CreatorFileReportXml;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class CreatorFileReportXmlTest {

	private List<Map<File, Integer>> getImagesAndNumbersHiddenBytes() {
		List<Map<File, Integer>> imagesAndNumbersHiddenBytes = new ArrayList<Map<File, Integer>>();
		
		// Files should exists - they needs for calculating hash checksums. Number of hidden bytes could be any.
		Map<File, Integer> fileAndNumberBytesFirst = new HashMap<File, Integer>();
		fileAndNumberBytesFirst.put(new File("unittests_files\\xml\\creator_file_report_xml\\expected\\files_for_report\\image_01.png"), 7);
		Map<File, Integer> fileAndNumberBytesSecond = new HashMap<File, Integer>();
		fileAndNumberBytesSecond.put(new File("unittests_files\\xml\\creator_file_report_xml\\expected\\files_for_report\\image_02.png"), 1234321);
		
		imagesAndNumbersHiddenBytes.add(fileAndNumberBytesFirst);
		imagesAndNumbersHiddenBytes.add(fileAndNumberBytesSecond);
		
		return imagesAndNumbersHiddenBytes;
	}
	
	private File directoryForOutputReportFile = new File("unittests_files\\xml\\creator_file_report_xml\\obtained\\");
	private File getDirectoryForOutputReportFile() {
		return this.directoryForOutputReportFile;
	}
	
	private File originalFileReport = new File("unittests_files\\xml\\creator_file_report_xml\\expected\\report.xml");
	private File getOriginalFileReport() {
		return this.originalFileReport; 
	}
	
	@Test
	public void test() throws IOException {
		CreatorFileReportXml cfrx = new CreatorFileReportXml();
		File receivedReportFile = cfrx.makeReport(this.getImagesAndNumbersHiddenBytes(), this.getDirectoryForOutputReportFile());
		assertTrue(FileUtils.contentEquals(receivedReportFile, this.getOriginalFileReport()));
		assertTrue(receivedReportFile.delete());
	}

}
