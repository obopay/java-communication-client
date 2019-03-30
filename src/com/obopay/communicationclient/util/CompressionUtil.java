package com.obopay.communicationclient.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CompressionUtil {

	private CompressionUtil() {

	}

	public static byte[] compressZlib(byte[] input) {

		byte[] buffer = new byte[512];
		Deflater compresser = new Deflater();
		compresser.setInput(input);
		compresser.finish();

		int count = 0;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		while (!compresser.finished()) {
			count = compresser.deflate(buffer);
			stream.write(buffer, 0, count);
		}
		compresser.end();
		return stream.toByteArray();
	}

	public static byte[] decompressZlib(byte[] input) {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(input, 0, input.length);
			byte[] buffer = new byte[1024];

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			byte[] output = outputStream.toByteArray();
			inflater.end();
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
