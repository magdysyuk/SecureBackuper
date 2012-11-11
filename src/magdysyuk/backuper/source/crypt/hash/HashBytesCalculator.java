package magdysyuk.backuper.source.crypt.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;


public class HashBytesCalculator {
	public byte[] getHash(byte[] rawData, String hashAlgorithmName) {
		/*
		 * Here is used standard MessageDigest, not Apache Commons DigestUtils,
		 * because it allows to easy configure hash algorithm name with String-value.
		 */
		MessageDigest messageDigest = null;
		byte[] hash = null;
		try {
			messageDigest = MessageDigest.getInstance(hashAlgorithmName);
			messageDigest.reset();
			messageDigest.update(rawData);
			hash = messageDigest.digest();
		} catch (NoSuchAlgorithmException messageDigestNoSuchAlgorithmEx) {
			Loggers.fatal(this, TextMessage.MESSAGE_DIGEST_ALGORITHM_IS_INVALID, new Object[]{hashAlgorithmName}, messageDigestNoSuchAlgorithmEx);
		}
		return hash;
	}

}
