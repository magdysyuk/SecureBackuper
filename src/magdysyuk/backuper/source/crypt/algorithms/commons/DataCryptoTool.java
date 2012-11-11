package magdysyuk.backuper.source.crypt.algorithms.commons;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.util.Arrays;
import java.util.SortedMap;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

import magdysyuk.backuper.source.crypt.hash.HashBytesCalculator;
import magdysyuk.backuper.source.filesystem.FileIO;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.apache.commons.codec.binary.Hex;


public class DataCryptoTool {
	
	public boolean crypt(InputStream inputStream, OutputStream outputStream, Cipher cipher) {
		Loggers.debug(this, TextMessage.CRYPT_DATA_PROCESSING_START);
		boolean isCryptSuccessful = false;
		FileIO fileIO = new FileIO();
		CipherOutputStream cipherOutputStream = null;
		try {
			cipherOutputStream = new CipherOutputStream(outputStream, cipher);
			fileIO.writeProcessingData(inputStream, cipherOutputStream);
			inputStream.close();
			cipherOutputStream.flush();
			cipherOutputStream.close();
			outputStream.close();
			isCryptSuccessful = true;
		} catch (Exception ex) {
			Loggers.fatal(this, TextMessage.CRYPT_DATA_PROCESSING_IMPOSSIBLE, ex);
			try {
				inputStream.close();
				cipherOutputStream.close();
				outputStream.close();
			} catch (Exception exc) {
				Loggers.fatal(this, TextMessage.CRYPT_DATA_CLOSE_DESCRIPTORS_IMPOSSIBLE);
			}
			
		}
		Loggers.debug(this, TextMessage.CRYPT_DATA_PROCESSING_END, new Object[]{isCryptSuccessful});
		return isCryptSuccessful;
	}
	
	
	public Cipher getCipher(int cipherMode, SecretKey secretKey, 
							CipherAlgorithmName algorithmNameId, CipherMode modeId, 
							CipherPadding paddingId) 
							throws NoSuchAlgorithmException, NoSuchPaddingException, 
									InvalidKeyException, InvalidAlgorithmParameterException {
		String transformationString = this.getTransformationString(algorithmNameId, modeId, paddingId);
		Cipher cipher = Cipher.getInstance(transformationString);
		switch (modeId) {
		case ECB:
			cipher.init(cipherMode, secretKey);
			Loggers.debug(this, TextMessage.CIPHER_INIT_ECB);
			break;
		case CBC:
			byte[] secretKeyEncoded = secretKey.getEncoded();
			IvParameterSpec ivParameterSpec = this.getIvParameterSpec(secretKeyEncoded, algorithmNameId);
			cipher.init(cipherMode, secretKey, ivParameterSpec);
			Loggers.debug(this, TextMessage.CIPHER_INIT_CBC, new Object[]{secretKeyEncoded.length, this.convertBytesToHexString(secretKeyEncoded), this.convertBytesToHexString(ivParameterSpec.getIV())});
			break;
		default:
			cipher.init(cipherMode, secretKey);
			Loggers.fatal(this, TextMessage.CIPHER_INIT_UNKNOWN);
			break;
		}
		
		return cipher;
	}
	
	public byte[] getSecretPBEKeyEncoded(char[] password, CipherAlgorithmName algorithmNameId) {
		Loggers.debug(this, TextMessage.PBE_KEY_SECRET_ENCODED_START_GETTING);
		SecretKeyFactory skf = null;
		String pbeAlgorithmName = CryptoConfig.getPBEAlgorithmName();
		try {
			skf = SecretKeyFactory.getInstance(pbeAlgorithmName);
			Loggers.debug(this, TextMessage.SECRET_KEY_FACTORY_INSTANCE_CREATED, new Object[]{pbeAlgorithmName});
		} catch (NullPointerException skfNullPointerException) {
			Loggers.fatal(this, TextMessage.SECRET_KEY_FACTORY_ALGORITHM_IS_NULL, null, skfNullPointerException);
		} catch (NoSuchAlgorithmException skfNoSuchAlgorithmException) {
			Loggers.fatal(this, TextMessage.SECRET_KEY_FACTORY_ALGORITHM_IS_INVALID, new Object[]{pbeAlgorithmName}, skfNoSuchAlgorithmException);
		}
		KeySpec pbeKeySpec = this.getPBEKeySpec(password, algorithmNameId);
		SecretKey secretPBEKey = null;
		byte[] rawSecretPBEKey = null;
		if (skf != null) {
			try {
				secretPBEKey = skf.generateSecret(pbeKeySpec);
				Loggers.debug(this, TextMessage.PBE_KEY_IS_CALCULATED);
			} catch (InvalidKeySpecException skfInvalidKeySpecEx) {
				Loggers.fatal(this, TextMessage.SECRET_KEY_FACTORY_INVALID_KEY_SPEC, null, skfInvalidKeySpecEx);
			}
			rawSecretPBEKey = secretPBEKey.getEncoded();
		}
		if (rawSecretPBEKey != null && rawSecretPBEKey.length > 0) {
			Loggers.debug(this, TextMessage.PBE_KEY_SECRET_ENCODED_LENGTH_POSITIVE, new Object[]{rawSecretPBEKey.length});
			/*
			 * Length of encoded secret PBE key should be reduced to recommended
			 * Length of key in bits (e.g.: "128").
			 */
		} else {
			Loggers.fatal(this, TextMessage.PBE_KEY_SECRET_ENCODED_IS_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.PBE_KEY_SECRET_ENCODED_END_GETTING);
		return rawSecretPBEKey;
	}
	
	private KeySpec getPBEKeySpec(char[] password, CipherAlgorithmName algorithmNameId) {
		Loggers.debug(this, TextMessage.PBE_KEY_SPEC_START_GETTING);
		KeySpec pbeKeySpec = null;
		byte[] salt = this.getSalt(password);
		int iterationCount = CryptoConfig.getPBEIterationCount();
		int keyLength = this.getKeyLength(algorithmNameId);
		if (salt != null && salt.length > 0) {
			if (iterationCount > 0 && keyLength > 0) {
				/*
				 * Regardless of parameters for PBEKeySpec (especially key length) 
				 * require check and change key length in array of encoded secret PBE key,
				 * otherwise can be received java.security.InvalidKeyException.
				 * Thus, for avoid this situation, there will be get a hash from password,
				 * then get it as hex string and will be use first chars of hash as password.
				 */
				password = this.getNormalizedPassword(password, keyLength);
				pbeKeySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
				Loggers.debug(this, TextMessage.PBE_KEY_SPEC_IS_CALCULATED, new Object[]{password.length, salt.length, iterationCount, keyLength});
			} else {
				Loggers.fatal(this, TextMessage.PBE_KEY_SPEC_INVALID_ITERATIONCOUNT_OR_KEYLENGTH, new Object[]{iterationCount, keyLength, salt.length});
			}
		} else {
			Loggers.fatal(this, TextMessage.PBE_KEY_SPEC_INPUT_SALT_IS_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.PBE_KEY_SPEC_END_GETTING);
		return pbeKeySpec;
	}
	
	/**
	 * Get length of key for chosen cipher algorithm. 
	 * This value depends on max allowed key length for this algorithm (set by JRE) 
	 * and recommended length for this algorithm.
	 * @param algorithmNameId Chosen algorithm
	 * @return Length of key (in bits)
	 */
	private int getKeyLength(CipherAlgorithmName algorithmNameId) {
		Loggers.debug(this, TextMessage.CIPHER_KEY_LENGTH_START_CHOOSE);
		String algorithmName = algorithmNameId.getAlgorithmName();
		int maxAllowedKeyLength = this.getMaxAllowedCipherKeyLength(algorithmName);
		int recommendedKeyLength = algorithmNameId.getRecomendedKeyLength();
		int keyLength = 0;
		keyLength = Math.min(maxAllowedKeyLength,recommendedKeyLength);
		Loggers.debug(this, TextMessage.CIPHER_CURRENT_KEY_LENGTH, new Object[]{algorithmName, maxAllowedKeyLength, recommendedKeyLength, keyLength});
		Loggers.debug(this, TextMessage.CIPHER_KEY_LENGTH_END_CHOOSE);
		return keyLength;
	}
	
	/**
	 * Because usually password has not acceptable length,
	 * and password should contains only ASCII chars, 
	 * otherwise will be throws "java.security.spec.InvalidKeySpecException: Password is not ASCII",
	 * will be calculated hash of password, 
	 * and used instead of password. If hash is over than key length, 
	 * hash will be truncated, if hash is less than key length, hash will be repeated.
	 * @param password Input password
	 * @param keyLength Length of key for current crypto algorithm (in bits, e.g.: 128)
	 * @return Truncated or repeated part of hash as hex string which will be used as password
	 */
	private char[] getNormalizedPassword(char[] password, int keyLength) {
		Loggers.debug(this, TextMessage.NORMALIZED_PASSWORD_START_GETTING);
		byte[] passwordHashBytes = this.getHashBytes(this.convertCharsToBytes(password));
		char[] passwordHashChars = this.convertBytesToHexString(passwordHashBytes).toCharArray();
		String fullHashString = new String(passwordHashChars);
		Loggers.debug(this, TextMessage.NORMALIZED_PASSWORD_FULL_HASH_OF_INPUT_PASSWORD, new Object[]{fullHashString.length(), fullHashString});
		int keyLengthByte = keyLength / 8;
		char[] normalizedPassword = new char[keyLengthByte];
		if (passwordHashChars.length >= keyLengthByte) {
			normalizedPassword = Arrays.copyOf(passwordHashChars, keyLengthByte);
		} else {
			/*
			 * How many time required repeat hash:
			 * If "key length = 128" - means that "byte key length = 128/8 = 16", 
			 * and if password hash char array contains only 5 elements 
			 * (example only, hash with length 5*8 = 40 bits will be very weak), 
			 * then we need repeat hash full 3 time (16/5 = 3 (and some fraction part))
			 * and add 1 char (because 16 = 5*3 + 1)
			 */
			int numberTimesFullRepeatHash = keyLengthByte / passwordHashChars.length;
			int numberAdditionChars = keyLengthByte % passwordHashChars.length;
			for (int i = 0; i < numberTimesFullRepeatHash; i++) {
				System.arraycopy(passwordHashChars, 0, normalizedPassword, 
						(i * passwordHashChars.length), passwordHashChars.length);
			}
			int numberElementsFilledByFullRepeatHash = passwordHashChars.length * numberTimesFullRepeatHash;
			System.arraycopy(passwordHashChars, 0, normalizedPassword, 
					numberElementsFilledByFullRepeatHash, numberAdditionChars);
		}
		Loggers.debug(this, TextMessage.NORMALIZED_PASSWORD_IS_CALCULATED, new Object[]{normalizedPassword.length, new String(normalizedPassword)});
		Loggers.debug(this, TextMessage.NORMALIZED_PASSWORD_END_GETTING);
		return normalizedPassword;
	}
	
	
	/**
	 * Initialization vector used for some cipher mode such as CBC.
	 * Here IV will be reversed array of input bytes of key.
	 * @param inputBytes Input raw material for IV
	 * @return Reversed input data array
	 */
	private IvParameterSpec getIvParameterSpec(byte[] inputBytes, CipherAlgorithmName algorithmNameId) {
		Loggers.debug(this, TextMessage.IV_START_GETTING);
		IvParameterSpec ivParameterSpec = null;
		if (inputBytes != null && inputBytes.length > 0) {
			// Length of IV should be equals length of block size for chosen algorithm
			int ivLength = algorithmNameId.getBlockLength();
			//Because length of block is measured in bits, there is converting to length in bytes
			int ivLengthByte = ivLength / 8; 
			int inputBytesLength = inputBytes.length;
			byte[] iv = new byte[ivLengthByte];
			if (inputBytesLength >= ivLengthByte) {
				iv = this.reverseArray(Arrays.copyOf(inputBytes, ivLengthByte));
			} else {
				/*
				 * If length of input bytes less than need for IV,
				 * bytes will be repeated
				 */
				int numberTimesFullRepeatInputBytes = ivLengthByte / inputBytesLength;
				int numberAdditionBytes = ivLengthByte % inputBytesLength;
				byte[] repeatInputBytes = new byte[ivLengthByte];
				for (int i = 0; i < numberTimesFullRepeatInputBytes; i++) {
					System.arraycopy(inputBytes, 0, repeatInputBytes, 
							(i * inputBytesLength), inputBytesLength);
				}
				int numberElementsFilledByFullRepeatInputBytes = inputBytesLength * numberTimesFullRepeatInputBytes;
				System.arraycopy(inputBytes, 0, repeatInputBytes, 
						numberElementsFilledByFullRepeatInputBytes, numberAdditionBytes);
				iv = this.reverseArray(repeatInputBytes);
			}
			ivParameterSpec = new IvParameterSpec(iv);
			Loggers.debug(this, TextMessage.IV_IS_CALCULATED, new Object[]{ivLength, iv.length, inputBytesLength, this.convertBytesToHexString(inputBytes), this.convertBytesToHexString(iv)});
		} else {
			Loggers.fatal(this, TextMessage.IV_INPUT_BYTES_ARE_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.IV_END_GETTING);
		return ivParameterSpec;
	}
	
	private byte[] reverseArray(byte[] inputBytes) {
		int arrLength = inputBytes.length;
		byte[] reversedArray = new byte[arrLength];
		for (int i = 0; i < arrLength; i++) {
			reversedArray[i] = inputBytes[arrLength - i - 1];
		}
		return reversedArray;
	}
	
	
	/**
	 * Concatenate algorithm name, mode and padding for receive transformation string 
	 * @param algorithmNameId Id of algorithm name
	 * @param modeId Id of algorithm mode
	 * @param paddingId Id of padding
	 * @return Transformation string such as "AES/ECB/NoPadding"
	 */
	private String getTransformationString(CipherAlgorithmName algorithmNameId, 
											CipherMode modeId, CipherPadding paddingId) {
		Loggers.debug(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_START_COMPOUND);
		String algorithmName = algorithmNameId.getAlgorithmName();
		String mode = modeId.getCipherMode();
		String padding = paddingId.getPadding();
		String transformation = algorithmName + "/" + mode + "/" + padding;
		Loggers.debug(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_COMPOUND, new Object[]{transformation});
		Loggers.debug(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_END_COMPOUND);
		return transformation;
	}
	
	
	/**
	 * Take some first bytes of hash from password and return it as salt
	 * @param password Input password
	 * @return Salt based on password
	 */
	private byte[] getSalt(char[] password) {
		Loggers.debug(this, TextMessage.SALT_FROM_PASSWORD_START_CALCULATING);
		byte[] salt = null;
		if (password != null && password.length > 0) {
			byte[] fullHash = this.getHashBytes(this.convertCharsToBytes(password));
			/*
			 * If received planned length of salt is over than length of full hash array,
			 * will be received as a salt something about (completed with null-symbols):
			 * "27518ba9...hash-chars...92e32600000000...null-chars...0000000000"
			 * So will be set length of salt as minimum of full hash length and "-1" for exclude 
			 * same results as with double-hashing
			 */
			int saltLength = CryptoConfig.getSaltBytesLength();
			if (CryptoConfig.getSaltBytesLength() >= fullHash.length) {
				saltLength = Math.min(CryptoConfig.getSaltBytesLength(), fullHash.length) - 1;
				Loggers.debug(this, TextMessage.SALT_FROM_PASSWORD_LENGTH_EQUALS_OR_OVER_HASH_PASSWORD, new Object[]{CryptoConfig.getSaltBytesLength(), fullHash.length, saltLength});
			} else if (CryptoConfig.getSaltBytesLength() <= 0) {
				Loggers.fatal(this, TextMessage.SALT_FROM_PASSWORD_INVALID_LENGTH_OF_SALT, new Object[]{CryptoConfig.getSaltBytesLength()});
			}
			salt = Arrays.copyOf(fullHash, saltLength);
			if (salt != null && salt.length > 0) {
				Loggers.debug(this, TextMessage.SALT_FROM_PASSWORD_IS_CALCULATED, new Object[]{password.length, salt.length, CryptoConfig.getSaltBytesLength(), fullHash.length, this.convertBytesToHexString(salt), this.convertBytesToHexString(fullHash), CryptoConfig.getCharset(), CryptoConfig.getHashAlgorithmName()});
			}
			else {
				Loggers.fatal(this, TextMessage.SALT_FROM_PASSWORD_OUTPUT_BYTES_ARE_EMPTY_OR_NULL, new Object[]{password.length});
			}
		}
		else {
			Loggers.fatal(this, TextMessage.SALT_FROM_PASSWORD_INPUT_PASSWORD_IS_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.SALT_FROM_PASSWORD_END_CALCULATING);
		return salt;
	}
	
	private byte[] convertCharsToBytes(char[] charsData) {
		// Start and end of method logging commented because it is often non-important operation
		byte[] bytesData = null;
		if (charsData != null && charsData.length > 0) {
			try {
				CharBuffer charBuffer = CharBuffer.wrap(charsData);
				ByteBuffer byteBuffer = Charset.forName(CryptoConfig.getCharset()).encode(charBuffer);
				bytesData = byteBuffer.array();
				
				/*
				 * Three exceptions from "Charset.forName()" in version 7: <a href="http://download.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html#forName%28java.lang.String%29">Charset: forName(...)</a>
				 * but only 2 in version 1.4.2: <a href="http://download.oracle.com/javase/1.4.2/docs/api/java/nio/charset/Charset.html#forName%28java.lang.String%29">Charset: forName(...)</a>
				 * I just catch here all exceptions.
				 */
			} catch (Exception charsetEx) {
				SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();
				Loggers.debug(this, TextMessage.AVAILABLE_CHARSETS, new Object[]{availableCharsets.size(), availableCharsets.toString()});
				Loggers.fatal(this, TextMessage.CHAR_ARRAY_TO_BYTE_ARRAY_INVALID_CHARSET, new Object[]{CryptoConfig.getCharset()}, charsetEx);
			}
			if (bytesData != null && bytesData.length > 0) {
				Loggers.debug(this, TextMessage.CHAR_ARRAY_TO_BYTE_ARRAY_CONVERTED_SUCCESSFUL, new Object[]{charsData.length, bytesData.length, CryptoConfig.getCharset()});
			} else {
				Loggers.fatal(this, TextMessage.CHAR_ARRAY_TO_BYTE_ARRAY_OUTPUT_BYTES_ARE_EMPTY_OR_NULL, new Object[]{charsData.length});
			}
		}
		else {
			Loggers.fatal(this, TextMessage.CHAR_ARRAY_TO_BYTE_ARRAY_INPUT_CHARS_ARE_EMPTY_OR_NULL);
		}
		return bytesData;
	}
	
	
	/**
	 * Calculate hash for input byte[] array
	 * @param rawData
	 * @return Byte array of hash
	 */
	private byte[] getHashBytes(byte[] rawData) {
		Loggers.debug(this, TextMessage.MESSAGE_DIGEST_START_CALCULATING_HASH);
		byte[] hash = null;
		if (rawData != null && rawData.length > 0) {
			String hashAlgorithmName = CryptoConfig.getHashAlgorithmName();
			HashBytesCalculator hashCalculator = new HashBytesCalculator();
			hash = hashCalculator.getHash(rawData, hashAlgorithmName);
			if (hash != null && hash.length > 0) {
				Loggers.debug(this, TextMessage.MESSAGE_DIGEST_HASH_IS_CALCULATED, new Object[]{rawData.length, hash.length, hashAlgorithmName});
			} else {
				Loggers.fatal(this, TextMessage.MESSAGE_DIGEST_OUTPUT_BYTES_ARE_EMPTY_OR_NULL, new Object[]{rawData.length});
			}
		} else {
			Loggers.fatal(this, TextMessage.MESSAGE_DIGEST_INPUT_BYTES_ARE_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.MESSAGE_DIGEST_END_CALCULATING_HASH);
		return hash;
	}
	
	private String convertBytesToHexString(byte[] rawData) {
		/*
		 * See reference: 
		 * <a href="http://www.spiration.co.uk/post/1199/Java-md5-example-with-MessageDigest">Java md5 example with MessageDigest</a>
		 * Too many different ways and features for one simple method, thus 
		 * there is just used Apache Commons Codec (http://commons.apache.org/codec/).
		 * 
		 * Change in logs parameter Hex.DEFAULT_CHARSET_NAME if change this
		 * library to something else.
		 * 
		 * Logging of start convert and end convert is disable, because it's often operation,
		 * and create many unimportant records
		 */
		String hexString = null;
		if (rawData != null && rawData.length > 0) {
			hexString = Hex.encodeHexString(rawData);
			Loggers.debug(this, TextMessage.HEX_STRING_FROM_BYTES, new Object[]{rawData.length, Hex.DEFAULT_CHARSET_NAME, hexString.length(), hexString});
		} else {
			Loggers.fatal(this, TextMessage.HEX_STRING_FROM_BYTES_INPUT_BYTES_ARE_EMPTY_OR_NULL);
		}
		return hexString;
	}
	
	
	/**
	 * By default max key length is limited, but this limitation can be removed.
	 * <p/>Limits on maximum key size: <a href="http://download.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#importlimits">Import Limits on Cryptographic Algorithms</a>
	 * @param transformation String for encrypt/decrypt such as "AES/CBC/NoPadding"
	 * @return Max key length for chosen algorithm (if this parameter is not limited returns max int value (2147483647))
	 */
	private int getMaxAllowedCipherKeyLength(String transformation) {
		Loggers.debug(this, TextMessage.CIPHER_START_GETTING_MAX_KEY_LENGTH);
		int maxKeyLength = 0;
		String currentCryptoProvider = new String();
		try {
			maxKeyLength = Cipher.getMaxAllowedKeyLength(transformation);
			currentCryptoProvider = Cipher.getInstance(transformation).getProvider().getInfo();
			Loggers.debug(this, TextMessage.CIPHER_MAX_KEY_LENGTH, new Object[]{maxKeyLength, transformation, currentCryptoProvider});

			// If exception occurs, value of "current crypto provider" will be empty, thus I don't log it in catch-block		
		} catch (NullPointerException transformationStringIsNullEx) {
			Loggers.fatal(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_IS_NULL, new Object[]{transformation}, transformationStringIsNullEx);
		} catch (NoSuchPaddingException transformationStringNoSuchPaddingEx) {
			// "NoSuchPaddingException" can be throw by Cipher.getInstance()
			Loggers.fatal(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_PADDING_IS_INVALID, new Object[]{transformation}, transformationStringNoSuchPaddingEx);
		}
		catch (NoSuchAlgorithmException transformationStringNoSuchAlgorithmEx) {
			Loggers.fatal(this, TextMessage.TRANSFORMATION_STRING_FOR_CRYPTO_ALGORITHM_IS_INVALID, new Object[]{transformation}, transformationStringNoSuchAlgorithmEx);
		}
		Loggers.debug(this, TextMessage.CIPHER_END_GETTING_MAX_KEY_LENGTH);
		return maxKeyLength;
	}
	
}
