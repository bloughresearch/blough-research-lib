package gt.research.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

public class FileManager {

	public static KeyGenerator KeyGen;
	public static SecretKey SecKey;
	public static Cipher AesCipher;

	public static void init(File keyFile) {
		try {
			KeyGen = KeyGenerator.getInstance("AES");
			KeyGen.init(256);
			byte[] keyData = IOUtils.toByteArray(new FileInputStream(keyFile));
			SecKey = new SecretKeySpec(keyData, 0, keyData.length, "AES");
			AesCipher = Cipher.getInstance("AES");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	public static void updateFile(File fileToUpdate, byte[] content) {
		if (fileToUpdate.exists()) {
			fileToUpdate.delete();
		}
		try {
			FileOutputStream fos = new FileOutputStream(fileToUpdate);
			IOUtils.write(content, fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean decrypt(File encryptedFile, File decryptedFile) {
		try {
			byte[] encryptedData = IOUtils.toByteArray(new FileInputStream(encryptedFile));
			AesCipher.init(Cipher.DECRYPT_MODE, SecKey);
			byte[] decrypted_data = AesCipher.doFinal(encryptedData);
			updateFile(decryptedFile, decrypted_data);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean encrypt(File decryptedFile, File encryptedFile) {
		if (decryptedFile.exists()) {
			try {
				byte[] data = IOUtils.toByteArray(new FileInputStream(decryptedFile));
				AesCipher.init(Cipher.ENCRYPT_MODE, SecKey);
				byte[] encrypted_data = AesCipher.doFinal(data);
				updateFile(encryptedFile, encrypted_data);
				decryptedFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
