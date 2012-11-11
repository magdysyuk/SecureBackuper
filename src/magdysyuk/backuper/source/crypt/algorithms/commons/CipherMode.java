package magdysyuk.backuper.source.crypt.algorithms.commons;


public enum CipherMode {
	/**
	 * ECB - Electronic CodeBook mode. The disadvantage of this method is that 
	 * identical blocks are encrypted into identical ciphertext blocks; 
	 * thus, it does not hide data patterns well. 
	 */
	ECB ("ECB"),
	
	/** 
	 * CBC - Cipher-Block Chaining mode, each block is XORed with the 
	 * previous ciphertext block before being encrypted. 
	 * This way, each ciphertext block is dependent on all blocks 
	 * processed up to that point. 
	 * Also, to make each message unique, 
	 * an initialization vector must be used in the first block. 
	 * IV (initialization vector) has same length that
	 * algorithm cipher block size.
	 */
	CBC ("CBC");
	private String mode;
	private CipherMode(String mode) {
		this.mode = mode;
	}
	public String getCipherMode() {
		return this.mode;
	}
}
