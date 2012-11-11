package magdysyuk.backuper.source.compress;


import java.io.File;

public interface IDataCompress {
	public boolean compress(File inputData, File archiveFile);
	public boolean uncompress(File archiveFile, File uncompressDestinationDirectory);
}
