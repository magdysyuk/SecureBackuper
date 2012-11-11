package magdysyuk.backuper.source.crypt;

import java.io.InputStream;
import java.io.OutputStream;

import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;


public interface IStreamCrypt {
	public boolean cryptStream(int cipherFlag, InputStream inputStream, 
			OutputStream outputStream, char[] password, 
			CipherMode modeId, CipherPadding paddingId);
}
