package magdysyuk.backuper.source.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class ParserFileReportXml {
	/**
	 * That's all is received from report xml file.
	 * Maps contains information about file (e.g. checksums)
	 * @param reportXml XML report file
	 * @return List of maps with parsed information about file: filename, filesize, number hidden files, checksums
	 */
	public List<Map<String, String>> getExpectedListImageFilesWithProperties(File reportXml) {
		List<Map<String, String>> listExpectedImageFilesAndProperties = new ArrayList<Map<String,String>>();
		
		try {
			/*
			 * In the future could be useful to use XMLDog http://code.google.com/p/jlibs/wiki/XMLDog 
			 * because it supports XPath and don't use DOM tree 
			 * (if will be troubles with required memory for large xml files)
			 */
			
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document document = builder.parse(reportXml);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			
			XPath xpath = xpathFactory.newXPath();
			XPathExpression xpathExpression = null;
			Object result = null;
			NodeList nodes = null;
			
			xpathExpression = xpath.compile("/root/file_entity");
			
			result = xpathExpression.evaluate(document, XPathConstants.NODESET);
			nodes = (NodeList) result;
			
			int numberFiles = nodes.getLength();

			for (int i = 0; i < numberFiles; i++) {
				Map<String, String> fileEntityInfo = new HashMap<String, String>();
				
				xpathExpression = xpath.compile("/root/file_entity["+ (i+1) +"]/name");
				String fileName = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
				fileEntityInfo.put("filename", fileName);
				
				xpathExpression = xpath.compile("/root/file_entity["+ (i+1) +"]/filesize_bytes");
				String fileSize = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
				fileEntityInfo.put("filesize_bytes", fileSize);
				
				xpathExpression = xpath.compile("/root/file_entity["+ (i+1) +"]/number_hidden_data_bytes");
				String numberHiddenDataBytes = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
				fileEntityInfo.put("number_hidden_data_bytes", numberHiddenDataBytes);
				
				xpathExpression = xpath.compile("/root/file_entity["+ (i+1) +"]/hash_cheksum/hash_cheksum_entity[algorithm_name = 'md5']/cheksum");
				String md5Checksum = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
				fileEntityInfo.put("md5_checksum", md5Checksum);
				
				xpathExpression = xpath.compile("/root/file_entity["+ (i+1) +"]/hash_cheksum/hash_cheksum_entity[algorithm_name = 'adler32']/cheksum");
				String adler32Checksum = (String) xpathExpression.evaluate(document, XPathConstants.STRING);
				fileEntityInfo.put("adler32_checksum", adler32Checksum);
				
				listExpectedImageFilesAndProperties.add(fileEntityInfo);
			}
					
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.PARSER_XML_UNKNOWN_ERROR, new Object[]{reportXml}, ex);
		}
		
		return listExpectedImageFilesAndProperties;
	}
}
