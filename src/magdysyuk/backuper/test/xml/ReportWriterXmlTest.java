package magdysyuk.backuper.test.xml;

import static org.junit.Assert.*;

import java.io.File;

import magdysyuk.backuper.source.xml.report.ReportWriterXml;

import org.apache.commons.io.FileUtils;

import org.junit.Test;




public class ReportWriterXmlTest {
	
	private File expectedOutReportXmlFile = new File("unittests_files\\xml\\report_writer_xml\\expected\\out.xml");
	private File getExpectedOutReportXmlFile() {
		return this.expectedOutReportXmlFile;
	}
	
	private File outReportXmlFile = new File("unittests_files\\xml\\report_writer_xml\\obtained\\out.xml");
	private File getOutReportXmlFile() {
		return this.outReportXmlFile;
	}
	
	private ReportWriterXml reportWriterXml = new ReportWriterXml(this.getOutReportXmlFile());
	private ReportWriterXml getReportWriterXml() {
		return this.reportWriterXml;
	}
	
	@Test
	public void testSaveInfoToFile() {
		this.getOutReportXmlFile().delete();
		ReportWriterXml reportWriterXml = this.getReportWriterXml();
		reportWriterXml.startDocument();
		reportWriterXml.startTagElement("tag01");
		reportWriterXml.putValue("value of tag01");
		reportWriterXml.endTagElement("tag01");
		reportWriterXml.startTagElement("tag02");
		reportWriterXml.putTaggedValue("tag001", "value of tag02-tag001");
		reportWriterXml.putTaggedValue("tag002", "value of tag02-tag002");
		reportWriterXml.startTagElement("tag03");
		reportWriterXml.putTaggedValue("tag001", "value of tag03-tag001");
		reportWriterXml.endTagElement("tag03");
		reportWriterXml.startTagElement("tag04");
		reportWriterXml.startTagElement("tag004");
		reportWriterXml.putTaggedValue("tag001", "value of tag04-tag004-tag001");
		reportWriterXml.endTagElement("tag004");
		reportWriterXml.endTagElement("tag04");
		reportWriterXml.endTagElement("tag02");
		reportWriterXml.endDocument();
		try {
			boolean isFilesContentsEquals = (FileUtils.contentEquals(this.getExpectedOutReportXmlFile(), 
					this.getOutReportXmlFile()));
			assertTrue(isFilesContentsEquals);
			if (isFilesContentsEquals) {
				/*
				 * Check that file is unlocked (means that output stream was closed) 
				 * through removing file.
				 * We do it only if all was good (files are equals) 
				 * for saving incorrect file for analyze if something went wrong 
				 * (files are different).
				 */
				assertTrue(this.getOutReportXmlFile().delete());
			}
		} catch (Exception ex){
			fail("Expected and obtained xml reports are different: " + ex.getMessage());
		}
		
	}
	
}
