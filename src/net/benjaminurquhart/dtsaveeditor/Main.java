package net.benjaminurquhart.dtsaveeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.benjaminurquhart.dtsaveeditor.deserialize.ArraySingle;
import net.benjaminurquhart.dtsaveeditor.deserialize.ClassWithMembersAndTypes;
import net.benjaminurquhart.dtsaveeditor.deserialize.Deserializer;
import net.benjaminurquhart.dtsaveeditor.deserialize.MemberReference;
import net.benjaminurquhart.dtsaveeditor.deserialize.StreamHeader;

public class Main {

	public static void main(String[] args) throws Exception {
		// Add conversion from C# Lists to Java Lists
		ClassWithMembersAndTypes.setSystemObjectMapping("System.Collections.Generic.List", values -> {
			List<Object> out = new ArrayList<>();
			int length = (int)values.get("_size");
			Object[] arr = ((MemberReference)values.get("_items")).getReference(ArraySingle.class).getValues();
			for(int i = 0; i < length; i++) {
				out.add(arr[i]);
			}
			return out;
		});
		
		StreamHeader root = Deserializer.deserialize(new File("SAVE0.sav")); // Load the save
		SAVEFile save = root.getRootObject(SAVEFile.class);                  // Convert to Java object
		System.out.println(save);
	}
}
