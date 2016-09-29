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

	public static String res_dir; 
	public static String file_data;
	public static String file_data_encrypted;
	public static String file_key;
	public static String file_policy;


	public FileManager() {
/*
		try {
			KeyGen = KeyGenerator.getInstance("AES");
			KeyGen.init(256);
			File keyFile = new File(file_key);
			byte[] keyData = IOUtils.toByteArray(new FileInputStream(keyFile));
			SecKey = new SecretKeySpec(keyData, 0, keyData.length, "AES");
			AesCipher = Cipher.getInstance("AES");
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	public void updateFile(File path, byte[] content) {
		if (path.exists()) {
			path.delete();
		}
		try {
			FileOutputStream fos = new FileOutputStream(path);
			IOUtils.write(content, fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean decrypt() {
		try {
			File encryptedFile = new File(file_data_encrypted);
			byte[] encryptedData = IOUtils.toByteArray(new FileInputStream(encryptedFile));
			AesCipher.init(Cipher.DECRYPT_MODE, SecKey);
			byte[] decryptedData = AesCipher.doFinal(encryptedData);
			File decryptedFile = new File(file_data);
			updateFile(decryptedFile, decryptedData);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean encrypt() {
		File dataFile = new File(file_data);
		if (dataFile.exists()) {
			try {
				byte[] data = IOUtils.toByteArray(new FileInputStream(dataFile));
				AesCipher.init(Cipher.ENCRYPT_MODE, SecKey);
				byte[] data_encrypted = AesCipher.doFinal(data);
				File encryptedFile = new File(file_data_encrypted);
				updateFile(encryptedFile, data_encrypted);
				dataFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
