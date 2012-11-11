package magdysyuk.backuper.test.compress;

import static org.junit.Assert.*;

import java.io.File;

import magdysyuk.backuper.source.compress.IDataCompress;
import magdysyuk.backuper.source.compress.zip.CompressZip;
import magdysyuk.backuper.source.filesystem.DirectoryUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class CompressZipTest {
	
	private File dirForCompress = new File("unittests_files\\compress\\expected\\files_for_compression");
	private File getOriginalDirectoryForCompress() {
		return this.dirForCompress;
	}
	
	private File fileDirCompressed = new File("unittests_files\\compress\\obtained\\files_after_compression.zip");
	private File getFileDirCompressed() {
		return this.fileDirCompressed;
	}
	
	private File fileForCompress = new File("unittests_files\\compress\\expected\\single_file_for_compression.exe");
	private File getOriginalFileForCompress() {
		return this.fileForCompress;
	}
	
	private File fileCompressed = new File("unittests_files\\compress\\obtained\\single_file_after_compression.zip");
	private File getFileCompressed() {
		return this.fileCompressed;
	}
	
	private File directoryForUncompressed = new File("unittests_files\\compress\\obtained\\files_after_uncompression");
	private File getDirectoryForUncompressed() {
		return this.directoryForUncompressed;
	}

	
	@Test
	public void testZipCompressUncompressDirectory() {
		try {
			IDataCompress compressZip = new CompressZip();
			boolean isCompressedSuccessful = compressZip.compress(this.getOriginalDirectoryForCompress(), this.getFileDirCompressed());
			boolean isUncompressedSuccessful = compressZip.uncompress(this.getFileDirCompressed(), this.getDirectoryForUncompressed());
			DirectoryUtils directoryUtils = new DirectoryUtils();
			File uncompressedFile = new File(this.getDirectoryForUncompressed().getPath() + File.separator + this.getOriginalDirectoryForCompress().getName());
			assertTrue(isCompressedSuccessful && isUncompressedSuccessful && 
					directoryUtils.isDirectoriesEquals(this.getOriginalDirectoryForCompress(), uncompressedFile));
			assertTrue(this.getFileDirCompressed().delete());
			FileUtils.deleteDirectory(this.getDirectoryForUncompressed());
		} catch (Exception ex){
			fail("ERROR file compress-uncompress: " + ex.getMessage());
		}
	}
	
	@Test
	public void testZipCompressUncompressNormalFile() {
		try {
			IDataCompress compressZip = new CompressZip();
			boolean isCompressedSuccessful = compressZip.compress(this.getOriginalFileForCompress(), this.getFileCompressed());
			boolean isUncompressedSuccessful = compressZip.uncompress(this.getFileCompressed(), this.getDirectoryForUncompressed());
			File uncompressedFile = new File(this.getDirectoryForUncompressed().getPath() + File.separator + this.getOriginalFileForCompress().getName());
			assertTrue(isCompressedSuccessful && isUncompressedSuccessful && 
					FileUtils.contentEquals(this.getOriginalFileForCompress(), uncompressedFile));
			assertTrue(this.getFileCompressed().delete());
			FileUtils.deleteDirectory(this.getDirectoryForUncompressed());
		} catch (Exception ex){
			fail("ERROR file compress-uncompress: " + ex.getMessage());
		}
	}
}
