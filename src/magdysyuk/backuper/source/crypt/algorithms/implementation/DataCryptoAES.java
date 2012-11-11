package magdysyuk.backuper.source.crypt.algorithms.implementation;

import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import magdysyuk.backuper.source.crypt.IStreamCrypt;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherAlgorithmName;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;
import magdysyuk.backuper.source.crypt.algorithms.commons.DataCryptoTool;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;


public class DataCryptoAES implements IStreamCrypt {

	private CipherAlgorithmName algorithmNameId = CipherAlgorithmName.AES;
	
	@Override
	public boolean cryptStream(int cipherMode, InputStream inputStream, OutputStream outputStream, char[] password,
				CipherMode modeId, CipherPadding paddingId) {
		Loggers.debug(this, TextMessage.CRYPT_DATA_START, new Object[]{cipherMode});
		boolean isCryptSuccessful = false;
		DataCryptoTool dct = new DataCryptoTool();
		try {
			SecretKeySpec secretKeySpec = this.getSecretKeySpec(password, this.algorithmNameId);
			SecretKey secretKey = new SecretKeySpec(secretKeySpec.getEncoded(), "AES");
			Cipher cipher = dct.getCipher(cipherMode, secretKey, this.algorithmNameId, modeId, paddingId);
			isCryptSuccessful = dct.crypt(inputStream, outputStream, cipher);
		} catch (Exception encryptEx) {
			Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE, encryptEx);
		}
		Loggers.debug(this, TextMessage.CRYPT_DATA_END, new Object[]{isCryptSuccessful});
		return isCryptSuccessful;
	}
	
	
	private SecretKeySpec getSecretKeySpec(char[] password, CipherAlgorithmName algorithmNameId) {
		Loggers.debug(this, TextMessage.SECRET_KEY_SPEC_START_GETTING);
		DataCryptoTool dataCryptoTool = new DataCryptoTool();
		byte[] rawSecretPBEKey = dataCryptoTool.getSecretPBEKeyEncoded(password, algorithmNameId);
		SecretKeySpec secretKeySpec = null;
		if (rawSecretPBEKey != null && rawSecretPBEKey.length > 0) {
			secretKeySpec = new SecretKeySpec(rawSecretPBEKey, "AES");
			Loggers.debug(this, TextMessage.SECRET_KEY_SPEC_CREATED);
		} else {
			Loggers.fatal(this, TextMessage.PBE_KEY_SECRET_ENCODED_IS_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.SECRET_KEY_SPEC_END_GETTING);
		return secretKeySpec;
	}


	
}
