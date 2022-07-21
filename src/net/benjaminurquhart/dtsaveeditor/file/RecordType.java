package net.benjaminurquhart.dtsaveeditor.file;

public enum RecordType {
	SerializedStreamHeader,
	ClassWithId,
	SystemClassWithMembers,
	ClassWithMembers,
	SystemClassWithMembersAndTypes,
	ClassWithMembersAndTypes,
	BinaryObjectString,
	BinaryArray,
	MemberPrimitiveTyped,
	MemberReference,
	ObjectNull,
	MessageEnd,
	BinaryLibrary,
	ObjectNullMultiple256,
	ObjectNullMultiple,
	ArraySinglePrimitive,
	ArraySingleObject,
	ArraySingleString,
	MethodCall,
	MethodReturn
}