package net.benjaminurquhart.dtsaveeditor;

import java.io.ByteArrayOutputStream;

import net.benjaminurquhart.dtsaveeditor.file.ClassWithMembersAndTypes;

public class HackySerialize {

	public static byte[] patch(byte[] bytes, ClassWithMembersAndTypes raw, SAVEFile obj) {
		
		int fakePtr = 0;
		int ptr = 0;
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
		
		return out.toByteArray();
	}
	
	private static byte[] serializeString(String str) {
		byte[] stringBytes = str.getBytes();
		return null;
	}
	
	private static byte[] compressLength(int length) {
		byte[] bytes = new byte[4];
		for(int i = 0; i < 4; i++) {
			bytes[i] = (byte)((length >> (i * 8)) & 0xff);
		}
		return bytes;
	}
}
