package magdysyuk.backuper.source.compress.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import magdysyuk.backuper.source.compress.IDataCompress;
import magdysyuk.backuper.source.filesystem.DirectoryUtils;
import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;


public class CompressZip implements IDataCompress {
	
	/*
	 * Here is not used internal java zip classes, however will be useful article: 
	 * <a href="http://java.sun.com/developer/technicalArticles/Programming/compression/">Compressing and Decompressing Data Using Java APIs</a>
	 * Internal java compression was not used by reason that it could have a problems with non-ascii filenames.
	 * Thus here I used Apache Commons library.
	 * Also see article <a href="https://blogs.oracle.com/xuemingshen/entry/non_utf_8_encoding_in">Non-UTF-8 encoding in ZIP file</a>
	 */
	
	public CompressZip() {
		this.fileIO = new FileIO();
		this.directoryUtils = new DirectoryUtils();
	}
	
	private FileIO fileIO;
	private FileIO getFileIO() {
		return this.fileIO;
	}
	
	private DirectoryUtils directoryUtils;
	private DirectoryUtils getDirectoryUtils() {
		return this.directoryUtils;
	}
	
	/**
	 * If input data is directory - it will be zipped recursively
	 */
	@Override
	public boolean compress(File inputData, File archiveFile) {
		Loggers.debug(this, TextMessage.ZIP_START_COMPRESS, new Object[]{inputData.getPath(), archiveFile.getPath()});
		boolean isCompressedSuccessful = false;
		try {
			OutputStream outputStream = this.getFileIO().getFileOutputStream(archiveFile);
			ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(outputStream);
			
			this.compressData(inputData, inputData, zipOutputStream);
	
			zipOutputStream.flush();
			zipOutputStream.close();
			outputStream.flush();
			outputStream.close();
			isCompressedSuccessful = true;
			Loggers.debug(this, TextMessage.ZIP_COMPRESS_SUCCESSFUL, new Object[]{archiveFile.getPath()});
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.ZIP_COMPRESS_IMPOSSIBLE, new Object[]{archiveFile.getPath()}, ex);
		}
		Loggers.debug(this, TextMessage.ZIP_END_COMPRESS);
		return isCompressedSuccessful;
	}
	
	/**
	 * Compress file or folder (include all inner files/folders)
	 * @param sourceData Input data
	 * @param rootFolder Need to calculate relative path to root of zip archive. Also used for nested folders/files. 
	 * Examples: "file.txt" (for single file in root of zip archive),
	 * "dir1/dir2/" for empty nested folder, "dir1/dir2/file" for nested file.
	 * @param zipOutputStream
	 * @throws IOException
	 */
	private void compressData(File sourceData, File rootFolder, ZipArchiveOutputStream zipOutputStream) throws IOException {
		if (sourceData.isDirectory() == true) {
			File[] innerFiles = sourceData.listFiles();
			for (File innerFile : innerFiles) {
				String relativeFilePath = this.getDirectoryUtils().getRelativeFilePathWithRoot(rootFolder, innerFile);
				if (innerFile.isDirectory() == true) {
					ZipArchiveEntry zipEntry = new ZipArchiveEntry(relativeFilePath);
					zipOutputStream.putArchiveEntry(zipEntry);
					zipOutputStream.closeArchiveEntry();
					zipOutputStream.flush();
					// Recursive call for going through filesystem directories tree
					compressData(innerFile, rootFolder, zipOutputStream);
				} else {
					this.compressNormalFile(innerFile, relativeFilePath, zipOutputStream);
				}
			}
		} else {
			this.compressNormalFile(sourceData, sourceData.getName(), zipOutputStream);
		}
	}
	
	// Compress file, not directory
	private void compressNormalFile(File sourceFile, String relativeFilePath, ZipArchiveOutputStream zipOutputStream) throws IOException {
		ZipArchiveEntry zipEntry = new ZipArchiveEntry(relativeFilePath);
		zipOutputStream.putArchiveEntry(zipEntry);
		this.getFileIO().writeProcessingData(sourceFile, zipOutputStream);
		zipOutputStream.closeArchiveEntry();
		zipOutputStream.flush();
	}
	
	@Override
	public boolean uncompress(File archiveFile, File uncompressDestinationDirectory) {
		Loggers.debug(this, TextMessage.ZIP_START_UNCOMPRESS, new Object[]{archiveFile.getPath(), uncompressDestinationDirectory.getPath()});
		boolean isUncompressSuccessful = false;
		InputStream archiveInputStream = this.getFileIO().getFileInputStream(archiveFile);
		ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(archiveInputStream);
		ZipArchiveEntry zipEntry = null;
		try {
			while ((zipEntry = zipInputStream.getNextZipEntry()) != null) {
				String entryName = zipEntry.getName();
				String outFilePath = uncompressDestinationDirectory.getPath() + File.separator + entryName;
				File outFile = new File(outFilePath);
				if (zipEntry.isDirectory() == true) {
					if(outFile.exists() || outFile.mkdirs()) {
						Loggers.debug(this, TextMessage.ZIP_UNCOMPRESS_ENTITY, new Object[]{outFilePath, "Directory"});
					} else {
						Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outFilePath});
					}
				} else {
					// Create directory and uncompress file
					if (outFile.getParentFile().exists() || outFile.getParentFile().mkdirs()) {
						OutputStream outputStream = this.getFileIO().getFileOutputStream(outFile);
						this.getFileIO().writeProcessingData(zipInputStream, outputStream);
						outputStream.flush();
						outputStream.close();
						Loggers.debug(this, TextMessage.ZIP_UNCOMPRESS_ENTITY, new Object[]{outFilePath, "File"});
					} else {
						Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outFile.getParent()});
					}
				}
			}
			zipInputStream.close();
			archiveInputStream.close();
			isUncompressSuccessful = true;
			Loggers.debug(this, TextMessage.ZIP_UNCOMPRESS_SUCCESSFUL, new Object[]{archiveFile.getPath(), uncompressDestinationDirectory.getPath()});
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.ZIP_UNCOMPRESS_IMPOSSIBLE, ex);
			try {
				zipInputStream.close();
				archiveInputStream.close();
			} catch (Exception e) {
				// If we can't close descriptor of input file - it's bad.
				Loggers.fatal(this, TextMessage.ZIP_UNCOMPRESS_IMPOSSIBLE, e);
			}
		}
		
		Loggers.debug(this, TextMessage.ZIP_END_UNCOMPRESS, new Object[]{isUncompressSuccessful});
		return isUncompressSuccessful;
	}
}
