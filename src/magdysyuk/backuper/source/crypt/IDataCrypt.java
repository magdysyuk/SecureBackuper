package magdysyuk.backuper.source.crypt;

import java.io.File;

import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;


public interface IDataCrypt {
	
	public boolean cryptFile(int cipherFlag, File inputFile, 
			File outputDirectory, char[] password, 
			CipherMode modeId, CipherPadding paddingId);

}
