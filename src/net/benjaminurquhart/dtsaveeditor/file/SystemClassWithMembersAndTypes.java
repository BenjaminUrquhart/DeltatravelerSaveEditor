package net.benjaminurquhart.dtsaveeditor.file;

public class SystemClassWithMembersAndTypes extends ClassWithMembersAndTypes {

	public SystemClassWithMembersAndTypes(Reader reader) {
		super(reader, false);
	}
	
	public String getClassName() {
		return this.classInfo.name.split("`", 2)[0];
	}
}
