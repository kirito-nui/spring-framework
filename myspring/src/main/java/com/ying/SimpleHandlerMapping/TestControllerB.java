package com.ying.SimpleHandlerMapping;

import org.springframework.stereotype.Controller;

@Controller(value = "/controllerB")
public class TestControllerB {


	@TestRequestMapping(value = "/TestB1")
	public void TestB1(){
		System.out.println("TestControllerA TestB1 method");
	}

	@TestRequestMapping(value = "/TestB2")
	public void TestB2(){
		System.out.println("TestControllerA TestB2 method");
	}
}
