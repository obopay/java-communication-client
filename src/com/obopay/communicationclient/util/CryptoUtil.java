package com.obopay.communicationclient.util;

import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtil {

	private static final byte[] IV = new byte[] { 0x01, 0x00, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x09,
			0x00, 0x07, 0x00, 0x00, 0x00 };

	private CryptoUtil() {

	}

	public static byte[] decryptWithClientPrivateKey(String cipherText, Key privateKey) {

		try {
			byte[] decryptedText = null;

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			decryptedText = cipher.doFinal(java.util.Base64.getDecoder().decode(cipherText.getBytes()));
			return decryptedText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static String encryptWithClientPrivateKey(byte[] plainText, Key privateKey) {
		try {
			String decryptedText = null;

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			decryptedText = new String(Base64.getEncoder().encode(cipher.doFinal(plainText)));
			return decryptedText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static String encryptWithServerPublicKey(byte[] plainText, Key publicKey) {
		try {
			String decryptedText = null;

			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			decryptedText = new String(Base64.getEncoder().encode(cipher.doFinal(plainText)));
			return decryptedText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static String decryptWithClientPublicKey(String cipherText, Key publicKey) {
		try {
			String decryptedText = null;
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			decryptedText = new String(cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes())));
			return decryptedText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] decryptWithServerPublicKey(String cipherText, Key publicKey) {
		try {
			byte[] decryptedText = null;
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

			Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			decryptedText = cipher.doFinal(Base64.getDecoder().decode(cipherText.getBytes()));
			return decryptedText;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Key generateAesKey() {

		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = new SecureRandom();

			keyGenerator.init(256, secureRandom);
			return keyGenerator.generateKey();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] decryptWithAes(byte[] body, byte[] secretKey) {
		try {
			SecretKey cipherKey = new SecretKeySpec(secretKey, "AES");
			IvParameterSpec objIV = new IvParameterSpec(IV);
			// Get Cipher Object & initialize it
			Cipher cipher;
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, cipherKey, objIV);
			byte[] decValue = cipher.doFinal(body);
			return decValue;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] encryptWithAes(byte[] plaintText, byte[] secretKey) {
		try {
			SecretKey cipherKey = new SecretKeySpec(secretKey, "AES");
			IvParameterSpec objIV = new IvParameterSpec(IV);
			// Get Cipher Object & initialize it
			Cipher cipher;
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, cipherKey, objIV);
			return cipher.doFinal(plaintText);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
