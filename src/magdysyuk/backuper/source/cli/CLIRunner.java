package magdysyuk.backuper.source.cli;

import java.io.File;

import magdysyuk.backuper.source.logic.MainProcessor;

import org.kohsuke.args4j.Option;


/**
 * Here is used args4j for parse parameters.
 * In this class exists a lot of hardcoded text for simplify of modification
 * Links:
 * <br/><a href="http://args4j.kohsuke.org/index.html">Home site args4j</a>
 * <br/><a href="http://fahdshariff.blogspot.com/2011/12/args4j-vs-jcommander-for-parsing.html">Args4j vs JCommander for Parsing Command Line Parameters</a>
 */
public class CLIRunner {
	
	// For both directions - "hide" and "extract"
	@Option(name="-action", metaVar="<\"hide\" | \"extract\">", required=true, usage="Encrypt data and hide them into image, or extract data from images and decrypt them (depends of value this option). \nIf you choose \"hide\" action, you also should set options: \n	-password\n	-input-data\n	-image-source\n	-report\n	-dir-images-with-data\nIf you choose \"extract\" action, you also should set options: \n	-password\n	-dir-images-with-data\n	-report\n	-dir-output-data")
	private String action;
	@Option(name="-password", aliases={"-p", "-pass"}, required=true, metaVar="<string>", usage="Password for en/de-crypt operation (by AES). Should not be empty.")
	private String password;
	@Option(name="-report", aliases={"-report-xml"}, required=true, metaVar="<file>", usage="Path to XML report. Contains information about right order of image files, and how many data bytes contains each of images. Also contains checksums.")
	private File reportFile;
	@Option(name="-dir-images-with-data", aliases={"-dir-img-data"}, required=true, metaVar="<directory>", usage="Path to directory with images contains data into them.")
	private File directoryForImagesWithData;
	
	// For "hide" direction
	@Option(name="-input-data", metaVar="<file or directory>", usage="Path to your secret file or directory for processing. Only for \"hide\" action.")
	private File inputDataFile;
	@Option(name="-image-source", aliases={"-image-original"}, metaVar="<file>", usage="Path to image file. This image will be used for hiding data by LSB method. Only for \"hide\" action.")
	private File imageForSteganography;
	
	// For "extract" direction
	@Option(name="-dir-output-data", metaVar="<directory>", usage="Path to directory with extracted from images data. Only for \"extract\" action.")
	private File outputDirectoryForExtractedFiles;
	
	
	
	public void runLogic() {
		boolean isOperationFinishedSuccessful = false;
		/*
		 * Because commons arguments (for hiding and extracting) are required (in options of annotations), 
		 * need to check on not-null only other parameters
		 */
		if (	(this.action == null)	||	( this.action.equals("hide") == false && this.action.equals("extract") == false )	) {
			System.out.println("Option \"-action\" is required and it should be equals one of these string values: \"hide\", \"extract\"");
		} else {
			MainProcessor mainProcessor = new MainProcessor();
			if(this.action.equals("hide") == true) {
				if (	(this.inputDataFile == null) || (this.inputDataFile.exists() == false)	) {
					System.out.println("You should specify path to real data file (or directory) for processing");
				} else if (	(this.imageForSteganography == null) || (this.imageForSteganography.exists() == false) ) {
					System.out.println("You should specify path to real image file");
				} else {
					isOperationFinishedSuccessful = mainProcessor.encryptCompressHideFiles(
							this.inputDataFile, this.password.toCharArray(), this.imageForSteganography, this.directoryForImagesWithData, this.reportFile);
				}
			} else	if (this.action.equals("extract") == true) {
				if (this.outputDirectoryForExtractedFiles == null) {
					System.out.println("You should specify path to place for extracted data");
				} else {
					isOperationFinishedSuccessful = mainProcessor.decryptUncompressExtractFiles(
							this.directoryForImagesWithData, this.reportFile, this.outputDirectoryForExtractedFiles, this.password.toCharArray());
				}
			}
		}
		if (isOperationFinishedSuccessful == true) {
			System.out.println("Result of operation: SUCCESS");
		} else {
			System.out.println("Result of operation: FAIL");
		}
		
	}
}
