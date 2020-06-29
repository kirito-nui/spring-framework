package com.ying.SimpleHandlerMapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface TestRequestMapping {

	String value() default "";
}
