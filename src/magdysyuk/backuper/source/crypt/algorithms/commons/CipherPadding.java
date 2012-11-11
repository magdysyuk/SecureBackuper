package magdysyuk.backuper.source.crypt.algorithms.commons;


public enum CipherPadding {
	/*
	 * Do not use "NoPadding", for little data file crypt can be impossible.
	 * SunJCE provider doesn't support PKCS1Padding. Be careful with paddings!
	 */
	
	PKCS5Padding ("PKCS5Padding");
	
	private String padding;
	private CipherPadding(String padding) {
		this.padding = padding;
	}
	public String getPadding() {
		return this.padding;
	}
}
