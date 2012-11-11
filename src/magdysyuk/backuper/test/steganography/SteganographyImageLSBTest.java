package magdysyuk.backuper.test.steganography;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;

import magdysyuk.backuper.source.steganography.SteganographyImageLSB;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class SteganographyImageLSBTest {
	
	private File[] originalImageFiles = new File[] {
		new File("unittests_files\\steganography\\expected\\images_for_steganography\\image_01.jpg"),
		new File("unittests_files\\steganography\\expected\\images_for_steganography\\image_02.png"),
		new File("unittests_files\\steganography\\expected\\images_for_steganography\\image_03.gif")
	};
	private File[] getOriginalImageFiles() {
		return this.originalImageFiles;
	}
	
	private File getDirectoryForOutProcessedFiles() {
		return (new File("unittests_files\\steganography\\obtained\\"));
	}
	
	private File[] originalDataFiles = new File[] {
		new File("unittests_files\\steganography\\expected\\data_for_steganography\\hello_world.txt"),
		new File("unittests_files\\steganography\\expected\\data_for_steganography\\eclipse.exe"),
		new File("unittests_files\\steganography\\expected\\data_for_steganography\\readme_eclipse.html"),
		new File("unittests_files\\steganography\\expected\\data_for_steganography\\SciLexer.dll"),
		new File("unittests_files\\steganography\\expected\\data_for_steganography\\mysql-workbench.msi")
	};
	private File[] getOriginalDataFiles() {
		return this.originalDataFiles;
	}
	
	@Test
	public void testExistsOriginalFiles() {
		File[] originalImagesFiles = this.getOriginalImageFiles();
		for (File originalImageFile : originalImagesFiles) {
			assertTrue(originalImageFile.exists());
		}
		File[] originalDataFiles = this.getOriginalDataFiles();
		for (File originalDataFile : originalDataFiles) {
			assertTrue(originalDataFile.exists());
		}
	}
	
	@Test
	public void testResizeImage () throws IOException {
		File[] originalImagesFiles = this.getOriginalImageFiles();
		
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		for (File originalImageFile : originalImagesFiles) {
			File resizedImageFile = new File(this.getDirectoryForOutProcessedFiles().getPath() + File.separator +
					originalImageFile.getName() + "_resized.png");
			assertTrue(steganographyImage.resizeImage(originalImageFile, resizedImageFile));
			assertTrue(resizedImageFile.exists());
			assertTrue(resizedImageFile.delete());
		}
	}
	
	@Test
	public void testPutDataStrChunkIntoImage() throws IOException {
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		byte[] byteStr = "hello".getBytes();
		for (File originalImageFile : this.getOriginalImageFiles()) {
			File processedImageFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_byte_str_chunk.png");
			assertTrue(steganographyImage.putDataChunkIntoImage(originalImageFile, byteStr, 
					processedImageFile));
			processedImageFile.delete();
		}
	}
	
	@Test
	public void testPutDataRandomChunkIntoImage() throws IOException {
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		
		for (File originalImageFile : this.getOriginalImageFiles()) {
			BufferedImage originalImage = ImageIO.read(originalImageFile);
			int imageWidth = originalImage.getWidth();
			int imageHeight = originalImage.getHeight();
			int maxDataBufferLength = ((imageWidth * imageHeight * 3) / 4) - 1;
			Random randomGenerator = new Random();
			byte[] randomData1Byte = new byte[1];
			randomGenerator.nextBytes(randomData1Byte);
			byte[] randomData50Bytes = new byte[50];
			randomGenerator.nextBytes(randomData50Bytes);
			byte[] maxAvailableData = new byte[maxDataBufferLength];
			randomGenerator.nextBytes(maxAvailableData);
			byte[] bigData = new byte[(maxDataBufferLength + 1000)];
			randomGenerator.nextBytes(bigData);
			
			byte[] anotherData = new byte[149];
			randomGenerator.nextBytes(anotherData);
			
			File processedImageFile = null;
			
			processedImageFile = new File(this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_random_byte_1_byte_chunk.png");
			assertTrue(steganographyImage.putDataChunkIntoImage(originalImageFile, randomData1Byte, processedImageFile));
			processedImageFile.delete();
			
			processedImageFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_random_byte_50_bytes_chunk.png");
			assertTrue(steganographyImage.putDataChunkIntoImage(originalImageFile, randomData50Bytes, processedImageFile));
			processedImageFile.delete();
			
			processedImageFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_random_byte_max_available_"+ maxDataBufferLength +"_bytes_chunk.png");
			assertTrue(steganographyImage.putDataChunkIntoImage(originalImageFile, maxAvailableData, processedImageFile));
			processedImageFile.delete();
			
			processedImageFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_random_byte__big_data_"+ bigData.length +"_bytes_chunk.png");
			// Used assertFalse because size is over of available
			assertFalse(steganographyImage.putDataChunkIntoImage(originalImageFile, bigData, processedImageFile));
			processedImageFile.delete();
			
			processedImageFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data_random_byte_"+ anotherData.length +"_bytes_chunk.png");
			assertTrue(steganographyImage.putDataChunkIntoImage(originalImageFile, anotherData, processedImageFile));
			processedImageFile.delete();
		}
	}
	
	@Test
	public void testPutDataFileIntoImages() throws IOException {
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		List<Map<File, Integer>> imagesWithData = null;
		for (File originalDataFile : this.getOriginalDataFiles()) {
			for (File originalImageFile : this.getOriginalImageFiles()) {
				imagesWithData = null;
				imagesWithData = steganographyImage.putDataFileIntoImages(originalDataFile, originalImageFile, this.getDirectoryForOutProcessedFiles());
				assertNotNull(imagesWithData);
			}
		}
		int numberImageFilesWithData = 0;
		if (imagesWithData != null) {
			for (Map<File, Integer> imageWithDataMap : imagesWithData) {
				for (Entry<File, Integer> imageWithDataEntry : imageWithDataMap.entrySet()) {
					assertTrue(imageWithDataEntry.getKey().exists());
					assertTrue(imageWithDataEntry.getKey().length() > 0);
					if (imageWithDataEntry.getKey().exists()) {
						numberImageFilesWithData += 1;
					}
					imageWithDataEntry.getKey().delete();
				}
			}
		}
		assertTrue(numberImageFilesWithData > 0);
	}
	
	@Test
	public void testPutAndExtractDataChunkFromImage() throws IOException {
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		String dataStr = "Hello WoRlD!";
		byte[] dataBytes = dataStr.getBytes();
		
		for (File originalImageFile : this.getOriginalImageFiles()) {
			File imageFileWithData = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + 
					originalImageFile.getName() + "_image_with_data.png");
			steganographyImage.putDataChunkIntoImage(originalImageFile, dataBytes, imageFileWithData);
			int numberDataBytes = dataBytes.length;
			byte[] extractedData = steganographyImage.extractDataChunkFromImage(imageFileWithData, numberDataBytes);
			assertTrue(Arrays.equals(dataBytes, extractedData));
			String extractedString = new String(extractedData);
			assertTrue(dataStr.equals(extractedString));
			imageFileWithData.delete();
		}
	}
	
	@Test
	public void testPutAndExtractDataFileFromImages() throws IOException {
		SteganographyImageLSB steganographyImage = new SteganographyImageLSB();
		for (File originalDataFile : this.getOriginalDataFiles()) {
			for (File originalImageFile : this.getOriginalImageFiles()) {
				List<Map<File, Integer>> imagesWithHiddenData = steganographyImage.putDataFileIntoImages(originalDataFile, originalImageFile, this.getDirectoryForOutProcessedFiles());
				File extractedDataFile = new File (this.getDirectoryForOutProcessedFiles().getPath() + File.separator + "extracted_from_images_data_file_" + originalDataFile.getName());

				assertTrue(steganographyImage.extractDataFileFromImages(imagesWithHiddenData, extractedDataFile));
				assertTrue(FileUtils.contentEquals(originalDataFile, extractedDataFile));
				assertTrue(extractedDataFile.delete());
				
				assertTrue(originalDataFile.canRead());
				assertTrue(originalDataFile.canWrite());
				
				for (Map<File, Integer> imageWithHiddenData : imagesWithHiddenData) {
					for (Entry<File, Integer> imageWithDataEntry : imageWithHiddenData.entrySet()) {
						assertTrue(imageWithDataEntry.getKey().delete());
					}
				}
			}
		}
	}
	
}
