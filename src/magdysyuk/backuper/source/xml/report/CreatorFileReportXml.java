package magdysyuk.backuper.source.xml.report;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import magdysyuk.backuper.source.crypt.hash.HashFileAlgorithmName;
import magdysyuk.backuper.source.crypt.hash.HashFileCalculator;


public class CreatorFileReportXml {
	
	/**
	 * Make a xml, which contains file name, size, and some checksums of file, and number hidden data bytes in that file
	 * @param imagesWithHiddenData List of maps, each map have only 1 record - file and number hidden bytes in that file
	 * @param directoryForOutputReportFile
	 * @return Report file with randomly generated unique name
	 */
	public File makeReport(List<Map<File, Integer>> imagesWithHiddenData, File directoryForOutputReportFile) {
		if (directoryForOutputReportFile.exists() && directoryForOutputReportFile.isDirectory()) {
			// do nothing, all is ok
		} else {
			directoryForOutputReportFile.mkdirs();
		}
		
		// Output report file should have unique file name
		this.outputReportFile = null;
		boolean isOutputReportFileAlreadyExists = true;
		while (isOutputReportFileAlreadyExists == true) {
			this.outputReportFile = new File(directoryForOutputReportFile + File.separator + "report_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".xml");
			isOutputReportFileAlreadyExists = this.outputReportFile.exists();
		}
		
		this.reportWriterXml = new ReportWriterXml(this.getOutputReportFile());
		
		
		this.startDocument();
		
		for (Map<File, Integer> imageWithDataMap : imagesWithHiddenData) {
			for (Entry<File, Integer> imageWithDataEntry : imageWithDataMap.entrySet()) {
				this.saveFileEntityInfo(imageWithDataEntry.getKey(), imageWithDataEntry.getValue());
			}
		}
		
		this.endDocument();
		
		return this.getOutputReportFile();
	}
	
	private File outputReportFile;
	private File getOutputReportFile() {
		return this.outputReportFile;
	}
	
	private ReportWriterXml reportWriterXml;
	private ReportWriterXml getReportWriterXml() {
		return this.reportWriterXml;
	}
	
	
	private void startDocument() {
		ReportWriterXml reportWriterXml = this.getReportWriterXml();
		reportWriterXml.startDocument();
		reportWriterXml.startTagElement("root");
		reportWriterXml.startTagElement("application");
		reportWriterXml.putTaggedValue("version", "1.0");
		reportWriterXml.endTagElement("application");
	}
	
	private void endDocument() {
		this.getReportWriterXml().endTagElement("root");
		this.getReportWriterXml().endDocument();
	}
	
	
	private void saveFileEntityInfo(File inputFile, int numberHiddenDataBytes) {
		ReportWriterXml reportWriterXml = this.getReportWriterXml();
		reportWriterXml.startTagElement("file_entity");
		
		reportWriterXml.putTaggedValue("name", inputFile.getName());
		
		reportWriterXml.putTaggedValue("filesize_bytes", String.valueOf(inputFile.length()));
		reportWriterXml.putTaggedValue("number_hidden_data_bytes", String.valueOf(numberHiddenDataBytes));
		reportWriterXml.startTagElement("hash_cheksum");
		HashFileCalculator hashFileCalculator = new HashFileCalculator(inputFile);
		for (HashFileAlgorithmName hashFileAlgorithmName : HashFileAlgorithmName.values()) {
			// Will use only some, not all hashes (calculating all checksums is very long)
			if (	(hashFileAlgorithmName == HashFileAlgorithmName.Adler32) || (hashFileAlgorithmName == HashFileAlgorithmName.MD5) ) {
				reportWriterXml.startTagElement("hash_cheksum_entity");
				reportWriterXml.putTaggedValue("algorithm_name", hashFileAlgorithmName.getAlgorithmName());
				reportWriterXml.putTaggedValue("cheksum", hashFileCalculator.getHash(hashFileAlgorithmName));
				reportWriterXml.endTagElement("hash_cheksum_entity");
			}
		}
		reportWriterXml.endTagElement("hash_cheksum");
	
		reportWriterXml.endTagElement("file_entity");
	}
	
}
