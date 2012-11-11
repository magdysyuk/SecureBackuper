package magdysyuk.backuper.source.crypt.algorithms.commons;


public enum CipherAlgorithmName {
	/*
	 * AES' recommend key length could be 256, but (see http://download.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#importlimits) it required to install "JCE Unlimited Strength Jurisdiction Policy Files". 
	 * So there will be used just 128 in the recommended key length - less secure, but more universal.
	 * DESede key should contains 24 bytes (192 bits): <a href="http://download.oracle.com/javase/7/docs/api/javax/crypto/spec/DESedeKeySpec.html">DESedeKeySpec</a>
	 * Block length need for IV (initialization vector) for some modes (such as CBC).
	 */
	
	AES ("AES", 128, 128), 
	DESede ("DESede", 192, 64);
	private String algorithmName;
	private int recommendedKeyLength;
	private int blockLength;
	
	private CipherAlgorithmName(String algorithmName, 
			int recommendedKeyLength, int blockLength) {
		this.algorithmName = algorithmName;
		this.recommendedKeyLength = recommendedKeyLength;
		this.blockLength = blockLength;
	}
	
	public String getAlgorithmName() {
		return this.algorithmName;
	}
	public int getRecomendedKeyLength() {
		return this.recommendedKeyLength;
	}
	public int getBlockLength() {
		return this.blockLength;
	}
}
