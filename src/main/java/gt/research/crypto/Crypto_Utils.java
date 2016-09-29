package gt.research.crypto;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Crypto_Utils {

	public static byte[] EncryptByteArrayRSA(byte[] toEnc, PublicKey pubK)
	{
		byte[] toRet = null;
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		    cipher.init(Cipher.ENCRYPT_MODE, pubK);
		    return cipher.doFinal(toEnc);
		}
		catch(Exception e)
		{
			System.err.println("Encryption failed in crypto class with: " + e.getMessage());
		}
		if(toRet == null)
		{
			toRet = new byte[] {0};
		}
		return toRet;
	}

	public static byte[] EncryptByteArrayAES(byte[] toEnc, SecretKey key)
	{
		byte[] toRet = null;
		try
		{
			SecretKeySpec skeySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			toRet = cipher.doFinal(toEnc);
		}
		catch(Exception e)
		{
			System.err.println("Failed AES encryption of byte array: " + e.getMessage());
		}
		return toRet;
	}

	public static byte[] DecryptByteArrayRSA(byte[] decrypt, PrivateKey privK)
	{
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.DECRYPT_MODE, privK);
			return cipher.doFinal(decrypt);
		}
		catch(Exception e)
		{
			System.err.println("Failed decryption in crypto class with: " +  e.getMessage());
		}
		return new byte[]{0};
	}

	public static byte[] DecryptByteArrayAES(byte[] toEnc, SecretKey key)
	{
		byte[] toRet = null;
		try
		{
			SecretKeySpec skeySpec = new SecretKeySpec(key.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			toRet = cipher.doFinal(toEnc);
		}
		catch(Exception e)
		{
			System.err.println("Failed AES encryption of byte array: " + e.getMessage());
		}
		return toRet;
	}

	public static byte[] SignHash(byte[] hash, PrivateKey privatek)
	{
		try
		{
			/*IssuerCommon2011 ic = new IssuerCommon2011();
			ic.init();
			byte[] protectedSig = ic.getProtectedSig(hash, MOA_CERT);
			return protectedSig;*/
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initSign(privatek);
			sig.update(hash);
			return sig.sign();
		}
		catch(Exception e)
		{
			System.err.println(e);
			return new byte[]{0};
		}
	}

	public static boolean VerifyHash(byte[] expectedHash, byte[] signedHash, PublicKey publicKey)
	{
		try
		{
			/*ClientCommon2011 cc = new ClientCommon2011();
			cc.init();
			boolean result = cc.verifyProtectedSig(signedHash, expectedHash);
			return result;*/
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(publicKey);
			sig.update(expectedHash);
			return sig.verify(signedHash);
		}
		catch(Exception e)
		{
			System.err.println(e);
			return false;
		}
	}

	public static boolean VerifyHash(byte[] expectedHash, byte[] signedHash, Vector<PublicKey> pubKeys)
	{
		try
		{
			/*byte[] hash = signedHash.clone();
			Cipher cipher = Cipher.getInstance("RSA");
			for(int x = pubKeys.size() -1; x > -1; x--)
			{
				cipher.init(Cipher.ENCRYPT_MODE, pubKeys.get(x));
				hash = cipher.doFinal(hash);
			}
			return hashesEqual(hash,expectedHash);*/
			//Was this previously
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(pubKeys.get(0));
			sig.update(expectedHash);
			return sig.verify(signedHash);
		}
		catch(Exception e)
		{
			System.err.println("Failed a layered signature verification: "  + e.getMessage());
			return false;
		}
	}

	public static byte[] hashString(String toHash)
	{
		try
		{
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			byte[] arrayToHash = toHash.getBytes("UTF-8");
			md.update(arrayToHash, 0, toHash.length());
			return md.digest();
		}
		catch(Exception e)
		{
			System.err.println(e);
			return new byte[]{0};
		}
	}

	public static byte[] hashByteArray(byte[] toHash)
	{
		try
		{
			MessageDigest md;
			md = MessageDigest.getInstance("SHA-1");
			md.update(toHash, 0, toHash.length);
			byte[] toRet = md.digest();
			return toRet;
		}
		catch(Exception e)
		{
			System.err.println(e);
			return new byte[]{0};
		}
	}

	public static boolean hashesEqual(byte[] hash1, byte[] hash2)
	{
		if(hash1.length == hash2.length)
		{
			for(int x = 0; x < hash1.length; x++)
			{
				if(hash1[x] != hash2[x])
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static byte[] concat(byte[] first, byte[] second)
	{
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

}
