package com.ying.SimpleHandlerMapping;

import org.springframework.stereotype.Controller;

@Controller(value = "/controllerA")
@TestRequestMapping
public class TestControllerA {

	@TestRequestMapping(value = "/TestA1")
	public void TestA1(){
		System.out.println("TestControllerA TestA1 method");
	}

	@TestRequestMapping(value = "/TestA2")
	public void TestA2(){
		System.out.println("TestControllerA TestA2 method");
	}
}
