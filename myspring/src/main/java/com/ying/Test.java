package com.ying;

import com.ying.entiy.Codes;
import org.springframework.core.io.*;

import java.io.IOException;
import java.io.InputStream;

public class Test {


	public static void main(String[] args) throws IOException {

		ResourceLoader loader = new DefaultResourceLoader();
//		Resource resource = new ClassPathResource("com.ying.entiy.Codes");
		Resource resource = loader.getResource("/");
		System.out.println(resource.getURL());
		System.out.println(resource.exists());
		System.out.println(resource.getInputStream());
//		InputStream test = Test.class.getResourceAsStream("");
//		System.out.println(test);
//		while (test.read() > 0) {
//			System.out.println(test.read());
//		}
	}
}
