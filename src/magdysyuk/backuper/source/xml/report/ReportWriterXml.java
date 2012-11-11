package magdysyuk.backuper.source.xml.report;

import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.xml.sax.SAXException;


/**
 * This class just save any content to xml.
 * Usual use-case:
 * <p/>call <code>startDocument()
 * <p/>use <code>startTagElement(String tagName)</code> -> <code>putValue(String value)</code> -> <code>endTagElement(String tagName)</code>
 * <p/>or
 * <p/>use <code>putTaggedValue(String tagName, String value)</code>
 * <p/>And finish with call:
 * <p/><code>endDocument()</code>
 */
public class ReportWriterXml {

	public ReportWriterXml (File outXmlFile) {
		this.outReportXmlFile = outXmlFile;
		this.fileIO = new FileIO();
		this.outputStream = this.getFileIO().getFileOutputStream(this.getOutReportXmlFile());
		this.setTransformerHandler();
		
	}
	
	private File outReportXmlFile;
	private File getOutReportXmlFile() {
		return this.outReportXmlFile;
	}
	
	private FileIO fileIO;
	private FileIO getFileIO() {
		return this.fileIO;
	}
	
	private OutputStream outputStream;
	private OutputStream getOutputStream() {
		return this.outputStream;
	}
	
	private boolean isDocumentStarted = false;
	private boolean getIsDocumentStarted() {
		return isDocumentStarted;
	}
	private void setIsDocumentStarted(boolean isDocumentStarted) {
		this.isDocumentStarted = isDocumentStarted;
	}
	
	public void startDocument() {
		Loggers.debug(this, TextMessage.REPORT_XML_BEGIN);
		try {
			if (this.getIsDocumentStarted() == false) {
				this.getTransformerHandler().startDocument();
				this.setIsDocumentStarted(true);
			}
		} catch (SAXException e) {
			Loggers.fatal(this, TextMessage.REPORT_XML_UNKNOWN_ERROR, e);
		}
	}
	public void endDocument() {
		try {
			this.getTransformerHandler().endDocument();
			this.getOutputStream().flush();
			this.getOutputStream().close();
		} catch (Exception e) {
			Loggers.fatal(this, TextMessage.REPORT_XML_UNKNOWN_ERROR, e);
		}
		Loggers.debug(this, TextMessage.REPORT_XML_END);
	}
	
	public void putTaggedValue(String tagName, String value) {
		this.startTagElement(tagName);
		this.putValue(value);
		this.endTagElement(tagName);
	}
	
	public void startTagElement(String tagName) {
		try {
			this.getTransformerHandler().startElement("", "", tagName, null);
		} catch (SAXException e) {
			Loggers.fatal(this, TextMessage.REPORT_XML_UNKNOWN_ERROR, e);
		}
	}
	
	public void endTagElement(String tagName) {
		try {
			this.getTransformerHandler().endElement("", "", tagName);
		} catch (SAXException e) {
			Loggers.fatal(this, TextMessage.REPORT_XML_UNKNOWN_ERROR, e);
		}
	}
	
	public void putValue(String value) {
		if (value == null) {
			value = "";
		}
		try {
			this.getTransformerHandler().characters(value.toCharArray(), 0, value.length());
		} catch (SAXException e) {
			Loggers.fatal(this, TextMessage.REPORT_XML_UNKNOWN_ERROR, e);
		}
	}
	
	private TransformerHandler transformerHandler;
	private TransformerHandler getTransformerHandler() {
		return this.transformerHandler;
	}
	
	private void setTransformerHandler() {
		/*
		 * For xml-formatting exists some ways
		 * 1) non-standart property, such as "{http://xml.apache.org/xslt}" for factory.
		 * Lists of non-standart properties can be found in sources of com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory class.
		 * Approximately example of use:
		 * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		 * transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		 * The "indent-amount" is optional.
		 * However usually is enough to do setOutputProperty(OutputKeys.INDENT,"yes"),
		 * in my test it add only new-line symbol, not space- or tab-indent example:
		 * SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		 * TransformerHandler transformerHandler = tf.newTransformerHandler();
		 * Transformer serializer = transformerHandler.getTransformer();
		 * serializer.setOutputProperty(OutputKeys.INDENT,"yes");
		 * 2) Using IndentingXMLStreamWriter from StAX Utilities Project, package javanet.staxutils.IndentingXMLStreamWriter,
		 * url: http://java.net/projects/stax-utils/pages/Home, example:
		 * XMLOutputFactory factory = XMLOutputFactory.newInstance();
		 * XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter("out.xml"));
		 * writer = new IndentingXMLStreamWriter(writer);
		 * 3) Using StaxMate + Woodstox,
		 * StaxMate project: http://staxmate.codehaus.org/
		 * Woodstox (for StaxMate): http://wiki.fasterxml.com/WoodstoxHome
		 * example:
		 * SMOutputFactory sf = SMOutputFactory(XMLOutputFactory.newInstance());
		 * SMOutputDocument doc = sf.createOutputDocument(new FileOutputStream("output.xml"));
		 * doc.setIndentation("\n ", 1, 2);
		 * 4) Other ways: using serialization such as JAXB, XStream: http://xstream.codehaus.org,
		 * libs as javolution: http://javolution.org/
		 * or manually add required indent "	" when you write data to out,
		 * or something else.
		 */
		SAXTransformerFactory transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler transformerHandler = null;
		try {
			transformerHandler = transformerFactory.newTransformerHandler();
			Transformer transformer = transformerHandler.getTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //also can be useful encoding ISO-8859-1
			transformer.setOutputProperty(OutputKeys.METHOD,"xml");
			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.REPORT_XML_IMPOSSIBLE_GET_TRANSFORMERHANDLER, ex);
		}
		if (this.getOutReportXmlFile() != null) {
			OutputStream outStreamResult = this.getOutputStream();
			Result streamResult = new StreamResult(outStreamResult);
			transformerHandler.setResult(streamResult);
			this.transformerHandler = transformerHandler;
		} else {
			Loggers.fatal(this, TextMessage.REPORT_XML_NOT_SET_OUT_FILE);
		}
	}
}
