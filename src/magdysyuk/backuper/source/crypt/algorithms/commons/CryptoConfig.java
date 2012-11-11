package magdysyuk.backuper.source.crypt.algorithms.commons;


class CryptoConfig {
	
	/**
	 * Change this parameter only with default charset in Apache Commons Codec
	 * (method "convertBytesToHexString") 
	 * @return Charset for all crypto transformation from chars (or string) to bytes and back
	 */
	static String getCharset() {
		return "UTF-8";
	}
	
	/**
	 * Resulted hash of password by this algorithm will be used for symmetric encrypting instead of real password
	 * (because for using Java's encrypt functional, password should contains only ASCII chars)
	 * @return String with name of hash algorithm
	 */
	static String getHashAlgorithmName() {
		return "SHA-256";
	}
	
	static int getSaltBytesLength() {
		return 8;
	}
	
	/**
	 * Get hash iteration count for PBEKeySpec
	 * @return Number of hash iterations
	 */
	static int getPBEIterationCount() {
		return 1024;
	}
	
	/**
	 * Usual used SunJCE provider. List of available algorithms can be found here:
	 * <p/><a href = "http://download.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html">Java Cryptography Architecture Oracle Providers Documentation</a>
	 * <p/>or received through Security.getAlgorithms("Cipher").
	 * <p/>List: <code>PBEWithMD5AndDES</code>, <code>PBEWithMD5AndTripleDES</code> (proprietary algorithm that has not been standardized), <code>PBEWithSHA1AndDESede</code>, <code>PBEWithSHA1AndRC2_40</code>
	 * @return Name of default PBE algorithm for generation key 
	 */
	static String getPBEAlgorithmName() {
		return "PBEWithSHA1AndDESede";
	}
	
}
