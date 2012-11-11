package magdysyuk.backuper.source.filesystem;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;

import org.apache.commons.io.FileUtils;


public class DirectoryUtils {
	
	/*
	 * Different unsorted methods for work with file system 
	 */
	
	private String getRelativeFilePathWithoutRoot(URI rootFolderUri, URI innerFilePath) throws UnsupportedEncodingException {
		/*
		 * URLDecoder need for backreplace of autoreplace special chars in URI 
		 * (e.g.: when convert space-char from canonical file path to URI we will get "%20",
		 * decode need for replace it back to space-char).
		 * 
		 * Here we get relative path from two paths, e.g. (in Windows-system): 
		 * "C:/eclipse/testfolder/testfolder2/testfolder3/" and 
		 * "C:/eclipse/testfolder/"
		 * relative path after rootFolderUri.relativize(innerFilePath).toString() will be:
		 * "testfolder2/testfolder3/"
		 */
		String relativeFilePath = URLDecoder.decode(rootFolderUri.relativize(innerFilePath).toString(), FileIOConfig.getFilenameEncoding());
		return relativeFilePath;
	}
	
	public String getRelativeFilePathWithoutRoot(File rootFolder, File innerFile) throws UnsupportedEncodingException {
		String relativeFilePathWithRoot = this.getRelativeFilePathWithoutRoot(rootFolder.toURI(), innerFile.toURI());
		return relativeFilePathWithRoot;
	}
	
	public String getRelativeFilePathWithRoot(File rootFolder, File innerFile) throws UnsupportedEncodingException {
		String relativeFilePathWithRoot = rootFolder.getName() + "/" + this.getRelativeFilePathWithoutRoot(rootFolder.toURI(), innerFile.toURI());
		return relativeFilePathWithRoot;
	}
	
	
	/**
	 * Compare directories or files.
	 * For directories comparing is recursively.
	 * @param First directory (or file)
	 * @param Second directory (or file)
	 * @return <code>true</code> if entities are equals (same name and content), <code>false</code> otherwise
	 */
	public boolean isDirectoriesEquals(File dir1, File dir2) {
		boolean isEquals = true;
		try {
			if (dir1.isDirectory() && dir2.isDirectory() 
					&& dir1.getName().equals(dir2.getName())
					&& FileUtils.sizeOfDirectory(dir1) == FileUtils.sizeOfDirectory(dir2)) {
				File[] fileList1 = dir1.listFiles();
				File[] fileList2 = dir2.listFiles();
				int numberFilesDir1 = fileList1.length;
				int numberFilesDir2 = fileList2.length;
				if (numberFilesDir1 == numberFilesDir2) {
					for (int i = 0; i < numberFilesDir1; i++) {
						String fileName1 = fileList1[i].getName();
						String fileName2 = fileList2[i].getName();
						if (fileName1.equals(fileName2)) {
							if (fileList1[i].isFile() && fileList2[i].isFile()) {
								if (FileUtils.contentEquals(fileList1[i], fileList2[i]) == false) {
									isEquals = false;
								}
							} else if(fileList1[i].isDirectory() && fileList2[i].isDirectory()) {
								isDirectoriesEquals(fileList1[i], fileList2[i]);
							} else {
								isEquals = false;
							}
						} else {
							isEquals = false;
						}
					}
				} else {
					isEquals = false;
				}
			} else if(dir1.isFile() && dir2.isFile() && dir1.getName().equals(dir2.getName())) {
				if (FileUtils.contentEquals(dir1, dir2) == false) {
					isEquals = false;
				}
			} else {
				isEquals = false;
			}
		} catch (Exception ex) {
			isEquals = false;
			Loggers.fatal(this, TextMessage.DIRECTORIES_COMPARE_IMPOSSIBLE, new Object[]{dir1.getPath(), dir2.getPath()}, ex);
		}
		return isEquals;
	}
	
	/**
	 * Get set of all nested nested directories and files
	 */
	private Set<File> setFiles = new HashSet<File>();
	public Set<File> getSetFilesRecursively(File inputFile) {
		this.setFiles.add(inputFile);
		if (inputFile.isDirectory()) {
			File[] innerFiles = inputFile.listFiles();
			for (File innerFile : innerFiles) {
				this.getSetFilesRecursively(innerFile);
			}
		}
		return this.setFiles;
	}
}
