package magdysyuk.backuper.test.xml;

import static org.junit.Assert.*;

import java.io.File;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import magdysyuk.backuper.source.xml.parser.ParserFileReportXml;

import org.junit.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.xpath.*;


public class ParserFileReportXmlTest {

	private File getFileExampleReport() {
		return new File("unittests_files\\xml\\parser_xml\\expected\\report.xml");
	}

	/**
	 * Just test standard java XPath through DOM.
	 */
	@Test
	public void testXPath() throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		Document document = builder.parse(this.getFileExampleReport());
		XPathFactory xpathFactory = XPathFactory.newInstance();
		
		XPath xpath = xpathFactory.newXPath();
		XPathExpression xpathExpression = null;
		Object result = null;
		NodeList nodes = null;
		
		xpathExpression = xpath.compile("/root/application/version");
		assertTrue("1.0".equals(xpathExpression.evaluate(document, XPathConstants.STRING)));
		
		xpathExpression = xpath.compile("//application/version");
		assertTrue("1.0".equals(xpathExpression.evaluate(document, XPathConstants.STRING)));
		
		xpathExpression = xpath.compile("/root/file_entity[1]/name");
		assertTrue("file_01.txt".equals(xpathExpression.evaluate(document, XPathConstants.STRING)));
		
		xpathExpression = xpath.compile("/root/file_entity[1]/hash_cheksum/hash_cheksum_entity[algorithm_name = 'md5']/cheksum");
		assertTrue("1xxx".equals(xpathExpression.evaluate(document, XPathConstants.STRING)));
		
		xpathExpression = xpath.compile("/root/file_entity[3]/hash_cheksum/hash_cheksum_entity[algorithm_name = 'adler32']/cheksum");
		assertTrue("3yyy".equals(xpathExpression.evaluate(document, XPathConstants.STRING)));
		
		xpathExpression = xpath.compile("/root/file_entity");
		result = xpathExpression.evaluate(document, XPathConstants.NODESET);
		nodes = (NodeList) result;
		assertTrue(3 == nodes.getLength());
		
		xpathExpression = xpath.compile("/root/file_entity/name");
		result = xpathExpression.evaluate(document, XPathConstants.NODESET);
		nodes = (NodeList) result;
		for (int i = 0; i < nodes.getLength(); i++) {
			if (i == 0) {
				assertTrue ("file_01.txt".equals(nodes.item(i).getTextContent()));
			}
			if (i == 1) {
				assertTrue ("file_02.txt".equals(nodes.item(i).getTextContent()));
			}
			if (i == 2) {
				assertTrue ("file_03.txt".equals(nodes.item(i).getTextContent()));
			}
		}

	}
	
	@Test
	public void testGetExpectedListImageFilesWithProperties() {
		ParserFileReportXml parserFileReportXml = new ParserFileReportXml();
		List<Map<String, String>> listExpectedImageFilesAndProperties = parserFileReportXml.getExpectedListImageFilesWithProperties(this.getFileExampleReport());
		assertTrue(listExpectedImageFilesAndProperties.size() == 3);
		String[][] properties = new String[3][5];
		int fileCounter = 0;
		for(Map<String, String> currentMapFileProperties : listExpectedImageFilesAndProperties) {
			int propertyCounter = 0;
			for(Entry<String, String> mapEntry : currentMapFileProperties.entrySet()) {
				properties[fileCounter][propertyCounter] = mapEntry.getKey() + ": " + mapEntry.getValue();
				propertyCounter++;
			}
			fileCounter++;
		}
		String resultAsString = "";
		for(int i = 0; i < 3; i++) {
			for (int j = 0; j < 5; j++) {
				resultAsString += properties[i][j] + " _ ";
			}
			resultAsString += "\n";
		}
		
		String expectedString = "adler32_checksum: 1yyy _ number_hidden_data_bytes: 199 _ md5_checksum: 1xxx _ filename: file_01.txt _ filesize_bytes: 100 _ " + "\n" +
"adler32_checksum: 2yyy _ number_hidden_data_bytes: 299 _ md5_checksum: 2xxx _ filename: file_02.txt _ filesize_bytes: 200 _ " + "\n" +
"adler32_checksum: 3yyy _ number_hidden_data_bytes: 399 _ md5_checksum: 3xxx _ filename: file_03.txt _ filesize_bytes: 300 _ " + "\n";
		
		assertTrue(resultAsString.equals(expectedString));
	}
}
