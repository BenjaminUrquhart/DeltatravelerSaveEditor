package net.benjaminurquhart.dtsaveeditor.deserialize;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
@interface RecordType {
	Deserializer.RecordType value();
}
