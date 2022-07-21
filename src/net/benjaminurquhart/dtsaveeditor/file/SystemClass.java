package net.benjaminurquhart.dtsaveeditor.file;

import java.util.Map;

public class SystemClass extends BinaryMember {
	public String name;
	public Map<String, Object> values;
	
	public SystemClass(String name, Map<String, Object> values) {
		super(BinaryType.SystemClass, values);
		this.values = values;
		this.name = name;
	}
}