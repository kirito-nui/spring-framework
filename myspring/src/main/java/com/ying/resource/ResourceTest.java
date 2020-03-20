package com.ying.resource;

import org.springframework.core.io.*;

import java.io.*;

public class ResourceTest {

	public static void main(String[] args) throws IOException {

		ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource("C:\\Users\\Administrator\\Desktop\\qunee-min.js");
		System.out.println(resource instanceof FileSystemResource);
		System.out.println(resource.getFilename());
		File file = resource.getFile();
		InputStream is = new FileInputStream(file);
	}
}
