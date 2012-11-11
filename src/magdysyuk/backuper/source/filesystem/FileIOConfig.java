package magdysyuk.backuper.source.filesystem;


class FileIOConfig {
	
	private static int bufferInputStreamSize = 16384;
	public static int getBufferInputStreamSize() {
		return bufferInputStreamSize;
	}
	
	private static int bufferOutputStreamSize = 16384;
	public static int getBufferOutputStreamSize() {
		return bufferOutputStreamSize;
	}
	
	private static String filenameEncoding = "UTF-8";
	public static String getFilenameEncoding() {
		return filenameEncoding;
	}
	
}
