package com.ying.factory;

import com.ying.entiy.Codes;
import com.ying.entiy.Worker;

import java.util.List;

/**
 * bean工厂
 */
public class MyBeanFactory {

	public Codes getCodesBean(){
		return new Codes("法外狂徒", 30);
	}

	public Codes getCodesBean(String name, int age){
		return new Codes(name, age);
	}
}
