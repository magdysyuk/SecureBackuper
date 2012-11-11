package magdysyuk.backuper.test.logic;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import magdysyuk.backuper.source.filesystem.DirectoryUtils;
import magdysyuk.backuper.source.logic.MainProcessor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;


public class MainProcessorTest {
	
	private File getOriginalDataDirectory() {
		return new File("unittests_files\\main_processing\\expected\\original_files\\data\\");
	}
	
	private File[] originalImageFiles = new File[] {
		new File("unittests_files\\main_processing\\expected\\original_files\\images_for_steganography\\image_01.jpg"),
	};
	private File[] getOriginalImageFiles() {
		return this.originalImageFiles;
	}
	
	private char[][] getPasswords() {
		char[][] passwords = new char[][]{
				"abc_рст() 		~ИЈ!@#$%^&*()_+}{><?/\\YUIowhoidOHDWSisdhoDWnioDOHNDWoihdioAhdOLADHOADHOAWxchc8wcywc8wq0oq8whodhDODHOWwaodhOHDWODAWDOHWdWJdhjkqpdwpqdw[wodwuedw$3433rwefju7iredf0df02.''[0(0k3r0-_uo8d8".toCharArray()
				};
		return passwords;
	}
	
	private File getOutputDirectoryForImages() {
		return new File("unittests_files\\main_processing\\obtained\\output_images\\");
	}
	
	private File getOutputReportFile() {
		return new File("unittests_files\\main_processing\\obtained\\output_report.xml");
	}
	
	private File getDirectoryForDecryptedFiles() {
		return new File("unittests_files\\main_processing\\obtained\\decrypted_files");
	}
	
	@Test
	public void testEncryptCompressHideExtractUncompressDecryptFiles() throws IOException {
		
		if (this.getOutputReportFile().exists()) {
			this.getOutputReportFile().delete();
		}
		if (this.getDirectoryForDecryptedFiles().exists()) {
			FileUtils.deleteDirectory(this.getDirectoryForDecryptedFiles());
		}
		if (this.getOutputDirectoryForImages().exists()) {
			FileUtils.deleteDirectory(this.getOutputDirectoryForImages());
		}
		
		DirectoryUtils directoryUtils = new DirectoryUtils();
		
		MainProcessor mainProcessor = new MainProcessor();
		boolean isEncryptPackHideOperationFinishedSuccessful = false;
		boolean isDecryptUnpackExtractOperationFinishedSuccessful = false;
		for (File originalImageFile : this.getOriginalImageFiles()) {
			for (char[] password : this.getPasswords()) {
				isEncryptPackHideOperationFinishedSuccessful = mainProcessor.encryptCompressHideFiles(this.getOriginalDataDirectory(), password, originalImageFile, this.getOutputDirectoryForImages(), this.getOutputReportFile());
				if (password == null || ArrayUtils.isEquals(password, "".toCharArray())) {
					assertFalse(isEncryptPackHideOperationFinishedSuccessful);
				} else {
					assertTrue(isEncryptPackHideOperationFinishedSuccessful);
					
					isDecryptUnpackExtractOperationFinishedSuccessful = mainProcessor.decryptUncompressExtractFiles(this.getOutputDirectoryForImages(), this.getOutputReportFile(), this.getDirectoryForDecryptedFiles(), password);
					assertTrue(isDecryptUnpackExtractOperationFinishedSuccessful);
					
					/*
					 * Because directory with received data is into directory for decryped files, 
					 * we are going to 1 level deeper for comparing content of original and received directories
					 */
					for (File receivedDataDir : this.getDirectoryForDecryptedFiles().listFiles()) {
						assertTrue(directoryUtils.isDirectoriesEquals(this.getOriginalDataDirectory(), receivedDataDir));
					}
				}
			}
		}

		

	}
	

}
