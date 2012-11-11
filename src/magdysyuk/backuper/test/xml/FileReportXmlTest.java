package magdysyuk.backuper.test.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.UnsupportedEncodingException;

import magdysyuk.backuper.source.crypt.hash.HashFileAlgorithmName;
import magdysyuk.backuper.source.crypt.hash.HashFileCalculator;
import magdysyuk.backuper.source.filesystem.DirectoryUtils;
import magdysyuk.backuper.source.xml.report.ReportWriterXml;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


/**
 * Example testing report file with all checksums
 */
public class FileReportXmlTest {
	
	private File[] filesForReport = new File[] {new File("unittests_files\\xml\\file_report_xml\\expected\\files_for_report")};
	private File[] getFilesForReport() {
		return this.filesForReport;
	}
	
	private File rootFile = new File("unittests_files");
	private File getRootFile() {
		return this.rootFile;
	}
	
	private File expectedOutXmlFile = new File("unittests_files\\xml\\file_report_xml\\expected\\out.xml");
	private File getExpectedOutXmlFile() {
		return this.expectedOutXmlFile;
	}
	
	private File outXmlFile = new File("unittests_files\\xml\\file_report_xml\\obtained\\out.xml");
	private File getOutXmlFile() {
		return this.outXmlFile;
	}
	
	private ReportWriterXml reportWriterXml = new ReportWriterXml(this.getOutXmlFile());
	private ReportWriterXml getReportWriterXml() {
		return this.reportWriterXml;
	}
	
	private FileReportXml fileReportXml = new FileReportXml(this.getReportWriterXml());
	private FileReportXml getFileReportXml() {
		return this.fileReportXml;
	}
	
	/**
	 * Trying to test creating of report for exists files (look at inner class FileReportXml)
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testSaveFileInfo() throws UnsupportedEncodingException {
		FileReportXml fileReportXml = this.getFileReportXml();
		fileReportXml.setRootFile(this.getRootFile());
		fileReportXml.startDocument();
		for (File observableFile : this.getFilesForReport()) {
			fileReportXml.saveFileInfo(observableFile);
		}
		fileReportXml.endDocument();
		try {
			boolean isFilesContentsEquals = FileUtils.contentEquals(this.getExpectedOutXmlFile(), 
					this.getOutXmlFile());
			assertTrue(isFilesContentsEquals);
			if (isFilesContentsEquals) {
				assertTrue(this.getOutXmlFile().delete());
			}
		} catch (Exception ex) {
			fail("Expected and obtained xml files are different: " + ex.getMessage());
		}
	}
	
	
	/**
	 * Save information about file (using another class for saving report to xml).
	 * Moved to unit-test for saving as example of working class with all hash checksums.
	 * Also works with directories.
	 * Example of using:
	 * 	<code>
	 * <br/>	ReportWriterXml reportWriterXml = new ReportWriterXml(new File("out.xml"));
	 * <br/>	FileReportXml fileReportXml = new FileReportXml(reportWriterXml);
	 * <br/>	fileReportXml.setRootFile(new File("C:\\"));
	 * <br/>	fileReportXml.startDocument();
	 * <br/>	fileReportXml.saveFileInfo(new File("file_for_scan.txt"));
	 * <br/>	fileReportXml.saveFileInfo(new File("directory_for_scan"));
	 * <br/>	fileReportXml.endDocument();
	 *	</code>
	 */
	private class FileReportXml {
		private FileReportXml(ReportWriterXml reportWriterXml) {
			this.reportWriterXml = reportWriterXml;
			this.directoryUtils = new DirectoryUtils();
		}
		
		private ReportWriterXml reportWriterXml;
		private ReportWriterXml getReportWriterXml() {
			return this.reportWriterXml;
		}
		private DirectoryUtils directoryUtils;
		private DirectoryUtils getDirectoryUtils() {
			return this.directoryUtils;
		}
		
		/**
		 * From this filepath will be calculated relative file path for nested files and directories
		 */
		private File rootFile = null;
		private File getRootFile() {
			return this.rootFile;
		}
		public void setRootFile(File rootFilePath) {
			this.rootFile = rootFilePath;
		}
		
		
		public void startDocument() {
			ReportWriterXml reportWriterXml = this.getReportWriterXml();
			reportWriterXml.startDocument();
			reportWriterXml.startTagElement("hash_cheksum_description");
			for (HashFileAlgorithmName hashFileAlgorithmName : HashFileAlgorithmName.values()) {
				reportWriterXml.putTaggedValue(hashFileAlgorithmName.getAlgorithmName(), hashFileAlgorithmName.getDescription());
			}
			reportWriterXml.endTagElement("hash_cheksum_description");
			reportWriterXml.putTaggedValue("root_filepath", this.getRootFile().getPath());
		}
		
		public void endDocument() {
			this.getReportWriterXml().endDocument();
		}
		
		/*
		 * We need to call saveFileEntityInfo for going through 
		 * separate file (or directory),
		 * and saveDirectoryRecursively for going by all
		 * nested files and folders
		 */
		public void saveFileInfo(File inputFile) throws UnsupportedEncodingException {
			if (inputFile.isDirectory() == true) {
				this.saveDirectoryRecursively(inputFile);
			} else {
				this.saveFileEntityInfo(inputFile);
			}
		}
		
		private void saveDirectoryRecursively(File inputDirectory) throws UnsupportedEncodingException {
			this.saveFileEntityInfo(inputDirectory);
			File[] listFiles = inputDirectory.listFiles();
			for (File innerFile : listFiles) {
				if (innerFile.isDirectory() == true) {
					this.saveDirectoryRecursively(innerFile);
				} else {
					this.saveFileInfo(innerFile);
				}
			}
		}
		
		private void saveFileEntityInfo(File inputFile) throws UnsupportedEncodingException {
			ReportWriterXml reportWriterXml = this.getReportWriterXml();
			reportWriterXml.startTagElement("file_entity");
			reportWriterXml.putTaggedValue("name", inputFile.getName());
			reportWriterXml.putTaggedValue("original_filepath", inputFile.getPath());
			reportWriterXml.putTaggedValue("original_filepath_sepatator", File.separator);
			reportWriterXml.putTaggedValue("relative_filepath_with_root", 
					this.getDirectoryUtils().getRelativeFilePathWithRoot(this.getRootFile(), inputFile));
			reportWriterXml.putTaggedValue("relative_filepath_without_root", 
					this.getDirectoryUtils().getRelativeFilePathWithoutRoot(this.getRootFile(), inputFile));
			
			if (inputFile.isDirectory()) {
				reportWriterXml.putTaggedValue("type", "directory");
			} else {
				reportWriterXml.putTaggedValue("type", "file");
				reportWriterXml.putTaggedValue("filesize_bytes", String.valueOf(inputFile.length()));
				reportWriterXml.startTagElement("hash_cheksum");
				HashFileCalculator hashFileCalculator = new HashFileCalculator(inputFile);
				for (HashFileAlgorithmName hashFileAlgorithmName : HashFileAlgorithmName.values()) {
					reportWriterXml.startTagElement("hash_cheksum_entity");
					reportWriterXml.putTaggedValue("algorithm_name", hashFileAlgorithmName.getAlgorithmName());
					reportWriterXml.putTaggedValue("cheksum", hashFileCalculator.getHash(hashFileAlgorithmName));
					reportWriterXml.endTagElement("hash_cheksum_entity");
				}
				reportWriterXml.endTagElement("hash_cheksum");
			}
			
			reportWriterXml.endTagElement("file_entity");
		}
		
		
	}

}
