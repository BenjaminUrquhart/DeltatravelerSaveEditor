package net.benjaminurquhart.dtsaveeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.benjaminurquhart.dtsaveeditor.file.ArraySingle;
import net.benjaminurquhart.dtsaveeditor.file.ArraySingleObject;
import net.benjaminurquhart.dtsaveeditor.file.ClassWithMembersAndTypes;
import net.benjaminurquhart.dtsaveeditor.file.Deserializer;
import net.benjaminurquhart.dtsaveeditor.file.MemberReference;
import net.benjaminurquhart.dtsaveeditor.file.ObjectMapper;
import net.benjaminurquhart.dtsaveeditor.file.Serializer;
import net.benjaminurquhart.dtsaveeditor.file.StreamHeader;

public class Main {

	public static void main(String[] args) throws Exception {
		// Add conversion from C# Lists to Java Lists
		ObjectMapper.setSystemObjectMapping("System.Collections.Generic.List", values -> {
			List<Object> out = new ArrayList<>();
			int length = (int)values.get("_size");
			Object[] arr = ((MemberReference)values.get("_items")).getReference(ArraySingle.class).getValues();
			for(int i = 0; i < length; i++) {
				out.add(arr[i]);
			}
			return out;
		});
		// And the other way around
		ObjectMapper.setJavaObjectMapping("System.Collections.Generic.List", List.class, (list, fields) -> {
			fields.put("_size", list.size());
			fields.put("_items", list.toArray());
			
			fields.put("_version", 0);
		});
		
		StreamHeader root = Deserializer.deserialize(new File("SAVE1.sav"));
		SAVEFile save = SAVEFile.from(root);
		System.out.println(save);
		
		
		ClassWithMembersAndTypes rawRoot = root.getRootObject(ClassWithMembersAndTypes.class);
		ArraySingleObject flags = ((MemberReference)rawRoot.values.get("flags")).getReference(ArraySingleObject.class);
		
		String field = "zone";
		System.out.printf("%s offset is 0x%08x (value is %s)\n", field, rawRoot.offsets.get(field), rawRoot.values.get(field));
		
		int index = 0;
		for(Object flag : flags.values) {
			System.out.printf("%04d: %s\n", index, flag == null ? String.format("null @ 0x%08x", flags.offsets[index]) : flag);
			index++;
		}
		
		Serializer.serialize(rawRoot.getLibrary().libraryName, save);
	}
}
