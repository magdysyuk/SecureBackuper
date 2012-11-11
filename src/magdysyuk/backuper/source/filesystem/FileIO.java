package magdysyuk.backuper.source.filesystem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;


public class FileIO {
	
	public InputStream getFileInputStream (File inputFile) {
		return this.getFileInputStream(inputFile.getPath());
	}
	
	private InputStream getFileInputStream (String inputFilePath) {
		Loggers.debug(this, TextMessage.FILE_READ_START, new Object[]{inputFilePath});
		BufferedInputStream bufferedInputStream = null;
		File inputFile = new File(inputFilePath);
		if (inputFile.canRead() == true && inputFile.isDirectory() == false) {
			Loggers.debug(this, TextMessage.FILE_IS_READABLE, new Object[]{inputFilePath, inputFile.length(), inputFile.isFile()});
			int bufferSize = FileIOConfig.getBufferInputStreamSize();
			try {
				FileInputStream inputStream = new FileInputStream(inputFile);
				bufferedInputStream = new BufferedInputStream(inputStream, bufferSize);
				Loggers.debug(this, TextMessage.FILE_READ_WILL_BE_SUCCESSFUL, new Object[]{inputFilePath});
			} catch (Exception ex) {
				Loggers.fatal(this, TextMessage.FILE_READ_IMPOSSIBLE, new Object[]{inputFilePath, bufferSize}, ex);
			}
		} else {
			Loggers.fatal(this, TextMessage.FILE_IS_NOT_READABLE, new Object[]{inputFilePath});
		}
		Loggers.debug(this, TextMessage.FILE_READ_METHOD_END);
		return bufferedInputStream;
	}
	
	public OutputStream getFileOutputStream(File outputFile) {
		return this.getFileOutputStream(outputFile.getPath(), false);
	}
	
	public OutputStream getFileOutputStream(File outputFile, boolean appendMode) {
		return this.getFileOutputStream(outputFile.getPath(), appendMode);
	}
	
	private OutputStream getFileOutputStream(String outputFilePath, boolean appendMode) {
		Loggers.debug(this, TextMessage.FILE_WRITE_START, new Object[]{outputFilePath});
		BufferedOutputStream bufferedOutputStream = null;
		File outputFile = new File(outputFilePath);
		if (outputFile.exists() == false) {
			Loggers.debug(this, TextMessage.FILE_DOES_NOT_EXISTS, new Object[]{outputFilePath});
			fileCreate(outputFile);
		}
		if (outputFile.canWrite() == true) {
			Loggers.debug(this, TextMessage.FILE_IS_WRITEABLE, new Object[]{outputFilePath});
			int bufferSize = FileIOConfig.getBufferOutputStreamSize();
			try {
				FileOutputStream outputStream = new FileOutputStream(outputFile, appendMode);
				bufferedOutputStream = new BufferedOutputStream(outputStream, bufferSize);
				Loggers.debug(this, TextMessage.FILE_WRITE_WILL_BE_SUCCESSFUL, new Object[]{outputFilePath});
			} catch(Exception ex) {
				Loggers.fatal(this, TextMessage.FILE_WRITE_IMPOSSIBLE, new Object[]{outputFilePath, bufferSize}, ex);
			}
		} else {
			Loggers.fatal(this, TextMessage.FILE_IS_NOT_WRITEABLE, new Object[]{outputFilePath});
		}
		Loggers.debug(this, TextMessage.FILE_WRITE_METHOD_END);
		return bufferedOutputStream;
	}
	
	private boolean fileCreate(File outputFile) {
		boolean isFileCreated = false;
		boolean isDirectoryCreatedSuccessful = false;
		try {
			File outDir = outputFile.getParentFile();
			if (outDir.exists() || outDir.mkdirs()) {
				isDirectoryCreatedSuccessful = true;
			}
			isFileCreated = outputFile.createNewFile();
			if (isDirectoryCreatedSuccessful && isFileCreated) {
				Loggers.debug(this, TextMessage.FILE_CREATED_SUCCESSFUL, new Object[]{outputFile.getPath()});
			} else {
				Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outputFile.getPath()});
			}
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outputFile.getPath()});
		}
		return isFileCreated;
	}
	
	/**
	 * Allow to process and write data "on the fly" 
	 * (if for chosen data process exists actionOutputStream)
	 * @param inputStream
	 * @param actionOutputStream Example: CipherOutputStream, ZipArchiveOutputStream, and other
	 * @return Flag of operation result: true
	 * @throws IOException 
	 */
	public boolean writeProcessingData(InputStream inputStream, OutputStream actionOutputStream) throws IOException {
		Loggers.debug(this, TextMessage.FILE_WRITE_PROCESSING_DATA_START);
		boolean isProcessingWritingSuccessful = false;
		int bufferSize = FileIOConfig.getBufferInputStreamSize();
		byte[] buffer = new byte[bufferSize];
		int numberReadedBytes = 0;
		int numberIteration = 0;
		// used "long" type because size of file can be over 2 Gb
		long totalNumberReadedBytes = 0;
		actionOutputStream.flush();
		while ((numberReadedBytes = inputStream.read(buffer)) != -1) {
			actionOutputStream.write(buffer, 0, numberReadedBytes);
			actionOutputStream.flush();
			totalNumberReadedBytes += numberReadedBytes;
			numberIteration += 1;
			Loggers.trace(this, TextMessage.FILE_WRITE_PROCESSING_DATA_CURRENT_CONDITION, new Object[]{numberReadedBytes, totalNumberReadedBytes, numberIteration});
		}
		isProcessingWritingSuccessful = true;
		// No exception - means all was good
		Loggers.debug(this, TextMessage.FILE_WRITE_PROCESSING_DATA_SUCCESSFUL, new Object[]{totalNumberReadedBytes, numberIteration, bufferSize});
		
		Loggers.debug(this, TextMessage.FILE_WRITE_PROCESSING_DATA_END);
		return isProcessingWritingSuccessful;
	}
	
	private boolean writeProcessingData(String inputFilePath, OutputStream actionOutputStream) throws IOException {
		InputStream inputStream = this.getFileInputStream(inputFilePath);
		boolean isProcessingWritingSuccessful = this.writeProcessingData(inputStream, actionOutputStream);
		inputStream.close();
		return isProcessingWritingSuccessful;
	}
	
	public boolean writeProcessingData(File inputFile, OutputStream actionOutputStream) throws IOException {
		Loggers.debug(this, TextMessage.FILE_WRITE_PROCESSING_DATA_FILE_INFO, new Object[]{inputFile.getPath(), inputFile.length(), inputFile.isFile()});
		boolean isProcessingWritingSuccessful = this.writeProcessingData(inputFile.getPath(), actionOutputStream);
		return isProcessingWritingSuccessful;
	}
	
	
	/**
	 * Create file or directory. Name is random and unique (UUID) and can be a little modified with prefix.
	 * @param destinationDirectory Where will be placed file or directory
	 * @param specificPrefix Prefix at the begin of created file
	 * @param fileType Define type of file by string: <code>file</code> or <code>directory</code>
	 * @return Created file or directory. Or <code>null</code> if something went wrong.
	 */
	public File createSpecificFile(File destinationDirectory, String specificPrefix, String fileType) {
		fileType = fileType.toLowerCase().trim();
		File specificFile = null;
		try {
			boolean isSpecificFileExists = true;
			UUID currentIdentifier = null;
			// Avoid repeat identifiers
			while(isSpecificFileExists) {
				currentIdentifier = UUID.randomUUID();
				specificFile = new File(destinationDirectory.getPath() + File.separator + specificPrefix + "_" + currentIdentifier);
				isSpecificFileExists = specificFile.exists();
			}
			if (fileType.equals("directory") || fileType.equals("folder")) {
				if (	(specificFile.exists() == false) || (specificFile.isDirectory() == false)) {
					specificFile.mkdirs();
				}
			} else if (fileType.equals("file")) {
				destinationDirectory.mkdirs();
				specificFile.createNewFile();
			}
		} catch (IOException ex) {
			Loggers.fatal(this, TextMessage.SPECIFIC_FILE_CREATE_IMPOSSIBLE, new Object[]{destinationDirectory, specificPrefix, fileType}, ex);
		}
		if (	(specificFile != null) && (specificFile.exists() == true)	) {
			Loggers.debug(this, TextMessage.SPECIFIC_FILE_CREATED_SUCCESSFUL, new Object[]{destinationDirectory, specificPrefix, fileType, specificFile.getPath()});
		} else {
			Loggers.fatal(this, TextMessage.SPECIFIC_FILE_CREATE_IMPOSSIBLE, new Object[]{destinationDirectory, specificPrefix, fileType});
		}
		return specificFile;
	}
	
	/**
	 * Create temporary directory with random unique name
	 * @return Created directory, or <code>null</code> when error occurred
	 */
	public File createTempDirectory() {
		File tempDirectory = null;
		String tempDirectoryPath = "";
		boolean isTempDirectoryAlreadyExists = true;
		while (isTempDirectoryAlreadyExists == true) {
			tempDirectoryPath = System.getProperty("user.home") + File.separator + "temp_" +UUID.randomUUID();
			tempDirectory = new File(tempDirectoryPath);
			isTempDirectoryAlreadyExists = tempDirectory.exists();
		}
		if (tempDirectory.mkdirs() == false) {
			tempDirectory = null;
		}
		
		if (tempDirectory.exists()) {
			Loggers.debug(this, TextMessage.FILE_CREATED_SUCCESSFUL, new Object[]{tempDirectory.getPath()});
		} else {
			Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{tempDirectoryPath});
		}

		return tempDirectory;
	}
	
}
