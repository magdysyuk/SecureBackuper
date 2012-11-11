package magdysyuk.backuper.source.crypt.hash;

public enum HashFileAlgorithmName {
	MD5 ("md5", null),
	SHA1 ("sha1", null),
	SHA256 ("sha256", null),
	SHA384 ("sha384", null),
	SHA512 ("sha512", null),
	CRC32 ("crc32", null),
	Adler32 ("adler32", null),
	GOST ("gost", "GOST (R 34.11-94)"),
	TTH ("tth", "Tiger Tree Hash"),
	T2TH ("t2th", "Tiger2 Tree Hash"),
	ED2K ("ed2k", null);
	
	private String algorithmName;
	private String description;
	private HashFileAlgorithmName(String algorithmName, String description) {
		this.algorithmName = algorithmName;
		this.description = description;
	}
	
	public String getAlgorithmName() {
		return this.algorithmName;
	}
	
	public String getDescription() {
		return this.description;
	}
	
}
