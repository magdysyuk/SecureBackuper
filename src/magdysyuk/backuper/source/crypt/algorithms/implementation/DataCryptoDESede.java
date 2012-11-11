package magdysyuk.backuper.source.crypt.algorithms.implementation;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

import magdysyuk.backuper.source.crypt.IStreamCrypt;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherAlgorithmName;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherMode;
import magdysyuk.backuper.source.crypt.algorithms.commons.CipherPadding;
import magdysyuk.backuper.source.crypt.algorithms.commons.DataCryptoTool;
import magdysyuk.backuper.source.logger.Loggers;
import magdysyuk.backuper.source.logger.TextMessage;


public class DataCryptoDESede implements IStreamCrypt {
	 
	
	private CipherAlgorithmName algorithmNameId = CipherAlgorithmName.DESede;
	
	@Override
	public boolean cryptStream(int cipherMode, InputStream inputStream, OutputStream outputStream, char[] password,
				CipherMode modeId, CipherPadding paddingId) {
		Loggers.debug(this, TextMessage.CRYPT_DATA_START, new Object[]{cipherMode});
		boolean isCryptSuccessful = false;
		DataCryptoTool dct = new DataCryptoTool();
		try {
			DESedeKeySpec desedeKeySpec = this.getDESedeKeySpec(password, this.algorithmNameId);
			SecretKey secretKey = new SecretKeySpec(desedeKeySpec.getKey(), "DESede");
			Cipher cipher = dct.getCipher(cipherMode, secretKey, this.algorithmNameId, modeId, paddingId);
			isCryptSuccessful = dct.crypt(inputStream, outputStream, cipher);
		} catch (Exception encryptEx) {
			Loggers.fatal(this, TextMessage.CRYPT_DATA_IMPOSSIBLE, encryptEx);
		}
		Loggers.debug(this, TextMessage.CRYPT_DATA_END, new Object[]{isCryptSuccessful});
		return isCryptSuccessful;
	}
	
	
	private DESedeKeySpec getDESedeKeySpec(char[] password, CipherAlgorithmName algorithmNameId) throws InvalidKeyException {
		Loggers.debug(this, TextMessage.DESEDE_KEY_SPEC_START_GETTING);
		DataCryptoTool dataCryptoTool = new DataCryptoTool();
		byte[] rawSecretPBEKey = dataCryptoTool.getSecretPBEKeyEncoded(password, algorithmNameId);
		DESedeKeySpec desedeKeySpec = null;
		if (rawSecretPBEKey != null && rawSecretPBEKey.length > 0) {
			desedeKeySpec = new DESedeKeySpec(rawSecretPBEKey);
			Loggers.debug(this, TextMessage.DESEDE_KEY_SPEC_CREATED);
		} else {
			Loggers.fatal(this, TextMessage.PBE_KEY_SECRET_ENCODED_IS_EMPTY_OR_NULL);
		}
		Loggers.debug(this, TextMessage.DESEDE_KEY_SPEC_END_GETTING);
		return desedeKeySpec;
	}


}