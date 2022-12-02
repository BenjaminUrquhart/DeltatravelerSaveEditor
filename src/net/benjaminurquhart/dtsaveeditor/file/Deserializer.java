package net.benjaminurquhart.dtsaveeditor.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Deserializer {
	
	public static class DeserializationException extends RuntimeException {
		
		private static final long serialVersionUID = -6206411312435090662L;
		
		public DeserializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	public static StreamHeader deserialize(File file) throws IOException {
		Reader reader = new Reader(Files.readAllBytes(file.toPath()));
		RecordType type = RecordType.values()[reader.buff.get(0)];
		
		if(type != RecordType.SerializedStreamHeader) {
			throw new IllegalStateException("Expected SerializedStreamHeader, got " + type);
		}
		
		Record record = reader.readRecord();
		StreamHeader header = (StreamHeader)record;
		
		while(record != null) {
			//System.out.println(record);
			record = reader.readRecord();
		}
		
		reader.finish();
		
		return header;
	}
}
