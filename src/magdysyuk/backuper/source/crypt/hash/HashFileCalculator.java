package magdysyuk.backuper.source.crypt.hash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.apache.commons.codec.digest.DigestUtils;


public class HashFileCalculator {
	/*
	 * There are used libs: 
	 * Apache Commons Codec library (for MD5 and for all types of SHA),
	 * Jacksum (for GOST, TTH, TTH2, and ED2K).
	 * Also used internal java classes (for CRC32 and Adler32), 
	 * however it is possible to use Jacksum for this too.
	 * Idea of using Jacksum (site: http://www.jonelo.de/java/jacksum/ ) 
	 * was taken from http://www.koders.com/java/fid87D016CCB814DF738B7804E749CBCC530034F305.aspx 
	 * (jDCBot project, http://sourceforge.net/projects/jdcbot/).
	 * Jacksum provide calculating more than 50 different types of checksums.
	 * Base32 used for TTH because it's one of using in p2p-clients.
	 */
	
	public HashFileCalculator (File inputFile) {

		this.inputFile = inputFile;
		this.fileIO = new FileIO();
		
		// Only for logger
		this.inputFilePath = this.getInputFile().getPath();
		
		if (inputFile.canRead() == false || inputFile.isDirectory() == true) {
			Loggers.fatal(this, TextMessage.FILE_IS_NOT_READABLE, new Object[]{this.getInputFilePath()});
		}
	}
	
	
	private File inputFile;
	private File getInputFile() {
		return this.inputFile;
	}

	private FileIO fileIO;
	private FileIO getFileIO() {
		return this.fileIO;
	}
	
	private String inputFilePath;
	private String getInputFilePath() {
		return this.inputFilePath;
	}
	
	public String getHash(HashFileAlgorithmName algorithmName) {
		String hash = null;
		try {
			switch (algorithmName) {
				case MD5:
					hash = this.getMD5ofFile();
					break;
				case SHA1:
					hash = this.getSHA1ofFile();
					break;
				case SHA256:
					hash = this.getSHA256ofFile();
					break;
				case SHA384:
					hash = this.getSHA384ofFile();
					break;
				case SHA512:
					hash = this.getSHA512ofFile();
					break;
				case CRC32:
					hash = this.getCRC32ofFile();
					break;
				case Adler32:
					hash = this.getAdler32ofFile();
					break;
				case GOST:
					hash = this.getGOSTofFile();
					break;
				case TTH:
					hash = this.getTTHofFile();
					break;
				case T2TH:
					hash = this.getTTH2ofFile();
					break;
				case ED2K:
					hash = this.getED2KofFile();
					break;
				default:
					Loggers.fatal(this, TextMessage.HASH_CHECKSUM_FILE_UNSUPPORTED_ALGORITHM, new Object[]{algorithmName.toString()});
					hash = "MD5: " + this.getMD5ofFile() + ", SHA1: " + this.getSHA1ofFile() +
							", TTH: " + this.getTTHofFile() + ", ED2K: " + this.getED2KofFile();
					break;
			}
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.HASH_CHECKSUM_FILE_IMPOSSIBLE, ex);
		}
		return hash;
	}
	
	private String getMD5ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_MD5_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		String hashMD5 = DigestUtils.md5Hex(inputStream);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_MD5_END, new Object[]{this.getInputFilePath(), hashMD5});
		return hashMD5;
	}
	
	private String getSHA1ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA1_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		String hashSHA1 = DigestUtils.sha1Hex(inputStream);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA1_END, new Object[]{this.getInputFilePath(), hashSHA1});
		return hashSHA1;
	}

	private String getSHA256ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA256_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		String hashSHA256 = DigestUtils.sha256Hex(inputStream);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA256_END, new Object[]{this.getInputFilePath(), hashSHA256});
		return hashSHA256;
	}
	
	private String getSHA384ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA384_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		String hashSHA384 = DigestUtils.sha384Hex(inputStream);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA384_END, new Object[]{this.getInputFilePath(), hashSHA384});
		return hashSHA384;
	}
	
	private String getSHA512ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA512_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		String hashSHA512 = DigestUtils.sha512Hex(inputStream);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_SHA512_END, new Object[]{this.getInputFilePath(), hashSHA512});
		return hashSHA512;
	}
	private String getCRC32ofFile() throws IOException {
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		CheckedInputStream checkedInputStream = new CheckedInputStream(inputStream, new CRC32());
		while (checkedInputStream.read() != -1) {
		}
		long crc32 = checkedInputStream.getChecksum().getValue();
		String hashCRC32 = String.valueOf(crc32);
		inputStream.close();
		return hashCRC32;
	}
	
	private String getAdler32ofFile() throws IOException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_ADLER32_START, new Object[]{this.getInputFilePath()});
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		CheckedInputStream checkedInputStream = new CheckedInputStream(inputStream, new Adler32());
		while (checkedInputStream.read() != -1) {
		}
		long adler32 = checkedInputStream.getChecksum().getValue();
		String hashAdler32 = String.valueOf(adler32);
		inputStream.close();
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_ADLER32_END, new Object[]{this.getInputFilePath(), hashAdler32});
		return hashAdler32;
	}
	
	
	/**
	 * Hash by algorithm GOST (R 34.11-94)
	 * @return Hash string
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private String getGOSTofFile() throws IOException, NoSuchAlgorithmException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_GOST_START, new Object[]{this.getInputFilePath()});
		String hashGOST = this.getJacksumChecksumHash("gost");
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_GOST_END, new Object[]{this.getInputFilePath(), hashGOST});
		return hashGOST;
	}
	
	/**
	 * Hash by algorithm Tiger tree hash.
	 * <p/>
	 * For checking TTH will be good to look at <a href="http://docs.amazonwebservices.com/amazonglacier/latest/dev/checksum-calculations.html">Developer Guide for Amazon Glacier</a>
	 * @return Hash string
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	private String getTTHofFile() throws IOException, NoSuchAlgorithmException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_TTH_START, new Object[]{this.getInputFilePath()});
		String hashTTH = this.getJacksumChecksumHash("tree:tiger");
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_TTH_END, new Object[]{this.getInputFilePath(), hashTTH});
		return hashTTH;
	}
	
	
	/**
	 * Hash by algorithm Tiger2 Tree Hash
	 * @return Hash string
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private String getTTH2ofFile() throws IOException, NoSuchAlgorithmException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_TTH2_START, new Object[]{this.getInputFilePath()});
		String hashTTH2 = this.getJacksumChecksumHash("tree:tiger2");
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_TTH2_END, new Object[]{this.getInputFilePath(), hashTTH2});
		return hashTTH2;
	}
	
	private String getED2KofFile() throws IOException, NoSuchAlgorithmException {
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_ED2K_START, new Object[]{this.getInputFilePath()});
		String hashED2K = this.getJacksumChecksumHash("ed2k");
		Loggers.debug(this, TextMessage.HASH_CHECKSUM_FILE_ED2K_END, new Object[]{this.getInputFilePath(), hashED2K});
		return hashED2K;
	}
	
	
	private String getJacksumChecksumHash(String algorithm) throws IOException, NoSuchAlgorithmException {
		InputStream inputStream = this.getFileIO().getFileInputStream(this.getInputFile());
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algorithm);
		checksum.reset();
		int numberReadedBytes = 0;
		while ((numberReadedBytes = inputStream.read()) != -1) {
			checksum.update((byte) numberReadedBytes);
		}
		String hashChecksum = checksum.format("#CHECKSUM");
		inputStream.close();
		return hashChecksum;
	}
	
	
}
