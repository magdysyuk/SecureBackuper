package magdysyuk.backuper.test.crypt;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.crypto.Cipher;

import magdysyuk.backuper.source.crypt.DataCrypt;
import magdysyuk.backuper.source.crypt.IDataCrypt;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherAlgorithmName;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;
import magdysyuk.backuper.source.filesystem.DirectoryUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class DataCryptSymmetricTest {
	
	private char[][] getPasswords() {
		char[][] passwords = new char[][]{
				"1".toCharArray(),
				"a".toCharArray(),
				"abc".toCharArray(),
				"є".toCharArray(),
				"abc*_+=$%".toCharArray(),
				"abc_рст() 		~ИЈ!@#$%^&*()_+}{><?/\\YUIowhoidOHDWSisdhoDWnioDOHNDWoihdioAhdOLADHOADHOAWxchc8wcywc8wq0oq8whodhDODHOWwaodhOHDWODAWDOHWdWJdhjkqpdwpqdw[wodwuedw$3433rwefju7iredf0df02.''[0(0k3r0-_uo8d8".toCharArray()
				};
		return passwords;
	}
	
	private File[] filesForCrypt = new File[]{	new File("unittests_files\\crypt\\expected\\eclipse.exe"),
												new File("unittests_files\\crypt\\expected\\Empty File.txt"),
												new File("unittests_files\\crypt\\expected\\Small File.txt")};
	private File[] getFilesForCrypt() {
		return this.filesForCrypt;
	}
	
	private File[] directoriesForCrypt = new File[] {new File("unittests_files\\crypt\\expected\\directory_for_crypt")};
	private File[] getDirectoriesForCrypt() {
		return this.directoriesForCrypt;
	}
	
	private File directoryForEncrypted = new File("unittests_files\\crypt\\obtained\\files_encrypted");
	private File getDirectoryForEncrypted() {
		return this.directoryForEncrypted;
	}
	
	private File directoryForDecrypted = new File("unittests_files\\crypt\\obtained\\files_decrypted");
	private File getDirectoryForDecrypted() {
		return this.directoryForDecrypted;
	}
	
	
	@Test
	public void testCryptOnlyOneFile() throws IOException {
		boolean isEncryptSuccessful = false;
		boolean isDecryptSuccessful = false;
		boolean cryptEnDeResult = false;
		
		File fileForEncrypt = this.getFilesForCrypt()[0];
		File encryptedFile = new File(this.getDirectoryForEncrypted().getPath() + File.separator + fileForEncrypt.getName());
		File decryptedFile = new File(this.getDirectoryForDecrypted().getPath() + File.separator + fileForEncrypt.getName());
		String password = "testpassword";
		
		for (CipherAlgorithmName cipherAlgorithm : CipherAlgorithmName.values()) {
			for (CipherMode cipherMode : CipherMode.values()) {
				for (CipherPadding cipherPadding : CipherPadding.values()) {
					IDataCrypt dataCrypt = new DataCrypt(cipherAlgorithm);
					isEncryptSuccessful = dataCrypt.cryptFile(Cipher.ENCRYPT_MODE, fileForEncrypt, 
							this.getDirectoryForEncrypted(), password.toCharArray(), cipherMode, cipherPadding);
					isDecryptSuccessful = dataCrypt.cryptFile(Cipher.DECRYPT_MODE, encryptedFile, 
							this.getDirectoryForDecrypted(), password.toCharArray(), cipherMode, cipherPadding);
					assertTrue(isEncryptSuccessful);
					assertTrue(isDecryptSuccessful);
					cryptEnDeResult = (isEncryptSuccessful && isDecryptSuccessful && 
							FileUtils.contentEquals(fileForEncrypt, decryptedFile));
					assertTrue(cryptEnDeResult);
					assertFalse(FileUtils.contentEquals(fileForEncrypt, encryptedFile));
					FileUtils.deleteDirectory(this.getDirectoryForEncrypted());
					FileUtils.deleteDirectory(this.getDirectoryForDecrypted());
				}
			}
		}
	}
	
	@Test
	public void testCryptFiles() {
		boolean isEncryptSuccessful = false;
		boolean isDecryptSuccessful = false;
		boolean cryptEnDeResult = false;
		for (File fileForEncrypt : this.getFilesForCrypt()){
			for (CipherAlgorithmName cipherAlgorithm : CipherAlgorithmName.values()) {
				for (char[] password : this.getPasswords()) {
					for (CipherMode cipherMode : CipherMode.values()) {
						for (CipherPadding cipherPadding : CipherPadding.values()) {
							try {
								cryptEnDeResult = false;
								IDataCrypt dataCrypt = new DataCrypt(cipherAlgorithm);
								isEncryptSuccessful = dataCrypt.cryptFile(Cipher.ENCRYPT_MODE, fileForEncrypt, this.getDirectoryForEncrypted(), password, cipherMode, cipherPadding);
								isDecryptSuccessful = dataCrypt.cryptFile(Cipher.DECRYPT_MODE, new File(this.getDirectoryForEncrypted().getPath() + File.separator + fileForEncrypt.getName()), this.getDirectoryForDecrypted(), password, cipherMode, cipherPadding);
								File decryptedFile = new File(getDirectoryForDecrypted().getPath() + File.separator + fileForEncrypt.getName());
								cryptEnDeResult = (isEncryptSuccessful && isDecryptSuccessful && 
										FileUtils.contentEquals(fileForEncrypt, decryptedFile));
								assertTrue("Crypt file failed, parameters: file for encrypt: " + fileForEncrypt.getPath() + 
										", decrypted file: "+ decryptedFile.getPath() + ", algorithm: " + cipherAlgorithm + 
										", password: " + new String(password) + ", cipher mode: " + cipherMode + 
										", cipher padding: " + cipherPadding, 
										cryptEnDeResult);
								FileUtils.deleteDirectory(this.getDirectoryForEncrypted());
								FileUtils.deleteDirectory(this.getDirectoryForDecrypted());
							} catch (Exception ex) {
								fail(ex.getMessage());
							}
						}
					}
				}
			}
		}
	}
	
	
	@Test
	public void testCryptFileWithEmptyPassword() {
		boolean isEncryptSuccessful = false;
		boolean isDecryptSuccessful = false;
		boolean cryptEnDeResult = false;
		char[][] passwords = new char[][]{	null,
											"".toCharArray()};
		for (File fileForEncrypt : this.getFilesForCrypt()){
			for (CipherAlgorithmName cipherAlgorithm : CipherAlgorithmName.values()) {
				for (char[] password : passwords) {
					for (CipherMode cipherMode : CipherMode.values()) {
						for (CipherPadding cipherPadding : CipherPadding.values()) {
							try {
								cryptEnDeResult = false;
								IDataCrypt dataCrypt = new DataCrypt(cipherAlgorithm);
								isEncryptSuccessful = dataCrypt.cryptFile(Cipher.ENCRYPT_MODE, fileForEncrypt, this.getDirectoryForEncrypted(), password, cipherMode, cipherPadding);
								isDecryptSuccessful = dataCrypt.cryptFile(Cipher.DECRYPT_MODE, new File(this.getDirectoryForEncrypted().getPath() + File.separator + fileForEncrypt.getName()), this.getDirectoryForDecrypted(), password, cipherMode, cipherPadding);
								File decryptedFile = new File(getDirectoryForDecrypted().getPath() + File.separator + fileForEncrypt.getName());
								cryptEnDeResult = (isEncryptSuccessful && isDecryptSuccessful && 
										FileUtils.contentEquals(fileForEncrypt, decryptedFile));
								// Crypt with empty password should fail always
								assertFalse(cryptEnDeResult);
								FileUtils.deleteDirectory(this.getDirectoryForEncrypted());
								FileUtils.deleteDirectory(this.getDirectoryForDecrypted());
							} catch (Exception ex) {
								fail(ex.getMessage());
							}
						}
					}
				}
			}
		}
	}
	
	
	@Test
	public void testCryptDirectory() {
		boolean isEncryptSuccessful = false;
		boolean isDecryptSuccessful = false;
		boolean cryptEnDeResult = false;
		DirectoryUtils directoryUtils = new DirectoryUtils(); 
		
		for (File dirForEncrypt : this.getDirectoriesForCrypt()){
			for (CipherAlgorithmName cipherAlgorithm : CipherAlgorithmName.values()) {
				for (char[] password : this.getPasswords()) {
					for (CipherMode cipherMode : CipherMode.values()) {
						for (CipherPadding cipherPadding : CipherPadding.values()) {
							try {
								cryptEnDeResult = false;
								IDataCrypt dataCrypt = new DataCrypt(cipherAlgorithm);
								File encryptedDir = new File(this.getDirectoryForEncrypted().getPath() + File.separator + dirForEncrypt.getName());
								File decryptedDir = new File(getDirectoryForDecrypted().getPath() + File.separator + dirForEncrypt.getName());
								isEncryptSuccessful = dataCrypt.cryptFile(Cipher.ENCRYPT_MODE, dirForEncrypt, this.getDirectoryForEncrypted(), password, cipherMode, cipherPadding);
								isDecryptSuccessful = dataCrypt.cryptFile(Cipher.DECRYPT_MODE, encryptedDir, this.getDirectoryForDecrypted(), password, cipherMode, cipherPadding);
								cryptEnDeResult = (isEncryptSuccessful && isDecryptSuccessful && 
										directoryUtils.isDirectoriesEquals(dirForEncrypt, decryptedDir));

								assertTrue("Crypt directory failed, parameters: file for encrypt: " + dirForEncrypt.getPath() + ", decrypted file: "+ decryptedDir.getPath() + ", algorithm: " + cipherAlgorithm + ", password: " + new String(password) + ", cipher mode: " + cipherMode + ", cipher padding: " + cipherPadding, 
											cryptEnDeResult);
								FileUtils.deleteDirectory(this.getDirectoryForEncrypted());
								FileUtils.deleteDirectory(this.getDirectoryForDecrypted());
							} catch (Exception ex) {
								fail(ex.getMessage());
							}
						}
					}
				}
			}
		}
	}

}
