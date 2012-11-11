package magdysyuk.backuper.source.crypt;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Cipher;

import magdysyuk.backuper.source.crypt.algorithms.commons.CipherAlgorithmName;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;
import magdysyuk.backuper.source.crypt.algorithms.implementation.DataCryptoAES;
import magdysyuk.backuper.source.crypt.algorithms.implementation.DataCryptoDESede;
import magdysyuk.backuper.source.filesystem.DirectoryUtils;
import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;


/**
 * <p/>Limits on maximum key size: <a href="http://download.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#importlimits">Import Limits on Cryptographic Algorithms</a>
 * <p/>Limits on JCE provider and algorithm names: <a href="http://download.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">The SunJCE Provider</a>
 * <p/>List of standard algorithm, mode, padding names: <a href="http://download.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html">JCE Standard Algorithm Name Documentation</a>
 * <p/>Vendor's article about AES: <a href="http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html">Using AES with Java Technology</a>
 */
public class DataCrypt implements IDataCrypt {
	private IStreamCrypt iStreamCrypt;
	public IStreamCrypt getIStreamCrypt() {
		return this.iStreamCrypt;
	}
	private CipherAlgorithmName algorithmNameId;
	
	public DataCrypt (CipherAlgorithmName algorithmNameId) {
		this.algorithmNameId = algorithmNameId;
		switch (this.algorithmNameId) {
			case AES:
				this.iStreamCrypt = new DataCryptoAES();
				break;
			case DESede:
				this.iStreamCrypt = new DataCryptoDESede();
				break;
			default:
				this.iStreamCrypt = new DataCryptoAES();
				break;
		}
	}
	
	
	/**
	 * If chosen directory, she will be en/de-crypted recursively 
	 * (tree of directories will be same with original directory, but all files will be separately en/de-crypted)
	 * @param cipherFlag Choose want you want - encrypt or decrypt information
	 * @param inputFile File for en/de-crypt
	 * @param password Password should not be empty
	 * @param outputDirectory Place where will be putted result of operation
	 * @param modeId Will it "ECB", "CBC" or something such as from available
	 * @param paddingId Will it "PKCS5Padding" or something such as from available
	 * @return <code>true</code> if en/de-crypt is successful, <code>false</code> otherwise
	 */
	@Override
	public boolean cryptFile(int cipherFlag, File inputFile, File outputDirectory, char[] password, 
			CipherMode modeId, CipherPadding paddingId) {
		Loggers.debug(this, TextMessage.CRYPT_FILE_START, new Object[]{cipherFlag, inputFile.getPath(), outputDirectory.getPath(), modeId.getCipherMode(), paddingId.getPadding()});
		boolean isFileCryptedSuccessful = false;
		if ((cipherFlag == Cipher.ENCRYPT_MODE || cipherFlag == Cipher.DECRYPT_MODE) && (password != null) && (password.length > 0)) {
			FileIO fileIO = new FileIO();
			DirectoryUtils dirUtils = new DirectoryUtils();
			if ((outputDirectory.exists() && outputDirectory.isDirectory()) || (outputDirectory.mkdirs())) {
				try {
					Set<File> setFiles = dirUtils.getSetFilesRecursively(inputFile);
					Iterator<File> filesIterator = setFiles.iterator();
					while (filesIterator.hasNext()) {
						File currentFile = filesIterator.next();
						String outFilePath = outputDirectory.getPath() + File.separator + 
								dirUtils.getRelativeFilePathWithRoot(inputFile, currentFile);
						File outFile = new File(outFilePath);
						if (currentFile.isDirectory()) {
							 if ((outFile.exists() && outFile.isDirectory()) || outFile.mkdirs()) {
								 // All was good. Directory was created successful - that is all what we need
								 isFileCryptedSuccessful = true;
							 } else {
								Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outFilePath});
							 }
						} else {
							if ((outFile.getParentFile().exists() || outFile.getParentFile().mkdirs()) && 
									(outFile.exists() || outFile.createNewFile())) {
								boolean isCryptProcessingSuccessful = false;
								if (currentFile.length() > 0) {
									InputStream inputStream = fileIO.getFileInputStream(currentFile);
									OutputStream outputStream = fileIO.getFileOutputStream(outFile);
									isCryptProcessingSuccessful = this.getIStreamCrypt().cryptStream(cipherFlag, inputStream, outputStream, password, modeId, paddingId);
									outputStream.flush();
									inputStream.close();
									outputStream.close();
								} else {
									// If length of file equals 0 - just enough that file was already created, don't need crypt that
									isCryptProcessingSuccessful = true;
								}
								// Check if stream processing was successful and files are different
								if (isCryptProcessingSuccessful) {
									isFileCryptedSuccessful = true;
									Loggers.debug(this, TextMessage.CRYPT_DATA_SUCCESSFUL);
								} else {
									Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE);
								}
							} else {
								Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outFile.getPath()});
							}
						}
					}
				} catch (Exception ex) {
					Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE, ex);
				}
			} else {
				Loggers.fatal(this, TextMessage.FILE_CREATE_IMPOSSIBLE, new Object[]{outputDirectory.getPath()});
			}
		} else {
			Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE);
		}
		Loggers.debug(this, TextMessage.CRYPT_FILE_END, new Object[]{isFileCryptedSuccessful});
		return isFileCryptedSuccessful;
	}
}
