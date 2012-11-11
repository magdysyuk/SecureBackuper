package magdysyuk.backuper.source.steganography;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.apache.commons.codec.binary.BinaryCodec;


public class SteganographyImageLSB {
	/**
	 * Put content of data file in images files by LSB method.
	 * @param dataFile File which we want to hide into images
	 * @param inputImage If data file have a size over than can be hidden in image,
	 * this image just will be copied. So, if you want to hide secret.txt (5 Mb length)
	 * into image waterfall.jpg (20 kb), you will get a lot of images with waterfall, 
	 * which contains your secret file (each of images will contain a part of secret information).
	 * @param directoryProcessedImageFiles Directory where will be saved images with data 
	 * @return List of maps with image file and number data bytes, putted into image
	 */
	public List<Map<File, Integer>> putDataFileIntoImages (File dataFile, File inputImage, File directoryProcessedImageFiles) {
		List<Map<File, Integer>> outputImagesWithData = new ArrayList<Map<File, Integer>>();
		try {
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_START_PUT_FILE_INTO_IMAGES, new Object[]{dataFile.getPath(), inputImage.getPath(), directoryProcessedImageFiles.getPath()});
			
			boolean isPuttingDataSuccessful = false;
			
			if (	(inputImage.exists() == true) && (inputImage.length() > 0) &&
					(dataFile.exists() == true) && (dataFile.length() > 0) &&
					(inputImage.canRead() == true) && (dataFile.canRead() == true)	) {
				
				if (	(directoryProcessedImageFiles.exists() == false) || (directoryProcessedImageFiles.isDirectory() == false)	) {
					directoryProcessedImageFiles.mkdirs();
				}
				
				boolean isResizedImageFileAlreadyExists = true;
				File resizedImageFile = null;
				// Avoid of already existed files - we just add unique UUID to filename while name will be not unique
				while (isResizedImageFileAlreadyExists == true) {
					resizedImageFile = new File(inputImage.getPath() + "_resized_" + UUID.randomUUID() + ".png");
					isResizedImageFileAlreadyExists = resizedImageFile.exists();
				}
				
				boolean isImageResizedSuccessful = this.resizeImage(inputImage, resizedImageFile);
				
				if (isImageResizedSuccessful == true) {
					/*
					 * We should know size of byte[] array, which we should read
					 * from data file, and this byte[] array should be fit into image.
					 * 
					 * Length of buffer is calculated based on knowledge of used LSB method.
					 * 2 bits in each of 3 colors in pixels -> 6 bits in 1 pixel.
					 * numberPixels = numberBits / 6 => numberBytes = (6 / 8) * numberPixels (digit 8 is number bits in byte)
					 * 
					 * If multiple is over than Integer.MAX_VALUE, we will have wrong results, 
					 * but I don't assume this use case - image should be too large,
					 * and don't forget, we will use resized, small images.
					 */
					int dataBufferLength = ((this.getResizedImageWidth() * this.getResizedImageHeight() * 3) / 4) - 1;
					// For small files which could be putted in 1 image
					if (dataFile.length() < dataBufferLength) {
						dataBufferLength = (int) dataFile.length();
					}
					
					FileIO fileIO = new FileIO();
					InputStream inputStream = fileIO.getFileInputStream(dataFile);
					byte[] buffer = new byte[dataBufferLength];
					int numberReadedBytes = 0;
					
					boolean isDataFilePuttedSuccessful = true;
					
					while ((numberReadedBytes = inputStream.read(buffer)) != -1) {
						// Removing useless empty bytes from data array (when was read less bytes than we could store in all pixels of image)
						buffer = Arrays.copyOfRange(buffer, 0, numberReadedBytes);
						
						File outputImageFileWithData = null;
						// UUID's could exists before, so we repeat that until find non used (for unique filename)
						boolean isOutputImageFileWithDataAlreadyExists = true;
						while(isOutputImageFileWithDataAlreadyExists == true) {
							outputImageFileWithData = new File(directoryProcessedImageFiles.getPath() + File.separator + resizedImageFile.getName() + "_data_container_" + UUID.randomUUID() + "_" + dataFile.length() + "_" + inputImage.length() + ".png");
							isOutputImageFileWithDataAlreadyExists = outputImageFileWithData.exists();
						}
						
						boolean isDataChunkPuttedSuccessful = this.putDataChunkIntoImage(resizedImageFile, buffer, outputImageFileWithData);
						if (isDataChunkPuttedSuccessful == false) {
							isDataFilePuttedSuccessful = false;
							Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_PUT_FILE_DATA_CHUNK_INTO_IMAGES_IMPOSSIBLE, new Object[]{inputImage.getPath(), inputImage.length(), resizedImageFile.getPath(), resizedImageFile.length(), dataFile.getPath(), dataFile.length(), dataBufferLength, numberReadedBytes});
							break;
						} else {
							Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_PUT_FILE_DATA_CHUNK_INTO_IMAGES_SUCCESSFUL, new Object[]{inputImage.getPath(), inputImage.length(), resizedImageFile.getPath(), resizedImageFile.length(), dataFile.getPath(), dataFile.length(), dataBufferLength, numberReadedBytes, outputImageFileWithData, outputImageFileWithData.length()});
							// We should know, which length of data contains in file - needed in the future for extract data
							Map<File, Integer> currentImageMap = new HashMap<File, Integer>();
							currentImageMap.put(outputImageFileWithData, numberReadedBytes);
							outputImagesWithData.add(currentImageMap);
						}
					}
					isPuttingDataSuccessful = isDataFilePuttedSuccessful;
				}
				resizedImageFile.delete();
			}
			
			int numberImagesWithData = 0;
			
			if (isPuttingDataSuccessful == false) {
				outputImagesWithData = null;
			} else {
				numberImagesWithData = outputImagesWithData.size();
			}
			
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_END_PUT_FILE_INTO_IMAGES, new Object[]{numberImagesWithData});
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_PUT_FILE_INTO_IMAGES_UNKNOWN_ERROR, new Object[]{dataFile, inputImage, directoryProcessedImageFiles}, ex);
		}
		return outputImagesWithData;
	}
	
	/**
	 * Extract hidden data from images
	 * @param imagesWithHiddenData List of maps which contains image file and number stored in this image data bytes
	 * @param extractedDataFile
	 * @return
	 */
	public boolean extractDataFileFromImages(List<Map<File, Integer>> imagesWithHiddenData, File extractedDataFile) {
		boolean isFileExtractedSuccessful = false;
		try {
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_START_EXTRACT_FILE_FROM_IMAGES, new Object[]{imagesWithHiddenData.size(), extractedDataFile.getPath()});
			
			// If required file already exists - we will rewrite it
			if(extractedDataFile.exists()) {
				extractedDataFile.delete();
			}
			
			int numberImagesWithHiddenData = imagesWithHiddenData.size();
			long numberExtractedBytes = 0;
			
			// How many bytes we really should to get
			long numberExpectExtractBytes = 0;
			for (int i = 0; i < numberImagesWithHiddenData; i++) {
				Map<File, Integer> imagesWithHiddenDataMap = imagesWithHiddenData.get(i);
				for (Entry<File, Integer> imagesWithHiddenDataEntry : imagesWithHiddenDataMap.entrySet()) {
					long numberHiddenBytes = imagesWithHiddenDataEntry.getValue();
					numberExpectExtractBytes += numberHiddenBytes;
				}
			}
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_EXTRACT_FILE_FROM_IMAGES_TOTAL_BYTES_EXPECT, new Object[]{numberExpectExtractBytes, numberImagesWithHiddenData});
	
			if (extractedDataFile.exists() == false) {
				FileIO fileIO = new FileIO();
				OutputStream outputStream = fileIO.getFileOutputStream(extractedDataFile, true);
				
				for (int i = 0; i < numberImagesWithHiddenData; i++) {
					
					// We assume that images in list are in the right order.
					Map<File, Integer> imagesWithHiddenDataMap = imagesWithHiddenData.get(i);
					for (Entry<File, Integer> imagesWithHiddenDataEntry : imagesWithHiddenDataMap.entrySet()) {
						File imageWithHiddenData = imagesWithHiddenDataEntry.getKey();
						int numberHiddenBytesInImage = imagesWithHiddenDataEntry.getValue();
						
						if (imageWithHiddenData.exists()) {
							byte[] extractedData = this.extractDataChunkFromImage(imageWithHiddenData, numberHiddenBytesInImage);
							outputStream.write(extractedData);
							outputStream.flush();
							numberExtractedBytes += extractedData.length;
						} else {
							Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_IMAGE_DOES_NOT_EXISTS, new Object[]{imageWithHiddenData.getPath()});
						}
					}
				}
				outputStream.close();
			}
			
			if (	(numberExtractedBytes == extractedDataFile.length()) &&
					(numberExtractedBytes == numberExpectExtractBytes)	) {
				isFileExtractedSuccessful = true;
			}
			
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_END_EXTRACT_FILE_FROM_IMAGES, new Object[]{numberExtractedBytes, extractedDataFile.getPath(), extractedDataFile.length(), isFileExtractedSuccessful});
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_EXTRACT_FILE_FROM_IMAGES_UNKNOWN_ERROR, new Object[]{extractedDataFile}, ex);
		}
		return isFileExtractedSuccessful;
	}
	
	public byte[] extractDataChunkFromImage(File imageWithHiddenData, int numberDataBytes) throws IOException {
		byte[] extractedData = null;
		if (numberDataBytes > 0) {
			BufferedImage inputImage = ImageIO.read(imageWithHiddenData);
			int inputImageWidth = inputImage.getWidth();
			int inputImageHeight = inputImage.getHeight();
			int numberRequiredDataBits = numberDataBytes * 8;
			int numberExtractedDataBits = 0;
			StringBuffer dataBinaryStrBuffer = new StringBuffer(numberRequiredDataBits);
			
			/*
			 * Getting from each of colors in each of pixels last 2 bits,
			 * make binary string from them and convert result to byte[]
			 */
			allPixelsProcessed : for (int x = 0; x < inputImageWidth; x++) {
				for (int y = 0; y < inputImageHeight; y++) {
					int pixelInt = inputImage.getRGB(x, y);
					int redInt = (pixelInt >> 16) & 0xff;
					int greenInt = (pixelInt >> 8) & 0xff;
					int blueInt = (pixelInt) & 0xff;
					String formatBinStr = "%08d";
					
					// Again, as in putting info into image, here is a little copypaste for each of color
					String redBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(redInt)));
					String greenBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(greenInt)));
					String blueBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(blueInt)));
					if (numberExtractedDataBits < numberRequiredDataBits) {
						dataBinaryStrBuffer.append(redBinStr.substring(6, 8));
						numberExtractedDataBits += 2;
					}
					if (numberExtractedDataBits < numberRequiredDataBits) {
						dataBinaryStrBuffer.append(greenBinStr.substring(6, 8));
						numberExtractedDataBits += 2;
					}
					if (numberExtractedDataBits < numberRequiredDataBits) {
						dataBinaryStrBuffer.append(blueBinStr.substring(6, 8));
						numberExtractedDataBits += 2;
					}

					if (numberExtractedDataBits == numberRequiredDataBits) {
						break allPixelsProcessed;
					}
				}
			}
			BinaryCodec binaryCoded = new BinaryCodec();
			extractedData = binaryCoded.toByteArray(dataBinaryStrBuffer.toString());
		}
		return extractedData;
	}
	
	
	private int getResizedImageWidth() {
		return 640;
	}
	
	private int getResizedImageHeight() {
		return 480;
	}
	
	/**
	 * Resize original image for getting small image file.
	 * Need for prevent of auto-scaling by 3rd party services
	 * (they are often resize large images).
	 * New width and height can be retrieved from methods 
	 * getResizedImageWidth(), getResizedImageHeight(), 
	 * and at the moment 
	 * of writing this comment they are equals 640x480.
	 * @param originalImageFile
	 * @param resizedImageFile
	 * @return <code>true</code> if image was resized and saved successful, <code>false</code> otherwise
	 * @throws IOException
	 */
	public boolean resizeImage(File originalImageFile, File resizedImageFile) throws IOException {
		boolean isResizingSuccessful = false;
		
		if (resizedImageFile.exists() == false) {
		
			int resizedImageWidth = this.getResizedImageWidth();
			int resizedImageHeight = this.getResizedImageHeight();
			
			BufferedImage originalImage = ImageIO.read(originalImageFile);
			BufferedImage resizedImage = new BufferedImage(resizedImageWidth, 
					resizedImageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2dResizedImage = resizedImage.createGraphics();
			boolean isProcessingSuccessful = graphics2dResizedImage.drawImage(originalImage, 0, 0, resizedImageWidth,
					resizedImageHeight, null);
			graphics2dResizedImage.dispose();
			if (isProcessingSuccessful) {
				ImageIO.write(resizedImage, "png", resizedImageFile);
			}
			
			if ((resizedImage.getWidth() == resizedImageWidth) &&
				(resizedImage.getHeight() == resizedImageHeight) &&
				(resizedImageFile.exists() == true) && (resizedImageFile.length() > 0)) {
				isResizingSuccessful = true;
			}
		}
		return isResizingSuccessful;
	}
	
	public boolean putDataChunkIntoImage(File inputImageFile, byte[] rawData, File outputImageFile) throws IOException {
		
		boolean isDataPuttedIntoImageSuccessful = false;
		
		BufferedImage inputImage = ImageIO.read(inputImageFile);
		int inputImageWidth = inputImage.getWidth();
		int inputImageHeight = inputImage.getHeight();
		
		/* 
		 * Will be used LSB (Least Significant Bit) method: 
		 * last 2 bits of each color 
		 * in original image 
		 * will be replaced by 2 bits of data
		 * 
		 * Look at the size of input data,
		 * because there is a converting binary data to String like "01001010...",
		 * work with large raw data is specific (just don't work with it in this case).
		 * 
		 * Also we check that have enough pixels*colors information for using LSB.
		 * 
		 * For example, image has size 640 x 480 pixels.
		 * Number pixel-color entities (in each pixel are 3 - red, green, blue - entities):
		 * 640 x 480 x 3 = 921600, each of them will store 2 bits of data 
		 * (because we will use LSB with replacing 2 bits in each color).
		 * Thus in 1 pixel will be stored 3 * 2 = 6 bits.
		 * numberRequiredPixels = numberDataBits / 6 (with rounding to greater side).
		 * For image 640 x 460 we have max allowed size of data:
		 * numberDataBits = numberRequiredPixels * 6 = 640 x 480 x 6 = 1843200 bits = 230400 bytes = 225 kbytes
		 * 
		 * Max limits (15000) on image Height and Width exists for prevent of using super-large images
		 * (don't sure in avoid of out of memory error while work through ImageIO, 
		 * also will be useful for prevent going out of limits Integer.MAX_VALUE in calculating data chunk sizes).
		 * It shouldn't be a problem, because we should work with little or resized images
		 * (for avoid of resizing image in future by 3rd party web services).
		 * 
		 * Min limits on Height and Width are exists for enough space for 
		 * putting at least 1 byte. 
		 * 1 byte = 8 bits -> we put 6 bits in each pixel (by 2 bits of each of 3 (r,g,b) colors) ->
		 * -> 2 pixels
		 */
		long rawDataBinaryLength = 0;
		if (rawData != null) {
			rawDataBinaryLength = Long.valueOf(rawData.length) * 8;
		}
		long numberRequiredPixels = Math.round(Math.ceil((double)rawDataBinaryLength / 6));
		if (	(rawDataBinaryLength > 0) && (rawDataBinaryLength < Integer.MAX_VALUE) &&
				((inputImageWidth * inputImageHeight) > 2 ) &&
				(inputImageWidth < 15000) && (inputImageHeight < 15000) &&
				(numberRequiredPixels < (inputImageWidth * inputImageHeight)) ) {
			
			String dataBinaryStr = BinaryCodec.toAsciiString(rawData);
			
			Loggers.debug(this, TextMessage.STEGANOGRAPHY_IMAGE_LSB_START_PUT_DATA_CHUNK_INTO_IMAGES, new Object[]{inputImageFile.getPath(), outputImageFile.getPath(), rawData.length, dataBinaryStr.length(), dataBinaryStr.substring(0, 8)});
			
			int numberUsedDataBits = 0;
			BufferedImage outputImage = new BufferedImage(inputImageWidth, inputImageHeight, BufferedImage.TYPE_INT_RGB);
			
			for (int x = 0; x < inputImageWidth; x++) {
				for (int y = 0; y < inputImageHeight; y++) {
					int pixelInt = inputImage.getRGB(x, y);
					int redInt = (pixelInt >> 16) & 0xff;
					int greenInt = (pixelInt >> 8) & 0xff;
					int blueInt = (pixelInt) & 0xff;
					String formatBinStr = "%08d";
					
					/*
					 * Formatter required for making all values as binary strings same 
					 * (8 chars) length, otherwise little values will have unknown length
					 * (e.g.: Integer.toBinaryString(5) => 101 => length 3).
					 * For correct work formatter we need to convert
					 * received binary string to integer, 
					 * and this integer format to a string (str -> int -> str).
					 * "%08s" doesn't works.
					 * One of alternate solution - using 
					 * <a href="http://commons.apache.org/lang/apidocs/org/apache/commons/lang3/StringUtils.html#leftPad(java.lang.String,%20int,%20char)">Apache Commons Lang</a>
					 */
					String redBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(redInt)));
					String greenBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(greenInt)));
					String blueBinStr = String.format(formatBinStr, Integer.valueOf(Integer.toBinaryString(blueInt)));
					
					/*
					 * LSB: replace last 2 bits in each color entity of current pixel.
					 * For example: in color 10101000 (168 in decimal), need to put 2 bits: 10.
					 * In output we have color 10101010 (170 in decimal).
					 * 
					 * Below is a little copy-paste for each of color.
					 */
					String modifiedRedBinaryStr = redBinStr;
					if (numberUsedDataBits < rawDataBinaryLength) {
						modifiedRedBinaryStr = redBinStr.substring(0, 6) + dataBinaryStr.substring(numberUsedDataBits, numberUsedDataBits + 2);
						numberUsedDataBits += 2;
					}
					String modifiedGreenBinaryStr = greenBinStr;
					if (numberUsedDataBits < rawDataBinaryLength) {
						modifiedGreenBinaryStr = greenBinStr.substring(0, 6) + dataBinaryStr.substring(numberUsedDataBits, numberUsedDataBits + 2);
						numberUsedDataBits += 2;
					}
					String modifiedBlueBinaryStr = blueBinStr;
					if (numberUsedDataBits < rawDataBinaryLength) {
						modifiedBlueBinaryStr = blueBinStr.substring(0, 6) + dataBinaryStr.substring(numberUsedDataBits, numberUsedDataBits + 2);
						numberUsedDataBits += 2;
					}
					
					if (numberUsedDataBits <= rawDataBinaryLength) {
						int modifiedPixelInt = Integer.parseInt((modifiedRedBinaryStr + modifiedGreenBinaryStr + modifiedBlueBinaryStr), 2);
						outputImage.setRGB(x, y, modifiedPixelInt);
					} else {
						outputImage.setRGB(x, y, inputImage.getRGB(x, y));
					}
					
				}
			}
			
			/*
			 * Resulted file, saved through 
			 * ImageIO.write(outputImage, "png", outputImageFile);
			 * is too large like almost non-compressed bmp.
			 * 
			 * Example of image compression for jpeg (not png) file is here:
			 * http://www.exampledepot.com/egs/javax.imageio/JpegWrite.html
			 * (used ImageWriteParam and ImageWriter)
			 * 
			 * also look at
			 * http://www.java.net/node/689678
			 * http://www.coderanch.com/t/416163/GUI/java/compression-not-supported-PNG/
			 * http://www.coderanch.com/t/526224/java/java/Image-Compression
			 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4829970
			 * 
			 * Be careful, because under "compression" people often 
			 * means lossy compression, but we need lossless compression.
			 * 
			 * In my case it (image size) shouldn't be critical.
			 */
			ImageIO.write(outputImage, "png", outputImageFile);
			
			if ((outputImageFile.exists() == true) && (outputImageFile.length() > 0)) {
				isDataPuttedIntoImageSuccessful = true;
			}
		}
		return isDataPuttedIntoImageSuccessful;
	}
	
	
}
