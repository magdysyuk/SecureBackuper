package magdysyuk.backuper.source.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;

import magdysyuk.backuper.source.compress.IDataCompress;
import magdysyuk.backuper.source.compress.zip.CompressZip;
import magdysyuk.backuper.source.crypt.DataCrypt;
import magdysyuk.backuper.source.crypt.IDataCrypt;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherAlgorithmName;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;
import magdysyuk.backuper.source.crypt.hash.HashFileAlgorithmName;
import magdysyuk.backuper.source.crypt.hash.HashFileCalculator;
import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;
import magdysyuk.backuper.source.steganography.SteganographyImageLSB;
import magdysyuk.backuper.source.xml.parser.ParserFileReportXml;
import magdysyuk.backuper.source.xml.report.CreatorFileReportXml;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;


/**
 * Logic of application is in this class.
 * Here will be used some hardcoded parameters (which encrypt algorithm will be used, order of operations).
 * They define, how application will works.
 */
public class MainProcessor {
	
	
	/**
	 * First will be encrypting, second - compressing to one zip file, and at the end - putting it by LSB method into image.
	 * If full zip data file could not be packed in 1 image file, image will be copied so many time as required.
	 * At the end you will get folder with images (which contains zipped encrypted files), 
	 * and xml report file (where is saved order of images for unpacking them).
	 * Do not rename received image files manually.
	 * @param inputDataFile Input file (or directory) for processing
	 * @param password Secret phrase for encryption
	 * @param imageForSteganography Image file, will be used as container for data
	 * @param outputDirectoryForImages Directory for received image file with data into them
	 * @param outputReportFile XML file for restoring information in the future
	 * @return <code>true</code> if all operations are successful 
	 * (received image files, and made report xml file for restoring original files),
	 * <br/><code>false</code> otherwise
	 */
	public boolean encryptCompressHideFiles(File inputDataFile, char[] password, File imageForSteganography, File outputDirectoryForImages, File outputReportFile) {
		boolean isOperationSuccessful = false;
		try {
			Loggers.debug(this, TextMessage.MAIN_PROCESSOR_OPERATION_ENCRYPT_COMPRESS_HIDE_FILES_BEGIN, new Object[]{inputDataFile.getPath(), imageForSteganography.getPath(), outputDirectoryForImages.getPath(), outputReportFile.getPath()} );
			if (	(inputDataFile.exists()) && (password != null) && (password.length > 0) && (ArrayUtils.isEquals(password, "".toCharArray()) == false) )	 {
				
				FileIO fileIO = new FileIO();
				File tempDirectory = fileIO.createTempDirectory();
				
				CipherAlgorithmName cipherAlgorithm = CipherAlgorithmName.AES;
				CipherMode cipherMode = CipherMode.CBC;
				CipherPadding cipherPadding = CipherPadding.PKCS5Padding;
				
				File directoryForEncryptedFiles = fileIO.createSpecificFile(tempDirectory, "encrypted", "directory");
				
				if (directoryForEncryptedFiles.exists() && directoryForEncryptedFiles.isDirectory()) {
					// Create folder with random name in user's home directory
					tempDirectory = fileIO.createTempDirectory();
					
					IDataCrypt dataCrypt = new DataCrypt(cipherAlgorithm);
					boolean isEncryptSuccessful = dataCrypt.cryptFile(Cipher.ENCRYPT_MODE, inputDataFile,
							directoryForEncryptedFiles, password, cipherMode, cipherPadding);
					File archiveFile = fileIO.createSpecificFile(tempDirectory, "archive", "file");
					
					if (isEncryptSuccessful) {
						IDataCompress compressZip = new CompressZip();
						boolean isCompressionSuccessful = compressZip.compress(directoryForEncryptedFiles, archiveFile);
						if (isCompressionSuccessful) {
							SteganographyImageLSB steganographyImageLSB = new SteganographyImageLSB();
							if (	(outputDirectoryForImages.exists() == false) || (outputDirectoryForImages.isDirectory() == false)	) {
								outputDirectoryForImages.mkdirs();
							}
							List<Map<File, Integer>> receivedImagesWithHiddenData = steganographyImageLSB.putDataFileIntoImages(archiveFile, imageForSteganography, outputDirectoryForImages);
							if (receivedImagesWithHiddenData != null) {
								// All operations are done successful. Now need to save information in report file (for correct restoring information in the future)
								isOperationSuccessful = makeAndSaveReport(receivedImagesWithHiddenData, outputReportFile);
							} else {
								Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_PUT_FILE_INTO_IMAGES_UNKNOWN_ERROR, new Object[]{archiveFile, imageForSteganography, outputDirectoryForImages});
							}
						} else {
							Loggers.fatal(this, TextMessage.ZIP_COMPRESS_IMPOSSIBLE, new Object[]{archiveFile.getPath()});
						}
						
					} else {
						Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE);
					}
					
					// Removing temporary files. Sometimes it doesn't works (impossible remove archive file). TODO: check where is blocked that file
					try {
						FileUtils.deleteDirectory(tempDirectory);
					} catch (Exception ex) {
						Loggers.debug(this, TextMessage.FILE_DELETE_IMPOSSIBLE, new Object[]{directoryForEncryptedFiles.getPath(), ex});
					}
				}
			}
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.MAIN_PROCESSOR_OPERATION_ENCRYPT_COMPRESS_HIDE_FILES_UNKNOWN_ERROR, new Object[]{inputDataFile, imageForSteganography, outputDirectoryForImages, outputReportFile}, ex);
		}
		Loggers.debug(this, TextMessage.MAIN_PROCESSOR_OPERATION_ENCRYPT_COMPRESS_HIDE_FILES_END, new Object[]{isOperationSuccessful});
		return isOperationSuccessful;
	}
	
	
	/**
	 * Receive original data files
	 * @param inputDirectoryWithImagesContainsData Directory for image file with data into them 
	 * @param inputReportXml XML file for restoring information
	 * @param outputDirectoryForExtractedFiles Directory where will be placed extracted data
	 * @param password Secret phrase for decryption
	 * @return <code>true</code> if date extracted successful,
	 * <br/><code>false</code> otherwise
	 */
	public boolean decryptUncompressExtractFiles (File inputDirectoryWithImagesContainsData, File inputReportXml, File outputDirectoryForExtractedFiles, char[] password) {
		boolean isOperationSuccessful = false;
		
			Loggers.debug(this, TextMessage.MAIN_PROCESSOR_OPERATION_DECRYPT_UNCOMPRESS_EXTRACT_FILES_BEGIN, new Object[]{inputDirectoryWithImagesContainsData.getPath(), inputReportXml.getPath(), outputDirectoryForExtractedFiles.getPath()});
			List<Map<File, Integer>> imagesWithHiddenData = new ArrayList<Map<File,Integer>>();
			ParserFileReportXml parserFileReportXml = new ParserFileReportXml();
			List<Map<String, String>> listExpectedImageFilesAndProperties = parserFileReportXml.getExpectedListImageFilesWithProperties(inputReportXml);
			// Check that image files exists and have right size and checksums
			for (Map<String, String> fileAndProperties : listExpectedImageFilesAndProperties) {
				// String constants (keys) are getting from class of parser report file (because parser class has putted data in that map)
				File currentImageFile = new File(inputDirectoryWithImagesContainsData.getPath() + File.separator + fileAndProperties.get("filename"));
				long expectedFileSize = Long.valueOf(fileAndProperties.get("filesize_bytes"));
				int expectedNumberHiddenDataBytes = Integer.valueOf(fileAndProperties.get("number_hidden_data_bytes"));
				String expectedMd5Checksum = fileAndProperties.get("md5_checksum");
				String expectedAdler32Checksum = fileAndProperties.get("adler32_checksum");
				
				HashFileCalculator hashFileCalculator = new HashFileCalculator(currentImageFile);
				String receivedMd5Checksum = hashFileCalculator.getHash(HashFileAlgorithmName.MD5);
				String receivedAdler32Checksum = hashFileCalculator.getHash(HashFileAlgorithmName.Adler32);
				long receivedFileSize = currentImageFile.length();
				if(		(expectedFileSize != receivedFileSize) || 
						(expectedMd5Checksum.equals(receivedMd5Checksum) == false) || 
						(expectedAdler32Checksum.equals(receivedAdler32Checksum) == false)	) {
					Loggers.fatal(this, TextMessage.HASH_CHECKSUM_FILE_NOT_EQUALS, new Object[]{expectedMd5Checksum + " | " + expectedAdler32Checksum, receivedMd5Checksum + " | " + receivedAdler32Checksum, expectedFileSize, receivedFileSize, currentImageFile.getPath()});
				} else {
					Map<File, Integer> imageWithHiddenData = new HashMap<File, Integer>();
					imageWithHiddenData.put(currentImageFile, expectedNumberHiddenDataBytes);
					imagesWithHiddenData.add(imageWithHiddenData);
				}
			}
			try {
			// Now we have completed list of maps with files and numbers of hidden data bytes
			if(imagesWithHiddenData.size() > 0) {
				FileIO fileIO = new FileIO();
				File tempDirectory = fileIO.createTempDirectory();
				
				SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
				File extractedArchiveDataFile = new File(tempDirectory + File.separator + "archive_" + UUID.randomUUID());
				boolean isReceivingArchiveFromImagesSuccessful = steganographyImage.extractDataFileFromImages(imagesWithHiddenData, extractedArchiveDataFile);
				if ((isReceivingArchiveFromImagesSuccessful == true) && (extractedArchiveDataFile.exists() && 
						extractedArchiveDataFile.length() > 0)) {
					// We have got a zip archive. Next step - uncompress it
					IDataCompress compressZip = new CompressZip();
					File directoryForUncompressedFiles = new File(tempDirectory + File.separator + "uncompressed_" + UUID.randomUUID());
					boolean isUncompressionSuccessful = compressZip.uncompress(extractedArchiveDataFile, directoryForUncompressedFiles);
					if (isUncompressionSuccessful == true) {
						if (outputDirectoryForExtractedFiles.exists() == false || outputDirectoryForExtractedFiles.isDirectory() == false) {
							outputDirectoryForExtractedFiles.mkdirs();
						}
						// Need to decrypt it
						File directoryForDecryptedFiles = new File(tempDirectory + File.separator + "decrypted_" + UUID.randomUUID());
						IDataCrypt dataCrypt = new DataCrypt(CipherAlgorithmName.AES);
						boolean isDecryptSuccessful = dataCrypt.cryptFile(Cipher.DECRYPT_MODE, directoryForUncompressedFiles, directoryForDecryptedFiles, password, CipherMode.CBC, CipherPadding.PKCS5Padding);
						if(isDecryptSuccessful == true) {
							/*
							 * When packed, we archived encrypted directory. 
							 * So, original data are into encrypted directory, which is into uncompressed directory.
							 */
							for (File uncompressedDir : directoryForDecryptedFiles.listFiles()) {
								for(File encryptedDir : uncompressedDir.listFiles()) {
									for (File decryptedFile : encryptedDir.listFiles()) {
										try {
											FileUtils.moveToDirectory(decryptedFile, outputDirectoryForExtractedFiles, false);
											isOperationSuccessful = true;
										} catch (IOException ex) {
											Loggers.fatal(this, TextMessage.FILE_MOVE_IMPOSSIBLE, new Object[]{decryptedFile, outputDirectoryForExtractedFiles}, ex);
										}
									}
								}
							}
						}
					} else {
						Loggers.fatal(this, TextMessage.ZIP_UNCOMPRESS_IMPOSSIBLE);
					}
						
				} else {
					Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_EXTRACT_FILE_FROM_IMAGES_UNKNOWN_ERROR, new Object[]{extractedArchiveDataFile});
				}
			} else {
				Loggers.fatal(this, TextMessage.MAIN_PROCESSOR_OPERATION_DECRYPT_UNCOMPRESS_EXTRACT_FILES_ERROR_NO_DATA_FOR_PROCESSING);
			}
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.MAIN_PROCESSOR_OPERATION_DECRYPT_UNCOMPRESS_EXTRACT_FILES_UNKNOWN_ERROR, new Object[]{inputDirectoryWithImagesContainsData, inputReportXml, outputDirectoryForExtractedFiles}, ex);
		}
		Loggers.debug(this, TextMessage.MAIN_PROCESSOR_OPERATION_DECRYPT_UNCOMPRESS_EXTRACT_FILES_END, new Object[]{isOperationSuccessful});
		return isOperationSuccessful;
	}
	
	
	
	private boolean makeAndSaveReport(List<Map<File, Integer>> receivedImagesWithHiddenData, File outXmlFile) {
		boolean isSavingReportSuccessful = false;
		CreatorFileReportXml cfrx = new CreatorFileReportXml();
		// It will be file with unique, randomly generated name
		File receivedReportFile = cfrx.makeReport(receivedImagesWithHiddenData, outXmlFile.getParentFile());
		// Now we need to put it by required file path
		if (	(receivedReportFile.exists()) && (receivedReportFile.length() > 0)	) {
			// If file already exists, we should replace old file by new file
			boolean isRemovingOldReportSuccessful = false;
			if (outXmlFile.exists()) {
				isRemovingOldReportSuccessful = outXmlFile.delete();
			} else {
				isRemovingOldReportSuccessful = true;
			}
			if(isRemovingOldReportSuccessful == true) {
				try {
					FileUtils.copyFile(receivedReportFile, outXmlFile);
					if (outXmlFile.exists() && outXmlFile.length() > 0) {
						isSavingReportSuccessful = true;
						Loggers.debug(this, TextMessage.REPORT_XML_SUCCESSFUL, new Object[]{outXmlFile.getPath()});
					}
					receivedReportFile.delete();
				} catch (IOException ex) {
					Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outXmlFile.getPath()}, ex);
				}
			}
		}
		
		return isSavingReportSuccessful;
	}
	

	
	
}
